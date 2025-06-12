package com.example.news_app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class RegularNewsAdapter extends RecyclerView.Adapter<RegularNewsAdapter.RegularViewHolder> {

    private static final String TAG = "RegularNewsAdapter";

    // Add click listener interface
    public interface OnNewsClickListener {
        void onNewsClick(NewsItem newsItem);
    }


    private final Context context;
    private final List<NewsItem> regularItems;
    private final OnNewsClickListener listener;  // Add listener field


    public RegularNewsAdapter(Context context, List<NewsItem> regularItems, OnNewsClickListener listener) {
        this.context = context;
        this.regularItems = regularItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RegularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_regular_news, parent, false);
        return new RegularViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegularViewHolder holder, int position) {
        NewsItem item = regularItems.get(position);

        holder.title.setText(item.getTitle());

        // Calculate time since posting
        long currentTime = System.currentTimeMillis();
        long postedTime = item.getTimestamp();
        long diff = currentTime - postedTime;

        // Convert to appropriate units
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        String timeText;
        if (days > 0) {
            timeText = days + "d";
        } else if (hours > 0) {
            timeText = hours + "h";
        } else if (minutes > 0) {
            timeText = minutes + "m";
        } else {
            timeText = "Just now";
        }

        holder.time.setText(timeText);

        // Add detailed logging for image loading
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_news_placeholder)
                .error(R.drawable.ic_news_placeholder)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Image load failed: " + item.getImageUrl(), e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        Log.d(TAG, "Image loaded successfully: " + item.getImageUrl());
                        return false;
                    }
                })
                .into(holder.image);

        // Set click listener on the item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewsClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return regularItems.size();
    }

    public static class RegularViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView time;

        public RegularViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.regularImage);
            title = itemView.findViewById(R.id.regularTitle);
            time = itemView.findViewById(R.id.regularTime);
        }
    }
}