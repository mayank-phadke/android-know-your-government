package com.example.knowyourgovernment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class OfficialActivity extends AppCompatActivity {

    private static final String TAG = "OfficialActivity";
    Representative representative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_official);

        representative = (Representative) getIntent().getSerializableExtra("representative");
        String location = getIntent().getStringExtra("current_location");

        ((TextView) findViewById(R.id.current_location)).setText(location);

        handleMandatoryFields();
        handlePhoto();
        handleAddress();
        handlePhone();
        handleWebsite();
        handleEmail();
        handleChannels();
    }

    private void handleChannels() {
        if(!representative.hasFacebook()) {
            ImageView fb = findViewById(R.id.facebook_logo);
            fb.setVisibility(View.INVISIBLE);
        }

        if(!representative.hasTwitter()) {
            ImageView tw = findViewById(R.id.twitter_logo);
            tw.setVisibility(View.INVISIBLE);
        }

        if(!representative.hasYoutube()) {
            ImageView yt = findViewById(R.id.youtube_logo);
            yt.setVisibility(View.INVISIBLE);
        }
    }

    private
    void handleEmail() {
        TextView label, email;
        label = findViewById(R.id.email_label);
        email = findViewById(R.id.email_text_view);
        if(representative.hasEmail()) {
            email.setText(representative.getEmail());
        } else {
            label.setVisibility(View.INVISIBLE);
            email.setVisibility(View.INVISIBLE);
        }
    }

    private void handleWebsite() {
        TextView label, website;
        label = findViewById(R.id.website_label);
        website = findViewById(R.id.website_text_view);
        if(representative.hasWebsite()) {
            website.setText(representative.getWebsite());
        } else {
            label.setVisibility(View.INVISIBLE);
            website.setVisibility(View.INVISIBLE);
        }
    }

    private void handlePhone() {
        TextView label, phone;
        label = findViewById(R.id.phone_label);
        phone = findViewById(R.id.phone_text_view);
        if(representative.hasPhone()) {
            phone.setText(representative.getPhone());
        } else {
            label.setVisibility(View.INVISIBLE);
            phone.setVisibility(View.INVISIBLE);
        }
    }

    private void handleAddress() {
        TextView label, address;
        label = findViewById(R.id.address_label);
        address = findViewById(R.id.address_text_view);
        if(representative.hasAddress()) {
            address.setText(representative.getAddress());
        } else {
            label.setVisibility(View.INVISIBLE);
            address.setVisibility(View.INVISIBLE);
        }
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
        TextView party = findViewById(R.id.party_text_view);
        ConstraintLayout background_layout = findViewById(R.id.official_activity_layout);
        ImageView party_logo = findViewById(R.id.party_logo);

        office.setText(representative.getOffice());
        name.setText(representative.getName());

        String party_name = representative.getParty();
        party.setText(String.format("(%s)", party_name));

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

    public void facebookClicked(View v) {
        String username = representative.getFacebook();
        String FACEBOOK_URL = "https://www.facebook.com/" + username;
        String urlToUse;
        PackageManager packageManager = getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                urlToUse = "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                urlToUse = "fb://page/" + username;
            }
        } catch (PackageManager.NameNotFoundException e) {
            urlToUse = FACEBOOK_URL; //normal web url
        }
        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
        facebookIntent.setData(Uri.parse(urlToUse));
        startActivity(facebookIntent);
    }

    public void twitterClicked(View v) {
        Intent intent = null;
        String name = representative.getTwitter();
        try {
            // get the Twitter app if possible
            getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + name));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (Exception e) {
            // no Twitter app, revert to browser
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + name));
        }
        startActivity(intent);
    }

    public void youTubeClicked(View v) {
        String name = representative.getYoutube();
        Intent intent = null;
        try {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.google.android.youtube");
            intent.setData(Uri.parse("https://www.youtube.com/" + name));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/" + name)));
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

    public void addressClicked(View v) {
        if(!representative.hasAddress()) {
            return;
        }

        String address = representative.getAddress();
        Uri mapIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    public void phoneClicked(View v) {
        if(!representative.hasPhone()) {
            return;
        }

        String phone = representative.getPhone();
        Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
        phoneIntent.setData(Uri.parse("tel:" + phone));
        startActivity(phoneIntent);
    }

    public void websiteClicked(View v) {
        if(!representative.hasWebsite()) {
            return;
        }

        String website = representative.getWebsite();
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
        startActivity(webIntent);
    }

    public void emailClicked(View v) {
        if(!representative.hasEmail()) {
            return;
        }

        String email = representative.getEmail();
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {email});
        startActivity(emailIntent);
    }

    public void photoClicked(View v) {
        if(!representative.hasPhotoUrl()) {
            return;
        }

        TextView current_location = findViewById(R.id.current_location);

        Intent photoDetailIntent = new Intent(this, PhotoDetailActivity.class);
        photoDetailIntent.putExtra("representative", representative);
        photoDetailIntent.putExtra("current_location", current_location.getText().toString());
        startActivity(photoDetailIntent);
    }
}