package info.ozkan.vipera.android;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by omer on 6/18/14.
 */
public class GcmIntentService extends IntentService {
    private static int UNIQUE_INT_PER_CALL = 1;
    public static int NOTIFICATION_ID = 1;
    private static final String TAG = "GcmIntentService";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
               sendNotification("Send error: " + extras.toString(), null);
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString(), null);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // This loop represents the service doing some work.
                for (int i=0; i<1; i++) {
                    Log.i(TAG, "Working... " + (i+1)
                            + "/5 @ " + SystemClock.elapsedRealtime());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                String msg = extras.get("message").toString();



                sendNotification(msg, extras);
                Log.i(TAG, msg);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, Bundle extras) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Vipera Acil Bildirimi")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg)
                        .setOnlyAlertOnce(false);

        Context context = getApplicationContext();

        if(extras != null) {
            Intent intent = new Intent(this, ViewNotification.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            intent.putExtra("mobilePhone", extras.getString("patientMobilePhone", ""));
            intent.putExtra("phone",extras.getString("patientPhone", ""));
            intent.putExtra("patientName",extras.getString("patientName" ,""));
            intent.putExtra("fieldName", extras.getString("fieldName", ""));
            intent.putExtra("fieldUnit", extras.getString("fieldUnit" ,""));
            intent.putExtra("fieldValue", extras.getString("value", ""));
            PendingIntent contentIntent = PendingIntent.getActivity(this, UNIQUE_INT_PER_CALL++,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);

            mBuilder.setContentIntent(contentIntent);
        }
        Notification notification = mBuilder.build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        long[] vibrate = {0,300,400,500, 1000};
        notification.vibrate = vibrate;
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        mNotificationManager.notify(NOTIFICATION_ID++, notification );
    }

}
