package com.example.powerscout;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.ParseException;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import java.util.Map;



public class Dashboard extends BaseActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private PieChart pieChart;
    private BarChart barChart;
    private List<BarEntry> barEntries = new ArrayList<>();
    private DatabaseReference databaseRef;
    private TextView todayUsageTextView;
    private TextView monthUsageTextView;
    ImageView notificationBell;
    private TextView costTextView;
    private TextView textMonth, textYear, textTotal;
    private static final double RATE_PER_KWH = 8.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setupNavigationDrawer();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        firestore = FirebaseFirestore.getInstance();
        String userId = currentUser.getUid();
        // ← POINT AT /users/{uid}, not /users/{uid}/sensorData
        databaseRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(userId);

        todayUsageTextView = findViewById(R.id.todayUsage);
        monthUsageTextView = findViewById(R.id.monthUsage);
        costTextView = findViewById(R.id.costTextView);
        textMonth = findViewById(R.id.textMonth);
        textYear = findViewById(R.id.textYear);
        textTotal = findViewById(R.id.textTotal);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);

//        >...

        fetchDataAndUpdateCharts();
        TextView dateTextView = findViewById(R.id.dateTextView);
        String currentDate = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);

        notificationBell = findViewById(R.id.notificationBell);

        if (notificationBell != null) {
            notificationBell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Trigger the action when the bell icon is clicked
                    fetchNotificationsFromRealtimeDatabase();
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        startService(new Intent(this, ConsumptionService.class));

    }

    // Fetch notifications from Realtime Database
    private void fetchNotificationsFromRealtimeDatabase() {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");

        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> notifications = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String notificationMessage = snapshot.getValue(String.class);
                    notifications.add(notificationMessage);
                }

                // Pass notifications to NotificationActivity
                Intent intent = new Intent(Dashboard.this, NotificationActivity.class);
                intent.putStringArrayListExtra("notifications", (ArrayList<String>) notifications);
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Dashboard", "Error fetching notifications: ", databaseError.toException());
            }
        });
    }

    private void highlightSelected(TextView selected, TextView... others) {
        selected.setTextColor(Color.parseColor("#1E88E5"));
        selected.setTypeface(null, android.graphics.Typeface.BOLD);
        for (TextView other : others) {
            other.setTextColor(Color.parseColor("#757575"));
            other.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }


    private void fetchDataAndUpdateCharts() {
        Log.d("Dashboard", "Starting fetchDataAndUpdateCharts()");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot root) {
                Map<String, Float> applianceTotals = new HashMap<>();

                for (DataSnapshot wrapper : root.getChildren()) {
                    if (!wrapper.hasChild("sensorData") || wrapper.getKey().equals("sensorData")) {
                        continue;
                    }

                    String deviceName = wrapper.child("deviceName").getValue(String.class);
                    if (deviceName == null) deviceName = wrapper.getKey();

                    float sum = 0f;
                    DataSnapshot inner = wrapper.child("sensorData");

                    for (DataSnapshot reading : inner.getChildren()) {
                        if (reading.hasChild("voltage") && reading.hasChild("current")) {
                            Double v = reading.child("voltage").getValue(Double.class);
                            Double c = reading.child("current").getValue(Double.class);
                            if (v != null && c != null) {
                                sum += (v * c)*1000; // actual power calculation in watts
                            }
                        } else {
                            for (DataSnapshot tsNode : reading.getChildren()) {
                                Double v = tsNode.child("voltage").getValue(Double.class);
                                Double c = tsNode.child("current").getValue(Double.class);
                                if (v != null && c != null) {
                                    sum += (v * c)*1000; // actual power calculation in watts
                                }
                            }
                        }
                    }

                    applianceTotals.put(deviceName, sum / 1000f); // convert to kWh if needed
                }

                // Prepare common readings list for bar and pie charts
                DataSnapshot sensorData = root.child("sensorData");
                if (!sensorData.exists()) return;

                DataSnapshot tsWrapper = null;
                for (DataSnapshot child : sensorData.getChildren()) {
                    if (!child.hasChild("voltage") || !child.hasChild("current")) {
                        tsWrapper = child;
                        break;
                    }
                }
                if (tsWrapper == null) return;

                List<DataSnapshot> readings = new ArrayList<>();
                for (DataSnapshot tsNode : tsWrapper.getChildren()) {
                    readings.add(tsNode);
                }
                Collections.sort(readings, (a, b) -> a.getKey().compareTo(b.getKey()));

                int total = readings.size();
                int start = Math.max(0, total - 5);

                // === Update Pie chart ===
                ArrayList<PieEntry> pieEntries = new ArrayList<>();
                float totalEnergy = 0f;

                // Process the LED bulb readings
                for (int i = start; i < total; i++) {
                    DataSnapshot r = readings.get(i);
                    Double v = r.child("voltage").getValue(Double.class);
                    Double c = r.child("current").getValue(Double.class);
                    if (v == null || c == null) continue;

                    if (c < 0.01) continue; // skip readings when bulb is OFF or current is negligible
                    float energy = (float) (v * c) * 1000;

                    totalEnergy += energy;

                    // Add the entry for LED bulb
                    String label = "LED Bulb";
                    pieEntries.add(new PieEntry(energy, label));
                }

                // Add dummy washing machine entry (for example, assume it's consuming 500W)
                float washingMachineEnergy = 100f; // Dummy energy consumption in Watts
                totalEnergy += washingMachineEnergy;

                // Add the washing machine entry
                pieEntries.add(new PieEntry(washingMachineEnergy, "Washing Machine"));
                pieEntries.add(new PieEntry(totalEnergy, "Total Consumption"));


                // Update Pie chart with total energy
                updatePieChart(pieEntries);

                // === Update Bar chart ===
                ArrayList<BarEntry> barEntries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();

                for (int i = start; i < total; i++) {
                    DataSnapshot r = readings.get(i);
                    Double v = r.child("voltage").getValue(Double.class);
                    Double c = r.child("current").getValue(Double.class);
                    if (v == null || c == null) continue;

                    float power = (float) (v * c)*1000; // in Watts
                    barEntries.add(new BarEntry(i - start, power));
                    labels.add(""); // Blank label or use device name here if desired
                }
                updateBarChart(barEntries, labels);

                // Update usage and cost info
                if (!barEntries.isEmpty()) {
                    float latest = barEntries.get(barEntries.size() - 1).getY(); // in Watts
                    float kWh = latest / 1000f;
                    todayUsageTextView.setText(String.format(Locale.getDefault(), "%.2f kWh", kWh));
                    monthUsageTextView.setText(String.format(Locale.getDefault(), "%.2f kWh", kWh));
                    costTextView.setText(String.format(Locale.getDefault(), "Cost: ₹%.2f", kWh * RATE_PER_KWH));
                }

                // Display total energy for Pie chart (in kWh)
                float totalEnergyInKWh = totalEnergy / 1000f; // Convert to kWh
                Log.d("Dashboard", "Total energy consumption: " + totalEnergyInKWh + " kWh");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Dashboard", "DB error", error.toException());
            }
        });
    }


    private void updatePieChart(List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Appearance settings
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f); // Slightly smaller center
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawEntryLabels(true); // Show labels on chart
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Appliance Usage");
        pieChart.setCenterTextSize(16f);


