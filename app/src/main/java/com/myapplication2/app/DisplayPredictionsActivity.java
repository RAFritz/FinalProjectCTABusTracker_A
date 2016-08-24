package com.myapplication2.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Robin on 5/1/14.
 */
public class DisplayPredictionsActivity extends ActionBarActivity {

    String routeNum;
    String stopID;
    String routeName;
    String weatherNum;
    String weatherValue;
    String weatherIcon;
    ArrayList<String> routeNums = new ArrayList<String>();
    ArrayList<String> predictions = new ArrayList<String>();
    ArrayList<String> destinations = new ArrayList<String>();
    ArrayList<String> timeStamps = new ArrayList<String>();
    ArrayList<String> predictionStamps = new ArrayList<String>();
    ArrayList<String> gridList = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    TextView tv;
    ImageView iv;
    TextView weath;
    final static String weatherURL = "http://api.openweathermap.org/data/2.5/weather?q=Chicago&mode=xml&APPID=821c0005394e00589dc1488cddab19e4";
    final static String predictURL = "http://www.ctabustracker.com/bustime/api/v1/getpredictions?key=gWVcU8apf8n54B6KHmqXhSB8n";
    final static String baseWeatherIconURL = "http://openweathermap.org/img/w/";
    GridView busList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displaypredictions);
        tv = (TextView) findViewById(R.id.txtStopName);
        iv = (ImageView) findViewById(R.id.imgWeather);
        weath = (TextView) findViewById(R.id.txtWeather);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, gridList);
        Intent intent = getIntent();
        routeNum = intent.getStringExtra("routeNum");
        stopID = intent.getStringExtra("stopID");
        routeName = intent.getStringExtra("routeName");
        busList = (GridView) findViewById(R.id.gridBus);
        loadPredictions();
        loadWeather();
    }

    public void loadPredictions() {
        new RequestPredictionTask().execute(predictURL);
    }

    class RequestPredictionTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                String predUrl = strings[0];
                predUrl += "&rt=" + routeNum + "&stpid=" + stopID;
                URL url = new URL(predUrl);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while((line = in.readLine()) != null) {
                    if(line.contains("<tmstmp>")) {
                        int firstPos = line.indexOf("<tmstmp>");
                        String temp = line.substring(firstPos);
                        temp = temp.replace("<tmstmp>", "");
                        int lastPos = temp.indexOf("</tmstmp>");
                        timeStamps.add(temp.substring(0, lastPos));
                    }
                    else if(line.contains("<rt>")) {
                        int firstPos = line.indexOf("<rt>");
                        String temp = line.substring(firstPos);
                        temp = temp.replace("<rt>", "");
                        int lastPos = temp.indexOf("</rt>");
                        routeNums.add(temp.substring(0, lastPos) + " To: ");
                    }
                    else if(line.contains("<des>")) {
                        int firstPos = line.indexOf("<des>");
                        String temp = line.substring(firstPos);
                        temp = temp.replace("<des>", "");
                        int lastPos = temp.indexOf("</des>");
                        destinations.add(temp.substring(0, lastPos));
                    }
                    else if(line.contains("<prdtm>")) {
                        int firstPos = line.indexOf("<prdtm>");
                        String temp = line.substring(firstPos);
                        temp = temp.replace("<prdtm>", "");
                        int lastPos = temp.indexOf("</prdtm>");
                        predictionStamps.add(temp.substring(0, lastPos));
                    }
                }
                in.close();
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            changeStampsToMins();
            addAllToGridList();
            busList.setAdapter(adapter);
            loadName();
        }
    }

    public void loadName() {
        tv.setText(routeName);
    }

    public void changeStampsToMins() {
        for(int i = 0; i < timeStamps.size(); i++) {
            String temp1 = timeStamps.get(i);
            String temp2 = predictionStamps.get(i);
            String[] splits1 = temp1.split(" ");
            String[] splits2 = temp2.split(" ");
            String[] timeSplit1 = splits1[1].split(":");
            String[] timeSplit2 = splits2[1].split(":");
            int hours1 = Integer.parseInt(timeSplit1[0]);
            int mins1 = Integer.parseInt(timeSplit1[1]);
            int hours2 = Integer.parseInt(timeSplit2[0]);
            int mins2 = Integer.parseInt(timeSplit2[1]);
            hours1 = hours1 * 60;
            int totalMins1 = hours1 + mins1;
            hours2 = hours2 * 60;
            int totalMins2 = hours2 + mins2;
            int finalMins = totalMins2 - totalMins1;
            predictions.add("" + finalMins + " minutes");
        }
    }

    public void addAllToGridList() {
        for(int i = 0; i < timeStamps.size(); i++) {
            gridList.add(routeNums.get(i));
            gridList.add(destinations.get(i));
            gridList.add(predictions.get(i));
        }
    }

    public void loadWeather() {
        new RequestWeatherTask().execute(weatherURL);
    }

    class RequestWeatherTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while((line = in.readLine()) != null) {
                    if(line.contains("<weather")) {
/*                        int firstPos = line.indexOf("number=");
                        String temp = line.substring(firstPos);
                        temp = temp.replace("number=\"", "");
                        int lastPos = firstPos + 12;
                        temp = temp.substring(0, lastPos);
                        temp = temp.replace("\"", "" );
                        weatherNum = temp;
                        firstPos = line.indexOf("value=");
                        temp = line.substring(firstPos);
                        temp = temp.replace("value=\"", "");
                        lastPos = temp.indexOf("icon") - 2;
                        temp = temp.substring(0, lastPos);
                        temp = temp.replace("\"", "");
                        weatherValue = temp;
                        firstPos = line.indexOf("icon=\"");
                        temp = line.substring(firstPos);
                        temp = temp.replace("icon=\"", "");
                        lastPos = temp.indexOf("/>") - 1;
                        temp = temp.substring(0, lastPos);
                        temp = temp.replace("\"", "");
                        weatherIcon = temp; */

                    }
                }
                in.close();
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadWeatherValue();
            loadWeatherIcon();
        }
    }

    public void loadWeatherValue() {
        weath.setText(weatherValue);
    }

    public void loadWeatherIcon() {
        new DownloadImageTask((ImageView) findViewById(R.id.imgWeather)).execute(baseWeatherIconURL);
    }

    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String url = strings[0];
            url += weatherIcon + ".png";
            Bitmap myIcon = null;
            try {
                URL url1 = new URL(url);
                InputStream in = url1.openStream();
                myIcon = BitmapFactory.decodeStream(in);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return myIcon;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            bmImage.setImageBitmap(bitmap);
        }
    }
}
