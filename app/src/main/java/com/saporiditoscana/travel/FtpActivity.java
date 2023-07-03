package com.saporiditoscana.travel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;

import com.saporiditoscana.travel.Orm.Terminale;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.InputStream;
import java.io.OutputStream;

public class FtpActivity extends AppCompatActivity {

    ContentLoadingProgressBar pbDownload;
    TextView txDownload;
    private static final String NOME_FILE = "travel.apk";
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);

        pbDownload = findViewById(R.id.pb_download);
        txDownload = findViewById(R.id.tx_download);

        InitToolBar("Aggiornamento");

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    pbDownload.hide();
                    String fullPath = (String) msg.obj;
                    final PackageManager pm = getPackageManager();
                    PackageInfo info = pm.getPackageArchiveInfo(fullPath, 0);
                    if (info != null) {
                        txDownload.setText("Download completato versione: " + info.versionName);
                    } else {
                        txDownload.setText("Download completato file non valido");
                    }
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("fullPath", fullPath);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else if (msg.what == 2) {
                    String errorMessage = (String) msg.obj;
                    txDownload.setText(errorMessage);
                    pbDownload.hide();
                }
            }
        };

        LoadUrlData loadUrlData = new LoadUrlData();
        Thread thread = new Thread(loadUrlData);
        thread.start();
    }

    private void InitToolBar(String subtitle) {
        Toolbar tb = findViewById(R.id.topAppBar);
        tb.setTitle("Travel - " + subtitle);
        TextView tv = findViewById(R.id.stato);
        tv.setVisibility(View.GONE);
        if (subtitle.equals("Home")) {
            tb.setNavigationIcon(null);
            tv.setVisibility(View.VISIBLE);
        }
        setSupportActionBar(tb);
    }

    private class LoadUrlData implements Runnable {

        @Override
        public void run() {
            downloadFileFromServer();
        }
    }

    @SuppressLint("SetTextI18n")
    private void downloadFileFromServer() {
        FTPClient ftp;
        final int connectionTimeout = 300000;
        try {
            Terminale terminale = new Terminale(this);

            ftp = new FTPClient();
            ftp.setConnectTimeout(connectionTimeout);
            try {
                ftp.connect(terminale.getFtpServerUrl(), 21);
                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                    sendMessageToHandler(2, "Connessione fallita");
                    return;
                } else {
                    sendMessageToHandler(2, "Connessione effettuata");
                }

                if (ftp.login("ftpsdt", "sapori")) {
                    ftp.changeWorkingDirectory("/Travel");

                    ftp.enterLocalPassiveMode();
                    ftp.setFileType(FTP.BINARY_FILE_TYPE);

                    transferFile(ftp);

                } else {
                    sendMessageToHandler(2, "Login fallito");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendMessageToHandler(2, e.getMessage());
            } finally {
                ftp.logout();
                ftp.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendMessageToHandler(2, e.getMessage());
        }
    }

    private void transferFile(FTPClient ftp) throws Exception {
        long fileSize;
        fileSize = getFileSize(ftp);
        if (!(fileSize == 0)) {
            InputStream is = retrieveFileStream(ftp);
            downloadFile(is, fileSize);
            is.close();
        } else if (!ftp.completePendingCommand()) {
            throw new Exception("Pending command failed: " + ftp.getReplyString());
        }
    }

    private InputStream retrieveFileStream(FTPClient ftp) throws Exception {
        InputStream is = ftp.retrieveFileStream(FtpActivity.NOME_FILE);
        int reply = ftp.getReplyCode();
        if (is == null || (!FTPReply.isPositivePreliminary(reply) && !FTPReply.isPositiveCompletion(reply))) {
            throw new Exception(ftp.getReplyString());
        }
        return is;
    }

    @SuppressLint("SetTextI18n")/*
    private void downloadFile(InputStream is, long fileSize) throws Exception {
        OutputStream os = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/" + NOME_FILE);
        byte[] buffer = new byte[(int) fileSize];
        int readCount;
        long total = 0;
        while ((readCount = is.read(buffer)) > 0) {
            total += readCount;
            final int progress = (int) ((total * 100) / fileSize);
            runOnUiThread(() -> txDownload.setText("Downloading " + progress + "%"));
            os.write(buffer, 0, readCount);
        }
        os.close();
        sendMessageToHandler(1, os.toString());
    }*/

    private void downloadFile(InputStream is, long fileSize) throws Exception {
        ContentResolver contentResolver = getContentResolver();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, NOME_FILE);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        }
        if (uri != null) {
            try {
                OutputStream os = contentResolver.openOutputStream(uri);
                if (os != null) {
                    byte[] buffer = new byte[1024];
                    int readCount;
                    long total = 0;
                    while ((readCount = is.read(buffer)) != -1) {
                        total += readCount;
                        final int progress = (int) ((total * 100) / fileSize);
                        runOnUiThread(() -> txDownload.setText("Downloading " + progress + "%"));
                        os.write(buffer, 0, readCount);
                    }
                    os.close();
                    sendMessageToHandler(1, uri.toString());
                } else {
                    // Errore nell'apertura dell'OutputStream
                    sendMessageToHandler(2, "Errore nell'apertura dell'OutputStream");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendMessageToHandler(2, e.getMessage());
            }
        } else {
            // Errore nell'ottenimento dell'URI per il file
            sendMessageToHandler(2, "Errore nell'ottenimento dell'URI per il file");
        }
    }

    private long getFileSize(FTPClient ftp) throws Exception {
        long fileSize = 0;
        FTPFile[] files = ftp.listFiles(FtpActivity.NOME_FILE);
        if (files.length == 1 && files[0].isFile()) {
            fileSize = files[0].getSize();
        }
        return fileSize;
    }

    private void sendMessageToHandler(int what, Object obj) {
        Message message = handler.obtainMessage();
        message.what = what;
        message.obj = obj;
        handler.sendMessage(message);
    }
}
