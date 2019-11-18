package com.coderboy19.notes.filter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private ArrayList<File> listItems;
    private ArrayList<File> selectedList;
    private Context context;
    private ItemClickListener clickListener;
    private final OnStartDragListener mDragStartListener;

    public MediaListAdapter(Context context, ArrayList<File> listItems, OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;
        this.context = context;
        this.listItems = listItems;
        selectedList= new ArrayList<File>();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener,ItemTouchHelperViewHolder {
        public TextView fileName;
        public TextView extension;
        public ImageView handleView;
        public CardView fileLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            fileName = (TextView) itemView.findViewById(R.id.fileName);
            extension = (TextView) itemView.findViewById(R.id.extension);
            handleView = (ImageView) itemView.findViewById(R.id.draghandle);
            fileLayout = (CardView) itemView.findViewById(R.id.layout_holder);

            itemView.setTag(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);


        }
        @Override
        public void onClick(View v) {
            if (clickListener != null) clickListener.onClick(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (clickListener != null) clickListener.onLongClick(v, getAdapterPosition());
            return true;
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onItemDismiss(int position) {
        File dismissedFile = listItems.get(position);
        if(dismissedFile.exists()) {
            dismissedFile.delete();
        }
        listItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(listItems, fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public MediaListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_media,parent,false);
        return new MediaListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MediaListAdapter.ViewHolder holder, final int position) {
        String name = "   " + listItems.get(position).getName().substring(0,listItems.get(position).getName().length()-4);
        String extension = listItems.get(position).getName().substring(listItems.get(position).getName().length()-3);
        holder.fileName.setText(name);
        holder.extension.setText(extension);

        if(selectedList.contains(listItems.get(position)))
           holder.fileLayout.setCardBackgroundColor(Color.rgb(255,137,94));
        else
            holder.fileLayout.setCardBackgroundColor(Color.rgb(255,255,255));

        if(extension.equals("jpg")) {
            holder.fileName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.image_32,0,0,0);
        } else if(extension.equals("txt")) {
            holder.fileName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.note_32,0,0,0);
        } else if (extension.equals("m4a")) {
            holder.fileName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.recording_32,0,0,0);
        }

        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });

    }

    //List Item Access Methods
    public ArrayList<File> getListItems() {
        return listItems;
    }

    public void setListItem(File file, int pos) {
        listItems.set(pos,file);
    }

    //Selected Item Access Methods
    public void addSelectedListItem(File value) {
        selectedList.add(value);
    }

    public void removeSelectedListItem(int pos) {
        selectedList.remove(pos);
    }

    public ArrayList<File> getSelectedList() {
        return selectedList;
    }

    public void clearSelectedList() {
        selectedList.clear();
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }
}
