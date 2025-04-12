package com.example.powerscout;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.Manifest;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConsumptionService extends Service {
    private static final String CHANNEL_ID = "consumption_channel";
    private static final int NOTIFICATION_ID = 1;
    private DatabaseReference sensorRef;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        sensorRef = FirebaseDatabase.getInstance().getReference("sensor");

        // Start the service in the foreground
        startForeground(NOTIFICATION_ID, createNotification("Fetching consumption data..."));
        fetchSensorData();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "PowerScout Consumption",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("PowerScout")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher_power) // Ensure the icon exists in your resources
                .setOngoing(true) // Keeps the notification ongoing
                .build();
    }

    private void fetchSensorData() {
        sensorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double current = snapshot.child("current").getValue(Double.class);
                Double voltage = snapshot.child("voltage").getValue(Double.class);
                if (current == null || voltage == null) return;

                // Calculate power in kilowatts (kW)
                double powerKW = (voltage * current) / 1000; // Power in kW

                // Optionally, if you want to calculate energy consumed (e.g., over 1 hour):
                double energyKWh = powerKW;  // For now, we're assuming real-time calculation.

                // Device name (for example, "Bulb")
                String deviceName = "Bulb";

                // Create a message with power and energy consumption in kWh
                String message = String.format("%s\nPower: %.5f kW\nEnergy: %.5f kWh", deviceName, powerKW, energyKWh);

                // Update notification with the fetched data
                Notification notification = createNotification(message);
                NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());

                // Check for POST_NOTIFICATIONS permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS)
                            == PackageManager.PERMISSION_GRANTED) {
                        manager.notify(NOTIFICATION_ID, notification);
                    }
                } else {
                    manager.notify(NOTIFICATION_ID, notification);
                }

                // Log for debugging purposes
                Log.d("ConsumptionService", message);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ConsumptionService", "Firebase error: " + error.getMessage());
            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Keeps the service alive even if the app is closed
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Binding is not needed for this service
    }
}
