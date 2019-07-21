package com.example.mychatapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Friend Request")
                .setContentText("You've received a new Friend Request")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        // notificationId
        int notificationId = (int)System.currentTimeMillis();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());



    }





//    public void initChannels(Context context) {
//        if (Build.VERSION.SDK_INT < 26) {
//            return;
//        }
//        NotificationManager notificationManager =
//                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationChannel channel = new NotificationChannel("default",
//                "Channel name",
//                NotificationManager.IMPORTANCE_DEFAULT);
//        channel.setDescription("Channel description");
//        notificationManager.createNotificationChannel(channel);
//    }



}
