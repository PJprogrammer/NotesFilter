package com.coderboy19.notes.filter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FolderListActivity extends AppCompatActivity implements ItemClickListener {
    public static final int RETRIEVE_MEDIA_FILES = 0;

    RecyclerView recyclerView;
    FolderListAdapter adapter;

    int monthNumber;
    int weekNumber;
    int dayNumber;
    String path;
    Boolean showEmptyFolders;
    String className;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbars);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pick the Date");

        className = getIntent().getExtras().getString("className");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showEmptyFolders = sharedPreferences.getBoolean("coderboy19_switch",true);

        createFolderNames();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(showEmptyFolders) {
            adapter.setListItems(DateGenerator.getMonths());
        }else{
            path = getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath() + File.separator + className;
            File dir = new File(path);

            if(dir.exists() && dir.isDirectory())
                adapter.setListItems(new ArrayList<>(Arrays.asList(dir.list())));
            else
                adapter.setListItems(new ArrayList<String>(0));
        }

    }

    @Override
    public void onBackPressed() {
        Intent resultData = new Intent();
        setResult(Activity.RESULT_OK, resultData);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RETRIEVE_MEDIA_FILES) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    public void createFolderNames () {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_folder_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if(showEmptyFolders) {
            adapter = new FolderListAdapter(this,DateGenerator.getMonths());
        } else {
            path = getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath() + File.separator + className;
            ArrayList<String> folderNames;
            File dir = new File(path);

            if(dir.exists() && dir.isDirectory())
                folderNames = new ArrayList<>(Arrays.asList(dir.list()));
            else
                folderNames = new ArrayList<>(0);
            adapter = new FolderListAdapter(this,folderNames);
        }

        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
    }
    @Override
    public void onClick(View view, int position) {
        //weeks = new FolderListAdapter(this,DateGenerator.getWeeks(7,2017),false,true,false);
        //recyclerView.setAdapter(weeks);
        //weeks.setClickListener(this);
        if(adapter.getMonthChooser()) {
            adapter.setMonthChooser(false);
            adapter.setWeekChooser(true);
            adapter.setDayChooser(false);
            if(showEmptyFolders) {
                monthNumber = position;
                adapter.setListItems(DateGenerator.getWeeks(monthNumber+1, 2017));
            } else {
                path += File.separator + adapter.getListItems().get(position);
                ArrayList<String> fileNames = new ArrayList<>(Arrays.asList(new File(path).list()));
                ArrayList<String> weeks = new ArrayList<>();
                Calendar cal = Calendar.getInstance();
                try{
                    Date date = new SimpleDateFormat("MMMM", Locale.US).parse(adapter.getListItems().get(position));
                    cal.setTime(date);
                    for(String week : fileNames) {
                        int weekNumber = Integer.parseInt(week.substring(week.length()-1));
                        weeks.add(DateGenerator.getWeeks(cal.get(Calendar.MONTH)+1,2017).get(weekNumber-1));
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                adapter.setListItems(weeks);
            }
        }
        else if (adapter.getWeekChooser()) {
            adapter.setMonthChooser(false);
            adapter.setWeekChooser(false);
            adapter.setDayChooser(true);
            if(showEmptyFolders) {
                weekNumber = position;
                adapter.setListItems(DateGenerator.getDays(monthNumber+1,2017).get(weekNumber));
            } else {
                path += File.separator + adapter.getListItems().get(position).substring(0,4) + adapter.getListItems().get(position).charAt(5);
                ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(new File(path).list()));
                adapter.setListItems(arrayList);
            }
        }else if(adapter.getDayChooser()) {
            adapter.setMonthChooser(true);
            adapter.setDayChooser(false);
            adapter.setWeekChooser(false);

            Intent intent = new Intent(this, MediaListActivity.class);
            intent.putExtra("Class Name",className);
            if(showEmptyFolders) {
                dayNumber = position;
                intent.putExtra("Month",monthNumber);
                intent.putExtra("Week", weekNumber);
                intent.putExtra("Day", dayNumber);
            }else {
                path += File.separator + adapter.getListItems().get(position);
                intent.putExtra("Full Path",path);
            }
            startActivityForResult(intent,RETRIEVE_MEDIA_FILES);
        }
    }

    @Override
    public void onLongClick(View view, int position) {
        Toast.makeText(this, "OnLongClick", Toast.LENGTH_LONG).show();
    }
}
