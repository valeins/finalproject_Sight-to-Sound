package CS12.finalproject.text_to_txt;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class TextActivity extends AppCompatActivity {

    private final static String TAG = "ttt:Main";

    private TextToSpeech toSpeak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        TextView textToSpeak = findViewById(R.id.text_text);
        List<String> resultText = MainActivity.getResultText();
        String showText = resultText.get(0);
        for (int i = 0; i < MainActivity.getResultText().size(); i++) {
            if (resultText.get(i).length() > showText.length()) {
                showText = resultText.get(i);
            }
        }
        textToSpeak.setText(showText);

        Button speakButton = findViewById(R.id.text_speak);

        toSpeak = new TextToSpeech(this, status -> {
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
        });
        /* Speak button */
        speakButton.setOnClickListener(v -> {
            String StringToSpeak = textToSpeak.getText().toString();
            toSpeak.speak(StringToSpeak, TextToSpeech.QUEUE_FLUSH, null);
        });
        /* Back button */
       findViewById(R.id.text_back).setOnClickListener(v -> {
           Intent setupIntent = new Intent(this, MainActivity.class);
           startActivity(setupIntent);
           finish();
       });
    }



    }
