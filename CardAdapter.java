package com.example.motion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private List<Hotel> hotels;

    public CardAdapter(List<Hotel> hotels) {
        this.hotels = hotels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);
        holder.title.setText(hotel.getName());
        holder.description.setText(hotel.getDescription());

        // Load hotel image using Glide
        if (hotel.getImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(hotel.getImageUrl())
                    .into(holder.hotelImage);
        } else {
            holder.hotelImage.setImageResource(R.drawable.rglobe); // Placeholder image
        }
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView description;
        public ImageView hotelImage;
        public ImageButton leftArrow;
        public ImageButton rightArrow;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.hotel_name);
            description = view.findViewById(R.id.hotel_info);
            hotelImage = view.findViewById(R.id.hotel_image);
            leftArrow = view.findViewById(R.id.left_arrow);
            rightArrow = view.findViewById(R.id.right_arrow);
        }
    }
}
