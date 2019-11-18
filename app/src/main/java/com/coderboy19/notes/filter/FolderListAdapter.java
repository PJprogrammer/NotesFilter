package com.coderboy19.notes.filter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.ViewHolder>  {
    private ArrayList<String> listItems;
    private Context context;
    private ItemClickListener clickListener;

    Boolean monthChooser;
    Boolean weekChooser;
    Boolean dayChooser;

    public FolderListAdapter(Context context, ArrayList<String> listItems) {
        this.context = context;
        this.listItems = listItems;
        monthChooser = true;
        weekChooser  = false;
        dayChooser   = false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView folderName;
        public TextView dateStamp;

        public ViewHolder(View itemView) {
            super(itemView);
            folderName = (TextView) itemView.findViewById(R.id.namefolder);
            dateStamp = (TextView) itemView.findViewById(R.id.datefolder);
            itemView.setTag(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) clickListener.onClick(v, getAdapterPosition());
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public FolderListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_folder,parent,false);
        return new FolderListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final FolderListAdapter.ViewHolder holder, final int position) {
        if(monthChooser) {
            String month = "   " + listItems.get(position);
            holder.folderName.setText(month);
            holder.dateStamp.setText("");
        }
        else if(weekChooser) {
            String week = "   " + listItems.get(position).substring(0,6);
            String date = listItems.get(position).substring(7);
            holder.folderName.setText(week);
            holder.dateStamp.setText(date);
        }else if(dayChooser) {
            String day = "   " + listItems.get(position);
            holder.folderName.setText(day);
            holder.dateStamp.setText("");
        }
    }

    public ArrayList<String> getListItems() {
        return listItems;
    }

    public void setListItems(ArrayList<String> listItems) {
        this.listItems = listItems;
        notifyDataSetChanged();
    }

    public Boolean getMonthChooser() { return monthChooser; }

    public void setMonthChooser(Boolean monthChooser) { this.monthChooser = monthChooser; }

    public Boolean getWeekChooser() { return weekChooser; }

    public void setWeekChooser(Boolean weekChooser) { this.weekChooser = weekChooser; }

    public Boolean getDayChooser() { return dayChooser; }

    public void setDayChooser(Boolean dayChooser) { this.dayChooser = dayChooser;  }

    @Override
    public int getItemCount() {
        return listItems.size();
    }
}
