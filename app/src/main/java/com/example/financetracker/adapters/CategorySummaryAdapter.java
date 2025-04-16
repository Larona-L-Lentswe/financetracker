package com.example.financetracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.models.CategorySummary;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.CategorySummaryViewHolder> {

    private List<CategorySummary> categorySummaries;
    private NumberFormat currencyFormat;

    public CategorySummaryAdapter(List<CategorySummary> categorySummaries) {
        this.categorySummaries = categorySummaries;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    }

    @NonNull
    @Override
    public CategorySummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_summary, parent, false);
        return new CategorySummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategorySummaryViewHolder holder, int position) {
        CategorySummary categorySummary = categorySummaries.get(position);

        holder.tvCategoryName.setText(categorySummary.getCategoryName());
        holder.tvAmount.setText(currencyFormat.format(categorySummary.getTotalAmount()));
        holder.tvPercentage.setText(String.format("%.1f%%", categorySummary.getPercentage()));
        holder.ivCategoryIcon.setImageResource(categorySummary.getIconResourceId());
    }

    @Override
    public int getItemCount() {
        return categorySummaries.size();
    }

    static class CategorySummaryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;
        TextView tvAmount;
        TextView tvPercentage;

        public CategorySummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
        }
    }
}