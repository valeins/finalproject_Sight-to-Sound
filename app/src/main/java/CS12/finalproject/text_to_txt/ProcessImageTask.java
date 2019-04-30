package CS12.finalproject.text_to_txt;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;

/*import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.utils.URIBuilder;
//import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils; */

class ProcessImageTask {
    public static class ProcessImage extends AsyncTask<Bitmap, Integer, Integer> {

        private static final String TAG = "ttt:ProcessImageTask";

        /** Url for the MS cognitive services API. */
        private static final String MS_CV_API_URL =
                "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/describe";

        /** Maximum number of Descriptions */
        private static final String MAX_CANDIDATES = "3";

        /** Subscription key. */
        private static final String SUBSCRIPTION_KEY = BuildConfig.API_KEY;

        /** Default quality level for bitmap compression. */
        private static final int DEFAULT_COMPRESSION_QUALITY_LEVEL = 100;

        /** Reference to the calling activity so that we can return results. */
        private WeakReference<MainActivity> activityReference;

        /** Request queue to use for our API call. */
        private RequestQueue requestQueue;

        /**
         * Create a new talk to upload data and return the API results.
         *
         * We pass in a reference to the app so that this task can be static.
         * Otherwise we get warnings about leaking the context.
         *
         * @param context calling activity context
         * @param setRequestQueue Volley request queue to use for the API request
         */
        ProcessImage(final MainActivity context, final RequestQueue setRequestQueue) {
            activityReference = new WeakReference<>(context);
            requestQueue = setRequestQueue;
        }

        /**
         * Before we start draw the waiting indicator.
         */
        @Override
        protected void onPreExecute() {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            ProgressBar progressBar = activity.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        /**
         * Convert an image to a byte array, upload to the Microsoft Cognitive Services API,
         * and return a result.
         *
         * @param currentBitmap the bitmap to process
         * @return unused result
         */
        protected Integer doInBackground(final Bitmap... currentBitmap) {
            /*
             * Convert the image from a Bitmap to a byte array for upload.
             */
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            currentBitmap[0].compress(Bitmap.CompressFormat.PNG,
                    DEFAULT_COMPRESSION_QUALITY_LEVEL, stream);

            // Prepare our API request
            String requestURL = Uri.parse(MS_CV_API_URL)
                    .buildUpon()
                    .appendQueryParameter("maxCandidates", MAX_CANDIDATES)
                    .build()
                    .toString();
            Log.d(TAG, "Using URL: " + requestURL);

            /*
             * Make the API request.
             */
            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST, requestURL,
                    this::handleApiResponse, this::handleApiError) {
                @Override
                public Map<String, String> getHeaders() {
                    // Set up headers properly
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/octet-stream");
                    headers.put("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY);
                    return headers;
                }
                @Override
                public String getBodyContentType() {
                    // Set the body content type properly for a binary upload
                    return "application/octet-stream";
                }
                @Override
                public byte[] getBody() {
                    return stream.toByteArray();
                }
            };
            requestQueue.add(stringRequest);

            /* doInBackground can't return void, otherwise we would. */
            return 0;
        }

        /**
         * Processes a response from the image recognition API.
         * @param response The JSON text of the response.
         */
        void handleApiResponse(final String response) {
            // On success, clear the progress bar and call finishProcessImage
            Log.d(TAG, "Response: " + response);
            MainActivity activity = activityReference.get();
          /*  if (activity == null || activity.isFinishing()) {
                return;
            }*/
            ProgressBar progressBar = activity.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
            activity.finishProcessImage(response);
        }

        /**
         * Handles an error encountered when trying to use the image recognition API.
         * @param error The error that caused the request to fail.
         */
        void handleApiError(final VolleyError error) {
            // On failure just clear the progress bar
            Log.w(TAG, "Error: " + error.toString());
            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse != null
                    && networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Log.w(TAG, "Unauthorized request. "
                        + "Make sure you added your API_KEY to app/secrets.properties");
            }
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            ProgressBar progressBar = activity.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}

