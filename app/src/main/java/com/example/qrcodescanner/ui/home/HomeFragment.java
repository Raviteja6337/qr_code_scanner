package com.example.qrcodescanner.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.qrcodescanner.DatabaseHandler;
import com.example.qrcodescanner.R;
import com.example.qrcodescanner.databinding.FragmentHomeBinding;
import com.example.qrcodescanner.ui.QRDetailsDAO;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    CameraView cameraViewObj;
    FirebaseVisionBarcodeDetectorOptions barcodeOptions;
    FirebaseVisionBarcodeDetector barcodeDetector;
    Button scanBtn;
    boolean isDetected = false;
    private TextToSpeech textToSpeech;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //Test TextView
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });


        //Checking and Requesting permissions
        Dexter.withContext(getContext()).withPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO})
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        openCameraToScanBarCode();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        Toast.makeText(getContext(),getResources().getString(R.string.req_permission),Toast.LENGTH_LONG).show();
                    }
                }).check();

        return root;
    }


    private void openCameraToScanBarCode() {
        try {
            scanBtn = binding.scanButton;
            scanBtn.setEnabled(isDetected);
            scanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isDetected = !isDetected;
                }
            });



            cameraViewObj = binding.cameraView;
            cameraViewObj.setLifecycleOwner(this);
            cameraViewObj.addFrameProcessor(new FrameProcessor() {
                @Override
                public void process(@NonNull @NotNull Frame frame) {
                    processImageCaptured(frame);
                }
            });

            barcodeOptions = new FirebaseVisionBarcodeDetectorOptions.Builder()
                    .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE).build();

            barcodeDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(barcodeOptions);
        }
        catch (Exception e)
        {
            Log.e(TAG,"Error in opening camera"+e.getMessage());
        }
    }


    private void processImageCaptured(Frame frame) {
        try {
            byte[] data = frame.getData();
            FirebaseVisionImage visionImage = null;
            FirebaseVisionImageMetadata imageMetadata = new FirebaseVisionImageMetadata.Builder()
                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setHeight(frame.getSize().getHeight())
                    .setWidth(frame.getSize().getWidth()).build();

            visionImage = FirebaseVisionImage.fromByteArray(data, imageMetadata);

            if (!isDetected)
            {
                barcodeDetector.detectInImage(visionImage)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                           @Override
                           public void onSuccess(@NonNull @NotNull List<FirebaseVisionBarcode> firebaseVisionBarcodes) {

                               //Process Result
                               processResultFromScan(firebaseVisionBarcodes);

                           }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull @NotNull Exception e) {
                               Toast.makeText(getContext(),getResources().getString(R.string.detect_error),Toast.LENGTH_LONG).show();
                           }
                        });
            }
        }
        catch (Exception e)
        {
            Log.e(TAG,"Error in Image captured processing"+e.getMessage());
        }
    }


    // BarCode scan results can be of different types like Text, URL or etc
    // This Method is to handle different cases of barcode scans
    private void processResultFromScan(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        if (firebaseVisionBarcodes!=null && firebaseVisionBarcodes.size()>0)
        {
            isDetected = true;
            scanBtn.setEnabled(isDetected);

            for(FirebaseVisionBarcode barcodeItem : firebaseVisionBarcodes)
            {
                int value_type = barcodeItem.getValueType();

                switch (value_type)
                {
                    case FirebaseVisionBarcode.TYPE_TEXT:
                    {
                        createDialogView(barcodeItem.getRawValue());
                        //saving the data to the DataBase
                        saveQRDataScannedToDB(barcodeItem.getRawValue());
                    }
                    break;

                    /* starting the browser intent incase of URL Type */
                    case FirebaseVisionBarcode.TYPE_URL:
                    {
                        Intent brwserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(barcodeItem.getRawValue()));
                        startActivity(brwserIntent);

                        //saving the data to the DataBase
                        saveQRDataScannedToDB(barcodeItem.getRawValue());
                    }
                    break;

                    /* starting the browser intent incase of Contact Info Type */
                    case FirebaseVisionBarcode.TYPE_CONTACT_INFO:
                    {
                        try {
                            String strBuilder = new StringBuilder("Name:  ").append(barcodeItem.getContactInfo().getName().getFormattedName())
                                    .append("\n")
                                    .append("Address:  ")
                                    .append(barcodeItem.getContactInfo().getAddresses().get(0).getAddressLines())
                                    .append("\n")
                                    .append("Email:  ")
                                    .append(barcodeItem.getContactInfo().getEmails().get(0).getAddress()).toString();
                            createDialogView(strBuilder);

                            //saving the data to the DataBase
                            saveQRDataScannedToDB(strBuilder);
                        }
                        catch (Exception e)
                        {
                            Log.e(TAG,"Error in Contact Info Type "+e.getMessage());
                        }
                    }
                    break;

                    default:
                        break;
                }
            }
        }
    }

    private void createDialogView(String rawValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(rawValue).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialogView = builder.create();
        dialogView.show();
    }

    private void saveQRDataScannedToDB(String rawValue) {
        try {
            DatabaseHandler dataBaseHandler = new DatabaseHandler(getContext());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd _ HH:mm", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());

            QRDetailsDAO qrDetailsDAO = new QRDetailsDAO();
            qrDetailsDAO.setStrQRData(rawValue);
            qrDetailsDAO.setTimeStampVal(currentDateandTime);
            dataBaseHandler.addQRDetails(qrDetailsDAO);

            //TODO ADDED EXTRA VOICE FEATURE ON SUCCESSFULL SCAN
            textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS)
                    {
                        textToSpeech.setLanguage(Locale.US);
                        textToSpeech.speak(getResources().getString(R.string.voice_onsucess),TextToSpeech.QUEUE_FLUSH,null);
                    }
                    else
                    {
                        Toast.makeText(getContext(),"Error in converting to speech ! please try again !",Toast.LENGTH_LONG).show();
                    }
                }
            });

            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {

                }

                @Override
                public void onDone(String utteranceId) {
                    textToSpeech.stop();
                }

                @Override
                public void onError(String utteranceId) {

                }
            });
        }
        catch (Exception e)
        {
            Log.d(TAG,"Error in adding QR Details to DB");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}