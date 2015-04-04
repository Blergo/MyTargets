package de.dreier.mytargets;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;

import de.dreier.mytargets.models.BitmapDataObject;
import de.dreier.mytargets.models.NotificationInfo;
import de.dreier.mytargets.utils.WearableUtils;

public class WearableListener extends WearableListenerService {

    private static final int NOTIFICATION_ID = 1;
    private static Bitmap image;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        // Transform byte[] to Bundle
        byte[] data = messageEvent.getData();

        Log.d("listener", messageEvent.getPath());
        if (messageEvent.getPath().equals(WearableUtils.STARTED_ROUND)) {
            if (data.length != 0) {
                try {
                    Bundle bundle = WearableUtils.deserializeToBundle(data);
                    BitmapDataObject b = (BitmapDataObject) bundle
                            .getSerializable(WearableUtils.BUNDLE_IMAGE);
                    image = b.getBitmap();
                    NotificationInfo info = (NotificationInfo) bundle
                            .getSerializable(WearableUtils.BUNDLE_INFO);
                    showNotification(info);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (messageEvent.getPath().equals(WearableUtils.UPDATE_ROUND)) {
            try {
                NotificationInfo info = WearableUtils.deserializeToInfo(data);
                showNotification(info);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (messageEvent.getPath().equals(WearableUtils.STOPPED_ROUND)) {
            cancelNotification();
        }
    }

    void showNotification(NotificationInfo info) {

        // Build the intent to display our custom notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra(MainActivity.EXTRA_ROUND, info.round);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build activity page
        Notification page = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .extend(new Notification.WearableExtender()
                                .setCustomSizePreset(Notification.WearableExtender.SIZE_FULL_SCREEN)
                                .setDisplayIntent(pendingIntent))
                .build();

        // Create the ongoing notification
        Notification.Builder notificationBuilder =
                new Notification.Builder(this)
                        .setContentTitle(info.title)
                        .setContentText(info.text)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setOngoing(true)
                        .extend(new Notification.WearableExtender()
                                        .addPage(page).setBackground(image));

        // Build the notification and show it
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void cancelNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}