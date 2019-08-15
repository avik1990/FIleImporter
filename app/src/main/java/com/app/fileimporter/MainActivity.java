package com.app.fileimporter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.fileimporter.utils.CheckForSDCard;
import com.app.fileimporter.utils.FileUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_import;
    Context context;
    File apkStorage = null;
    File outputFile = null;
    public final String downloadDirectory = "FileImporter";
    String name;
    SimpleDateFormat dateFormat;
    Calendar cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        btn_import = findViewById(R.id.btn_import);
        btn_import.setOnClickListener(this);
        cal = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");


        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();


    }

    @Override
    public void onClick(View v) {
        if (v == btn_import) {
            Intent intent = new Intent()
                    .setType("file/*")
                    .setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 7);
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 7:
                if (resultCode == RESULT_OK) {

                    try {
                        final Uri uri = data.getData();
                        //String PathHolder = data.getData().getPath();
                        Toast.makeText(context, uri.toString(), Toast.LENGTH_LONG).show();
                        File originalFile = new File(FileUtils.getRealPath(this,uri));
                        //File file = new File(getRealPathFromURI(uri));
                        //File file = FileUtils.getFile(this, uri);
                        Log.d("FilePATHS",uri.toString());
                        /*name = dateFormat.format(cal.getTime());

                        if (new CheckForSDCard().isSDCardPresent()) {
                            apkStorage = new File(Environment.getExternalStorageDirectory() + "/" + downloadDirectory);
                        } else {
                            Toast.makeText(context, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();
                        }

                        if (!apkStorage.exists()) {
                            apkStorage.mkdir();
                            Log.e("TAG", "Directory Created.");
                        }

                        outputFile = new File(apkStorage, name + ".xls");//Create Output file in Main File


                        //Create New File if not present
                        if (!outputFile.exists()) {
                            try {
                                outputFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.e("TAG", "File Created");
                        }

                        FileOutputStream fos = new FileOutputStream(outputFile);
                        fos.close();*/
                        openFile(context, originalFile,uri);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    public void openFile(Context context, File url,Uri uri) throws IOException {
        // Create URI
        // File file = url;
        //   Uri uri = Uri.fromFile(file);
        //Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", url);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
