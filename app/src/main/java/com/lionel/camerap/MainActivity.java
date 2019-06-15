package com.lionel.camerap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 101;

    private static final int REQUEST_IMAGE_FULL_SIZED = 1;
    private static final int REQUEST_IMAGE_THUMBNAIL = 2;
    private ViewDataBinding dataBinding;
    private File currentPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);


        checkPermission();
    }

    private void checkPermission() {
        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }
    }

    public void onFullSizeClicked(View view) {
        try {
            currentPhotoFile = createPhotoFile();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null && currentPhotoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".android.fileprovider", currentPhotoFile);
                Log.d("<>", "uri: " + photoUri);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, REQUEST_IMAGE_FULL_SIZED);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createPhotoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String photoFileName = "CameraP_" + timeStamp + ".jpg";

        File storageDir = new File(Environment.getExternalStorageDirectory(), "cameraP_pic");   // the child name have to be same with file_paths 's path
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        Log.d("<>", storageDir.getAbsolutePath());

        File photoFile = new File(storageDir, photoFileName);
        if (!photoFile.exists()) {
            photoFile.createNewFile();
        }
        Log.d("<>", photoFile.getAbsolutePath());

        return photoFile;
    }

    public void onThumbnailClicked(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_THUMBNAIL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_THUMBNAIL && resultCode == RESULT_OK) {
            Bundle extra = data.getExtras();
            Bitmap bitmap = (Bitmap) extra.get("data");
            dataBinding.setVariable(BR.imageFile, bitmap);
        }

        if (requestCode == REQUEST_IMAGE_FULL_SIZED && resultCode == RESULT_OK) {
            makePhotoScannable();

            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoFile.getAbsolutePath());
            if (bitmap != null) {
                dataBinding.setVariable(BR.imageFile, bitmap);
            }
        }
    }

    private void makePhotoScannable() {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(currentPhotoFile);
        intent.setData(contentUri);
        this.sendBroadcast(intent);
    }


    @BindingAdapter("setImageByGlide")
    public static void setImageByGlide(ImageView view, Bitmap bitmap) {
        if (bitmap != null) {
            Glide.with(view.getContext())
                    .load(bitmap)
                    .into(view);
        }
    }
}
