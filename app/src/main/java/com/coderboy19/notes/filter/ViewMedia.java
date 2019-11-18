package com.coderboy19.notes.filter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.coderboy19.notes.filter.ClassListActivity;
import com.coderboy19.notes.filter.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ViewMedia {
    public static void viewImage(Context context, File fileLocation) {
        Intent dispIntent = new Intent(Intent.ACTION_VIEW);
        dispIntent.setDataAndType(Uri.fromFile(fileLocation), "image/jpeg");
        context.startActivity(dispIntent);
    }
    public static void viewAudio(Context context, File fileLocation) {
        Intent dispIntent = new Intent(Intent.ACTION_VIEW);
        dispIntent.setDataAndType(Uri.fromFile(fileLocation), "audio/m4a");
        context.startActivity(dispIntent);
    }
    public static void viewMemo (Context context, File fileLocation) {
        Intent dispIntent = new Intent(Intent.ACTION_VIEW);
        dispIntent.setDataAndType(Uri.fromFile(fileLocation), "text/plain");
        context.startActivity(dispIntent);
    }
}
