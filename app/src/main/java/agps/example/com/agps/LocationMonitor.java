package agps.example.com.agps;

import java.util.HashMap;
import java.util.List;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

public class LocationMonitor {

    public static final String TAG = LocationMonitor.class.getSimpleName();

    protected final long minTimeInterval;
    protected final float minDistanceInterval;

    protected final Context context;

    private LocationManager locationManager;
    private Location lastLocation;

    private HashMap<String, Boolean> providerStatus = new HashMap<>();
    private List<String> providers;

    private SimpleLocationListener locationListener;
    private IgnoredLocationListener ignoredLocationListener;

    private LocationListener internalLocationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            providerStatus.put(provider, LocationProvider.AVAILABLE == status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Not really available, just enabled.
        }

        @Override
        public void onProviderDisabled(String provider) {
            providerStatus.put(provider, false);
        }

        @Override
        public void onLocationChanged(Location location) {
            if (null == location || null == location.getProvider()) {
                return;
            }

            final String provider = location.getProvider();
            providerStatus.put(provider, true);

            //Ignore other provider location if GPS is available.
            if (providerStatus.get(LocationManager.GPS_PROVIDER) && false == LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
                if(null != ignoredLocationListener) {
                    ignoredLocationListener.onLocationIgnored(location);
                }
                return;
            }

            lastLocation = location;
            if (null != locationListener) {
                locationListener.onLocationChanged(location);
            }
        }
    };

    public LocationMonitor(@NonNull Context context, float minDistanceInterval, long minTimeInterval) {
        this.context = context.getApplicationContext();
        this.minDistanceInterval = minDistanceInterval;
        this.minTimeInterval = minTimeInterval;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        providers = locationManager.getAllProviders();
    }

    public void startTracking() throws PermissionException {
        checkPermission(context);
        initProviderStatus();
        for (String provider : providers) {
            locationManager.requestLocationUpdates(provider, minTimeInterval, minDistanceInterval, internalLocationListener);
        }
    }

    public void stopTracking() {
        try {
            checkPermission(context);
        } catch (PermissionException e) {
            e.printStackTrace();
        }
        locationManager.removeUpdates(internalLocationListener);
        clearProviderStatus();
    }

    protected void initProviderStatus() {
        for (String provider : providers) {
            providerStatus.put(provider, false);
        }
    }

    protected void clearProviderStatus() {
        providerStatus.clear();
    }

    protected void checkPermission(Context context) throws PermissionException {
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) &&
            PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            throw new PermissionException("Location permission not granted");
        }
    }

    public LocationMonitor setLocationListener(SimpleLocationListener listener) {
        this.locationListener = listener;
        return this;
    }

    public LocationMonitor setIgnoredLocationListener(IgnoredLocationListener listener) {
        this.ignoredLocationListener = listener;
        return this;
    }

    public boolean hasCapabilityGettingLocation() {
        boolean result = false;
        for (String providerName : providers) {
            boolean enable = locationManager.isProviderEnabled(providerName);
            if(enable && false == "passive".equals(providerName)) {
                result = true;
            }
        }
        return result;
    }

    public Location getLastKnownLocation() {
        return lastLocation;
    }


    public static class Builder {

        private Context context;

        private long minTimeInterval = 3000;
        private float minDistanceInterval = 5;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        public Builder minDistanceInterval(float minDistanceInterval) {
            this.minDistanceInterval = minDistanceInterval;
            return this;
        }

        public Builder minTimeInterval(long minTimeInterval) {
            this.minTimeInterval = minTimeInterval;
            return this;
        }

        public LocationMonitor build() {
            return new LocationMonitor(context, minDistanceInterval, minTimeInterval);
        }
    }
}

