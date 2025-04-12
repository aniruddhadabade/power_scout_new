package com.example.powerscout;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Collections;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Locale;
import java.util.Map;



public class Dashboard extends BaseActivity {
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private PieChart pieChart;
    private BarChart barChart;
    private DatabaseReference databaseRef;
    private TextView todayUsageTextView;
    private TextView monthUsageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setupNavigationDrawer();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        databaseRef = FirebaseDatabase.getInstance().getReference("sensor");
        todayUsageTextView = findViewById(R.id.todayUsage);
        monthUsageTextView = findViewById(R.id.monthUsage);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);

        fetchDataAndUpdateCharts();
        TextView dateTextView = findViewById(R.id.dateTextView);
        String currentDate = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);
    }

    private void fetchDataAndUpdateCharts() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch voltage and current
                    double voltage = dataSnapshot.child("voltage").getValue(Double.class);
                    double current = dataSnapshot.child("current").getValue(Double.class);


                    double power = voltage + current ; // Power in watts
                    double energy = power * 10;

                    todayUsageTextView.setText(String.format(Locale.getDefault(), "%.1f kWh", energy));
                    monthUsageTextView.setText(String.format(Locale.getDefault(), "%.1f kWh", energy));
                    updatePieChart(energy);

                    updateBarChart(energy);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Dashboard", "Firebase Database Error: " + databaseError.getMessage());
            }
        });
//        fetchDeviceDataAndUpdatePieChart();
    }

//    private void fetchDeviceDataAndUpdatePieChart() {
//        if (currentUser != null) {
//            String userId = currentUser.getUid();
//            DocumentReference userRef = firestore.collection("users").document(userId);
//
//            userRef.get().addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        // Fetch device names from Firestore
//                        Map<String, Object> devices = (Map<String, Object>) document.get("device_name");
//                        if (devices != null) {
//                            // Fetch consumption data from Realtime Database
//                            databaseRef.child("sensor").addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    if (dataSnapshot.exists()) {
//                                        ArrayList<PieEntry> entries = new ArrayList<>();
//
//                                        // Iterate through devices and add consumption data to PieChart
//                                        for (Map.Entry<String, Object> device : devices.entrySet()) {
//                                            String deviceId = device.getKey();
//                                            String deviceName = (String) device.getValue();
//                                            Double consumption = dataSnapshot.child(deviceId).getValue(Double.class);
//
//                                            if (consumption != null) {
//                                                entries.add(new PieEntry(consumption.floatValue(), deviceName));
//                                            }
//                                        }
//
//                                        // Update PieChart with fetched data
//                                        Object PieEntry;
//                                        updatePieChart(ArrayList<PieEntry> entries);
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//                                    Log.e("Dashboard", "Firebase Database Error: " + databaseError.getMessage());
//                                }
//                            });
//                        }
//                    } else {
//                        Log.e("Dashboard", "Firestore Document does not exist");
//                    }
//                } else {
//                    Log.e("Dashboard", "Firestore Error: " + task.getException().getMessage());
//                }
//            });
//        }
//    }


    private void updatePieChart(double energy) {
        // Dummy data for the pie chart (device usage)
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(34f, "BULB"));  // 34% for Air Conditioner
        entries.add(new PieEntry(27f, "WM"));  // 27% for Washing Machine
//        entries.add(new PieEntry(23f, "Dry")); // 23% for Dryer
//        entries.add(new PieEntry(16f, "Car")); // 16% for Car

        // Create a dataset for the pie chart
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        // Create the pie data object
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Customize the pie chart
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText(String.format("%.2f\nkWh/Now", energy)); // Dynamic kWh value
        pieChart.setCenterTextSize(14f);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);

        // Move the legend to the right side
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false); // Ensures the legend is outside the chart
        legend.setWordWrapEnabled(true);
        legend.setTextSize(14f);
        legend.setXOffset(10f); // Adjust horizontal offset

        pieChart.invalidate(); // Refresh the chart
    }

    private void updateBarChart(double energy) {
        // 1) Single‐entry
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) energy));

        BarDataSet dataSet = new BarDataSet(entries, "kWh Now");
        dataSet.setColor(Color.parseColor("#A5D6A7"));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);

        // 2) X‐axis with one label "Now"
        List<String> labels = Collections.singletonList("Now");
        IndexAxisValueFormatter formatter = new IndexAxisValueFormatter(labels);

        XAxis x = barChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setLabelCount(1);
        x.setValueFormatter(formatter);

        // 3) Y‐axis styling
        YAxis left = barChart.getAxisLeft();
        left.setAxisMinimum(0f);
        left.setAxisMaximum((float) (energy * 1.5)); // 50% headroom
        left.setTextSize(12f);
        left.setGridColor(Color.parseColor("#E0E0E0"));
        barChart.getAxisRight().setEnabled(false);

        barChart.getLegend().setEnabled(false);
        barChart.invalidate();
    }




}