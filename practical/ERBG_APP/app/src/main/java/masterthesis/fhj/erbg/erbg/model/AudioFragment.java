package masterthesis.fhj.erbg.erbg.model;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v13.app.FragmentCompat;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import masterthesis.fhj.erbg.erbg.ERBGConstants;
import masterthesis.fhj.erbg.erbg.R;
import masterthesis.fhj.erbg.erbg.random.RandomService;

public class AudioFragment extends Fragment implements FragmentCompat.OnRequestPermissionsResultCallback  {
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
    private String outputFilePath;


    public static AudioFragment newInstance() {
        return new AudioFragment();
    }

    /**
     * MediaRecorder
     */
    private MediaRecorder mMediaRecorder;

    private ProgressBar mProgressBar;

    private EditText mEditText;

    private Button mRecordingButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_mode, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mEditText = view.findViewById(R.id.etLog);
        this.getView().setBackgroundColor(Color.WHITE);

        mRecordingButton = (Button) view.findViewById(R.id.btStartRecord);
        mRecordingButton.setText(R.string.record);

        // Set audio buttons
        view.findViewById(R.id.btStartRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAudioPermissions();
                startStopRecording();
            }
        });

    }

    private void startStopRecording(){
        if (isRecording) {
            stopRecording();
            mRecordingButton.setText(R.string.record);

        }
        else {
            startRecording();
            mRecordingButton.setText(R.string.stop);

        }

    }



    private void startRecording() {
        outputFilePath = getVideoFilePath();

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
        int count =0;
        StringBuilder sb = new StringBuilder();
        log("Started Recording");
        while (isRecording) {
            long amp = 0;

            // gets the sound input from microphone as byte format
            int readsize = recorder.read(sData, 0, BufferElements2Rec);
            long sum = 0;
            for(short data : sData) {
                sum+=data;
                if(count>=200)
                    sb.append(Math.abs(data)%2);
                count++;
                if (((count-200)%100000)==0) {
                    log("Collected: " + (count - 200));
                    this.writeToFileExternal(sb);
                    sb = new StringBuilder();
                }
            }
            if(sum<=0)
                amp=(sum*(-1))%100;

            mProgressBar.setProgress((int)amp);
        }

        log("Finished Recording");
    }

    /**
     * When all the data from an RAW file is processed this method is called and writes the given content from the StringBuilder to the given outputFilePath in append-mode
     * @param content
     */
    private void writeToFileExternal(StringBuilder content){
        File file = new File(outputFilePath);
        try {
            FileOutputStream f = new FileOutputStream(file,true);
            f.write(content.toString().getBytes());
            f.close();
            Log.i(ERBGConstants.LOG_TAG_ASYNC,"File saved:" + outputFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(ERBGConstants.LOG_TAG_ASYNC, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
            Log.i(ERBGConstants.LOG_TAG_ASYNC,"Failed to save file" + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(ERBGConstants.LOG_TAG_ASYNC,"Failed to save file" + outputFilePath);
        }
    }

    private String getVideoFilePath() {
        return randomService.getPublicAlbumStorageDir(ERBGConstants.PUBLIC_FOLDER)
                .getAbsolutePath()+"/"+randomService.getOutputFileNameForRaw("raw_audio");
    }


    private void log(final String data) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String currentText = mEditText.getText().toString();
                currentText=data+"\n"+currentText;
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
