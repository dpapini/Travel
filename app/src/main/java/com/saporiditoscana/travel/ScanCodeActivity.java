package com.saporiditoscana.travel;

import androidx.appcompat.app.AppCompatActivity;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.List;

public class ScanCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private static final String TAG = "ScanCodeActivity";
    public static final int PERMISSION_REQUEST_CAMERA = 1;

    ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScannerView = new ZXingScannerView(this);    // Programmatically initialize the scanner view
        mScannerView.setAutoFocus(true);
//        mScannerView.setFormats(listOf(BarcodeFormat.QR_CODE));
        setContentView(mScannerView);                // Set the scanner view as the content view

        // Request permission. This does it asynchronously so we have to wait for onRequestPermissionResult before trying to open the camera.
        if (!haveCameraPermission())
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);

    }

//    private List<BarcodeFormat> listOf(BarcodeFormat qrCode) {
//
//    }

    private boolean haveCameraPermission()
    {
        if (Build.VERSION.SDK_INT < 23)  return true;
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        // This is because the dialog was cancelled when we recreated the activity.
        if (permissions.length == 0 || grantResults.length == 0)
            return;

        switch (requestCode)
        {
            case PERMISSION_REQUEST_CAMERA:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startCamera();
                }
                else
                {
                    finish();
                }
            }
            break;
        }
    }

    public void startCamera()
    {
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    public void stopCamera()
    {
        try {
            mScannerView.stopCamera();
            onBackPressed();
        }catch (Exception e)
        {
//            Log.e(TAG, e.getMessage().toString());
        }
    }

    @Override
    public void handleResult(Result result) {
        MainActivity.qrCodeText = result.getText();
        Toast.makeText(this,result.getText(), Toast.LENGTH_LONG).show();
        onBackPressed();
    }

    @Override
    public  void  onPause(){
        super.onPause();
        stopCamera();
    }

    @Override
    public  void  onResume(){
        super.onResume();
        startCamera();
    }
}
