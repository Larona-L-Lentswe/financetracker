package com.example.financetracker.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom view to display the legend for the pie chart
 */
public class ChartLegendView extends View {

    // Paint objects
    private Paint squarePaint;
    private Paint textPaint;

    // Data
    private List<LegendEntry> entries = new ArrayList<>();

    // Layout measurements
    private float textSize = 36f;
    private float squareSize = 30f;
    private float itemPadding = 16f;
    private float rowSpacing = 40f;

    // Colors - same as pie chart for consistency
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

    public ChartLegendView(Context context) {
        super(context);
        init();
    }

    public ChartLegendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChartLegendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Initialize paints
        squarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        squarePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
    }

    /**
     * Set legend entries
     * @param newEntries List of legend entries (label and optional value)
     */
    public void setEntries(List<LegendEntry> newEntries) {
        this.entries = newEntries;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (entries == null || entries.isEmpty()) {
            return;
        }

        float xPos = getPaddingLeft();
        float yPos = getPaddingTop() + textSize; // Start with baseline at textSize

        int colorIndex = 0;

        for (LegendEntry entry : entries) {
            // Set color for the square
            squarePaint.setColor(colorList[colorIndex % colorList.length]);
            colorIndex++;

            // Draw the color square
            canvas.drawRect(xPos, yPos - squareSize, xPos + squareSize, yPos, squarePaint);

            // Draw the label
            String text = entry.getLabel();
            if (entry.getValue() > 0) {
                text += String.format(" (%.1f%%)", entry.getPercentage());
            }

            canvas.drawText(text, xPos + squareSize + itemPadding, yPos, textPaint);

            // Move to next row
            yPos += rowSpacing;

            // Check if we need to wrap to a new column
            if (yPos > getHeight() - getPaddingBottom()) {
                yPos = getPaddingTop() + textSize;

                // Measure the widest entry to determine the column width
                float maxWidth = 0;
                for (LegendEntry e : entries) {
                    String t = e.getLabel();
                    if (e.getValue() > 0) {
                        t += String.format(" (%.1f%%)", e.getPercentage());
                    }

                    Rect bounds = new Rect();
                    textPaint.getTextBounds(t, 0, t.length(), bounds);
                    maxWidth = Math.max(maxWidth, bounds.width());
                }

                xPos += squareSize + itemPadding + maxWidth + itemPadding * 2;
            }
        }
    }

    /**
     * Data class for legend entries
     */
    public static class LegendEntry {
        private String label;
        private float value;
        private float percentage;

        public LegendEntry(String label, float value, float percentage) {
            this.label = label;
            this.value = value;
            this.percentage = percentage;
        }

        public String getLabel() {
            return label;
        }

        public float getValue() {
            return value;
        }

        public float getPercentage() {
            return percentage;
        }
    }
}