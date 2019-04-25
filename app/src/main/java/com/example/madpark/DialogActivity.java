package com.example.madpark;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class DialogActivity extends AppCompatActivity {
    TextView mytext;
    String lotName;
    String lotAvailability;
    String lotWebsite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);
        mytext = findViewById(R.id.dialog_text);
        Intent myIntent = getIntent();
        lotName = myIntent.getStringExtra("name");
        lotAvailability = myIntent.getStringExtra("availability");
        lotWebsite = myIntent.getStringExtra("website");

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        mytext.setText(lotName + "\n\n" + "Availability: " + lotAvailability.replaceAll("[^0-9]", ""));
    }

    /**
     * Callback method defined by the View
     *
     * @param v
     */
    public void finishDialog(View v) {
        // Your code here.
        DialogActivity.this.finish();
    }

    public void startRouteIntent(View view) {
        Uri myUri = Uri.parse(lotWebsite);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, myUri);
        startActivity(intent);
    }
}
