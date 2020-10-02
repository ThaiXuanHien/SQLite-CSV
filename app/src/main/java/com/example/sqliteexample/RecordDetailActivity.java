package com.example.sqliteexample;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;

import java.util.Calendar;
import java.util.Locale;

public class RecordDetailActivity extends AppCompatActivity {

    private CircularImageView profileIV;
    private TextView nameTV, phoneTV, emailTV, dobTV, bioTV, addedTimeTV, updatedTimeTV;
    private ActionBar actionBar;
    private MyDBHelper myDBHelper;
    String idRecord = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);
        anhXa();

        Intent intent = getIntent();
        idRecord = intent.getStringExtra("RECORD_ID");
        showRecordDetails();
    }

    private void showRecordDetails() {
        String querySelect = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " + Constants.C_ID + " =\"" + idRecord + "\"";
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(querySelect, null);
        if (cursor.moveToFirst()) {
            do {
                String id = "" + cursor.getInt(cursor.getColumnIndex(Constants.C_ID));
                String name = cursor.getString(cursor.getColumnIndex(Constants.C_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(Constants.C_PHONE));
                String email = cursor.getString(cursor.getColumnIndex(Constants.C_EMAIL));
                String dob = cursor.getString(cursor.getColumnIndex(Constants.C_DOB));
                String bio = cursor.getString(cursor.getColumnIndex(Constants.C_BIO));
                String image = cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE));
                String addedTime = cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP));
                String updatedTime = cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP));

                Calendar calendar1 = Calendar.getInstance(Locale.getDefault());
                calendar1.setTimeInMillis(Long.parseLong(addedTime));
                String addedTimeStamp = "" + DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar1);

                Calendar calendar2 = Calendar.getInstance(Locale.getDefault());
                calendar2.setTimeInMillis(Long.parseLong(updatedTime));
                String updatedTimeStamp = "" + DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar2);

                nameTV.setText(name);
                phoneTV.setText(phone);
                emailTV.setText(email);
                dobTV.setText(dob);
                bioTV.setText(bio);
                addedTimeTV.setText(addedTimeStamp);
                updatedTimeTV.setText(updatedTimeStamp);
                if (image.equals("null")) {
                    profileIV.setImageResource(android.R.drawable.ic_delete);
                } else {
                    profileIV.setImageURI(Uri.parse(image));
                }
            } while (cursor.moveToNext());
        }
        db.close();
    }

    private void anhXa() {
        profileIV = findViewById(R.id.profileIV);
        nameTV = findViewById(R.id.nameTV);
        phoneTV = findViewById(R.id.phoneTV);
        emailTV = findViewById(R.id.emailTV);
        dobTV = findViewById(R.id.dobTV);
        bioTV = findViewById(R.id.bioTV);
        addedTimeTV = findViewById(R.id.addedTimeTV);
        updatedTimeTV = findViewById(R.id.updateTimeTV);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Record Details");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        myDBHelper = new MyDBHelper(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}