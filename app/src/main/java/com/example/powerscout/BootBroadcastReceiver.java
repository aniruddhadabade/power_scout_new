package com.example.powerscout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Start the service after the device has rebooted
            Intent serviceIntent = new Intent(context, ConsumptionService.class);
            context.startService(serviceIntent);
        }
    }
}
