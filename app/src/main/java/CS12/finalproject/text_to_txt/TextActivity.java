package CS12.finalproject.text_to_txt;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonParser;

import org.w3c.dom.Text;

import java.time.chrono.MinguoChronology;
import java.util.Locale;

import CS12.finalproject.text_to_txt.MainActivity;

public class TextActivity extends AppCompatActivity {

    private final static String TAG = "ttt:Main";

    private TextToSpeech toSpeak;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        TextView textToSpeak = findViewById(R.id.text_text);
        String showText = new String();
        if (MainActivity.getResultText().size() == 1) {
            textToSpeak.setText(MainActivity.getResultText().get(0) + ".");
        } else {
            for (int i = 0; i < MainActivity.getResultText().size(); i++) {
                showText = showText + MainActivity.getResultText().get(i) + "; ";
            }
            textToSpeak.setText(showText);
        }

        Button speakButton = findViewById(R.id.text_speak);

        toSpeak = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = toSpeak.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.d(TAG, "Language not supported");
                    } else {
                        speakButton.setEnabled(true);
                    }
                } else {
                    Log.d(TAG, "Initialization failed");
                }
            }
        });
        /* Speak button */
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String StringToSpeak = textToSpeak.getText().toString();
                toSpeak.speak(StringToSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
  /*      @Override
        protected void onDestroy() {
            if (toSpeak != null) {
                toSpeak.stop();
                toSpeak.shutdown();
            }
            super.onDestroy();
        }*/
        /* Back button */
       findViewById(R.id.text_back).setOnClickListener(v -> {
           Intent setupIntent = new Intent(this, MainActivity.class);
           startActivity(setupIntent);
           finish();
       });

     /*  findViewById(R.id.text_convert).setOnClickListener(v -> {
           Intent setupIntent = new Intent(this, EndActivity.class);
           startActivity(setupIntent);
           finish();
       });*/
    }



    }
