package com.coderboy19.notes.filter;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by notes on 7/10/2017.
 */

public class ClassListAdapter extends RecyclerView.Adapter<ClassListAdapter.ViewHolder> {

    private ArrayList<SchoolClass> listItems;
    private ClassListActivity activity;
    private ItemClickListener clickListener;

    public ClassListAdapter(ArrayList<SchoolClass> listItems, ClassListActivity activity) {
        this.listItems = listItems;
        this.activity = activity;
        Toast.makeText(activity,"hello",Toast.LENGTH_LONG);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView className;
        public TextView timeStamp;
        public TextView periodNumber;
        public TextView fileName1;
        public TextView fileName2;

        public ViewHolder(View itemView) {
            super(itemView);

            className = (TextView) itemView.findViewById(R.id.className);
            timeStamp = (TextView) itemView.findViewById(R.id.timeSlot);
            periodNumber = (TextView) itemView.findViewById(R.id.period);
            fileName1 = (TextView) itemView.findViewById(R.id.fileName1);
            fileName2 = (TextView) itemView.findViewById(R.id.fileName2);

            fileName1.setOnClickListener(this);
            fileName2.setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onClick(v, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (clickListener != null) {
                clickListener.onLongClick(v, getAdapterPosition());
            }
            return true;
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public ClassListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_class,parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ClassListAdapter.ViewHolder holder, final int position) {
        holder.className.setText(listItems.get(position).getClassName());
        holder.timeStamp.setText(listItems.get(position).getClassTime().toString());

        String test = listItems.get(position).getPeriodString();

        holder.periodNumber.setText(listItems.get(position).getPeriodString());


        ArrayList<File> todaysFiles = getTodayFiles(listItems.get(position));
        if(todaysFiles == null || todaysFiles.isEmpty()) {
            holder.fileName1.setVisibility(View.INVISIBLE);
            holder.fileName2.setVisibility(View.INVISIBLE);
        }
        else if(todaysFiles.size() == 1) {
            holder.fileName1.setVisibility(View.VISIBLE);
            holder.fileName1.setText(todaysFiles.get(0).getName());
            holder.fileName2.setVisibility(View.INVISIBLE);
        } else {
            holder.fileName1.setVisibility(View.VISIBLE);
            holder.fileName1.setText(todaysFiles.get(0).getName());
            holder.fileName2.setVisibility(View.VISIBLE);
            holder.fileName2.setText(todaysFiles.get(1).getName());
        }


    }

    public ArrayList<File> getTodayFiles (SchoolClass schoolClass) {
        Calendar now = Calendar.getInstance();
        String path = activity.getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath();
        path += File.separator + schoolClass.getClassName() + File.separator;
        path += DateGenerator.getMonths().get(now.get(Calendar.MONTH)) + File.separator;
        path += "Week" + now.get(Calendar.WEEK_OF_MONTH)+ File.separator;
        path += DateGenerator.getDays(now.get(Calendar.MONTH)+1,2017).get(now.get(Calendar.WEEK_OF_MONTH)-1).get(now.get(Calendar.DAY_OF_WEEK)-1);
        File todayDir  = new File(Uri.parse(path).getPath());

        if(todayDir.exists() && todayDir.isDirectory()) {
            try {
                File serializedFile = new File(path,"DataSet.ser");
                ArrayList<File> filesOrderedList = new ArrayList<>();
                ArrayList<File> filesUnorderedList = new ArrayList<File>(Arrays.asList(todayDir.listFiles()));

                if(serializedFile.exists()) {
                    FileInputStream fIS = new FileInputStream(serializedFile);
                    ObjectInputStream oIS = new ObjectInputStream(fIS);
                    filesOrderedList = (ArrayList<File>) oIS.readObject();
                    filesUnorderedList.remove(serializedFile);
                }

                if(filesOrderedList.size() < 2) {
                    filesUnorderedList.subList(0,filesOrderedList.size()).clear();
                    filesOrderedList.addAll(filesUnorderedList);
                }

                return filesOrderedList;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public SchoolClass getListItem(int position) {
        return listItems.get(position);
    }
}
