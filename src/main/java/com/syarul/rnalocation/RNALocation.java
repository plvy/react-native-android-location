package com.syarul.rnalocation;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.syarul.rnalocation.RNALocationModule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RNALocation implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules( ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(
                new RNALocationModule(reactContext)
        );
    }
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactApplicationContext) {
        return Collections.emptyList();
    }
}
