package com.coderboy19.notes.filter;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission.RECORD_AUDIO;

public class ClassListActivity extends AppCompatActivity implements ItemClickListener {
    public static final int RequestPermissionCode = 1;
    public static final int RETRIEVE_DATE_FOLDERS = 0;
    public static final int CAPTURE_RESULT = 1;
    public static final int RECORD_RESULT = 2;
    public static final int MEMO_RESULT = 3;
    private Context context = this;
    private ClassListActivity activity = this;

    private SharedPreferences sharedPreferences;

    private Boolean isRecording = false;
    private MediaRecorder mediaRecorder;

    private ClassListAdapter adapter;

    private ArrayList<SchoolClass> schoolClasses = new ArrayList<>();

    //CreateClass Variables
    private String className;
    private int period = 1;
    private TimeSlot timeSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Classes");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        setUpBottomNavigation();
        deserializeClassObj();
        createClassList();
        setUpAlarms(schoolClasses);
    }

    //toolbar_main
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_classlist, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_addclass) {
            createNewClassObject();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Creates a School Class
    public void createNewClassObject() {
        final Dialog classSetup1 = new Dialog(this);
        final Dialog classSetup2 = new Dialog(this);

        //declare references to widgets (Setup Screen Two)
        classSetup2.setContentView(R.layout.dialog_step2_class_setup);
        final NumberPicker periodPicker = (NumberPicker) classSetup2.findViewById(R.id.numberPicker);
        final Button btn_timePicker = (Button) classSetup2.findViewById(R.id.button2);
        Button okButton2 = (Button) classSetup2.findViewById(R.id.Btn_Ok2);
        classSetup2.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        //declare references to widgets (Setup Screen One)
        classSetup1.setContentView(R.layout.dialog_step1_class_setup);
        final EditText edit_className = (EditText) classSetup1.findViewById(R.id.edit_username);
        Button okButton1 = (Button) classSetup1.findViewById(R.id.Btn_Ok1);
        classSetup1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        //Display Setup Screen One
        classSetup1.show();

        okButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Retrieve String from EditText
                className = edit_className.getText().toString();
                //Remove Setup Screen One from Screen
                classSetup1.dismiss();
                //Display Setup Screen Two
                classSetup2.show();
            }
        });

        //Set up Picker
        final String[] periodPicker_list = new String[] { "Period 1", "Period 2", "Period 3", "Period 4",
                "Period 5", "Period 6", "Period 7", "Period 8", "Period 9"};
        periodPicker.setMinValue(0);
        periodPicker.setMaxValue(8);
        periodPicker.setDisplayedValues(periodPicker_list);
        periodPicker.setOnValueChangedListener( new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int
                    oldVal, int newVal) {
                period = newVal+1;
            }
        });

        //Get Current Time
        Calendar currentTime = Calendar.getInstance();
        final int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        final int currentMin = currentTime.get(Calendar.MINUTE);

        //Initialize Time obj
        timeSlot = new TimeSlot();

        //Create Time Picker Dialogs
        btn_timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TimePickerDialog endTimePicker = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                               timeSlot.setEndTime(selectedHour,selectedMinute);

                               //Display time to button
                               btn_timePicker.setText(timeSlot.toString());
                            }
                        }, currentHour, currentMin, false);
                endTimePicker.setTitle("Select End Time");
                endTimePicker.show();

                final TimePickerDialog startTimePicker = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                timeSlot.setStartTime(selectedHour,selectedMinute);
                            }
                        },currentHour,currentMin,false);

                startTimePicker.setTitle("Select Start Time");
                startTimePicker.show();
            }
        });


        //Close the Setup Screen
        okButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                classSetup2.dismiss();
                SchoolClass newSchoolClass = new SchoolClass(className,period,timeSlot);

                sortClass(newSchoolClass);
                serializeClassObj(newSchoolClass);
                setUpAlarms(new ArrayList<SchoolClass>(Arrays.asList(newSchoolClass)));
            }
        });

    }

    public void sortClass(SchoolClass schoolClass) {
        for(int i = 0 ;i < schoolClasses.size();i++) {
            if(schoolClass.getPeriodNumber() < schoolClasses.get(i).getPeriodNumber()){
                schoolClasses.add(i,schoolClass);
                break;
            }
        }

        if(schoolClasses.size() == 0 || schoolClass.getPeriodNumber() >= schoolClasses.get(schoolClasses.size()-1).getPeriodNumber())
            schoolClasses.add(schoolClass);

        createClassList();
    }

    public void createClassList () {
        RecyclerView classList = (RecyclerView) findViewById(R.id.recyclerview_main_activity);
        classList.setHasFixedSize(true);
        classList.setLayoutManager(new LinearLayoutManager(classList.getContext()));
        classList.setItemAnimator(new DefaultItemAnimator());

        adapter = new ClassListAdapter(schoolClasses,this);
        classList.setAdapter(adapter);
        adapter.setClickListener(this);
    }

    //Serializes a SchoolClass obj
    public void serializeClassObj (SchoolClass schoolClass) {
        try {
            File objFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                  + File.separator + schoolClass.getClassName() + ".ser");
            FileOutputStream fOS = new FileOutputStream(objFile);
            ObjectOutputStream oOs = new ObjectOutputStream(fOS);
            oOs.writeObject(schoolClass);
            oOs.close();
            fOS.close();
        } catch (IOException except) {
            System.out.println("/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("Exception: " + except.getMessage());
            except.printStackTrace();
            System.out.println("/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
        }
    }

    //De-serializes a SchoolClass obj
    public void deserializeClassObj () {
        try {
            File[] classFiles = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath()).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".ser");
                }
            });
            if (null != classFiles) {
                for (File f : classFiles) {
                    FileInputStream fIS = new FileInputStream(f.getAbsoluteFile());
                    ObjectInputStream oIS = new ObjectInputStream(fIS);
                    SchoolClass schoolClass = (SchoolClass) oIS.readObject();
                    schoolClasses.add(schoolClass);
                }
            }
        } catch (Exception except) {
            System.out.println("/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
            System.out.println("Exception: " + except.getMessage());
            except.printStackTrace();
            System.out.println("/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
        }

    }

    public void setUpBottomNavigation() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_main);
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if(DateGenerator.getClass(schoolClasses) == null) {
                            Toast.makeText(context, "Invalid Time", Toast.LENGTH_LONG).show();
                            return false;
                        }
                        switch (item.getItemId()) {
                            case R.id.navigation_camera:
                                captureImage();
                                return true;
                            case R.id.navigation_microphone:
                                captureAudio();
                                return true;
                            case R.id.navigation_memo:
                                captureMemo();
                                return true;
                        }
                        return false;
                    }
                });
    }

    public File createPath(String type) {
        Calendar now = Calendar.getInstance();
        String path = getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath();
        SchoolClass currentClass = DateGenerator.getClass(schoolClasses);

        path += File.separator + currentClass.getClassName() + File.separator;
        path += DateGenerator.getMonths().get(now.get(Calendar.MONTH)) + File.separator;
        path += "Week" + now.get(Calendar.WEEK_OF_MONTH)+ File.separator;
        path += DateGenerator.getDays(now.get(Calendar.MONTH)+1,2017).get(now.get(Calendar.WEEK_OF_MONTH)-1).get(now.get(Calendar.DAY_OF_WEEK)-1);
        File newDir  = new File(Uri.parse(path).getPath());
        newDir.mkdirs();

        int mediaNumber;
        if(type.equals(".jpg"))
            mediaNumber = currentClass.getImageNumber();
        else if(type.equals(".m4a"))
            mediaNumber = currentClass.getAudioNumber();
        else
            mediaNumber = currentClass.getMemoNumber();

        path += File.separator + "Untitled" + "_" + mediaNumber + type;
        return new File(Uri.parse(path).getPath());
    }

    //Checks permissions for recording audio
    boolean checkPermissions () {
        int resultAudio = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return resultAudio == PackageManager.PERMISSION_GRANTED;
    }

    //Requests permissions form the user to record audio
    private void requestPermissions() {
        ActivityCompat.requestPermissions(ClassListActivity.this, new
                String[]{RECORD_AUDIO}, RequestPermissionCode);
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                break;
        }
    }

    //Handle Activity Results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.notifyDataSetChanged();
        if (requestCode == RETRIEVE_DATE_FOLDERS) {
            if (resultCode == RESULT_OK) {
                //Toast.makeText(this, "Result_MainActivity", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == CAPTURE_RESULT) {
            if (resultCode == RESULT_OK) {
                //Toast.makeText(this, "Result_Capture", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == RECORD_RESULT) {
            if(resultCode == RESULT_OK) {
                //Toast.makeText(this, "Result_Record", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == MEMO_RESULT) {
            if (resultCode == RESULT_OK) {
                //Toast.makeText(this, "Result_Memo", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Set up Notifications
    private void scheduleNotification(Notification notification, int hour, int minute) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        //Toast.makeText(context, "Timer Set", Toast.LENGTH_LONG).show();

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
    }
    private Notification getNotification(String content, Date timeWhen) {
        Notification.Builder builder = new Notification.Builder(this);
        String timeChoice = sharedPreferences.getString("notificationTime","2");
        builder.setContentTitle("Only " + timeChoice + " Minutes Remaining!");
        builder.setContentText(content);
        builder.setWhen(timeWhen.getTime());
        PendingIntent returnToMainActivity = PendingIntent.getActivity(this,4,new Intent(this,ClassListActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(returnToMainActivity);
        builder.setSmallIcon(R.drawable.alarm_32);
        String strRingtonePreference = sharedPreferences.getString("notifications_new_message_ringtone", "DEFAULT_SOUND");
        builder.setSound(Uri.parse(strRingtonePreference));
        if(sharedPreferences.getBoolean("notifications_new_message_vibrate",true)) {
            builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        }
        return builder.build();
    }
    public void setUpAlarms (ArrayList<SchoolClass> classes) {
        if(sharedPreferences.getBoolean("notifications_new_message",true)) {
            for(SchoolClass schoolClass:classes) {
                Date alarmTime = schoolClass.getClassTime().compareEndTime(context);
                if(alarmTime != null)
                    scheduleNotification(getNotification(schoolClass.getClassName() + " Class",alarmTime),alarmTime.getHours(),alarmTime.getMinutes());
            }
        }
    }

    @Override
    public void onClick(View view, int position) {
        TextView fileName1 = (TextView) findViewById(R.id.fileName1);
        TextView fileName2 = (TextView) findViewById(R.id.fileName2);
        int viewId = view.getId();

        File file = null;
        ArrayList<File> listFiles = adapter.getTodayFiles(adapter.getListItem(position));

        if(viewId == fileName1.getId()) {
            file = listFiles.get(0);
        }else if(viewId == fileName2.getId()) {
            file = listFiles.get(1);
        } else {
            Intent intent = new Intent(this, FolderListActivity.class);
            intent.putExtra("className",adapter.getListItem(position).getClassName());
            startActivityForResult(intent,RETRIEVE_DATE_FOLDERS);
        }

        if(file != null) {
            if (file.getName().endsWith(".jpg"))
                ViewMedia.viewImage(context, file);
            if (file.getName().endsWith(".m4a"))
                ViewMedia.viewAudio(context, file);
            if (file.getName().endsWith(".txt"))
                ViewMedia.viewMemo(context, file);
        }
    }

    @Override
    public void onLongClick(View view, int position) {
        Toast.makeText(context, "Long Click", Toast.LENGTH_LONG).show();
    }

    public void captureAudio () {
        final Dialog recordDialog = new Dialog(this);
        recordDialog.setContentView(R.layout.dialog_record);
        recordDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final File fileLocation = createPath(".m4a");

        final String oneCount = "Recording.";
        final String twoCount = "Recording..";
        final String threeCount = "Recording...";
        final TextView recordingState = (TextView) recordDialog.findViewById(R.id.recordingState_title);
        final Chronometer timeKeeper = (Chronometer) recordDialog.findViewById(R.id.timekeeper);
        timeKeeper.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                    if (recordingState.getText().equals(threeCount) || recordingState.getText().equals("Record")) {
                        recordingState.setText(oneCount);
                    } else if (recordingState.getText().equals(oneCount)) {
                        recordingState.setText(twoCount);
                    } else if (recordingState.getText().equals(twoCount)) {
                        recordingState.setText(threeCount);
                    }
            }
        });
        final FloatingActionButton record = (FloatingActionButton) recordDialog.findViewById(R.id.recordButton);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRecording && checkPermissions()) {
                    isRecording = true;
                    record.setImageResource(R.drawable.stop_recording);
                    timeKeeper.setBase(SystemClock.elapsedRealtime());
                    timeKeeper.start();

                    Toast.makeText(context,"Recording Started",Toast.LENGTH_LONG);

                    mediaRecorder = new MediaRecorder();
                    try {
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        mediaRecorder.setOutputFile(fileLocation.getAbsolutePath());
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if(!checkPermissions()) {
                    requestPermissions();
                } else {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    mediaRecorder.release();
                    mediaRecorder = null;

                    Toast.makeText(context,"Recording Ended",Toast.LENGTH_LONG);

                    timeKeeper.stop();
                    record.setImageResource(R.drawable.microphone);
                    recordDialog.dismiss();

                    Intent resultData = new Intent();
                    resultData.putExtra("fileName",fileLocation);
                    activity.onActivityResult(RECORD_RESULT, ClassListActivity.RESULT_OK,resultData);
                    isRecording = false;
                }
            }
        });
        recordDialog.show();
    }
    public void captureImage() {
        final File fileLocation = createPath(".jpg");
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileLocation));
        startActivityForResult(camIntent,CAPTURE_RESULT);
    }
    public void captureMemo () {
        final Dialog memo = new Dialog(this);
        memo.setContentView(R.layout.dialog_memo);
        memo.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        final File fileLocation = createPath(".txt");
        final EditText memo_String = (EditText) memo.findViewById(R.id.edit_memo);

        memo_String.requestFocus();
        memo.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        Button okButton = (Button) memo.findViewById(R.id.accept_memo);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stringContent = memo_String.getText().toString();

                try {
                    FileWriter fw = new FileWriter(fileLocation.getAbsoluteFile());
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(stringContent);
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                memo.dismiss();

                Intent resultData = new Intent();
                resultData.putExtra("fileName",fileLocation);
                activity.onActivityResult(ClassListActivity.MEMO_RESULT, ClassListActivity.RESULT_OK,resultData);
            }
        });
        memo.show();
    }
}


