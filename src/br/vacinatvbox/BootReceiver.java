package br.vacinatvbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(ShieldEngine.PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(ShieldEngine.KEY_ENABLED, true)) {
            final Context appCtx = context.getApplicationContext();
            new Thread(() -> ShieldEngine.stopNativeMalwareDaemons(appCtx)).start();
            try {
                context.startService(new Intent(context, SinkholeVpnService.class));
            } catch (Throwable ignored) {}
        }
    }
}
