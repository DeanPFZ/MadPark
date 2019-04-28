package com.example.madpark.tools;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GarageAvailability {
    private ArrayList<String> rampAndSpots = new ArrayList<>();
    private ArrayList<String> mapOfUW = new ArrayList<>();
    private String TAG = "JSON";

    public GarageAvailability() {
        new getCityJSON().execute();
    }

    public ArrayList<String> getRampAndSpots() {
        return rampAndSpots;
    }

    public ArrayList<Uri> getMapOfUW() {
        ArrayList<Uri> finalMap = new ArrayList<>();
        for (int position = 0; position < rampAndSpots.size(); position++) {
            if (position >= 7) {
                finalMap.add(Uri.parse(mapOfUW.get(position - 7)));
            } else {
                String dest = rampAndSpots.get(position).replace(" ", "+").substring(0, rampAndSpots.get(position).indexOf(":"));
                finalMap.add(Uri.parse("https://www.google.com/maps/search/?api=1&query=" + dest));
            }
        }
        return finalMap;
    }

    private class getUWJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpHandler sh = new HttpHandler();
            String url = "https://gates.transportation.wisc.edu/occupancy/";
            String jsonStr = sh.makeServiceCall(url);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONArray names = jsonObject.names();
                    for (int i = 0; i < jsonObject.length() - 1; i++) { //last jsonObject is not relevant
                        JSONObject c = jsonObject.getJSONObject(names.getString(i));
                        JSONArray rampName = c.names();
                        for (int j = 0; j < rampName.length(); j++) {
                            String temp = rampName.getString(j).substring(rampName.getString(j).indexOf(" ") + 1) + ": " + c.getJSONObject(rampName.getString(j)).getString("vacancies");
                            if (temp.charAt(0) == ' ') {
                                temp = temp.substring(1);
                            }
                            rampAndSpots.add(temp);
                            mapOfUW.add(c.getJSONObject(rampName.getString(j)).getString("map").replace("\\\\", ""));
                        }
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "JSON Parse Err: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Couldn't get JSON");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    private class getCityJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpHandler sh = new HttpHandler();
            String url = "http://www.cityofmadison.com/parking-utility/data/ramp-availability.json";
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    JSONArray jsonArr = new JSONArray(jsonStr);
                    for (int i = 0; i < jsonArr.length(); i++) {
                        JSONObject c = jsonArr.getJSONObject(i);
                        String name = c.getString("name").toUpperCase();
                        String num = c.getString("vacant_stalls");
                        rampAndSpots.add(name + ": " + num);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "JSON Parse Err: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Couldn't get JSON");
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            new getUWJSON().execute();
            System.out.println("The data has been transferred completely");
        }

    }
}
