package com.example.voicevolume;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class newService extends Service {
    private static AudioRecordDemo demo;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        demo = new AudioRecordDemo(this);
        demo.startRecordVoice();
        return START_NOT_STICKY;
    }

    public static void startRecord(){
        if(null != demo){
            demo.startRecordVoice();
        }

    }

    public static void stopRecord(){
        if(null!=demo){
            demo.stopRecordVoice();
        }
    }
}
