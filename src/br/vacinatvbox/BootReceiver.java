package br.vacinatvbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(ShieldEngine.PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(ShieldEngine.KEY_ENABLED, false)) {
            // Delegated to JobScheduler: a thread started here dies when the receiver returns.
            BootJobService.schedule(context.getApplicationContext());
        }
    }
}
