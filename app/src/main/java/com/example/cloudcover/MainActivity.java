package com.example.cloudcover;

import static android.location.LocationManager.NETWORK_PROVIDER;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV,temperatureTV,conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<WeatherRVModel>weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE=1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        homeRL=findViewById(R.id.idRLHome);
        loadingPB=findViewById(R.id.idPBLoading);
        cityNameTV=findViewById(R.id.idTVCityName);
        temperatureTV=findViewById(R.id.idTVTemperature);
        conditionTV=findViewById(R.id.idTVCondition);
        weatherRV=findViewById(R.id.idRVWeather);
        cityEdt=findViewById(R.id.idEdtCity);
        backIV=findViewById(R.id.idIvBack);
        iconIV=findViewById(R.id.idTVIcon);
        searchIV=findViewById(R.id.idIVSearch);
        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);

        }
        Location location =locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = "Patna";

        getWeatherInfo(cityName);
        searchIV.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this,"Please enter city name",Toast.LENGTH_SHORT).show();
                }else {
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permissions granted..",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"Please provide the permissions",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);

            for (Address adr : addresses){

                if (adr != null){
                    String city = adr.getLocality();
                    if (city!=null && !city.equals(" ")){
                        cityName=city;
                    }else {
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this,"User City Not Found..",Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName ){

        String url ="https://api.weatherapi.com/v1/forecast.json?key=76305f38f6b24efb8cf131407220403&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        //"https://api.weatherapi.com/v1/forecast.json?key=76305f38f6b24efb8cf131407220403&q="+ cityName +"&days=1&aqi=yes&alerts=yes"
        Log.i("getWeatherInfo",url);
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        Log.i("getWeatherInfo","after requestQueue");

        JsonObjectRequest jsonObjectRequest =new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("getWeatherInfo","after onResponse");
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try {
                    Log.i("getWeatherInfo","after try");
                    String temperature  =  response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"ºC");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("https:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    /*
                    if (isDay==1){
                        Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwallpapercave.com%2Fcloud-desktop-background&psig=AOvVaw0n6SY2TFDNPm7eAcIB7kzs&ust=1646546934747000&source=images&cd=vfe&ved=0CAsQjRxqFwoTCKDMqL2nrvYCFQAAAAAdAAAAABAJ").into(backIV);
                    }else {
                        Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.rgbstock.com%2Fphoto%2FmeSbNaU%2FNight%2BSky%2BBackground&psig=AOvVaw1KQYlFqArChxQjmx7ZG6_P&ust=1646547060153000&source=images&cd=vfe&ved=0CAsQjRxqFwoTCNCfzvmnrvYCFQAAAAAdAAAAABAV").into(backIV);
                    }
                    */
                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forcastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray =forcastO.getJSONArray("hour");
                    Log.i(" ","after array");
                    for (int i =0; i<hourArray.length();i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        /*
                        TextView timeView = findViewById(R.id.idTVTime);
                        timeView.setText(time);
                        TextView temperView = (TextView)findViewById(R.id.idTVTemperature);
                        temperView.setText(temper);
                        TextView windView = (TextView)findViewById(R.id.idWindSpeed);
                        windView.setText(wind);

                         */
                    }
                    weatherRVAdapter.notifyDataSetChanged();
                    Log.i(" ","after notify");



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please enter valid city name..",Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

}