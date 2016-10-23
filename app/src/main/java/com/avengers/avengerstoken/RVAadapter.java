package com.avengers.avengerstoken;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Robert on 5/4/2016.
 */
public class RVAadapter extends RecyclerView.Adapter<RVAadapter.ItemViewHolder>{

    List<Item> items;
    private static RecyclerViewClickListener itemListener;
    private int selected_item;

    RVAadapter(List<Item> items, int sel_item, RecyclerViewClickListener itemListener){
        this.items = items;
        this.itemListener = itemListener;
        this.selected_item = sel_item;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        ItemViewHolder ivh = new ItemViewHolder(v);
        return ivh;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int i) {
        holder.personName.setText(items.get(i).transText);

        if(items.get(i).state==true){
            holder.homeView.setSelected(true);
        }
        else{
            holder.homeView.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView personName;
        ImageView personPhoto;
        public View homeView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            this.homeView = itemView;
            personName = (TextView)itemView.findViewById(R.id.person_name);
            personPhoto = (ImageView)itemView.findViewById(R.id.person_photo);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
        }
    }

}
