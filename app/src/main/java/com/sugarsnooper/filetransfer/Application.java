package com.sugarsnooper.filetransfer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Application extends android.app.Application {
    public static int[] avatars = {
            R.drawable.ic_av1,
            R.drawable.ic_av2,
            R.drawable.ic_av3,
            R.drawable.ic_av4,
            R.drawable.ic_av5,
            R.drawable.ic_av6,
            R.drawable.ic_av7,
            R.drawable.ic_av8,
            R.drawable.ic_av9,
            R.drawable.ic_av10,
            R.drawable.ic_av11,
            R.drawable.ic_av12
    };
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Application", "Create");
        Strings.dateString = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

//        Fresco.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.e("Application", "Terminate");
    }

    public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.setExact(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                    }
                } else {
                }
            } else {
            }
        } catch (Exception ex) {
        }
    }
}
