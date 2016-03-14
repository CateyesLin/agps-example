package agps.example.com.agps;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity {

    public final static String TAG = MainActivity.class.getSimpleName();

    MapView map;
    GoogleMap gMap;
    Marker currentLocationMarker;
    Marker ignoreLocationMarker;

    protected final static String[] LOCATION_PERMISSIONS = new String[]{
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    };
    protected final int REQUEST_PERMISSION_GRANT = 1;

    protected LocationMonitor locationMonitor;
    protected SimpleLocationListener listener = new SimpleLocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, String.format("Use location from:%s lat:%f, lng:%f", location.getProvider(), location.getLatitude(), location.getLongitude()));
            setRealyMarker(location);
        }
    };

    protected IgnoredLocationListener ignoredLocationListener = new IgnoredLocationListener() {
        @Override
        public void onLocationIgnored(Location location) {
            Log.i(TAG, String.format("Ignore location from:%s lat:%f, lng:%f", location.getProvider(), location.getLatitude(), location.getLongitude()));
            setIgnoreMarker(location);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationMonitor = new LocationMonitor.Builder(this).build();
        locationMonitor
                .setLocationListener(listener)
                .setIgnoredLocationListener(ignoredLocationListener);

        map = (MapView) findViewById(R.id.map);
        map.onCreate(savedInstanceState);
        map.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                gMap = googleMap;
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            locationMonitor.startTracking();
        } catch (PermissionException e) {
            e.printStackTrace();
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, REQUEST_PERMISSION_GRANT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationMonitor.stopTracking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(REQUEST_PERMISSION_GRANT == requestCode) {
            for(int result : grantResults) {

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setRealyMarker(Location location) {
        if(null == gMap) {
            return;
        }

        if(null != currentLocationMarker) {
            currentLocationMarker.remove();
        }

        MarkerOptions options = new MarkerOptions()
                .title("I'm here")
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .draggable(false)
                .visible(true);
        currentLocationMarker = gMap.addMarker(options);
    }

    protected void setIgnoreMarker(Location location) {
        if(null == gMap) {
            return;
        }

        if(null != ignoreLocationMarker) {
            ignoreLocationMarker.remove();
        }

        MarkerOptions options = new MarkerOptions()
                .title("Location ignored")
                .icon(BitmapDescriptorFactory.defaultMarker(180))
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .draggable(false)
                .visible(true);
        ignoreLocationMarker = gMap.addMarker(options);
    }
}
