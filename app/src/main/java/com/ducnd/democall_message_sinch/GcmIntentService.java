package com.ducnd.democall_message_sinch;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.parse.GcmBroadcastReceiver;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    public GcmIntentService(String name, NotificationManager mNotificationManager) {
        super(name);
        this.mNotificationManager = mNotificationManager;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Login_Signup_Activity.class), 0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("New Message!");
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        //GcmBroadcastReceiver.completeWakefulIntent(intent);

    }

}
