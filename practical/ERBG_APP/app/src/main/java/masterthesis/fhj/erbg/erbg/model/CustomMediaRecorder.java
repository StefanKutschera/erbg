package masterthesis.fhj.erbg.erbg.model;

import android.media.MediaRecorder;
import android.util.Log;

public class CustomMediaRecorder extends MediaRecorder {

    @Override
    public void start() throws IllegalStateException {
        Log.i("ERBG_LOG","Start Media Recorder");
        super.start();
    }


    @Override
    public void stop() throws IllegalStateException {
        super.stop();
    }
}
