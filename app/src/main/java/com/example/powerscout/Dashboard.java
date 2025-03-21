package com.example.powerscout;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.ArrayList;


public class Dashboard extends BaseActivity {

    private PieChart pieChart;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setupNavigationDrawer();

        // Initialize charts
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);

        // Set up the Pie Chart with dummy data
        setupPieChart();

        // Set up the Bar Chart with dummy data
        setupBarChart();
    }

    private void setupPieChart() {
        // Dummy data for the pie chart (device usage)
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(34f, "AC"));  // 34% for Air Conditioner
        entries.add(new PieEntry(27f, "WM"));  // 27% for Washing Machine
        entries.add(new PieEntry(23f, "Dry")); // 23% for Dryer
        entries.add(new PieEntry(16f, "Car")); // 16% for Car

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
        pieChart.setCenterText("0.48\nkWh/Now");
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



    private void setupBarChart() {
        BarChart barChart = findViewById(R.id.barChart);

        // Dummy data for the bar chart (monthly usage)
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(4, 6f));
        entries.add(new BarEntry(5, 7f));
        entries.add(new BarEntry(6, 8f));
        entries.add(new BarEntry(7, 9f));
        entries.add(new BarEntry(8, 10f));
        entries.add(new BarEntry(9, 9f));
        entries.add(new BarEntry(10, 10f));
        entries.add(new BarEntry(11, 11f));
        entries.add(new BarEntry(12, 12f));

        BarDataSet dataSet = new BarDataSet(entries, "kWh");
        dataSet.setColor(Color.parseColor("#A5D6A7")); // Light green color
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(false); // Hide values above bars

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f); // Adjust bar width

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);

        // X-Axis styling
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(4f);
        xAxis.setAxisMaximum(12f);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.BLACK);

        // Y-Axis styling
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(16f);
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0")); // Soft gray gridlines

        barChart.getAxisRight().setEnabled(false); // Hide right Y-axis
        barChart.setDragEnabled(true);
        barChart.setScaleXEnabled(true);
        barChart.setVisibleXRangeMaximum(6);

        // Disable legend for clean UI
        barChart.getLegend().setEnabled(false);

        barChart.invalidate(); // Refresh the chart
    }

}