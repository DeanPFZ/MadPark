package com.example.madpark;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.madpark.tools.NotificationReceiver;
import com.google.android.gms.maps.model.LatLng;

public class ParkCarActivity extends AppCompatActivity {

    NotificationCompat.Builder builder;

    private LatLng bundleCoords;
    private String TAG = "ParkCarActivity";
    private TextView tv;
    NotificationManager notificationManager;

    private String CHANNEL_ID = "ParkCarActivity";
    public static final String mypreference = "mypref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_car);
        tv = findViewById(R.id.editText);

        Bundle bund = getIntent().getParcelableExtra("bundle");
        bundleCoords = bund.getParcelable("coords");
        createNotificationChannel();
    }


    public void setNotification(View view) {
        if (tv.getText() == "") {
            Log.i(TAG, "Invalid Time");
            Toast.makeText(this, "Invalid Time", Toast.LENGTH_LONG).show();
            return;
        } else if (!tv.getText().toString().matches("[0-9]+")) {
            Log.i(TAG, "Invalid Time");
            Toast.makeText(this, "Invalid Time", Toast.LENGTH_LONG).show();
            return;
        } else if (Long.parseLong(tv.getText().toString()) <= 10) {
            Log.i(TAG, "Invalid Time");
            Toast.makeText(this, "Invalid Time! Enter a number larger than 10!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Set Parking Location! We will remind you when it's time to move your car!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=" + bundleCoords.latitude + "," + bundleCoords.longitude));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent mapIntent = PendingIntent.getActivity(this, 0, intent, 0);


        String tempTime = tv.getText().toString();
        tv.setText("");
        long lTime = Long.parseLong(tempTime) - 10;

        if (lTime < 50) {//user will be back in one hour, which means user is not far away from the car
            lTime = 5;
        }

        long delay = lTime * 60000; //sets the time to notify the user

        //create the notification
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_car) //gives car icon
                .setContentTitle("MadPark") //titled MadPark for app name
                .setContentText("Time to go move your car. Tap here for directions!") //give user hint
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(mapIntent) //sets the tap action to open google maps with directions
                .setAutoCancel(true); //when user taps, the notification will close


        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, builder.build());
        PendingIntent pendingNotifIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;

        //set an alarm (or timer) to have the notification occur
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingNotifIntent);
        saveCarLocation(bundleCoords);
        Intent returnIntent = new Intent(ParkCarActivity.this, MapsActivity.class);
        startActivity(returnIntent);
    }

    private void saveCarLocation(LatLng loc) {
        Log.i(TAG, "The location of the registered car is: " + loc);
        // Getting the shared preferences editor
        SharedPreferences mPrefs = getSharedPreferences(mypreference, MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.clear();
        // Save location information
        String mKey = "carRegLocation";
        String mLocation = Double.toString(loc.latitude) + "," + Double.toString(loc.longitude);
        mEditor.putString(mKey, mLocation);
        mEditor.apply();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_ID;
            String description = "Time to go move your car. Tap here for directions!";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}





