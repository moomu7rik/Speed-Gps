package com.example.ankan.gps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Helper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements MapEventsReceiver {
    private Button b;

    private LocationManager locationManager;
    private LocationListener listener;
    MapView map;
    private IMapController mapController;
    private Polyline polyline;
    private Polyline colorline;
    public int t=0;
    public float distance =0;
    public float tdistance =0;
    public double speed =0;
    Location s = new Location("");
    Location e = new Location("");
    GeoPoint start;
    GeoPoint end;
    String loc;
    String timeStamp ="";
    String file_name ="";
    public String gpsstorage="";
    private static final String IMAGE_DIRECTORY_NAME = "GPS_STORAGE";
    FileOutputStream fOut = null;
    OutputStreamWriter myOutWriter =null;



    ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
    ArrayList<GeoPoint> colorpts = new ArrayList<GeoPoint>();

  //  RoadManager roadManager = new OSRMRoadManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(16.7);
        final GeoPoint startPoint = new GeoPoint(23.5748702,87.2937521, 2.2944);
       mapController.setCenter(startPoint);

      Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
      startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
       map.getOverlays().add(startMarker);
        startMarker.setTitle("Durgapur");
       MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
       map.getOverlays().add(0, mapEventsOverlay);
        timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        file_name = "trailFile_" + timeStamp + ".txt";
        File mediaStorageDir = new File(
                Environment
                        .getExternalStorageDirectory().getAbsoluteFile()+"/"+IMAGE_DIRECTORY_NAME
                        );

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");

            }
        }
        gpsstorage = mediaStorageDir.getPath() + File.separator
                + file_name;
        final File file = new File(gpsstorage);




        b = (Button) findViewById(R.id.button);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                polyline = new Polyline();
                colorline = new Polyline();
                GeoPoint Point = new GeoPoint(location.getLatitude(),location.getLongitude());
                waypoints.add(Point);
                colorpts.add(Point);
                if(waypoints.size()==1)
                {
                     start = new GeoPoint(location.getLatitude(),location.getLongitude());
                    s.setLatitude(location.getLatitude());
                    s.setLongitude(location.getLongitude());

                }
                else if(waypoints.size()>=2 ) {

                    polyline.setColor(0x12121212);
                    polyline.setWidth(3);
                    polyline.setVisible(true);


                    polyline.setPoints(waypoints);
                    map.getOverlays().add(polyline);


                    end = new GeoPoint(location.getLatitude(),location.getLongitude());
                    e.setLatitude(location.getLatitude());
                    e.setLongitude(location.getLongitude());
                    distance = s.distanceTo(e);

                     tdistance = tdistance + distance;
                    s.setLatitude(e.getLatitude());
                    s.setLongitude(e.getLongitude());

                    if (tdistance < 100) {

                        t = t + 3;
                    }
                    else if (tdistance >= 100) {
                        speed = (tdistance / t)*(18.0/5.0);
                        if (speed < 20) {
                            colorline.setColor(Color.RED);
                            colorline.setWidth(7);
                            colorline.setVisible(true);


                            colorline.setPoints(colorpts);
                            map.getOverlays().add(colorline);
                        } else if (speed >= 20 && speed < 35) {
                            colorline.setColor(Color.YELLOW);

                            colorline.setWidth(7);
                            colorline.setVisible(true);


                            colorline.setPoints(colorpts);
                            map.getOverlays().add(colorline);
                        } else if (speed >= 35 && speed < 55) {
                            colorline.setColor(Color.BLUE);
                            colorline.setWidth(7);
                            colorline.setVisible(true);


                            colorline.setPoints(colorpts);
                            map.getOverlays().add(colorline);
                        } else {
                            colorline.setColor(Color.GREEN);
                            colorline.setWidth(7);
                            colorline.setVisible(true);


                            colorline.setPoints(colorpts);
                            map.getOverlays().add(colorline);
                        }
                        tdistance = 0;

                        colorpts.clear();
                        start=end;
                        colorpts.add(start);
                        t = 0;


                    }
                    if (location != null) {

                        double lat = location.getLatitude();
                        double longe = location.getLongitude();
                        loc = lat + ":" + longe ;

                        try {

                            fOut = new FileOutputStream(file,true);
                             myOutWriter = new OutputStreamWriter(fOut);
                            myOutWriter.append(loc).append("\n");
                            myOutWriter.close();
                            fOut.close();

                         //    fos = openFileOutput(file_name, Context.MODE_APPEND);
                         //   fos.write(loc.getBytes());

                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }





               // Road road = roadManager.getRoad(waypoints);


              ////  Polyline roadOverlay = RoadManager.buildRoadOverlay(road);


               // map.getOverlays().add(roadOverlay);





            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {


            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);

            }
        };
        configure_button();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        b.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {

                locationManager.requestLocationUpdates("gps", 3000, 0, listener);

            }
        });

    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }
    public void Trail(View view){
        Intent intent = new Intent(this,Trails.class);
        startActivity(intent);


    }
}
