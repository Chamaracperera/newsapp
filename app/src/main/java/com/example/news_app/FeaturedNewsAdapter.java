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

    private final Context context;
    private final List<NewsItem> featuredItems;

    public FeaturedNewsAdapter(Context context, List<NewsItem> featuredItems) {
        this.context = context;
        this.featuredItems = featuredItems;
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