package CS12.finalproject.text_to_txt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonParser;
import CS12.finalproject.text_to_txt.MainActivity;

public class TextActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        TextView textToConvert = findViewById(R.id.text_text);
        textToConvert.setText(MainActivity.getResultText().get(0));

        /* Convert button */
        //findViewById(R.id.text_convert).setOnClickListener();

        /* Back button */
       findViewById(R.id.text_back).setOnClickListener(v -> {
           Intent setupIntent = new Intent(this, MainActivity.class);
           startActivity(setupIntent);
           finish();
       });

       findViewById(R.id.text_convert).setOnClickListener(v -> {
           Intent setupIntent = new Intent(this, EndActivity.class);
           startActivity(setupIntent);
           finish();
       });
    }



    }
