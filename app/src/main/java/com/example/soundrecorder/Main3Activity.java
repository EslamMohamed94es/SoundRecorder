package com.example.soundrecorder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.concurrent.TimeUnit;
public class Main3Activity extends AppCompatActivity {
    TextView FileName;
    TextView FileDate;
    TextView ProgressPoint;
    TextView Duration;
    String path;
    MediaPlayer mediaPlayer;
    SeekBar seekBar;
    int button_state;
    int fprogres;
    String name;
    String date;
    String duration;
    FloatingActionButton fab;
    ImageButton imagestop;
    ImageButton imagedelete;
    ImageButton imagerename;
    ImageButton speakermode;
    SQLDataBase sqlDataBase;
    File directory;
    mythread mythread;
    boolean threadstate = true;
    SharedPreferences sharedPreferences;
    boolean speakerMode;
    long hours;
    long minutes;
    long seconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorAccent)));
        getSupportActionBar().setTitle(R.string.app_name);

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        speakerMode = sharedPreferences.getBoolean("Mode", true);
        speakermode = findViewById(R.id.imageButton3);
        if (speakerMode == true) {
            speakermode.setImageResource(R.drawable.speaker);
            Toast.makeText(Main3Activity.this, R.string.speakermode, Toast.LENGTH_LONG).show();

        } else {
            speakermode.setImageResource(R.drawable.earoiece);
            Toast.makeText(Main3Activity.this, R.string.earpiecemode, Toast.LENGTH_LONG).show();
        }
        button_state = 0;
        Bundle bundle = getIntent().getExtras();
        name = bundle.getString("FileName");
        date = bundle.getString("FileDate");
        duration = bundle.getString("FileDuration");
        FileName = findViewById(R.id.textView6);
        FileName.setText(name);
        FileDate = findViewById(R.id.textView7);
        FileDate.setText(date);
        ProgressPoint = findViewById(R.id.textView2);
        ProgressPoint.setText("00:00:00");
        Duration = findViewById(R.id.textView3);
        Duration.setText(duration);
        path = Environment.getExternalStorageDirectory() + "/SoundRecorder/" + name;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (Exception e) {

        }
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fprogres = progress;
                hours = TimeUnit.MILLISECONDS.toHours(mediaPlayer.getCurrentPosition());
                minutes = TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition());
                seconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition())
                        - TimeUnit.MINUTES.toSeconds(minutes);
                ProgressPoint.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(fprogres);
                hours = TimeUnit.MILLISECONDS.toHours(mediaPlayer.getCurrentPosition());
                minutes = TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition());
                seconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition())
                        - TimeUnit.MINUTES.toSeconds(minutes);
                ProgressPoint.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }
        });

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (button_state == 0) {
                    StartPlaying();
                } else if (button_state == 1) {
                    PausePlaying();
                } else if (button_state == 2) {
                    ResumePlaying();
                }

            }
        });

        imagestop = findViewById(R.id.imageButton);
        imagestop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopPlaying();
            }
        });
        imagedelete = findViewById(R.id.imageButton4);
        imagedelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Delete();
            }
        });
        imagerename = findViewById(R.id.imageButton2);
        imagerename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(Main3Activity.this)
                        .setIcon(R.drawable.rename)
                        .setTitle(R.string.renamerecord);
                final EditText input = new EditText(Main3Activity.this);
                input.setText(FileName.getText().toString());
                builder.setView(input);
                builder.setPositiveButton(R.string.button8, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RenameItem(input.getText().toString());
                    }
                }).show();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                fab.setImageResource(R.drawable.play);
                button_state = 0;
                threadstate = false;
                seekBar.setProgress(0);
                ProgressPoint.setText("00:00:00");
            }
        });
        speakermode = findViewById(R.id.imageButton3);
        speakermode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopPlaying();
                if (speakerMode == true) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("Mode", false);
                    editor.commit();
                    speakermode.setImageResource(R.drawable.earoiece);
                    speakerMode = false;
                    Toast.makeText(Main3Activity.this, R.string.earpiecemode, Toast.LENGTH_LONG).show();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("Mode", true);
                    editor.commit();
                    speakermode.setImageResource(R.drawable.speaker);
                    speakerMode = true;
                    Toast.makeText(Main3Activity.this, R.string.speakermode, Toast.LENGTH_LONG).show();
                }

            }
        });
        imagestop.setEnabled(false);
        speakermode.setEnabled(true);
        imagedelete.setEnabled(true);
        imagerename.setEnabled(true);
    }

    public void StartPlaying() {

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            speakerMode = sharedPreferences.getBoolean("Mode", true);
            if (speakerMode == false) {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            mediaPlayer.prepare();
        } catch (Exception e) {
            Toast.makeText(Main3Activity.this, R.string.failedplayrec, Toast.LENGTH_LONG).show();
        }
        threadstate = true;
        mythread = new mythread();
        mediaPlayer.start();
        mythread.start();
        button_state = 1;
        fab.setImageResource(R.drawable.pause1);
        imagestop.setEnabled(true);
        speakermode.setEnabled(false);
        imagedelete.setEnabled(false);
        imagerename.setEnabled(false);
    }

    public void PausePlaying() {
        button_state = 2;
        mediaPlayer.pause();
        fab.setImageResource(R.drawable.play);
    }

    public void ResumePlaying() {
        mediaPlayer.start();
        button_state = 1;
        fab.setImageResource(R.drawable.pause1);
    }

    public void StopPlaying() {
        button_state = 0;
        mediaPlayer.stop();
        fab.setImageResource(R.drawable.play);
        threadstate = false;
        seekBar.setProgress(0);
        ProgressPoint.setText("00:00:00");
        imagestop.setEnabled(false);
        speakermode.setEnabled(true);
        imagedelete.setEnabled(true);
        imagerename.setEnabled(true);
    }

    public void Delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Main3Activity.this);
        builder.setMessage(R.string.deleteconf)
                .setIcon(R.drawable.delete)
                .setTitle(R.string.deleteconfirmation)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StopPlaying();
                        directory = new File(path);
                        directory.delete();
                        SQLDataBase sqlDataBase;
                        sqlDataBase = new SQLDataBase(Main3Activity.this);
                        sqlDataBase.delete(name);
                        Toast.makeText(Main3Activity.this, R.string.itemdeleted, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Main3Activity.this, Main2Activity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

    }


    public void RenameItem(String NewName) {
        StopPlaying();
        sqlDataBase = new SQLDataBase(Main3Activity.this);
        directory = new File(path);
        path = Environment.getExternalStorageDirectory() + "/SoundRecorder/" + NewName + ".m4a";
        File file = new File(path);
        directory.renameTo(file);
        int ID = sqlDataBase.getID(name);
        name = NewName + ".m4a";
        sqlDataBase.update(ID, name);
        Toast.makeText(Main3Activity.this, R.string.itemupdated, Toast.LENGTH_LONG).show();
        FileName.setText(name);
        path = Environment.getExternalStorageDirectory() + "/SoundRecorder/" + name;
    }

    @Override
    protected void onPause() {
        if (button_state == 1) {
            Toast.makeText(Main3Activity.this, R.string.playerplaying, Toast.LENGTH_LONG).show();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if(button_state==1||button_state==2)
            {
                Toast.makeText(Main3Activity.this, R.string.backoption, Toast.LENGTH_LONG).show();
            }
            else
            {
                Intent intent = new Intent(Main3Activity.this, Main2Activity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(button_state==1||button_state==2)
        {
            Toast.makeText(Main3Activity.this, R.string.backoption, Toast.LENGTH_LONG).show();
        }
        else
        {
            Intent intent = new Intent(Main3Activity.this, Main2Activity.class);
            startActivity(intent);
        }
    }

    class mythread extends Thread {
        @Override
        public void run() {

            while (mediaPlayer != null) {
                try {
                    Thread.sleep(1000);
                    if (threadstate == false) {
                        return;
                    } else {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        ProgressPoint.setText(mediaPlayer.getCurrentPosition());
                    }

                } catch (Exception e) {

                }

            }
            if (threadstate == false)
                return;


        }
    }


}
