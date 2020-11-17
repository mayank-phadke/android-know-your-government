package com.example.knowyourgovernment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
    }

    public void linkClicked(View v) {
        String website = "https://developers.google.com/civic-information/";
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
        startActivity(webIntent);
    }
}