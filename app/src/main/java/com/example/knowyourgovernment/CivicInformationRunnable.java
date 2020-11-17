package com.example.knowyourgovernment;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CivicInformationRunnable implements Runnable {

    private static final String TAG = "CivicInformationRunnable";

    private static final String BASE_URL = "https://www.googleapis.com/civicinfo/v2/representatives";
    private static final String API_KEY = "YOUR_API_KEY";

    MainActivity mainActivity;
    String address;

    public CivicInformationRunnable(MainActivity mainActivity, String address) {
        this.mainActivity = mainActivity;
        this.address = address;
    }

    @Override
    public void run() {
        String DATA_URL = BASE_URL + "?key=" + API_KEY + "&address=" + address;
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.showBadAddressDialog();

                    }
                });
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "run: " + sb.toString());
            parseJson(sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
        }

    }

    private void parseJson(String jsonString) {
        try {
            JSONObject baseObject = new JSONObject(jsonString);
            JSONObject normalizedAddress = baseObject.getJSONObject("normalizedInput");
            final String user_address = normalizedAddress.getString("city") + ", " + normalizedAddress.getString("state") + " " + normalizedAddress.getString("zip");

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.setAddress(user_address.trim());
                }
            });

            JSONArray offices = baseObject.getJSONArray("offices");
            JSONArray officials = baseObject.getJSONArray("officials");

            final List<Representative> representatives = new ArrayList<>();

            for (int i = 0; i < offices.length(); i++) {
                JSONObject office = offices.getJSONObject(i);
                String office_name = office.getString("name");

                JSONArray official_indexes = office.getJSONArray("officialIndices");
                for (int j = 0; j < official_indexes.length(); j++) {
                    int index = official_indexes.getInt(j);

                    JSONObject official = officials.getJSONObject(index);
                    Representative rep = new Representative(
                            official.getString("name"),
                            office_name,
                            official.getString("party")
                    );

                    if(official.has("address")) {
                        JSONObject address = official.getJSONArray("address").getJSONObject(0);
                        rep.setAddress(buildAddressString(address));
                    }
                    rep.setPhone(official.getJSONArray("phones").getString(0));
                    if(official.has("urls")) {
                        rep.setWebsite(official.getJSONArray("urls").getString(0));
                    }
                    if(official.has("emails")) {
                        rep.setEmail(official.getJSONArray("emails").getString(0));
                    }
                    if(official.has("photoUrl")) {
                        rep.setPhotoUrl(official.getString("photoUrl"));
                    }
                    if(official.has("channels")) {
                        JSONArray channels = official.getJSONArray("channels");
                        for (int k = 0; k < channels.length(); k++) {
                            JSONObject channel = channels.getJSONObject(k);
                            switch (channel.getString("type")) {
                                case "Facebook":
                                    rep.setFacebook(channel.getString("id"));
                                    break;
                                case "Twitter":
                                    rep.setTwitter(channel.getString("id"));
                                    break;
                                case "YouTube":
                                    rep.setYoutube(channel.getString("id"));
                                    break;
                            }
                        }
                    }

                    representatives.add(rep);
                }
            }

            Log.d(TAG, "parseJson: " + representatives.get(0).getPhotoUrl());

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.updateRepresentativesList(representatives);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "parseJson: Error parsing JSON String");
        }
    }

    private String buildAddressString(JSONObject address) throws JSONException {
        StringBuilder sb = new StringBuilder();
        if(address.has("line1")) {
            sb.append(address.getString("line1"));
            sb.append(", ");
        }

        if(address.has("line2")) {
            sb.append(address.getString("line2"));
            sb.append(", ");
        }

        if(address.has("line3")) {
            sb.append(address.getString("line3"));
            sb.append(", ");
        }

        sb.append(address.getString("city"));
        sb.append(", ");
        sb.append(address.getString("state"));
        sb.append(" ");
        sb.append(address.getString("zip"));

        return sb.toString();
    }
}
