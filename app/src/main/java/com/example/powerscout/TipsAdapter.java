package com.example.powerscout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.TipViewHolder>{
    private List<Tip> tipList;
    private Context context;

    public TipsAdapter(List<Tip> tipList, Context context) {
        this.tipList = tipList;
        this.context = context;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tip, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        Tip tip = tipList.get(position);

        holder.tipTitle.setText(tip.getTitle());
        holder.tipAuthor.setText("By " + tip.getAuthor());
        holder.tipDescription.setText(tip.getDescription());
        holder.tipThanks.setText(tip.getThanks() + " Thanks");

        Glide.with(context).load(tip.getImageUrl()).into(holder.tipImage);

        // Open article link in browser when title is clicked
        holder.tipTitle.setOnClickListener(view -> {
            if (tip.getArticleLink() != null && !tip.getArticleLink().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tip.getArticleLink()));
                context.startActivity(browserIntent);
            } else {
                Toast.makeText(context, "No article link available", Toast.LENGTH_SHORT).show();
            }
        });

        // Save tip to favorites
        holder.btnSave.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = context.getSharedPreferences("PowerScoutPrefs", Context.MODE_PRIVATE);
            List<Tip> favoriteTips = getFavoriteTips(sharedPreferences);

            if (!favoriteTips.contains(tip)) {
                favoriteTips.add(tip);
                sharedPreferences.edit().putString("favorite_tips", new Gson().toJson(favoriteTips)).apply();
                Toast.makeText(context, "Tip saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Tip already saved!", Toast.LENGTH_SHORT).show();
            }
        });

        // Share tip via Intent
        holder.btnShare.setOnClickListener(view -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareText = tip.getTitle() + "\n" + tip.getDescription() + "\nRead more: " + tip.getArticleLink();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            context.startActivity(Intent.createChooser(shareIntent, "Share Tip via"));
        });
    }

    private List<Tip> getFavoriteTips(SharedPreferences sharedPreferences) {
        String json = sharedPreferences.getString("favorite_tips", "[]");
        Type type = new TypeToken<List<Tip>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    @Override
    public int getItemCount() {
        return tipList.size();
    }

    static class TipViewHolder extends RecyclerView.ViewHolder {
        TextView tipTitle, tipAuthor, tipDescription, tipThanks;
        ImageView tipImage, btnShare, btnSave;

        TipViewHolder(@NonNull View itemView) {
            super(itemView);
            tipTitle = itemView.findViewById(R.id.tipTitle);
            tipAuthor = itemView.findViewById(R.id.tipAuthor);
            tipDescription = itemView.findViewById(R.id.tipDescription);
            tipThanks = itemView.findViewById(R.id.tipThanks);
            tipImage = itemView.findViewById(R.id.tipImage);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnSave = itemView.findViewById(R.id.btnSave);
        }
    }

    public void filterList(List<Tip> filteredList) {
        tipList = filteredList;
        notifyDataSetChanged();
    }
}
