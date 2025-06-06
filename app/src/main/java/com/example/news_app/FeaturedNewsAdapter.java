package com.example.news_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class FeaturedNewsAdapter extends RecyclerView.Adapter<FeaturedNewsAdapter.FeaturedViewHolder> {

    // 1. Add click listener interface
    public interface OnNewsClickListener {
        void onNewsClick(NewsItem newsItem);
    }
    private final Context context;
    private final List<NewsItem> featuredItems;
    private final OnNewsClickListener listener; // Listener field

    // 2. Update constructor to include listener
    public FeaturedNewsAdapter(Context context, List<NewsItem> featuredItems, OnNewsClickListener listener) {
        this.context = context;
        this.featuredItems = featuredItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured_news, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        NewsItem item = featuredItems.get(position);

        holder.title.setText(item.getTitle());

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_news_placeholder)
                .error(R.drawable.ic_news_placeholder)
                .into(holder.image);

        // 3. Set click listener on the card
        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewsClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return featuredItems.size();
    }

    public static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView image;
        TextView title;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.featuredCard);
            image = itemView.findViewById(R.id.featuredImage);
            title = itemView.findViewById(R.id.featuredTitle);
        }
    }
}