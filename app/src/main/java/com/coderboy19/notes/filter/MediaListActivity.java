package com.coderboy19.notes.filter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class MediaListActivity extends AppCompatActivity implements ItemClickListener, OnStartDragListener {
    private String className;
    private ArrayList<File> fileNames;
    private String path;
    private Boolean showEmptyFolders;

    private ItemTouchHelper mItemTouchHelper;

    MediaListAdapter adapter;

    private ActionMode multiSelect;
    Boolean isSelected = false;

    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        className = getIntent().getExtras().getString("Class Name");
        getSupportActionBar().setTitle(className);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        showEmptyFolders = sharedPreferences.getBoolean("coderboy19_switch",true);

        getMediaFiles();
    }

    @Override
    public void onBackPressed() {
        serializeListItems();

        Intent resultData = new Intent();
        setResult(Activity.RESULT_OK, resultData);
        finish();
        super.onBackPressed();
    }

    public void getMediaFiles () {
        if(showEmptyFolders) {
            String month = DateGenerator.getMonths().get(getIntent().getExtras().getInt("Month"));
            String week  = "Week" + (getIntent().getExtras().getInt("Week")+1);
            String day   = DateGenerator.getDays(getIntent().getExtras().getInt("Month")+1,2017).get(getIntent().getExtras().getInt("Week")).get(getIntent().getExtras().getInt("Day"));

            path = getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath();
            path += File.separator + className + File.separator +  month + File.separator + week + File.separator + day;
        }else {
            path = getIntent().getExtras().getString("Full Path");
        }

        File mediaDir =  new File(path);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_media_list);
        TextView description = (TextView) findViewById(R.id.description);

        if(mediaDir.exists() && mediaDir.isDirectory()) {
            File[] files = mediaDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jpg") || name.endsWith(".m4a") || name.endsWith(".txt");
                }
            });
            description.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            fileNames = new ArrayList<File>(Arrays.asList(files));

            adapter = new MediaListAdapter(this,fileNames,this);
            recyclerView.setAdapter(adapter);
            adapter.setClickListener(this);

            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(recyclerView);
        }
        else {
            description.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            description.setText("Folder is Empty");
        }

    }

    @Override
    public void onClick(View view, int position) {
        if(!isSelected) {
            File mediaFile = adapter.getListItems().get(position);

            if (mediaFile.getName().endsWith(".jpg")) {
                ViewMedia.viewImage(this, mediaFile);
            } else if (mediaFile.getName().endsWith(".m4a")) {
                ViewMedia.viewAudio(this, mediaFile);
            } else if (mediaFile.getName().endsWith(".txt")) {
                ViewMedia.viewMemo(this, mediaFile);
            }
        }
        else {
            if(adapter.getSelectedList().contains(adapter.getListItems().get(position))) {
                adapter.removeSelectedListItem(adapter.getSelectedList().indexOf(adapter.getListItems().get(position)));
            }else {
                adapter.addSelectedListItem(adapter.getListItems().get(position));
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLongClick(View view, int position) {
        if(!isSelected) {
            getSupportActionBar().hide();
            multiSelect = startActionMode(ActionModeSelect);
            isSelected = true;
            adapter.addSelectedListItem(adapter.getListItems().get(position));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private ActionMode.Callback ActionModeSelect = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_medialist_selected, menu);
            //mode.setTitle(adapter.getSelectedListSize());
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_email_medialist:
                    sendEmail();
                    return false;
                case R.id.action_rename_medialist:
                    renameFile();
                    return false;
                case R.id.action_select_all:
                    selectAll();
                    return false;
                default:
                    return true;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getSupportActionBar().show();
            adapter.clearSelectedList();
            isSelected = false;
            multiSelect = null;
            adapter.notifyDataSetChanged();
        }
    };

    public void sendEmail () {
        ArrayList<File> selectedFiles = adapter.getSelectedList();
        ArrayList<Uri> attachments = new ArrayList<Uri>();
        String[] TO = {"pauljohn@bernardsboe.com"};
        String[] CC = {""};

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Notes");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Here are the attached files:");

        for(File fileName:selectedFiles) {
            attachments.add(Uri.fromFile(fileName));
        }

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,attachments);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {

        }
    }

    public void renameFile() {
        if(adapter.getSelectedList().size() != 1) {
            Toast.makeText(context,"Only one file can be renamed!", Toast.LENGTH_LONG).show();
        } else {
            final String selectedFile = adapter.getSelectedList().get(0).getName();
            final Dialog renameDialog = new Dialog(context);
            renameDialog.setContentView(R.layout.dialog_rename_file);
            renameDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            renameDialog.show();

            final EditText newFileName_edit = (EditText) renameDialog.findViewById(R.id.edit_file_name);
            final TextView fileName = (TextView) renameDialog.findViewById(R.id.fileName_selected);
            fileName.setText(selectedFile);
            Button okButton = (Button) renameDialog.findViewById(R.id.Btn_Ok3);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newFileName = newFileName_edit.getText().toString() + selectedFile.substring(selectedFile.length()-4);
                    File oldFile = new File(path + File.separator + selectedFile);
                    File newFile = new File(path + File.separator + newFileName);
                    if(oldFile.exists()) {
                        fileName.setText(selectedFile);
                        oldFile.renameTo(newFile);
                        adapter.setListItem(newFile,adapter.getListItems().indexOf(oldFile));
                        adapter.notifyDataSetChanged();
                        adapter.clearSelectedList();
                        Toast.makeText(context,"File: (" + selectedFile + ") has been renamed to " + newFileName , Toast.LENGTH_LONG).show();
                    }
                    renameDialog.dismiss();
                }
            });
        }
    }

    public void selectAll() {
        if(adapter.getListItems().size() != adapter.getSelectedList().size()) {
            adapter.clearSelectedList();
            adapter.getSelectedList().addAll(adapter.getListItems());
            adapter.notifyDataSetChanged();
        }
    }

    public void serializeListItems () {
        try {
            if(adapter != null) {
                File objFile = new File(path + File.separator + "DataSet.ser");
                FileOutputStream fOS = new FileOutputStream(objFile);
                ObjectOutputStream oOs = new ObjectOutputStream(fOS);
                oOs.writeObject(adapter.getListItems());
                oOs.close();
                fOS.close();
            }
        } catch (IOException except) {
            System.out.println("/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("Exception: " + except.getMessage());
            except.printStackTrace();
            System.out.println("/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
        }
    }
}
