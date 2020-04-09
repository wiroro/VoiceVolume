package com.example.voicevolume;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AudioRecordDemo {
    public static final String VOICE_VOLUME_ACTION = "VOICE_VOLUME_ACTION";
    private static final int SAMPLE_RATE_IN_HZ = 8000;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    private AudioRecord mAudioRecord;       //录音实例
    private boolean isGetVoiceRun;          //用来标识是否正在录制音频, 同时可以用来控制是否结束测量分贝
    private Object mLock;       //用来锁定线程的对象
    private Context mContext;
    private int recordInterval = 100;       //测量间隔

    public AudioRecordDemo(Context context) {
        this.mContext = context;
        mLock = new Object();
    }

    public void startRecordVoice() {
        if (isGetVoiceRun) {      //如果正在测量，则此方法调用后直接返回
            return;
        }
        if(!this.mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            return;
        }
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        isGetVoiceRun = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mAudioRecord.startRecording();
                }catch (Exception e){
                    e.printStackTrace();
                    return;
                }
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun){
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    for(int i=0; i<buffer.length; i++){
                        v+=buffer[i] * buffer[i];
                    }
                    double mean = v/(double)r;
                    final int volume = (int) (10* Math.log10(mean)+ 0.5);
                    sendVoiceVolumeOut(volume);
                    synchronized (mLock){
                        try{
                            mLock.wait(recordInterval);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }).start();
    }
    public void stopRecordVoice(){
        this.isGetVoiceRun = false;
    }

    private void sendVoiceVolumeOut(int volume) {
        Intent intent = new Intent();
        intent.setAction(VOICE_VOLUME_ACTION);
        intent.putExtra("value", volume);
        this.mContext.sendBroadcast(intent);
    }

    /**
     * 查询指定service是否已经在运行了
     * @param context
     * @param ServiceName
     * @return
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (null == ServiceName || ServiceName.length()==0) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }
}
