package CS12.finalproject.text_to_txt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = "ttt:Main";

    private static final int READ_REQUEST_CODE = 42;

    private static final int IMAGE_CAPTURE_REQUEST_CODE = 1;

    private boolean canWriteToPublicStorage = false;

    private RequestQueue requestQueue;

    private static List<String> resultText = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestQueue = Volley.newRequestQueue(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Upload button */
        final ImageButton openFile = findViewById(R.id.main_upload);
        openFile.setOnClickListener(v -> {
            Log.d(TAG, "Open file button clicked");
            startOpenFile();
        });

        /* Take Picture button */
        final ImageButton takePhoto = findViewById(R.id.main_picture);
        takePhoto.setOnClickListener(v -> {
            Log.d(TAG, "Take photo button clicked");
            startTakePhoto();
        });
        /* Confirm button */
        findViewById(R.id.main_confirm).setOnClickListener(v -> startProcessImage());
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent resultData) {
        if (resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "onActivityResult with code " + requestCode + " failed");
            if (requestCode == IMAGE_CAPTURE_REQUEST_CODE) {
                photoRequestActive = false;
            }
            return;
        }

        Uri currentPhotoURI;
        if (requestCode == READ_REQUEST_CODE) {
            currentPhotoURI = resultData.getData();
        } else if (requestCode == IMAGE_CAPTURE_REQUEST_CODE) {
            currentPhotoURI = Uri.fromFile(currentPhotoFile);
            photoRequestActive = false;
            if (canWriteToPublicStorage) {
                addPhotoToGallery(currentPhotoURI);
            }
        } else {
            Log.w(TAG, "Unhandled activityResult with code " + requestCode);
            return;
        }
        Log.d(TAG, "Photo selection produced URI " + currentPhotoURI);
        loadPhoto(currentPhotoURI);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void startOpenFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    /** Current file that we are using for our image request. */
    private boolean photoRequestActive = false;

    /** Whether a current photo request is being processed. */
    private File currentPhotoFile = null;

    /** Take a photo using the camera. */
    private void startTakePhoto() {
        if (photoRequestActive) {
            Log.w(TAG, "Overlapping photo requests");
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        currentPhotoFile = getSaveFilename();
        if (takePictureIntent.resolveActivity(getPackageManager()) == null
                || currentPhotoFile == null) {
            Toast.makeText(getApplicationContext(), "Problem taking photo",
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "Problem taking photo");
            return;
        }

        Uri photoURI = FileProvider.getUriForFile(this,
                "edu.illinois.cs.cs125.spring2019.finalproject.fileprovider", currentPhotoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        photoRequestActive = true;
        startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST_CODE);
    }
    File getSaveFilename() {
        String imageFileName = "text-to-txt_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
        File storageDir;
        if (canWriteToPublicStorage) {
            storageDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        } else {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.w(TAG, "Problem saving file: " + e);
            return null;
        }
    }

    void addPhotoToGallery(final Uri toAdd) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(toAdd);
        this.sendBroadcast(mediaScanIntent);
        Log.d(TAG, "Added photo to gallery: " + toAdd);
    }
    private Bitmap currentBitmap;

    private void loadPhoto(final Uri currentPhotoURI) {
        if (currentPhotoURI == null) {
            Toast.makeText(getApplicationContext(), "No image selected",
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "No image selected");
            return;
        }
        String uriScheme = currentPhotoURI.getScheme();

        byte[] imageData;
        try {
            assert uriScheme != null;
            switch (uriScheme) {
                case "file":
                    imageData = FileUtils.readFileToByteArray(new File(currentPhotoURI.getPath()));
                    break;
                case "content":
                    InputStream inputStream = getContentResolver().openInputStream(currentPhotoURI);
                    assert inputStream != null;
                    imageData = IOUtils.toByteArray(inputStream);
                    inputStream.close();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "Unknown scheme " + uriScheme,
                            Toast.LENGTH_LONG).show();
                    return;
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error processing file",
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "Error processing file: " + e);
            return;
        }

        final ImageView photoView = findViewById(R.id.main_foto);
        int targetWidth = photoView.getWidth();
        int targetHeight = photoView.getHeight();

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, decodeOptions);

        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;
        int scaleFactor = Math.min(actualWidth / targetWidth, actualHeight / targetHeight);

        BitmapFactory.Options modifyOptions = new BitmapFactory.Options();
        modifyOptions.inJustDecodeBounds = false;
        modifyOptions.inSampleSize = scaleFactor;

        updateCurrentBitmap(BitmapFactory.decodeByteArray(imageData,
                0, imageData.length, modifyOptions));
    }

    void updateCurrentBitmap(final Bitmap setCurrentBitmap) {
        currentBitmap = setCurrentBitmap;
        ImageView photoView = findViewById(R.id.main_foto);
        photoView.setImageBitmap(currentBitmap);
     //   enableOrDisableButtons(true);
    }
    private void startProcessImage() {
        if (currentBitmap == null) {
            Toast.makeText(getApplicationContext(), "No image selected",
                    Toast.LENGTH_LONG).show();
            Log.w(TAG, "No image selected");
            return;
        }

        new ProcessImageTask.ProcessImage(MainActivity.this, requestQueue)
                .execute(currentBitmap);
    }

    protected void finishProcessImage(final String jsonResult) {
        resultText.clear();
        System.out.println("Result: " + jsonResult);
        JsonParser parser = new JsonParser();
        JsonObject result = parser.parse(jsonResult).getAsJsonObject();
        JsonArray descriptions = result.get("description").getAsJsonObject()
                .get("captions").getAsJsonArray();
        for(int i = 0; i < descriptions.size(); i++) {
            String description = descriptions.get(i).getAsJsonObject().get("text").getAsString();
            resultText.add(description);
        }
        Log.d(TAG, "finished processing");
        Intent setupIntent = new Intent(this, TextActivity.class);
        startActivity(setupIntent);
        finish();
    }
    public static List<String> getResultText() {
        return resultText;
    }
}
