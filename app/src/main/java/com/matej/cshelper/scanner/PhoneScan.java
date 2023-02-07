package com.matej.cshelper.scanner;

import static androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED;

import androidx.camera.mlkit.vision.MlKitAnalyzer;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;

public class PhoneScan {

    private static PhoneScan instance;
    private static final String TAG = "PhoneScan";


    public static PhoneScan getInstance()
    {
        if (instance == null)
            instance = new PhoneScan();
        return instance;
    }

    private PhoneScan()
    { }

    public String CameraScan()
    {
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE,
                                Barcode.FORMAT_AZTEC)
                        .build();
        BarcodeScanner barcodeScanner = BarcodeScanning.getClient(options);

        return "koko";
    }

}
