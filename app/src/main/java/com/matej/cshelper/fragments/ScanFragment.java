package com.matej.cshelper.fragments;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.util.concurrent.ListenableFuture;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    private static final String TAG = "BARCODE_SCANER";
    public static final String ARG_SOURCE = "SOURCE";

    private int source = 0;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private View layout;
    private Camera camera;
    private PreviewView previewView;
    private TextView barcodeValue;
    private Activity activity;
    private String lastScanValue = "";
    private ScanFragment instance;

    private boolean torchOn = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.layout = inflater.inflate(R.layout.fragment_scan, container, false);

        this.layout.findViewById(R.id.torch_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                torchOn = !torchOn;
                camera.getCameraControl().enableTorch(torchOn);
            }
        });
        layout.findViewById(R.id.send_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnScannedValue();
            }
        });
        return this.layout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            this.source = getArguments().getInt(ARG_SOURCE);
        }
        instance = this;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = getActivity();
        previewView = layout.findViewById(R.id.scanner_preview);
        barcodeValue = layout.findViewById(R.id.barcode_value);

        cameraProviderFuture = ProcessCameraProvider.getInstance(MainActivity.getContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(MainActivity.getContext()));

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        //Add MLKit
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        ImageAnalysis analysis = new ImageAnalysis.Builder().build();
        analysis.setAnalyzer(
                // newSingleThreadExecutor() will let us perform analysis on a single worker thread
                Executors.newSingleThreadExecutor(),image -> {
                    processImage(scanner, image);
                });
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, analysis);
        this.camera = camera;
    }

    @SuppressLint("UnsafeOptInUsageError")
    void processImage(BarcodeScanner scanner, ImageProxy imageProxy)
    {
        Image image = imageProxy.getImage();
        if(image != null)
        {
            InputImage inputImage =
                    InputImage.fromMediaImage(
                            image,
                            imageProxy.getImageInfo().getRotationDegrees()
                    );

            scanner.process(inputImage)
                    .addOnSuccessListener(barcodeList -> {
                        if(!barcodeList.isEmpty())
                        {
                            Barcode barcode = barcodeList.get(0);
                            if (barcode != null) {
                                String value = barcode.getRawValue();
                                lastScanValue = value;
                                barcodeValue.setText("Scan: " + lastScanValue);
                            }
                        }
                        else
                        {
                            barcodeValue.setText("Scan: ");
                        }

                    })
                    .addOnFailureListener(e -> Log.e(TAG, e.getMessage()))
                    .addOnCompleteListener(task -> {
                        imageProxy.getImage().close();
                        imageProxy.close();
                    });
        }
    }

    void returnScannedValue()
    {
        if(source == 1) //SettingsFragment
        {
            try
            {
                Bundle args = new Bundle();
                args.putInt(SettingsFragment.ARG_USERID, Integer.parseInt(lastScanValue));
                NavHostFragment.findNavController(instance).navigate(R.id.settingsFragment,args);
            }
            catch (Exception e)
            {
                Log.w(TAG, e);
            }

        }
    }
}