package com.example.soundrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class SQLDataBase extends SQLiteOpenHelper {
    public static final String dbname = "recordingitems";
    public static final int version = 1;

    public SQLDataBase(Context context) {
        super(context, dbname, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table IF NOT EXISTS items(id INTEGER primary key,name TEXT,date TEXT,duration TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop table if EXISTS items");
        onCreate(db);
    }

    public void insertitem(String Name, String Date, String Duration) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", Name);
        contentValues.put("date", Date);
        contentValues.put("duration", Duration);
        database.insert("items", null, contentValues);
        database.close();
    }

    public Cursor getallrecords(String FileName) {
        ArrayList arrayList = new ArrayList();
        SQLiteDatabase database = this.getReadableDatabase();
        String[] name = {FileName};
        Cursor cursor = database.rawQuery("select * from items where name like ?", name);
        cursor.moveToFirst();
        database.close();
        return cursor;
    }

    public void delete(String Name) {
        String where = "name=?";
        SQLiteDatabase database = this.getWritableDatabase();
        int numberOFEntriesDeleted = database.delete("items", where, new String[]{Name});
        database.close();
    }

    public void deleteall() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("delete from items");
        database.close();
    }

    public void update(Integer id, String name) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("update items set name='" + name + "' where id=" + String.valueOf(id));
        database.close();
    }

    public Integer getID(String Name) {
        SQLiteDatabase database = this.getReadableDatabase();
        String[] name = {Name};
        Cursor cursor = database.rawQuery("select id from items where name like ?", name);
        cursor.moveToFirst();
        database.close();
        return cursor.getInt(0);
    }
}