//        Legend legend = pieChart.getLegend();
//        legend.setEnabled(true);
//        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
//        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
//        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//        legend.setDrawInside(false);
//        legend.setWordWrapEnabled(true);
//        legend.setTextSize(12f);

        pieChart.setExtraOffsets(5, 5, 5, 5); // Reduced offset to prevent compression
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
        Log.d("PieChartDebug", "Pie Entries Count: " + entries.size());
    }

    private void updateBarChart(List<BarEntry> entries, List<String> labels) {
        BarDataSet set = new BarDataSet(entries, "kWh");
        set.setValueTextSize(12f);
        set.setDrawValues(true);
        set.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData data = new BarData(set);
        data.setBarWidth(0.6f);
        barChart.setData(data);

        XAxis x = barChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis left = barChart.getAxisLeft();
        left.setAxisMinimum(0f);
        float maxY = entries.stream()
                .map(BarEntry::getY)
                .max(Float::compare)
                .orElse(1f);
        left.setAxisMaximum(maxY * 1.2f);
        barChart.getAxisRight().setEnabled(false);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.invalidate();
    }

    // Helper to turn your node‑key into a human‑readable time.
// If your keys *aren't* timestamps, swap in your own logic.
    private String formatTimestamp(String key) {
        try {
            long ts = Long.parseLong(key);
            Date d = new Date(ts);
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(d);
        } catch (NumberFormatException e) {
            return key;
        }
    }






}