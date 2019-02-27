package com.example.soundrecorder;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
public class Main2Activity extends AppCompatActivity {

    ArrayList<RecordingItems> listitems1;
    SQLDataBase sqlDataBase;
    String path;
    File directory;
    File[] files;
    Cursor cursor;
    TextView FileName;
    TextView FileDate;
    TextView FileDuration;
    int POsition;
    MyCustomAdapter myadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorAccent)));
        listitems1 = new ArrayList<RecordingItems>();
        sqlDataBase = new SQLDataBase(Main2Activity.this);
        path = Environment.getExternalStorageDirectory() + "/SoundRecorder/";
        directory = new File(path);
        if (!directory.exists()) {
            if (directory.mkdir()) ;
        }
        files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            try {
                cursor = sqlDataBase.getallrecords(files[i].getName());
                cursor.moveToFirst();
                listitems1.add(new RecordingItems(cursor.getString(1), cursor.getString(2), cursor.getString(3)));
            } catch (Exception e) {

            }
        }
        myadapter = new MyCustomAdapter(listitems1);
        final ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(myadapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Main2Activity.this, Main3Activity.class);
                Bundle bundle = new Bundle();
                FileName = (TextView) view.findViewById(R.id.textView);
                String itemname = FileName.getText().toString();
                FileDate = (TextView) view.findViewById(R.id.textView5);
                String itemdate = FileDate.getText().toString();
                FileDuration = (TextView) view.findViewById(R.id.textView4);
                String itemduration = FileDuration.getText().toString();
                bundle.putString("FileName", itemname);
                bundle.putString("FileDate", itemdate);
                bundle.putString("FileDuration", itemduration);
                intent.putExtras(bundle);
                startActivity(intent);


            }

        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                FileName = (TextView) view.findViewById(R.id.textView);
                POsition = position;

                final AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this)
                        .setIcon(R.drawable.options)
                        .setTitle(R.string.options);
                builder.setPositiveButton(R.string.button5, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                       DeleteItem();

                    }
                })
                        .setNeutralButton(R.string.share, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ShareItem();
                            }
                        })
                        .setNegativeButton(R.string.button8, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final AlertDialog.Builder builder2 = new AlertDialog.Builder(Main2Activity.this)
                                        .setIcon(R.drawable.rename)
                                        .setTitle(R.string.renamerecord);
                                final EditText input = new EditText(Main2Activity.this);
                                input.setText(FileName.getText().toString());
                                builder2.setView(input);
                                builder2.setPositiveButton(R.string.button8, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                       RenameItem(input.getText().toString());
                                    }
                                }).show();
                            }
                        }).show();


                return true;
            }
        });

    }

    public void DeleteItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
        builder.setMessage(R.string.deleteconf)
                .setIcon(R.drawable.delete)
                .setTitle(R.string.deleteconfirmation)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String itemname = FileName.getText().toString();
                        path = Environment.getExternalStorageDirectory() + "/SoundRecorder/" + itemname;
                        directory = new File(path);
                        directory.delete();
                        sqlDataBase = new SQLDataBase(Main2Activity.this);
                        sqlDataBase.delete(itemname);
                        Toast.makeText(Main2Activity.this, R.string.itemdeleted, Toast.LENGTH_LONG).show();
                        listitems1.remove(POsition);
                        myadapter.notifyDataSetChanged();

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();


    }

    public void ShareItem() {
        boolean write_perm;
        boolean read_perm;
        write_perm = ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
        read_perm = ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
        if ( write_perm && read_perm) {
            String itemname = FileName.getText().toString();
            path = Environment.getExternalStorageDirectory() + "/SoundRecorder/" + itemname;
            File f=new File(path);
            Uri uri = Uri.parse(f.getAbsolutePath());
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setType("audio/*");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Share audio File"));
        }
    }

    public void RenameItem(String NewName) {
        String itemname = FileName.getText().toString();
        sqlDataBase = new SQLDataBase(Main2Activity.this);
        path = Environment.getExternalStorageDirectory() + "/SoundRecorder/" + itemname;
        directory = new File(path);
        path = Environment.getExternalStorageDirectory() + "/SoundRecorder/" + NewName + ".m4a";
        File file = new File(path);
        directory.renameTo(file);
        int ID = sqlDataBase.getID(itemname);
        sqlDataBase.update(ID, NewName + ".m4a");
        Toast.makeText(Main2Activity.this, R.string.itemupdated, Toast.LENGTH_LONG).show();
        finish();
        startActivity(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Main2Activity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(Main2Activity.this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    class MyCustomAdapter extends BaseAdapter {
        ArrayList<RecordingItems> lsiitm = new ArrayList<RecordingItems>();

        MyCustomAdapter(ArrayList<RecordingItems> items) {
            this.lsiitm = items;
        }

        @Override
        public int getCount() {
            return lsiitm.size();
        }

        @Override
        public Object getItem(int position) {
            return lsiitm.get(position).RecorderName;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.recordingitems, null);
            TextView TXTName = (TextView) view.findViewById(R.id.textView);
            TextView TXTDate = (TextView) view.findViewById(R.id.textView4);
            TextView TXTDuration = (TextView) view.findViewById(R.id.textView5);
            TXTName.setText(lsiitm.get(position).RecorderName);
            TXTDate.setText(lsiitm.get(position).RecorderDate);
            TXTDuration.setText(lsiitm.get(position).RecorderDuration);
            return view;
        }

    }

}

