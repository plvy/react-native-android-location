package com.syarul.rnalocation;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.MultiFallbackProvider;
import io.nlopez.smartlocation.utils.Logger;

public class RNALocationModule extends ReactContextBaseJavaModule {

    // React Class Name as called from JS
    public static final String REACT_CLASS = "RNALocation";
    // Unique Name for Log TAG
    public static final String TAG = RNALocationModule.class.getSimpleName();
    public static final int ONE_MINUTE = 1000 * 60;
    private boolean started;
    private static Boolean emulator;

    //The React Native Context
    ReactApplicationContext mReactContext;

    private Location lastLocation;

    // Constructor Method as called in Package
    public RNALocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        // Save Context for later use
        mReactContext = reactContext;
    }


    @Override
    public String getName() {
        return REACT_CLASS;
    }



    private boolean isEmulator() {
        if(emulator==null) {
            emulator = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
        }
        return emulator;
    }

    /**
     * Location Callback as called by JS
     */
    @ReactMethod
    public void getLocation() {
        Log.d(TAG, "getLocation called");

        if(isEmulator()) {
            lastLocation=new Location("plvy");
            // berlin: 52.489383,13.391627
            lastLocation.setLatitude(52.489383d);
            lastLocation.setLongitude(13.391627d);
            // phnom penh: 11.540572,104.9124968
            // lastLocation.setLatitude(11.540572d);
            // lastLocation.setLongitude(104.9124968d);
            sendEvent(mReactContext, lastLocation);
        } else {
            if (!started) {
                started = true;
                LocationProvider fallbackProvider = new MultiFallbackProvider.Builder()
                    .withGooglePlayServicesProvider().withProvider(new NullLocationProvider()).build();
                SmartLocation.with(mReactContext).location(fallbackProvider)
                    .start(new OnLocationUpdatedListener() {

                        @Override
                        public void onLocationUpdated(Location location) {
                            Log.d(TAG, "onLocationUpdated called");
                            lastLocation = location;
                            sendEvent(mReactContext, location);
                        }
                    });
            } else {
                sendEvent(mReactContext, lastLocation);
            }
        }
    }

    /*
     * Internal function for communicating with JS
     */
    private void sendEvent(ReactContext reactContext, Location location) {
        Log.d(TAG, "about to send location update event");

        double longitude;
        double latitude;

        if (location == null) {
            longitude = 0;
            latitude = 0;
            Log.w(TAG, "location is null, using (0,0)");
        } else {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d(TAG, "Got new location. Lng: " + longitude + " Lat: " + latitude);
        }


        // Create Map with Parameters to send to JS
        WritableMap params = Arguments.createMap();
        params.putDouble("Longitude", longitude);
        params.putDouble("Latitude", latitude);

        if (reactContext.hasActiveCatalystInstance()) {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("updateLocation", params);
        } else {
            Log.e(TAG, "CatalystInstance is not active, cannot send updateLocation!");
        }
    }

    private static class NullLocationProvider implements LocationProvider {
        @Override
        public void init(Context context, Logger logger) {

        }

        @Override
        public void start(OnLocationUpdatedListener onLocationUpdatedListener, LocationParams locationParams,
            boolean b) {
            Log.d(TAG, "NullLocationProvider started");
        }

        @Override
        public void stop() {
            Log.d(TAG, "NullLocationProvider stopped");
        }

        @Override
        public Location getLastLocation() {
            Log.w(TAG, "NullLocationProvider called");
            return null;
        }
    }
}
