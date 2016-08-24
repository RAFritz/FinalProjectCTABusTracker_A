package com.myapplication2.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

//    String[] test = new String[] {"1", "2", "3"};
    ArrayList<String> routes = new ArrayList<String>();
    Spinner spRoute;
    Spinner spDirection;
    Spinner spStop;
    ArrayAdapter<String> adapter1;
    ArrayAdapter<String> adapter2;
    ArrayAdapter<String> adapter3;
    TextView tv;
    String[] directions = new String[2];
    ArrayList<String> stopIDS = new ArrayList<String>();
    ArrayList<String> stopNames = new ArrayList<String>();
    String rtNum;
    String rtDir;
    final static String routeURL = "http://www.ctabustracker.com/bustime/api/v1/getroutes?key=gWVcU8apf8n54B6KHmqXhSB8n";
    final static String baseDirURL = "http://www.ctabustracker.com/bustime/api/v1/getdirections?key=gWVcU8apf8n54B6KHmqXhSB8n&rt=";
    final static String baseStopURL = "http://www.ctabustracker.com/bustime/api/v1/getstops?key=gWVcU8apf8n54B6KHmqXhSB8n";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.txtHeader);
        adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, routes);
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, directions);
        adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stopNames);
        spRoute = (Spinner) findViewById(R.id.spnRoute);
        spRoute.setOnItemSelectedListener(new spnRouteListener());
        spDirection = (Spinner) findViewById(R.id.spnDirection);
        spDirection.setOnItemSelectedListener(new spnDirectionListener());
        spStop = (Spinner) findViewById(R.id.spnStop);
        loadRoutes();
//        routesArray = routes.toArray(new String[routes.size()]);
//        sp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, routesArray));
//        routesArray = routes.toArray(new String[routes.size()]);
//        sp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, routesArray));
//        sp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, test));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadRoutes() {
        new RequestRouteTask().execute(routeURL);
    }

    class RequestRouteTask extends AsyncTask<String, String[], Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL routesURL = new URL(strings[0]);
                BufferedReader in = new BufferedReader(new InputStreamReader(routesURL.openStream()));
                String line;
                while((line = in.readLine()) != null) {
                    if(line.contains("<rt>")) {
                        int firstPos = line.indexOf("<rt>");
                        String tempNum = line.substring(firstPos);
                        tempNum = tempNum.replace("<rt>", "");
                        int lastPos = tempNum.indexOf("</rt>");
                        tempNum = tempNum.substring(0, lastPos);
                        line = in.readLine();
                        firstPos = line.indexOf("<rtnm>");
                        String tempName = line.substring(firstPos);
                        tempName = tempName.replace("<rtnm>", "");
                        lastPos = tempName.indexOf("</rtnm>");
                        tempName = tempName.substring(0, lastPos);
                        routes.add(tempNum + " " + tempName);
//                        routes.add(tempNum.substring(0, lastPos));

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
//            tv.setText(routes.get(1));
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spRoute.setAdapter(adapter1);
            adapter1.notifyDataSetChanged();
        }
    }

    class spnRouteListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            loadDirections();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class spnDirectionListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            loadStops();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public void loadDirections() {
        new RequestDirectionTask().execute(baseDirURL);
    }

    class RequestDirectionTask extends AsyncTask<String, String[], Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                String dirURL = strings[0];
                String[] splits = spRoute.getSelectedItem().toString().split(" ");
                dirURL += splits[0];
//                dirURL += spRoute.getSelectedItem().toString();
                URL url = new URL(dirURL);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                int count = 0;
                while((line = in.readLine()) != null) {
                    if(line.contains("<dir>")) {
                        int firstPos = line.indexOf("<dir>");
                        String temp = line.substring(firstPos);
                        temp = temp.replace("<dir>", "");
                        int lastPos = line.indexOf("</dir>");
                        directions[count] = temp.substring(0, lastPos-8);
                        count++;
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
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDirection.setAdapter(adapter2);
            adapter2.notifyDataSetChanged();
        }
    }

    public void loadStops() {
        new RequestStopTask().execute(baseStopURL);
    }

    class RequestStopTask extends AsyncTask<String, String[], Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            stopIDS.clear();
            stopNames.clear();
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                String stopURL = strings[0];
                String[] splits = spRoute.getSelectedItem().toString().split(" ");
                rtNum = splits[0];
//                rtNum = spRoute.getSelectedItem().toString();
                rtDir = spDirection.getSelectedItem().toString();
                stopURL += "&rt=" + rtNum + "&dir=" + rtDir;
                URL url = new URL(stopURL);
//                URL url = new URL("http://www.ctabustracker.com/bustime/api/v1/getstops?key=gWVcU8apf8n54B6KHmqXhSB8n&rt=1&dir=NorthBound");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while((line = in.readLine()) != null) {
                    if(line.contains("<stpid>")) {
                        int firstPos = line.indexOf("<stpid>");
                        String temp = line.substring(firstPos);
                        temp = temp.replace("<stpid>", "");
                        int lastPos = line.indexOf("</stpid>");
                        stopIDS.add(temp.substring(0, lastPos - 10));
                        line = in.readLine();
                        firstPos = line.indexOf("<stpnm>");
                        temp = line.substring(firstPos);
                        temp = temp.replace("<stpnm>", "");
                        temp = temp.replace("&amp;", "&");
                        lastPos = line.indexOf("</stpnm>");
                        temp = temp.substring(0, lastPos - 10);
                        temp = temp.replace("</st", "");
//                        stopNames.add(temp.substring(0, lastPos - 10));
                        stopNames.add(temp);
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
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spStop.setAdapter(adapter3);
            adapter3.notifyDataSetChanged();
        }
    }

    public void sendMessage(View view) {
        // need route number and stop id
        Intent intent = new Intent(this, DisplayPredictionsActivity.class);
        String[] splits = spRoute.getSelectedItem().toString().split(" ");
        String routeNum = splits[0];
//        String routeNum = spRoute.getSelectedItem().toString();
        int getStop = spStop.getSelectedItemPosition();
        String stopID = stopIDS.get(getStop);
        String routeName = spStop.getSelectedItem().toString();
        intent.putExtra("stopID", stopID);
        intent.putExtra("routeNum", routeNum);
        intent.putExtra("routeName", routeName);
        startActivity(intent);
    }
}
