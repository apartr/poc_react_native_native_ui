package com.rn_version_0_70_12_native_ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleObserver;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class CameraPreview extends FrameLayout implements LifecycleObserver, ImageAnalysis.Analyzer, SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private SurfaceView surfaceView;
    private LifecycleOwner lifecycleOwner;
    private ImageAnalysis imageAnalysis;
    private boolean isSurfaceReady = false;

    public CameraPreview(@NonNull Context context, LifecycleOwner lifecycleOwner) {
        this(context, null, lifecycleOwner);
    }

    public CameraPreview(@NonNull Context context, @Nullable AttributeSet attrs, LifecycleOwner lifecycleOwner) {
        this(context, attrs, 0, lifecycleOwner);
    }

    public CameraPreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, LifecycleOwner lifecycleOwner) {
        super(context, attrs, defStyleAttr);
        this.lifecycleOwner = lifecycleOwner;
        init(context);
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    private void init(Context context) {
        Log.d(TAG, "Initializing CameraPreview");
        surfaceView = new SurfaceView(context);
        this.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        surfaceView.getHolder().addCallback(this);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                Log.d(TAG, "Getting ProcessCameraProvider");
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error getting ProcessCameraProvider", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Log.d(TAG, "Binding camera use cases");

        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getContext()), this);

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis);
        Log.d(TAG, "Camera use cases bound to lifecycle");
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        if (!isSurfaceReady) {
            image.close();
            return;
        }

        Log.d(TAG, "Analyzing image");
        // Convert ImageProxy to Bitmap
        Bitmap bitmap = imageProxyToBitmap(image);
        image.close();

        // Rotate and flip the Bitmap to correct orientation
        Bitmap rotatedBitmap = rotateAndFlipBitmap(bitmap, image.getImageInfo().getRotationDegrees());

        // Draw the Bitmap on the SurfaceView
        if (rotatedBitmap != null) {
            SurfaceHolder holder = surfaceView.getHolder();
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                drawBitmapFullScreen(canvas, rotatedBitmap);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void drawBitmapFullScreen(Canvas canvas, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        float scaleX = (float) canvas.getWidth() / bitmap.getWidth();
        float scaleY = (float) canvas.getHeight() / bitmap.getHeight();
        float scale = Math.max(scaleX, scaleY);
        float dx = (canvas.getWidth() - bitmap.getWidth() * scale) / 2f;
        float dy = (canvas.getHeight() - bitmap.getHeight() * scale) / 2f;
        matrix.postScale(scale, scale);
        matrix.postTranslate(dx, dy);
        canvas.drawBitmap(bitmap, matrix, null);
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        byte[] nv21 = getNv21(image);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    @NonNull
    private static byte[] getNv21(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);
        return nv21;
    }

    private Bitmap rotateAndFlipBitmap(Bitmap bitmap, int rotationDegrees) {
        Matrix matrix = new Matrix();
        // Flip horizontally
        matrix.postScale(1, -1);
        // Rotate
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        isSurfaceReady = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceReady = false;
    }

    // Add other lifecycle event methods as needed
}
