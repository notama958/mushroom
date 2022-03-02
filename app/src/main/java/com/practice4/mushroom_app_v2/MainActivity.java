package com.practice4.mushroom_app_v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity  {

    String wiki_url="https://en.wikipedia.org/api/rest_v1/page/summary";
    TextView inputName;
    ImageButton searchBtn;
    ImageButton galleryBtn;

    // text view
    TextView scienceName;
    TextView description;
    TextView extract;

    // SAVE img url to send to gallery screen
    String imgUrl="";

    // sensor manager
    SensorManager sensorManager;
    float maccel; // acceleration apart from gravity
    float maccelCurrent; // current acceleration including gravity
    float maccelLast; // last acceleration

    ArrayList<String> randoms= new ArrayList<String>(Arrays.asList("Oomycete","Octospora","Lachnella","Nectria","Abies lasiocarpa","Acarosporaceae","Oyster Mushroom","Cauliflower mushroom","Laccaria ochropurpurea ","Bacidia herbarum","bacteria","Badhamia gracilis","Laccaria"));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("DICTIONARY APP");
        inputName=findViewById(R.id.searchByName);
                // search btns
        searchBtn=findViewById(R.id.searchBtn);
        galleryBtn=findViewById(R.id.galleryBtn);

        // disable this galleryBtn initially
        galleryBtn.setEnabled(false);
        galleryBtn.setVisibility(View.GONE);



        scienceName=findViewById(R.id.sciName);
        description=findViewById(R.id.desc);
        extract=findViewById(R.id.ext);

        // while input image button is disabled
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                galleryBtn.setVisibility(View.GONE);
                galleryBtn.setEnabled(false);

            }
        });


        // open gallery activity button
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // go to gallery activity
                Intent galleryView= new Intent(MainActivity.this,GalleryActivity.class);
                if (   !imgUrl.equals(""))
                {
                    galleryView.putExtra("image",imgUrl);
                    startActivity(galleryView);
                }

            }
        });

        // search button
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputName.getText().toString().equals(""))
                {
                    Toast.makeText(MainActivity.this,"Please enter a name",Toast.LENGTH_SHORT).show();
                }
                else{
                    requestData(inputName.getText().toString());
                }

            }
        });


        // init sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // add listener to accelerator
        sensorManager.registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        maccel = 0.00f;
        maccelCurrent= SensorManager.GRAVITY_EARTH;
        maccelLast = SensorManager.GRAVITY_EARTH;



    }
    // get random object from random_url
    private void requestRandomData(){
        String randomObj=randoms.get(0+(int)(Math.random()*(randoms.size()-1-0)));
        Log.d("HERE",randomObj);
        wikiParser(wiki_url+"/"+randomObj);
    }
    // request data do wikiparser
    private void requestData(String location) {
        // get wiki info
        wikiParser(wiki_url+"/"+location);

    }


    // request with Volley and parse JSON
    private void wikiParser(String apiURL) {
        StringRequest stringRequest= new StringRequest(Request.Method.GET, apiURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // parse string
                        changeUI(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Toast.makeText(MainActivity.this,"Network Error",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        // use singletonVolley
        SingletonVolley.getInstance(this).addToRequestQueue(stringRequest);
    }
    // upadte mainactivity
    private void changeUI(String data) {
        try{

            JSONObject jsonObject= new JSONObject(data);
            String title= jsonObject.getString("title");
            String desc= jsonObject.getString("description");
            String ext= jsonObject.getString("extract");

            imgUrl=jsonObject.getJSONObject("originalimage").getString("source");
            description.setText("Description: "+desc);
            scienceName.setText("Name: "+title);
            extract.setText(ext);
            galleryBtn.setEnabled(true);
            galleryBtn.setVisibility(View.VISIBLE);

        }catch (JSONException e)
        {
            Toast.makeText(MainActivity.this,"cannot convert JSON",Toast.LENGTH_SHORT).show();

        }

    }
    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            maccelLast = maccelCurrent;
            maccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = maccelCurrent -maccelLast ;
            maccel = maccel * 0.9f + delta; // perform low-cut filter
            if(maccel >20)
            {
                Toast.makeText(MainActivity.this, "Device has shaken.", Toast.LENGTH_LONG).show();
                requestRandomData();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }


    };

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }
}