package com.rn_version_0_70_12_native_ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;

public class CameraPreviewManager extends ViewGroupManager<FrameLayout> implements LifecycleEventListener {
    public static final String REACT_CLASS = "CameraPreview";
    private final ReactApplicationContext reactContext;
    private CameraPreview cameraPreview;

    public CameraPreviewManager(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected FrameLayout createViewInstance(@NonNull ThemedReactContext reactContext) {
        FrameLayout frameLayout = new FrameLayout(reactContext);
        frameLayout.setId(View.generateViewId());

        // Initialize CameraPreview
        cameraPreview = new CameraPreview(reactContext);
        frameLayout.addView(cameraPreview);

        // Ensure the CameraPreview is laid out correctly
        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setupLayout(frameLayout);
            }
        });

        return frameLayout;
    }

    private void setupLayout(FrameLayout frameLayout) {
        frameLayout.post(new Runnable() {
            @Override
            public void run() {
                manuallyLayoutChildren(frameLayout);
                frameLayout.getViewTreeObserver().dispatchOnGlobalLayout();
            }
        });
    }

    private void manuallyLayoutChildren(View view) {
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();

        view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));

        view.layout(0, 0, width, height);
    }

    @Override
    public void onHostResume() {
        if (cameraPreview != null) {
            cameraPreview.startBackgroundThread();
            if (cameraPreview.textureView.isAvailable()) {
                cameraPreview.openCamera();
            } else {
                cameraPreview.textureView.setSurfaceTextureListener(cameraPreview.surfaceTextureListener);
            }
        }
    }

    @Override
    public void onHostPause() {
        if (cameraPreview != null) {
            cameraPreview.closeCamera();
            cameraPreview.stopBackgroundThread();
        }
    }

    @Override
    public void onHostDestroy() {
        // Handle any cleanup if necessary
        if (cameraPreview != null) {
            cameraPreview.closeCamera();
            cameraPreview.stopBackgroundThread();
        }
    }

    @Override
    public void onAfterUpdateTransaction(@NonNull FrameLayout view) {
        super.onAfterUpdateTransaction(view);
        view.requestLayout();
    }

    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        return new LayoutShadowNode();
    }

    @Override
    public Class<? extends LayoutShadowNode> getShadowNodeClass() {
        return LayoutShadowNode.class;
    }
}
