/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.voicevolume;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.databinding.DataBindingUtil;

import com.example.voicevolume.databinding.ActivityMainDB;

import java.lang.ref.WeakReference;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {
    public ActivityMainDB binding;
    private AudioRecordDemo demo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.main_view);
        requestNeedPermissions();
        registerReceiver(new VoiceVolumeReceiver(), new IntentFilter(AudioRecordDemo.VOICE_VOLUME_ACTION));
    }

    private static final String[] PERMISSION_ARRAY =
            {Manifest.permission.RECORD_AUDIO};
    private static final int ALL_PERMISSION = 0x0001;
    /**
     * 由于我们的程序是B端程序，首次安装都是由实施人员安装， 所以只需要在程序启动时检测权限并引导即可。
     */
    @AfterPermissionGranted(ALL_PERMISSION)
    private void requestNeedPermissions() {
        if (!EasyPermissions.hasPermissions(this, PERMISSION_ARRAY)) {
            EasyPermissions.requestPermissions(
                    this, "", ALL_PERMISSION, PERMISSION_ARRAY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ALL_PERMISSION){
            if (EasyPermissions.hasPermissions(this, PERMISSION_ARRAY)) {
                startService(new Intent(this, newService.class));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (EasyPermissions.hasPermissions(this, PERMISSION_ARRAY)) {
            if(!AudioRecordDemo.isServiceRunning(this, newService.class.getName())) {
                startService(new Intent(this, newService.class));
            }
            newService.startRecord();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        newService.stopRecord();
    }

    public class VoiceVolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(AudioRecordDemo.VOICE_VOLUME_ACTION.equals(intent.getAction())){
                int volume = intent.getIntExtra("value", 0);
                if(volume>0) {
                    binding.volumeNum.setText("" +volume);
                }
            }
        }
    }
}
