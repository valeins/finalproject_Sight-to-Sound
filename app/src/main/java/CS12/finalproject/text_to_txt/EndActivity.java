package CS12.finalproject.text_to_txt;

import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;

public class EndActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        /* Back button */
        findViewById(R.id.end_back).setOnClickListener(v -> {
            Intent setupIntent = new Intent(this, TextActivity.class);
            startActivity(setupIntent);
            finish();
        });

        /* Restart button */
        findViewById(R.id.end_restart).setOnClickListener(v -> {
            Intent setupIntent = new Intent(this, MainActivity.class);
            startActivity(setupIntent);
            finish();
        });

        findViewById(R.id.end_save).setOnClickListener(v -> {
            TextView saved = findViewById(R.id.end_saved);
            saved.setText("Your file was successfully saved!");
        });
    }


}
