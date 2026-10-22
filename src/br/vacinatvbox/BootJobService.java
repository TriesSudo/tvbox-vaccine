package br.vacinatvbox;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

/**
 * Boot-safe re-apply: scheduled by the system JobScheduler (not a bare thread in a
 * receiver, which Android kills as soon as the receiver returns).
 */
public class BootJobService extends JobService {
    private static final int JOB_ID = 4201;

    static void schedule(Context c) {
        try {
            JobScheduler js = (JobScheduler) c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (js == null) return;
            JobInfo job = new JobInfo.Builder(JOB_ID, new ComponentName(c, BootJobService.class))
                    .setMinimumLatency(20_000)
                    .setOverrideDeadline(120_000)
                    .build();
            js.schedule(job);
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        new Thread(() -> {
            try {
                for (int i = 0; i < 4; i++) {
                    ShieldEngine.stopNativeMalwareDaemons(getApplicationContext());
                    if (ShieldEngine.countStoppedDaemons() >= ShieldEngine.NATIVE_DAEMONS.length) break;
                    Thread.sleep(8_000);
                }
            } catch (Throwable ignored) {}
            jobFinished(params, false);
        }, "vacina-bootjob").start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
