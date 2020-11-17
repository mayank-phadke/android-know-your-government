package com.example.knowyourgovernment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "MainActivity";

    private static int MY_LOCATION_REQUEST_CODE_ID = 111;

    private final List<Representative> representatives = new ArrayList<>();

    private LocationManager locationManager;
    private Criteria criteria;

    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private TextView current_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();

        if(!haveInternet()) {
            showNoNetworkDialog();
            setAddress("No Data For Location");
            return;
        }

        recyclerView = findViewById(R.id.recycler_view);
        adapter = new RecyclerViewAdapter(representatives, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        current_location = findViewById(R.id.current_location);

        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    MY_LOCATION_REQUEST_CODE_ID);
        } else {
            setLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.info_menu_item:
                Intent i = new Intent(this, InfoActivity.class);
                startActivity(i);
                return true;
            case R.id.search_menu_item:
                showSearchDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    private void setLocation() {

        String bestProvider = locationManager.getBestProvider(criteria, true);

        Location currentLocation = null;
        if (bestProvider != null) {
            currentLocation = locationManager.getLastKnownLocation(bestProvider);
        }
        if (currentLocation != null) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                callApiForAddress(addresses.get(0).getAddressLine(0));
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Current Location is Unavailable", Toast.LENGTH_SHORT).show();
                setAddress("Location Unavailable");
            }
        } else {
            Log.d(TAG, "setLocation: currentLocation is NULL");
            Toast.makeText(this, "Current Location is Unavailable", Toast.LENGTH_SHORT).show();
            setAddress("Location Unavailable");
        }
    }

    private void callApiForAddress(String address) {
        CivicInformationRunnable runnable = new CivicInformationRunnable(this, address);
        new Thread(runnable).start();
    }

    void setAddress(String address) {
        current_location.setText(address);
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(editText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                callApiForAddress(editText.getText().toString().trim());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });
        builder.setTitle("Enter City, State or a Zip Code");
        builder.create().show();
    }

    public void showBadAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error Parsing Address");
        builder.setMessage("Please enter a valid address");
        builder.create().show();
    }

    public void showNoNetworkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage("Data cannot be accessed / loaded without an internet connection");
        builder.create().show();
    }

    private boolean haveInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Toast.makeText(this, "Cannot access ConnectivityManager", Toast.LENGTH_SHORT).show();
            return false;
        }

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }

    void updateRepresentativesList(List<Representative> representatives) {
        this.representatives.clear();
        this.representatives.addAll(representatives);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        int pos = recyclerView.getChildLayoutPosition(view);
        Representative rep = representatives.get(pos);

        Intent i = new Intent(this, OfficialActivity.class);
        i.putExtra("representative", rep);
        i.putExtra("current_location", current_location.getText().toString());

        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_LOCATION_REQUEST_CODE_ID) {
            if(grantResults[0] == PERMISSION_GRANTED) {
                setLocation();
            } else {
                Toast.makeText(this, "Location Permission Not Granted. Exiting Application", Toast.LENGTH_LONG).show();
                finish();
            }

            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}