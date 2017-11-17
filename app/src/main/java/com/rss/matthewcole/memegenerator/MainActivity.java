package com.rss.matthewcole.memegenerator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final int PHOTO_INTENT_REQUEST_CODE = 10;
    public static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button shareButton = (Button)findViewById(R.id.button_share);
        shareButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                sharePhoto();
            }
        });

    }

    private void sharePhoto() {
        createCompositeImage();
        createShareIntent();
    }

    private void createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        File sharedFile = new File(getCacheDir(), "images/image.png");
        Uri uriToImage = FileProvider.getUriForFile(this, "com.mydomain.fileprovider", sharedFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
        shareIntent.setType("image/png");
        startActivity(shareIntent);
    }

    private void createCompositeImage() {
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frame_layout_meme);
        frameLayout.setDrawingCacheEnabled(true);
        Bitmap bitmap = frameLayout.getDrawingCache();
        File sharedFile = new File(getCacheDir(), "images");
        sharedFile.mkdirs();
        try {
            FileOutputStream stream = new FileOutputStream(sharedFile + "/image.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        frameLayout.setDrawingCacheEnabled(false);
        frameLayout.destroyDrawingCache();
    }


    public void pickPhotoFromGallery(View v) {
        requestPermission();
    }

    private void createPhotoIntent() {
        Intent photoIntent = new Intent(Intent.ACTION_PICK);
        File photoDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Uri photoUri = Uri.parse(photoDirectory.getPath());
        photoIntent.setDataAndType(photoUri, "image/*");
        startActivityForResult(photoIntent, PHOTO_INTENT_REQUEST_CODE);
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            createPhotoIntent();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createPhotoIntent();
                } else {
                    Toast.makeText(this, "Gallery Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO_INTENT_REQUEST_CODE) {
                Uri photoUri = data.getData();
                ImageView imageView = (ImageView)findViewById(R.id.image_view_meme);
                Picasso.with(this).load(photoUri).into(imageView);
            }
        }
    }


}
