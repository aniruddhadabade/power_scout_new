package com.example.powerscout;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Dashboard extends BaseActivity {
    private static final double RATE_PER_KWH = 8.0;

    private LineChart lineChartAllTime;
    private BarChart  barChart;
    private TextView  todayUsageTextView;
    private TextView  monthUsageTextView;
    private TextView  costTextView;
    private ImageView notificationBell;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setupNavigationDrawer();

        // Firebase setup
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        // View bindings
        todayUsageTextView  = findViewById(R.id.todayUsage);
        monthUsageTextView  = findViewById(R.id.monthUsage);
        costTextView        = findViewById(R.id.costTextView);
        notificationBell    = findViewById(R.id.notificationBell);
        lineChartAllTime    = findViewById(R.id.lineChartAllTime);
        barChart            = findViewById(R.id.barChart);

        // Date display
        TextView dateTextView = findViewById(R.id.dateTextView);
        dateTextView.setText(
                new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date())
        );

        // Notifications
        notificationBell.setOnClickListener(v -> fetchNotificationsFromRealtimeDatabase());

        // iOS‑13+ notification permission
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
//                ContextCompat.checkSelfPermission(this,
//                        Manifest.permission.POST_NOTIFICATIONS)
//                        != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
//        }

        // Kick off our charts
        fetchDataAndUpdateCharts();

        // Start background service
        startService(new Intent(this, ConsumptionService.class));
    }
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
        databaseRef.child("unknown")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(DataSnapshot tsWrapper) {
                        if (!tsWrapper.exists()) return;

                        // 1) Collect & sort all timestamp nodes
                        List<DataSnapshot> allTs = new ArrayList<>();
                        for (DataSnapshot tsNode : tsWrapper.getChildren()) {
                            allTs.add(tsNode);
                        }
                        Collections.sort(allTs,
                                (a, b) -> a.getKey().compareTo(b.getKey())
                        );

                        // 2) Build LineChart entries (ALL ON‑state)
                        List<Entry>  lineEntries = new ArrayList<>();
                        List<String> lineLabels  = new ArrayList<>();
                        int idx = 0;
                        for (DataSnapshot r : allTs) {
                            String state = r.child("state").getValue(String.class);
                            Double v     = r.child("voltage").getValue(Double.class);
                            Double c     = r.child("current").getValue(Double.class);
                            if (!"ON".equals(state) || v == null || c == null) continue;

                            float kwh = (float)((v + c) * 10);
                            lineEntries.add(new Entry(idx, kwh));
                            lineLabels .add(r.getKey().replace('_', ':'));
                            idx++;
                        }
                        updateAllTimeLineChart(lineEntries, lineLabels);

                        // 3) Build BarChart entries (LAST 5 ON‑state)
                        List<BarEntry> barEntries = new ArrayList<>();
                        List<String>   barLabels  = new ArrayList<>();
                        int kept = 0;
                        // iterate backwards, pick up to 5
                        for (int i = allTs.size()-1; i >= 0 && kept < 5; i--) {
                            DataSnapshot r = allTs.get(i);
                            String state = r.child("state").getValue(String.class);
                            Double v     = r.child("voltage").getValue(Double.class);
                            Double c     = r.child("current").getValue(Double.class);
                            if (!"ON".equals(state) || v == null || c == null) continue;

                            float kwh = (float)((v + c) * 10);
                            // prepend oldest of the 5 to index 0
                            barEntries.add(0, new BarEntry(kept, kwh));
                            barLabels .add(0, r.getKey().replace('_', ':'));
                            kept++;
                        }
                        updateBarChart(barEntries, barLabels);

                        // 4) Update Today/Month/Cost from most recent bar
                        if (!barEntries.isEmpty()) {
                            float latest = barEntries.get(barEntries.size()-1).getY();
                            todayUsageTextView.setText(
                                    String.format(Locale.getDefault(), "%.1f kWh", latest)
                            );
                            monthUsageTextView.setText(
                                    String.format(Locale.getDefault(), "%.1f kWh", latest)
                            );
                            costTextView.setText(
                                    String.format(Locale.getDefault(),
                                            "Cost: ₹%.2f", latest * RATE_PER_KWH)
                            );
                        }
                    }

                    @Override public void onCancelled(DatabaseError err) {
                        Log.e("Dashboard", "DB error", err.toException());
                    }
                });
    }

    private void updateAllTimeLineChart(
            List<Entry> entries, List<String> labels) {

        lineChartAllTime.clear();
        if (entries.isEmpty()) {
            lineChartAllTime.invalidate();
            return;
        }

        LineDataSet set = new LineDataSet(entries, "kWh per Reading");
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        set.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        set.setCircleColor(ColorTemplate.MATERIAL_COLORS[0]);

        LineData data = new LineData(set);
        lineChartAllTime.setData(data);

        XAxis x = lineChartAllTime.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setGranularity(1f);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);

        YAxis left = lineChartAllTime.getAxisLeft();
        left.setAxisMinimum(0f);
        float maxY = (float) entries.stream()
                .mapToDouble(Entry::getY)
                .max()
                .orElse(1.0);
        left.setAxisMaximum(maxY * 1.2f);
        lineChartAllTime.getAxisRight().setEnabled(false);

        lineChartAllTime.getDescription().setEnabled(false);
        lineChartAllTime.getLegend().setEnabled(false);
        lineChartAllTime.animateX(800);
        lineChartAllTime.invalidate();
    }

    private void updateBarChart(
            List<BarEntry> entries, List<String> labels) {

        barChart.clear();
        if (entries.isEmpty()) {
            barChart.invalidate();
            return;
        }

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
        x.setDrawGridLines(false);

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
        barChart.animateY(800);
        barChart.invalidate();
    }
}
