package com.mobileappscompany.training.mycameraapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY_PIC = 2;
    private static final int PERMISSION_REQUEST_CODE = 3;
    private static final String PHOTOS_PATH = "";
    private static final String LOG_CAT = "LOG_CAT";
    private ImageView mainImageView;
    private String mCurrentPhotoPath;
    private Uri photoURI;
    private File storageDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainImageView = (ImageView) findViewById(R.id.imageView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), PHOTOS_PATH);

        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
                createPhotosDirectory();
            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {
            // Code for Below 23 API Oriented Device
            // Do next code
            createPhotosDirectory();
        }
        createPhotosDirectory();
    }

    private void createPhotosDirectory() {
        if(!storageDir.exists()){
            if(!storageDir.mkdir()) {
                showToastMessage("The directory can't be created.");
            }
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createPhotosDirectory();
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    showToastMessage("Permission Denied, You cannot use local drive to create photos directory.");
                    Log.e("value", "Permission Denied, You cannot use local drive.");
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void launchCamera(View view) {
        dispatchTakePictureIntent();
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                File photoFile = createImageFile();
                photoURI = FileProvider.getUriForFile(this,
                        "com.mobileappscompany.training.mycameraapp",
                        photoFile);
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    grantUriPermission(resolveInfo.activityInfo.packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ex) {
                showToastMessage("There was a problem saving the photo.");
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            mainImageView.setImageBitmap(imageBitmap);

            setPic();

            revokeUriPermission(photoURI,Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        else if(requestCode == REQUEST_GALLERY_PIC && resultCode == RESULT_OK)
        {
            InputStream inputStream = null;
            try
            {
                Uri photoUri = data.getData();
                if(photoUri != null) {
                    inputStream = getContentResolver().openInputStream(photoUri);
                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    mainImageView.setImageBitmap(image);
                    Log.d(LOG_CAT, "trying to set image from gallery");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showToastMessage("Unable to open image");
            } finally {
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showToastMessage("Unable to close the image");
                    }
                }
            }
        }
    }

    public void launchGallery(View view) {
        getPicFromGallery();
    }

    public void getPicFromGallery()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        Uri data = Uri.parse(storageDir.getPath());
        photoPickerIntent.setDataAndType(data, "image/*");
        startActivityForResult(photoPickerIntent, REQUEST_GALLERY_PIC);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mainImageView.getWidth();
        int targetH = mainImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mainImageView.setImageBitmap(bitmap);

//        bmOptions.inBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
//        mainImageView.setImageBitmap(bmOptions.inBitmap);
    }

    private void showToastMessage(String message) {
        Toast msg = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        msg.show();
    }
}
