package com.example.soundrecorder;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private FloatingActionButton Record;
    private Button Stop;
    private Button ListRecording;
    private boolean record_perm;
    private boolean write_perm;
    private boolean read_perm;
    private File file;
    private File file1;
    private File file2;
    private File file3;
    private String filename;
    private String folder;
    private String filepath = "";
    private SimpleDateFormat simpleDateFormat;
    private SimpleDateFormat simpleDateFormat1;
    private String currentDatedTime;
    private String currentDate;
    private Chronometer chronometer;
    private String hh;
    private String mm;
    private String ss;
    private String duration;
    private int button_state = 0;
    private long timeWhenStopped = 0;
    private MediaRecorder mediaRecorder;
    private final int request_code_recording = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chronometer = (Chronometer) findViewById(R.id.ch);
        chronometer.setText("00:00:00");
        toolbar = findViewById(R.id.toolbar);
        button_state = 0;
        setSupportActionBar(toolbar);
        Record = findViewById(R.id.floatingActionButton);
        Record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record_perm = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED;
                write_perm = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED;
                read_perm = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED;

                if (record_perm && write_perm && read_perm) {
                    if (button_state == 0) {

                            Start_Record();
                    }

                 else if (button_state == 1) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Pause_Record();
                        }
                } else {
                    Resume_Record();
                }

                    } else {
                        Request_Permission();
                    }
            }
        });
        Stop = findViewById(R.id.button2);
        Stop.setEnabled(false);
        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button_state != 0) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.savethisrecord)
                            .setIcon(R.drawable.save)
                            .setTitle(R.string.savingrecord);
                    final EditText input = new EditText(MainActivity.this);
                    input.setText(currentDatedTime);
                    builder.setView(input);
                    builder.setPositiveButton(R.string.button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Save_POPUp(input.getText().toString());

                        }
                    })
                            .setNeutralButton(R.string.button4, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setNegativeButton(R.string.button5, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Delete_POPUp();
                                }
                            }).show();
                }
            }
        });
        ListRecording = findViewById(R.id.button3);
        ListRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });
    }
    public void Delete_POPUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.deleteconf)
                .setIcon(R.drawable.delete)
                .setTitle(R.string.deleteconfirmation)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        button_state = 0;
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        chronometer.stop();
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.setText("00:00:00");
                        file2 = new File(filepath);
                        file2.delete();
                        Record.setImageResource(R.drawable.mic);
                        Toast.makeText(MainActivity.this, R.string.itemdeleted, Toast.LENGTH_LONG).show();
                        ListRecording.setEnabled(true);
                        Stop.setEnabled(false);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
    public void Save_POPUp(String FileName) {
        try {

            button_state = 0;
            duration = hh + ":" + mm + ":" + ss;
            mediaRecorder.stop();
            mediaRecorder.release();
            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.setText("00:00:00");
            file2 = new File(filepath);
            filename = FileName + ".m4a";
            filepath = file1 + folder + filename;
            file3 = new File(filepath);
            file2.renameTo(file3);
            Toast.makeText(MainActivity.this, R.string.savedrecord + "Internal Storage : "+filename, Toast.LENGTH_LONG).show();
            SQLDataBase sqlDataBase = new SQLDataBase(MainActivity.this);
            sqlDataBase.insertitem(filename, currentDate, duration);
            Record.setImageResource(R.drawable.mic);
            ListRecording.setEnabled(true);
            Stop.setEnabled(false);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, R.string.failedsaverec, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        if (button_state == 1) {
            Toast.makeText(MainActivity.this, R.string.recorderplaying, Toast.LENGTH_LONG).show();
        }
        super.onPause();
    }

    public void Resume_Record() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            Record.setImageResource(R.drawable.pause1);
            chronometer.start();
            mediaRecorder.resume();
            button_state = 1;
        }
    }

    public void Pause_Record() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder.pause();
            chronometer.stop();
            Record.setImageResource(R.drawable.mic);
            timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
            button_state = 2;
        } else {
            Toast.makeText(MainActivity.this, R.string.androidpause, Toast.LENGTH_LONG).show();
        }
    }

    public void Start_Record() {
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        currentDatedTime = simpleDateFormat.format(new Date());
        filename = currentDatedTime + ".m4a";
        folder = "/SoundRecorder/";
        file1 = Environment.getExternalStorageDirectory();
        file = new File(file1 + folder);
        if (!file.exists()) {
            if (file.mkdir()) ;
        }
        filepath = file1 + folder + filename;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(filepath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncodingBitRate(192000);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            button_state = 1;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Record.setImageResource(R.drawable.pause1);
            }
            simpleDateFormat1 = new SimpleDateFormat("yyyy/MM/dd");
            currentDate = simpleDateFormat1.format(new Date());
            Toast.makeText(MainActivity.this, R.string.recording, Toast.LENGTH_LONG).show();
            ListRecording.setEnabled(false);
            Stop.setEnabled(true);
            chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer cArg) {
                    long time = SystemClock.elapsedRealtime() - cArg.getBase();
                    int h = (int) (time / 3600000);
                    int m = (int) (time - h * 3600000) / 60000;
                    int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                    hh = h < 10 ? "0" + h : h + "";
                    mm = m < 10 ? "0" + m : m + "";
                    ss = s < 10 ? "0" + s : s + "";
                    cArg.setText(hh + ":" + mm + ":" + ss);
                }
            });
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, R.string.failedstartrec, Toast.LENGTH_LONG).show();
        }
    }


    public void Request_Permission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, request_code_recording);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        record_perm = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED;
        write_perm = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
        read_perm = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
        if (record_perm && write_perm && read_perm) {
            Toast.makeText(MainActivity.this, R.string.permissingranted, Toast.LENGTH_LONG).show();
            Start_Record();
        } else {
            Toast.makeText(MainActivity.this, R.string.permissindenied, Toast.LENGTH_LONG).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {

    }
}

