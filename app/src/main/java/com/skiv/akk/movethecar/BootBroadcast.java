package com.skiv.akk.movethecar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcast extends BroadcastReceiver {
    public void AddToLog(String logtxt){
        String TAG="ServiseAutostart";
        Log.v(TAG, logtxt);
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        AddToLog("--сервис стартовал после перезагрузки " + intent.getStringExtra("com.skiv.akk.movethecar"));
        context.startService(new Intent(context, McPushService.class));
    }
}
