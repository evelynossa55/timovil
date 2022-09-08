package com.cm.timovil2.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;

import com.cm.timovil2.front.ActivityRutero;


public class TiMovil_GCM_Service extends IntentService{

    private static final int NOTIFICATION_ID = 1;
    public TiMovil_GCM_Service(){
        super("TiMovil_GCM_Service");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        //GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        //String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            //if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
              //  sendNotification("Send error: " + extras.toString());
            //} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
              //  sendNotification("Deleted messages on server: " +
                //        extras.toString());
            //} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // TODO: procesar el mensaje y mostrarlo dependiendo del tipo
                // Post notification of received message.
                sendNotification("Received: " + extras.toString());
         //   }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        TiMovil_GCM_Receiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    private void sendNotification(String msg) {
        //todo:Mostrar notoficaciones en cualquier pantalla de la aplicación, si es necesario
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ActivityRutero.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                        .setSmallIcon(com.cm.timovil2.R.drawable.icon_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        builder.setContentIntent(contentIntent);
        if(mNotificationManager != null)
            mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
