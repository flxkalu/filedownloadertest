package com.felixkalu.filedownloadertest;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.folioreader.FolioReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {


    //FolioReader folioReader;

    Button btnShowProgress;
    private ProgressDialog pDialog;

    ImageView my_image;

    public static final int progress_bar_type = 0;

    private static String file_url = "https://res.cloudinary.com/flxkalu/raw/upload/v1534787203/1534787191410A_Clash_of_Kings_-_George_RR_Martin_eefg7o.epub";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnShowProgress = (Button)findViewById(R.id.button);
        my_image = (ImageView) findViewById(R.id.imageView);

        btnShowProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadFileFromURL().execute(file_url);
            }
        });

        Log.i("INTERNAL MEMORY", getAvailableInternalMemorySize());
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch(id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please Wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute () {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int lengthOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                String storageDir = getFilesDir().getAbsolutePath();

                Log.i("REAL PATH", storageDir);

                String fileName = "/downloadedfile.epub";
                File imageFile = new File(storageDir+fileName);
                OutputStream output = new FileOutputStream(imageFile);

                byte data[] = new byte[1024];
                long total = 0;

                while((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" +(int)((total*100)/lengthOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                Log.i("Error: ", e.getMessage());
            }
            return null;
        }
        //to update the progressbar...
        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        //to dismiss the progress bar when the download is complete and save somewhere
        @Override
        protected void onPostExecute(String file_url){
            dismissDialog(progress_bar_type);

            String imagePath = getFilesDir().getAbsolutePath() + "/downloadedfile.epub";
            if(imagePath.endsWith(".png") || imagePath.endsWith("./jpg")){
                my_image.setImageDrawable(Drawable.createFromPath(imagePath));
            } else if(imagePath.endsWith("epub")){
                Log.i("PATH", imagePath);
                FolioReader folioReader = FolioReader.getInstance(getApplicationContext());
                folioReader.openBook(imagePath);
            }
        }
    }

    //method for displaying the amount of internal memory left on the device on the Log.i.
    @TargetApi(18)
    public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return formatSize(availableBlocks * blockSize);
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }
}
