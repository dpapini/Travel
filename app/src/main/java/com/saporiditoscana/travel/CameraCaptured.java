package com.saporiditoscana.travel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.saporiditoscana.travel.Orm.PictureTravel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraCaptured extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static PackageInfo packageInfo;
    private String consegna;
    private int esito;
    private String commento;
    private boolean isNew;

    private ActivityResultLauncher<Intent> cameraLauncher;
    String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent sourceIntent = getIntent();
        consegna = sourceIntent.getStringExtra("consegna");
        esito = sourceIntent.getIntExtra("esito", -1);
        commento = sourceIntent.getStringExtra("commento");
        isNew = sourceIntent.getBooleanExtra("isNew", false);
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleCameraActivityResult);

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCamera();
    }

    public void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                return;
            }

            Uri photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION); // Aggiungi questa riga
            cameraLauncher.launch(intent);
        }
    }
    private Bitmap getBitmapFromFile(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void handleCameraActivityResult(ActivityResult result) {
        Intent resultIntent = new Intent();
        if (result.getResultCode() == RESULT_OK) {
            File file = new File(PictureTravel.compressImage(currentPhotoPath, getBaseContext()));

            try {
                Bitmap bitmap = getBitmapFromFile(file);
//                Uri imageUri = Uri.fromFile(file);
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                if (bitmap != null) {
                    galleryAddPic();

                    String encodedImage = encodeImage(bitmap);
                    resultIntent.putExtra("image64", encodedImage);
                    resultIntent.putExtra("esito", esito);
                    resultIntent.putExtra("consegna", consegna);
                    resultIntent.putExtra("commento", commento);
                    resultIntent.putExtra("isNew", isNew);
                    resultIntent.putExtra("filename", file.getName());
                    setResult(Activity.RESULT_OK, resultIntent);
                } else {
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                }
            } catch (Exception ex) {
                setResult(Activity.RESULT_CANCELED, resultIntent);
            }
        } else {
            setResult(Activity.RESULT_CANCELED, resultIntent);
        }
        finish();
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] b = baos.toByteArray();

        return android.util.Base64.encodeToString(b, android.util.Base64.DEFAULT);
    }
    @SuppressLint("SimpleDateFormat")
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = new File(storageDir, imageFileName + ".jpg");
        if (image.createNewFile()) {
            FileOutputStream out = new FileOutputStream(image);
            out.close();
        }
        if (!image.exists()) {
            Log.e("CameraCaptured", "File creation failed: " + image.getAbsolutePath());
        }

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
