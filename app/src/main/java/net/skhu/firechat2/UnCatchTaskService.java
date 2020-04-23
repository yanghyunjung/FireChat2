package net.skhu.firechat2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class UnCatchTaskService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) { //핸들링 하는 부분
        Log.e("Error","onTaskRemoved - " + rootIntent);

        // 핸들링 해 적용할 내용

        stopSelf(); //서비스도 같이 종료
    }
}