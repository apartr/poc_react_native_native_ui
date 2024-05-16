package com.rn_version_0_70_12_native_ui;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import java.util.Map;

public class CameraPreviewManager extends SimpleViewManager<CameraPreview> {
    public static final String REACT_CLASS = "CameraPreview";
    ReactApplicationContext mCallerContext;

    public CameraPreviewManager(ReactApplicationContext reactContext) {
        mCallerContext = reactContext;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected CameraPreview createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new CameraPreview(reactContext);
    }

    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder().put(
                "topChange",
                MapBuilder.of(
                        "phasedRegistrationNames",
                        MapBuilder.of("bubbled", "onChange")
                )
        ).build();
    }

    @ReactMethod
    public void testMethod() {
        Log.d("TEST", "TEST");
        // Implement capturing image here
    }
}