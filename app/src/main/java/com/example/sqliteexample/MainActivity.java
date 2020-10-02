package com.example.sqliteexample;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // views
    private FloatingActionButton addRecordBtn;
    private RecyclerView recordsRCV;
    private MyDBHelper myDBHelper;
    private ActionBar actionBar;

    private String orderByNewest = Constants.C_ADDED_TIMESTAMP + " DESC";
    private String orderByOldest = Constants.C_ADDED_TIMESTAMP + " ASC";
    private String orderByNameASC = Constants.C_NAME + " ASC";
    private String orderByNameDESC = Constants.C_NAME + " DESC";
    private String currentOrderByStatus = orderByNewest;


    private static final int STORAGE_REQUEST_CODE_EXPORT = 1;
    private static final int STORAGE_REQUEST_CODE_IMPORT = 2;
    private String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init views
        actionBar = getSupportActionBar();
        actionBar.setTitle("All Records");


        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        recordsRCV = findViewById(R.id.recordsRCV);
        addRecordBtn = findViewById(R.id.addRecordBtn);
        myDBHelper = new MyDBHelper(this);

        loadRecords(orderByNewest);

        // click to start add record activity
        addRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddUpdateRecordActivity.class);
                intent.putExtra("isEditMode", false);
                startActivity(intent);
            }
        });
    }

    private boolean checkStoragePermission() {
        // check if storage permission enabled or not
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermissionImport() {
        // request the storage permission

        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE_IMPORT);
    }

    private void requestStoragePermissionExport() {
        // request the storage permission

        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE_EXPORT);
    }

    private void loadRecords(String orderBy) {
        currentOrderByStatus = orderBy;
        AdapterRecord adapterRecord = new AdapterRecord(MainActivity.this, myDBHelper.getAllRecords(orderBy));
        recordsRCV.setAdapter(adapterRecord);
        actionBar.setSubtitle("Total : " + myDBHelper.getRecordsCount());
    }

    private void searchRecords(String query) {
        AdapterRecord adapterRecord = new AdapterRecord(MainActivity.this, myDBHelper.searchRecords(query));
        recordsRCV.setAdapter(adapterRecord);
    }

    @Override
    protected void onResume() {
        loadRecords(currentOrderByStatus);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchRecords(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchRecords(s);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            sortOptionDialog();
        } else if (id == R.id.deleteAll) {
            myDBHelper.deleteAllData();
            onResume();
        } else if (id == R.id.action_backup) {
            if (checkStoragePermission()) {
                // permission allowed
                exportCSV();
            } else {
                requestStoragePermissionExport();
            }
        } else if (id == R.id.action_restore) {
            if (checkStoragePermission()) {
                // permission allowed
                importCSV();
                onResume();
            } else {
                requestStoragePermissionImport();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortOptionDialog() {
        String options[] = {"Name ASC", "Name DESC", "Newest", "Oldest"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort by").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == 0) { // name asc
                    loadRecords(orderByNameASC);
                } else if (which == 1) { // name desc
                    loadRecords(orderByNameDESC);
                } else if (which == 2) {
                    loadRecords(orderByNewest);
                } else if (which == 3) {
                    loadRecords(orderByOldest);
                }
            }
        }).create().show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case STORAGE_REQUEST_CODE_EXPORT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportCSV();
                } else {
                    Toast.makeText(this, "STORAGE Permission Required...", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case STORAGE_REQUEST_CODE_IMPORT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importCSV();
                } else {
                    Toast.makeText(this, "STORAGE Permission Required...", Toast.LENGTH_SHORT).show();
                }
            }
            break;

        }

    }

    private void importCSV() {
        String filePathAndName = Environment.getExternalStorageDirectory() + "/SQLiteBackup/" + "SQLite_Backup.csv";
        File csvFile = new File(filePathAndName);
        if (csvFile.exists()) {
            // backup exists
            try {
                CSVReader csvReader = new CSVReader(new FileReader(csvFile.getAbsolutePath()));
                String[] nextLine;
                while ((nextLine = csvReader.readNext()) != null) {
                    String id = nextLine[0];
                    String name = nextLine[1];
                    String phone = nextLine[2];
                    String email = nextLine[3];
                    String dob = nextLine[4];
                    String bio = nextLine[5];
                    String image = nextLine[6];
                    String addedTime = nextLine[7];
                    String updatedTime = nextLine[8];
                    String timeStamp = System.currentTimeMillis() + "";
                    long ids = myDBHelper.insertRecord(name, phone, email, dob, bio, image + "", addedTime, updatedTime);
                }
                Toast.makeText(this, "Backup Restored", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No Backup Found", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportCSV() {
        // path of csv file
        File folder = new File(Environment.getExternalStorageDirectory() + "/" + "SQLiteBackup"); // SQLiteBckup is foler name
        boolean isFolderCreated = false;
        if (!folder.exists()) {
            isFolderCreated = folder.mkdir(); // create foler if not exists
        }
        Log.d("CSV_TAG", "exportCSV: " + isFolderCreated);
        String csvFileName = "SQLite_Backup.csv";

        String filePathAndName = folder.toString() + "/" + csvFileName;

        // get records
        ArrayList<ModelRecord> recordArrayList = new ArrayList<>();
        recordArrayList.clear();
        recordArrayList = myDBHelper.getAllRecords(orderByOldest);

        try {
            // write csv file
            FileWriter fw = new FileWriter(filePathAndName);
            for (int i = 0; i < recordArrayList.size(); i++) {
                fw.append("" + recordArrayList.get(i).getId());
                fw.append(",");
                fw.append("" + recordArrayList.get(i).getName());
                fw.append(",");
                fw.append("" + recordArrayList.get(i).getPhone());
                fw.append(",");
                fw.append("" + recordArrayList.get(i).getEmail());
                fw.append(",");
                fw.append("" + recordArrayList.get(i).getDob());
                fw.append(",");
                fw.append("" + recordArrayList.get(i).getBio());
                fw.append(",");
                fw.append("" + recordArrayList.get(i).getImage());
                fw.append(",");
                fw.append("" + recordArrayList.get(i).getAddedTime());
                fw.append(",");
                fw.append("" + recordArrayList.get(i).getUpdatedTime());
                fw.append("\n");
            }
            fw.flush();
            fw.close();
            Toast.makeText(this, "Backup Exported to" + filePathAndName, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}