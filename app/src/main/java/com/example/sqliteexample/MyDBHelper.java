package com.example.sqliteexample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MyDBHelper extends SQLiteOpenHelper {
    public MyDBHelper(@Nullable Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create table
        sqLiteDatabase.execSQL(Constants.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        // Upgrade table ( if there is any structure change the change db version )

        // drop older table if exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);
        // create table again
        onCreate(sqLiteDatabase);
    }

    public long insertRecord(String name, String phone, String email, String dob, String bio, String image, String addedTime, String updatedTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(Constants.C_NAME, name);
        contentValues.put(Constants.C_PHONE, phone);
        contentValues.put(Constants.C_EMAIL, email);
        contentValues.put(Constants.C_DOB, dob);
        contentValues.put(Constants.C_BIO, bio);
        contentValues.put(Constants.C_IMAGE, image);
        contentValues.put(Constants.C_ADDED_TIMESTAMP, addedTime);
        contentValues.put(Constants.C_UPDATED_TIMESTAMP, updatedTime);

        // insert row , it will return record id of saved record
        long id = db.insert(Constants.TABLE_NAME, null, contentValues);

        db.close();

        return id;
    }

    public void updateRecord(String id, String name, String phone, String email, String dob, String bio, String image, String addedTime, String updatedTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(Constants.C_NAME, name);
        contentValues.put(Constants.C_PHONE, phone);
        contentValues.put(Constants.C_EMAIL, email);
        contentValues.put(Constants.C_DOB, dob);
        contentValues.put(Constants.C_BIO, bio);
        contentValues.put(Constants.C_IMAGE, image);
        contentValues.put(Constants.C_ADDED_TIMESTAMP, addedTime);
        contentValues.put(Constants.C_UPDATED_TIMESTAMP, updatedTime);

        // insert row , it will return record id of saved record
        db.update(Constants.TABLE_NAME, contentValues, Constants.C_ID + " = ?", new String[]{id});

        db.close();

    }

    public ArrayList<ModelRecord> getAllRecords(String orderBy) {
        ArrayList<ModelRecord> recordArrayList = new ArrayList<>();
        String select = "SELECT * FROM " + Constants.TABLE_NAME + " ORDER BY " + orderBy;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(select, null);
        if (cursor.moveToFirst()) {
            do {
                recordArrayList.add(new ModelRecord(cursor.getInt(cursor.getColumnIndex(Constants.C_ID)) + "",
                        cursor.getString(cursor.getColumnIndex(Constants.C_NAME)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_PHONE)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_EMAIL)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_DOB)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_BIO)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))));
            } while (cursor.moveToNext());
        }
        db.close();
        return recordArrayList;
    }

    public ArrayList<ModelRecord> searchRecords(String query) {
        ArrayList<ModelRecord> recordArrayList = new ArrayList<>();
        String select = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + Constants.C_NAME + " LIKE '%" + query + "%'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(select, null);
        if (cursor.moveToFirst()) {
            do {
                recordArrayList.add(new ModelRecord(cursor.getInt(cursor.getColumnIndex(Constants.C_ID)) + "",
                        cursor.getString(cursor.getColumnIndex(Constants.C_NAME)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_PHONE)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_EMAIL)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_DOB)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_BIO)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                        cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))));
            } while (cursor.moveToNext());
        }
        db.close();
        return recordArrayList;
    }

    public int getRecordsCount() {
        String countQuery = "SELECT * FROM " + Constants.TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public void deleteData(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Constants.TABLE_NAME, Constants.C_ID + " = ?", new String[]{id});
        db.close();
    }

    public void deleteAllData() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + Constants.TABLE_NAME);
        db.close();
    }
}
