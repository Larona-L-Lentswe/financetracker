package com.example.financetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.models.Transaction;
import com.example.financetracker.utils.CategoryManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private Context context;
    private SimpleDateFormat dateFormat;
    private NumberFormat currencyFormat;
    private CategoryManager categoryManager;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        this.categoryManager = CategoryManager.getInstance(context);
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Set transaction details
        holder.tvDescription.setText(transaction.getDescription());
        holder.tvAmount.setText(currencyFormat.format(transaction.getAmount()));
        holder.tvDate.setText(dateFormat.format(transaction.getDate()));

        // Get category details
        String categoryName = categoryManager.getCategoryName(transaction.getCategoryId());
        int categoryIconResourceId = categoryManager.getCategoryIcon(transaction.getCategoryId());

        // Set category details
        holder.tvCategory.setText(categoryName);
        holder.ivCategoryIcon.setImageResource(categoryIconResourceId);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateData(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvDescription;
        TextView tvCategory;
        TextView tvDate;
        TextView tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}