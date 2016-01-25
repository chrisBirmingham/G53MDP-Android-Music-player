package com.example.psycb3.coursework;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

/**
 *  Utility Class for producing error messages and notifications
 */
public class Util {
    static final int NotificationID = 1;

    /**
     * Method to check if the external storage is mounted and readable
     * @return
     *      True if it is else false
     */
    public static boolean checkExternalStorage() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    /**
     * Method to show an error message when required. Kills the activity on ok press
     * @param context
     *      The context of the activity wanting to create the error
     * @param errorTitle
     *      The title of the error
     * @param errorMessage
     *      The error message
     */
    public static void createError(final Context context, String errorTitle, String errorMessage){
        new AlertDialog.Builder(context)
                .setTitle(errorTitle)
                .setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    /**
                     * On click method kills the calling activity
                     * @param dialog
                     *      The dialog box being created
                     * @param which
                     *      Which button is pressed. In this case only one
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity)context).finish();
                    }
                }).create().show();
    }

    /**
     * Method to create a notification when a song starts playing. Clears any notifications
     * that are currently there. Notification is non-clickable. Notification shows when the
     * app is minimised
     * @param context
     *      The context of the calling activity
     * @param content
     *      The notification message
     */
    public static void createNotification(Context context, String content){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.music)
                .setTicker(content)
            .setContentTitle("Music Player")
            .setContentText(content);
        manager.cancel(NotificationID);
        manager.notify(NotificationID, mBuilder.build());
    }
}
