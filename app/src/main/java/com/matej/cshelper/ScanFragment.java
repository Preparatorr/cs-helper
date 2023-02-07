package com.matej.cshelper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    private static int REQUEST_CODE = 6659;
    private View root;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_scan, container, false);
        if(!hasCameraPermission())
            requestCameraPermission();

        return root;
    }

    private void requestCameraPermission()
    {
        ActivityCompat.requestPermissions(
                (Activity) MainActivity.getContext(),
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CODE
        );
    }

    private boolean hasCameraPermission()
    {
         return ActivityCompat.checkSelfPermission(MainActivity.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public void Scan()
    {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(MainActivity.getContext());
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreviewAndImageAnalysis(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },ContextCompat.getMainExecutor(MainActivity.getContext()));

    }

    private void bindPreviewAndImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {

        int orientation = MainActivity.getContext().getResources().getConfiguration().orientation;
        Size resolution;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            resolution = new Size(720, 1280);
        }else{
            resolution = new Size(1280, 720);
        }

        Preview.Builder previewBuilder = new Preview.Builder();
        previewBuilder.setTargetResolution(resolution);
        Preview preview = previewBuilder.build();

        ImageAnalysis.Builder imageAnalysisBuilder = new ImageAnalysis.Builder();

        imageAnalysisBuilder.setTargetResolution(resolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST);

        ImageAnalysis imageAnalysis = imageAnalysisBuilder.build();

        imageAnalysis.setAnalyzer(exec, new ImageAnalysis.Analyzer() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                TextResult[] results = null;
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                int nRowStride = image.getPlanes()[0].getRowStride();
                int nPixelStride = image.getPlanes()[0].getPixelStride();
                int length = buffer.remaining();
                byte[] bytes = new byte[length];
                buffer.get(bytes);
                ImageData imageData = new ImageData(bytes, image.getWidth(), image.getHeight(), nRowStride * nPixelStride);
                try {
                    results = dbr.decodeBuffer(imageData.mBytes, imageData.mWidth, imageData.mHeight, imageData.mStride, EnumImagePixelFormat.IPF_NV21, "");
                } catch (BarcodeReaderException e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Found ").append(results.length).append(" barcode(s):\n");
                for (int i = 0; i < results.length; i++) {
                    sb.append(results[i].barcodeText);
                    sb.append("\n");
                }
                Log.d("DBR", sb.toString());
                runOnUiThread(()->{resultView.setText(sb.toString());});
                image.close();
            }
        });

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalysis)
                .build();
        camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, useCaseGroup);
    }


}