package com.baidu.doctor.scheduler;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.baidu.lib.wifi.WifiCompute;
import com.baidu.wifi.R;
import com.baidu.wifi.demo.MainActivity;
import com.baidu.wifi.demo.Utils;


/**
 * 
 */
public class SchedulingService extends IntentService {
    public SchedulingService() {
        super("SchedulingService");
    }
    
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = SchedulingService.class.getSimpleName();
    public WifiCompute wificompute = new WifiCompute();
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    @Override
    protected void onHandleIntent(Intent intent) {
        // BEGIN_INCLUDE(service_onhandle)    
        String result ="";
        
        // Try to connect to the Google homepage and download content.
        try {
            result = Html.fromHtml(wificompute.get_wifi_level_check((Utils.getInteger(getApplicationContext(), 
            													wificompute.NOW_CONNECTED_WIFI_LEVEL)))).toString();
            Log.d(TAG, "wife level =" + result);
        } catch (Exception e) {
//            Log.i(TAG, getString(R.string.connection_error));
        }
        
        //wifi信号差时手机上弹出notificationBar
        if (result.equalsIgnoreCase("差")) {
            sendNotification("Wifi 信号差");
            Log.i(TAG, "Found doodle!!");
        } else {
//            sendNotification(getString(R.string.no_doodle));
            Log.i(TAG, "No doodle found. :-(");
        }
        
        // Release the wake lock provided by the BroadcastReceiver.
//        AlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    } 
    
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
               this.getSystemService(Context.NOTIFICATION_SERVICE);
    
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Wifi Alert!")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
   
}
