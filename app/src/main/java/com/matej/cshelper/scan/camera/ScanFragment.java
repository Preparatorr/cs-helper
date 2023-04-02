package com.matej.cshelper.scan.camera;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.camera.lifecycle.ProcessCameraProvider;
import com.google.common.util.concurrent.ListenableFuture;

import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.matej.cshelper.MainActivity;
import com.matej.cshelper.R;

import java.util.concurrent.ExecutionException;

public class ScanFragment extends Fragment {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private View layout;
    private Camera camera;
    private PreviewView previewView;
    private Activity activity;

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
        return this.layout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = getActivity();
        previewView = layout.findViewById(R.id.scanner_preview);

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

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
        this.camera = camera;
    }

    void scanCode()
    {
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
    }


}