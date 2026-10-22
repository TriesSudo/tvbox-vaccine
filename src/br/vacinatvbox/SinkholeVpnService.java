package br.vacinatvbox;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

public class SinkholeVpnService extends VpnService {
    public static final String ACTION_STOP = "br.vacinatvbox.STOP_VPN";
    public static volatile boolean isRunning = false;
    private ParcelFileDescriptor tunInterface;
    private Thread watchdogThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSinkhole();
            stopSelf();
            return START_NOT_STICKY;
        }
        startSinkhole();
        startDaemonWatchdog();
        return START_STICKY;
    }

    private synchronized void startSinkhole() {
        if (tunInterface != null) return;
        try {
            Builder builder = new Builder()
                .setSession("Escudo Anti-Proxy TV Box")
                .setMtu(1500)
                .addAddress("10.254.254.1", 30);

            for (String ip : ShieldEngine.C2_IPS) {
                builder.addRoute(ip, 32);
            }

            tunInterface = builder.establish();
            isRunning = (tunInterface != null);
        } catch (Throwable t) {
            isRunning = false;
        }
    }

    private synchronized void stopSinkhole() {
        isRunning = false;
        if (watchdogThread != null) {
            watchdogThread.interrupt();
            watchdogThread = null;
        }
        if (tunInterface != null) {
            try { tunInterface.close(); } catch (Throwable ignored) {}
            tunInterface = null;
        }
    }

    private synchronized void startDaemonWatchdog() {
        if (watchdogThread != null && watchdogThread.isAlive()) return;
        watchdogThread = new Thread(() -> {
            try { Thread.sleep(15_000); } catch (InterruptedException e) { return; }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ShieldEngine.stopNativeMalwareDaemons(getApplicationContext());
                    Thread.sleep(15_000);
                } catch (InterruptedException e) {
                    break;
                } catch (Throwable ignored) {}
            }
        }, "vacina-watchdog");
        watchdogThread.setDaemon(true);
        watchdogThread.start();
    }

    @Override
    public void onDestroy() {
        stopSinkhole();
        super.onDestroy();
    }
}
