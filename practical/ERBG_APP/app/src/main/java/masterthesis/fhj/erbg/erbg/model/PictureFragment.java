package masterthesis.fhj.erbg.erbg.model;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import masterthesis.fhj.erbg.erbg.ERBGConstants;
import masterthesis.fhj.erbg.erbg.R;
import masterthesis.fhj.erbg.erbg.random.RandomService;

public class PictureFragment extends Fragment implements FragmentCompat.OnRequestPermissionsResultCallback  {
    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 4711;
    private static final int REQUEST_WRITE_STORAGE = 4712;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    int BufferElements2Rec = 2048; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format
    RandomService randomService = RandomService.getInstance();

    private static final int TAKE_VIDEO = 2;

    private static final int CAPTURE_MODE=TAKE_VIDEO;


    public static PictureFragment newInstance() {
        return new PictureFragment();
    }

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    private ProgressBar mProgressBar;

    private EditText mEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_picture_mode, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mEditText = view.findViewById(R.id.etLog);
        this.getView().setBackgroundColor(Color.WHITE);
        // Set audio buttons
        view.findViewById(R.id.btTakePicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Bugs are sensitive things. They usually hide in hidden places. Please do not not scare them too much, Thanks!", Toast.LENGTH_SHORT).show();
//                requestAudioPermissions();
//                startRecording();
            }
        });


    }


    private void takePicture(){

    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }



    /**
     * BELOW OLD CODE
     *  BELOW OLD CODE
     *      BELOW OLD CODE
     *          BELOW OLD CODE
     */


    private void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                collectAudioData();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
        //TODO handle the bytes (store them locally) and align it with new implementation of RandomService
//        randomService.writeToFileExternal(getActivity(), "audioOnly");
        randomService.generateRandomFromFile(getActivity(),"audioOnly");
        mEditText.setText("");
        mEditText.scrollTo(0, mEditText.getBottom());
    }


    /**
     * Method for collecting audio data AND extract randomness
     */
    private void collectAudioData() {
        // Write the output audio in byte

        short sData[] = new short[BufferElements2Rec];
        int sumMax = 0;
        int sumMin = 0;
        while (isRecording) {

            // gets the sound input from microphone as byte format
            int readsize = recorder.read(sData, 0, BufferElements2Rec);
            long sum = 0;
            StringBuilder sb = new StringBuilder();
            for(short data : sData) {
                sum+=data;
                sb.append(Math.abs(data)%2);
            }
            randomService.addContent(sb.toString());
            if(sum==0)
                sum=1;
            if (sum>sumMax)
                sumMax=(int)sum;
            if (sum<sumMin)
                sumMin=(int)sum;

            double amp = sum/readsize;
            long minX = -10000;
            long maxX = 10000;
            double normalized = (((sum-minX)/(maxX-minX))*100);
            Log.i(ERBGConstants.LOG_TAG,"Normalized: ("+sum+") " + normalized);
            mProgressBar.setProgress((int)normalized);
        }
        Log.i(ERBGConstants.LOG_TAG,"sumMax: " + sumMax);
        Log.i(ERBGConstants.LOG_TAG,"sumMin: " + sumMin);
    }


    private void log(final String data) {

        randomService.addContent(data);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String currentText = mEditText.getText().toString();
                currentText=data+currentText;
                mEditText.setText(currentText);
                mEditText.scrollTo(0,mEditText.getBottom());
            }
        });
    }

    private static final int REQUEST_ERBG_AUDIOONLY_PERMISSIONS = 1;

    private static final String FRAGMENT_DIALOG = "dialog";
    private static final String[] ERBG_AUDIOONLY_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    /**
     * Requests permissions needed for recording video.
     */
    private void requestAudioPermissions() {
        if (shouldShowRequestPermissionRationale(ERBG_AUDIOONLY_PERMISSIONS)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            FragmentCompat.requestPermissions(this, ERBG_AUDIOONLY_PERMISSIONS, REQUEST_ERBG_AUDIOONLY_PERMISSIONS);
        }
    }


    /**
     * Gets whether you should show UI with rationale for requesting permissions.
     *
     * @param permissions The permissions your app wants to request.
     * @return Whether you can show permission rationale UI.
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (FragmentCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    public static class ConfirmationDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.permission_request)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FragmentCompat.requestPermissions(parent, ERBG_AUDIOONLY_PERMISSIONS,
                                    REQUEST_ERBG_AUDIOONLY_PERMISSIONS);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parent.getActivity().finish();
                                }
                            })
                    .create();
        }

    }


}
