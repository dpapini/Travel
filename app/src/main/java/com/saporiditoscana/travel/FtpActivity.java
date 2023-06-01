package com.saporiditoscana.travel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FtpActivity extends AppCompatActivity  {

    ContentLoadingProgressBar pbDownload;
    TextView txDownload;
    private static final String NOME_FILE ="travel.apk";
    private LoadUrlData loadUrlData;

    public FtpActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);

        pbDownload = findViewById(R.id.pb_download);
        txDownload = findViewById(R.id.tx_download);

        InitToolBar("Aggiornamento");

        loadUrlData= new LoadUrlData();
        loadUrlData.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadUrlData.cancel(true);
    }

    private void InitToolBar(String subtitle) {
        Toolbar tb = findViewById(R.id.topAppBar);
        tb.setTitle("Travel - " + subtitle);
        TextView tv = findViewById(R.id.stato);
        tv.setVisibility(View.GONE);
        if(subtitle.equals("Home")){
            tb.setNavigationIcon(null);
            tv.setVisibility(View.VISIBLE);
        }
        setSupportActionBar(tb);
    }

    class LoadUrlData extends AsyncTask<Void, Void,String>
    {

        @Override
        protected void onPreExecute() {
            pbDownload.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            downloadFileFromServer();

            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            final PackageManager pm = getPackageManager();
            String fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + NOME_FILE;
            PackageInfo info = pm.getPackageArchiveInfo(fullPath, 0);

            if (info != null)
                txDownload.setText("Download completato versione: " + info.versionName);
            else txDownload.setText("Download completato file non valido");

            pbDownload.hide();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("fullPath", fullPath);
            setResult(Activity.RESULT_OK, resultIntent);

            super.onPostExecute(result);
            finish();
        }

    }

    @SuppressLint("SetTextI18n")
    public void downloadFileFromServer()
    {
        FTPClient ftp;
        final int connectionTimeout = 300000;
        try {
            //set the download URL, a url that points to a file on the internet
            //this is the file to be downloaded
            Terminale terminale = new Terminale(this);
//            URL url = new URL(terminale.getFtpServerUrl());


            ftp = new FTPClient();
            ftp.setConnectTimeout(connectionTimeout);
            try{
                ftp.connect(terminale.getFtpServerUrl(), 21);
                if (!FTPReply.isPositiveCompletion(ftp.getReplyCode()))
                {
                    txDownload.setText("Connessione fallita");

                    return;
                }else{
                    txDownload.setText("Connessione effettuata");
                }


                if (ftp.login("ftpsdt", "sapori")) {
                    ftp.changeWorkingDirectory("/Travel");

                    ftp.enterLocalPassiveMode();
                    ftp.setFileType(FTP.BINARY_FILE_TYPE);

                    transferFile(ftp);

                }else{
                    txDownload.setText("Login fallito");
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }finally {

                ftp.logout();
                ftp.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void transferFile(FTPClient ftp) throws Exception {
        long fileSize;
        fileSize = getFileSize(ftp);
        if(!(fileSize==0)){
            InputStream is = retrieveFileStream(ftp);
            downloadFile(is,  fileSize);
            is.close();
        }
        else
            if (!ftp.completePendingCommand()) {
                throw new Exception("Pending command failed: " + ftp.getReplyString());
            }
    }

    private InputStream retrieveFileStream(FTPClient ftp) throws Exception {
        InputStream is = ftp.retrieveFileStream(FtpActivity.NOME_FILE);
        int reply = ftp.getReplyCode();
        if (is == null
                || (!FTPReply.isPositivePreliminary(reply)
                && !FTPReply.isPositiveCompletion(reply))) {
            throw new Exception(ftp.getReplyString());
        }
        return is;
    }

    @SuppressLint("SetTextI18n")
    private byte[] downloadFile(InputStream is, long fileSize) throws Exception {
        OutputStream os = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/" + NOME_FILE);
        byte[] buffer = new byte[(int) fileSize];
        int readCount;
        long total=0;
        while( (readCount = is.read(buffer)) > 0) {
            total +=readCount;
            txDownload.setText("Downloading " + (int)((total*100)/fileSize) +"%");
            os.write(buffer, 0, readCount);
        }
        return buffer; // <-- Here is your file's contents !!!
    }

    private long getFileSize(FTPClient ftp) throws Exception {
        long fileSize = 0;
        FTPFile[] files = ftp.listFiles(FtpActivity.NOME_FILE);
        if (files.length == 1 && files[0].isFile()) {
            fileSize = files[0].getSize();
        }
        return fileSize;
    }
}
