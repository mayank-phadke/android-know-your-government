package com.example.knowyourgovernment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PhotoDetailActivity extends AppCompatActivity {

    private static final String TAG = "PhotoDetailActivity";
    Representative representative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        representative = (Representative) getIntent().getSerializableExtra("representative");
        String location = getIntent().getStringExtra("current_location");

        ((TextView) findViewById(R.id.current_location)).setText(location);

        handleMandatoryFields();
        handlePhoto();
    }

    private void handlePhoto() {
        if(representative.hasPhotoUrl()) {
            ImageView imageView = findViewById(R.id.official_photo);
            Picasso.get().setLoggingEnabled(true);
            Log.d(TAG, "handlePhoto: " + representative.getPhotoUrl());
            Picasso.get().load(representative.getPhotoUrl())
                    .error(R.drawable.brokenimage)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
        }
    }

    private void handleMandatoryFields() {
        TextView office = findViewById(R.id.office_text_view);
        TextView name = findViewById(R.id.name_text_view);
        ConstraintLayout background_layout = findViewById(R.id.photo_detail_layout);
        ImageView party_logo = findViewById(R.id.party_logo);

        office.setText(representative.getOffice());
        name.setText(representative.getName());

        String party_name = representative.getParty();

        switch (party_name) {
            case "Republican Party":
                background_layout.setBackgroundColor(Color.RED);
                party_logo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.rep_logo));
                break;
            case "Democratic Party":
                background_layout.setBackgroundColor(Color.BLUE);
                party_logo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dem_logo));
                break;
            default:
                background_layout.setBackgroundColor(Color.BLACK);
                party_logo.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void logoClicked(View v) {
        String party = representative.getParty();
        if(party.equals("Democratic Party")) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://democrats.org/"));
            startActivity(i);
            return;
        }

        if(party.equals("Republican Party")) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gop.com/"));
            startActivity(i);
        }
    }
}