package com.example.proye;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SensorManager SM;
    private Sensor giro;
    private SensorEventListener giroListener;
    final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 0;
    private String longitud;
    private String latitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //permisos
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{ Manifest.permission.SEND_SMS},MY_PERMISSIONS_REQUEST_CALL_PHONE);
        }
        // dar la ubicacion actual
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //seccion del giroscopio
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);
        giro = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        giroListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.values[2] > 2f) {
                    Toast.makeText(MapsActivity.this, "Mensaje enviado "+longitud+" "+latitud ,Toast.LENGTH_LONG).show();
                    mensaje();
                }else{

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }
    @Override
    protected void onResume() {
        super.onResume();
        SM.registerListener(giroListener, giro, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SM.unregisterListener(giroListener);
    }
    public void localizar(){
        LocationManager locationManager=
                (LocationManager)MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                LatLng posicion = new LatLng(location.getLatitude(),  location.getLongitude());
                latitud=location.getLatitude()+"";
                longitud=location.getLongitude()+"";
                mMap.addMarker(new MarkerOptions().position(posicion).title("Localizacion"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        int permissionCheck = ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public void mensaje(){
        try{
            String enviado = "MENSAJE ENVIADO";
            String deliverado = "MENSAJE DELIVERADO";
            PendingIntent sentPI = PendingIntent.getBroadcast(
                    this, 0, new Intent(enviado), 0);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(
                    this, 0, new Intent(deliverado), 0);
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("4451031920", null, "Estas son mis coordenadas bro "+longitud+" "+latitud+" ,pasa por mi", sentPI, deliveredPI);
        }catch (Exception e){

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
       /* LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        localizar();
    }
}
