package com.example.sqliteexample;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class AddUpdateRecordActivity extends AppCompatActivity {

    private CircularImageView profileIV;
    private EditText nameET, phoneET, emailET, dobET, bioET;
    private FloatingActionButton saveBtn;
    private ActionBar actionBar;

    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int STORAGE_REQUEST_CODE = 102;

    private static final int IMAGE_PICK_CAMERA_CODE = 103;
    private static final int IMAGE_PICK_GALLERY_CODE = 104;

    private String[] cameraPermissions; // camera and storage
    private String[] storagePermissions; // only storage

    private Uri uriImage;

    private String id, name, phone, email, dob, bio, addedTime, updatedTime;
    private MyDBHelper dbHelper;
    private boolean isEditMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_update_record);

        anhXa();

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);


        if (isEditMode) {
            actionBar.setTitle("Update Record");
            id = intent.getStringExtra("ID");
            name = intent.getStringExtra("NAME");
            phone = intent.getStringExtra("PHONE");
            email = intent.getStringExtra("EMAIL");
            dob = intent.getStringExtra("DOB");
            bio = intent.getStringExtra("BIO");
            uriImage = Uri.parse(intent.getStringExtra("IMAGE"));
            addedTime = intent.getStringExtra("ADDED_TIME");
            updatedTime = intent.getStringExtra("UPDATED_TIME");

            nameET.setText(name);
            phoneET.setText(phone);
            emailET.setText(email);
            dobET.setText(dob);
            bioET.setText(bio);
            // không chọn ảnh
            if (uriImage.toString().equals("null")) {
                profileIV.setImageResource(android.R.drawable.ic_delete);
            } else {
                profileIV.setImageURI(uriImage);
            }

        } else {
            actionBar.setTitle("Add Record");
        }


        profileIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickDialog();


            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });
    }

    private void inputData() {
        name = nameET.getText().toString().trim();
        phone = phoneET.getText().toString().trim();
        email = emailET.getText().toString().trim();
        dob = dobET.getText().toString().trim();
        bio = bioET.getText().toString().trim();

        // save to db
        if (isEditMode) {
            String timeStamp = System.currentTimeMillis() + "";
            dbHelper.updateRecord(id, name, phone, email, dob, bio, uriImage + "", addedTime, timeStamp);
            Toast.makeText(this, "Record Updated", Toast.LENGTH_SHORT).show();
        } else {
            String timeStamp = System.currentTimeMillis() + "";
            long id = dbHelper.insertRecord(name, phone, email, dob, bio, uriImage + "", timeStamp, timeStamp);
            Toast.makeText(this, "Record Added Against ID : " + id, Toast.LENGTH_SHORT).show();
        }


    }

    private void imagePickDialog() {
        // options to display in dialog
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // handle clicks
                if (which == 0) {
                    if (!checkCameraPermission()) { // camera clicked
                        requestCameraPermission();
                    } else {
                        // permission already granted
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        // permission already granted
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        // Intent to pick image from gallery, the image will be returned in onActivityResult method
        Intent intentGallery = new Intent(Intent.ACTION_PICK);
        intentGallery.setType("image/*");
        startActivityForResult(intentGallery, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        // Intent to pick image from camera, the image will be returned in onActivityResult method
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image title");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image description");

        // put image uri
        uriImage = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Intent to open camera for image
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);
        startActivityForResult(intentCamera, IMAGE_PICK_CAMERA_CODE);
    }

    private void anhXa() {
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Record");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        profileIV = findViewById(R.id.profileIV);
        nameET = findViewById(R.id.nameET);
        phoneET = findViewById(R.id.phoneET);
        emailET = findViewById(R.id.emailET);
        dobET = findViewById(R.id.dobET);
        bioET = findViewById(R.id.bioET);
        saveBtn = findViewById(R.id.saveBtn);

        dbHelper = new MyDBHelper(this);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private boolean checkStoragePermission() {
        // check if storage permission enabled or not
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        // request the storage permission

        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        // check if camera permission enabled or not
        boolean storage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        boolean camera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        return storage && camera;
    }

    private void requestCameraPermission() {
        // request the camera permission

        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void copyFileOrDirectory(String srcDir, String desDir) {
        try {
            File src = new File(srcDir);
            File des = new File(desDir, src.getName());
            if (src.isDirectory()) {
                String[] files = src.list();
                int filesLength = files.length;
                for (String file : files) {
                    String src1 = new File(src, file).getPath();
                    String des1 = des.getPath();
                    copyFileOrDirectory(src1, des1);
                }
            } else {
                copyFile(src, des);
            }
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void copyFile(File src, File des) throws IOException {
        if (!des.getParentFile().exists()) {
            des.mkdirs();  // create if exists
        }
        if (!des.exists()) {
            des.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;   // nơi đến

        try {
            source = new FileInputStream(src).getChannel();
            destination = new FileInputStream(des).getChannel();
            destination.transferFrom(source, 0, source.size());
            uriImage = Uri.parse(des.getPath());

            Log.d("Image Path", "CopyFile : " + uriImage);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // go back by clicking back button of actionbar
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // result of permission allowed/denied
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    // if allowed returns true ortherwise false
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) { // both permission allowed
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera and Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    // if allowed returns true ortherwise false
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permissions is required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // image picked from camera or gallery will be received here
        if (resultCode == RESULT_OK) { // image is picked
            if (requestCode == IMAGE_PICK_GALLERY_CODE) { // picked galelry
                // crop image
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1).start(this);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // crop image
                CropImage.activity(uriImage).setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1).start(this);
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                // cropped image received
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    uriImage = resultUri;
                    // set image
                    profileIV.setImageURI(uriImage);

                    copyFileOrDirectory(uriImage.getPath() + "", "" + getDir("SQLiteRecordImages", MODE_PRIVATE));
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
                }
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}