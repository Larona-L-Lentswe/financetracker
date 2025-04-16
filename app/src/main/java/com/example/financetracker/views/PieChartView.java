package com.example.financetracker.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple custom PieChart implementation that doesn't rely on external libraries
 */
public class PieChartView extends View {

    // Paint objects for drawing
    private Paint piePaint;
    private Paint textPaint;
    private Paint centerPaint;

    // Chart data and colors
    private List<PieEntry> entries = new ArrayList<>();
    private int[] colorList = {
            Color.rgb(25, 118, 210),   // Blue
            Color.rgb(244, 67, 54),    // Red
            Color.rgb(76, 175, 80),    // Green
            Color.rgb(255, 193, 7),    // Yellow
            Color.rgb(156, 39, 176),   // Purple
            Color.rgb(255, 87, 34),    // Orange
            Color.rgb(3, 169, 244),    // Light Blue
            Color.rgb(233, 30, 99),    // Pink
            Color.rgb(0, 150, 136),    // Teal
            Color.rgb(121, 85, 72)     // Brown
    };

    // Layout measurements
    private RectF chartBounds = new RectF();
    private float centerX, centerY, radius;
    private String noDataText = "No data available";

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Initialize paints
        piePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        piePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.WHITE);
        centerPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Set data for the pie chart
     * @param newEntries List of PieEntry objects containing value and label
     */
    public void setData(List<PieEntry> newEntries) {
        this.entries = newEntries;
        invalidate(); // Redraw the view
    }

    /**
     * Set the text to display when there's no data
     * @param text No data message
     */
    public void setNoDataText(String text) {
        this.noDataText = text;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Calculate dimensions for the pie chart
        float padding = 50f;
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(w, h) / 2f - padding;

        // Set the bounds for the pie chart
        chartBounds.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (entries == null || entries.isEmpty()) {
            // Draw no data text
            canvas.drawText(noDataText, centerX, centerY, textPaint);
            return;
        }

        // Calculate total value for percentages
        float total = 0f;
        for (PieEntry entry : entries) {
            total += entry.getValue();
        }

        // Draw pie slices
        float startAngle = 0f;
        int colorIndex = 0;

        for (PieEntry entry : entries) {
            // Skip entries with zero or negative values
            if (entry.getValue() <= 0) continue;

            // Calculate sweep angle (percentage of the pie)
            float sweepAngle = 360f * (entry.getValue() / total);

            // Set slice color
            piePaint.setColor(colorList[colorIndex % colorList.length]);
            colorIndex++;

            // Draw the slice
            canvas.drawArc(chartBounds, startAngle, sweepAngle, true, piePaint);

            // Calculate angle for label
            float labelAngle = startAngle + (sweepAngle / 2f);
            float labelX = centerX + (float) (radius * 0.7 * Math.cos(Math.toRadians(labelAngle)));
            float labelY = centerY + (float) (radius * 0.7 * Math.sin(Math.toRadians(labelAngle)));

            // Draw percentage label if the slice is big enough
            if (sweepAngle > 15) {
                String percentText = String.format("%.1f%%", (entry.getValue() / total) * 100);
                canvas.drawText(percentText, labelX, labelY, textPaint);
            }

            // Update start angle for next slice
            startAngle += sweepAngle;
        }

        // Draw center circle (optional for donut style)
        float holeRadius = radius * 0.5f;
        canvas.drawCircle(centerX, centerY, holeRadius, centerPaint);
    }

    /**
     * Data class for pie chart entries
     */
    public static class PieEntry {
        private float value;
        private String label;

        public PieEntry(float value, String label) {
            this.value = value;
            this.label = label;
        }

        public float getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }
}