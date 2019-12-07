package masterthesis.fhj.erbg.erbg;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import masterthesis.fhj.erbg.erbg.model.AudioFragment;
import masterthesis.fhj.erbg.erbg.model.VideoFragment;
import masterthesis.fhj.erbg.erbg.model.ERNGFragment;
import masterthesis.fhj.erbg.erbg.model.PictureFragment;

public class MainActivity extends AppCompatActivity {

    private Button btnAudioOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Economy Random Bit Generator");
        setContentView(R.layout.activity_container);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setOnClickListener();
    }

    @Override
    public void onBackPressed(){
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i(ERBGConstants.LOG_TAG, "popping backstack");
            fm.popBackStack();
            setContentView(R.layout.activity_main);
            setOnClickListener();

        } else {
            Log.i(ERBGConstants.LOG_TAG, "nothing on backstack, calling super");
            super.onBackPressed();
        }
    }

    private void setOnClickListener(){
        /**
         * Set Audio Mode Button and load {@link AudioFragment}
         */
        this.findViewById(R.id.btnModeAudio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_container);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack("AudioFragment");
                ft.replace(R.id.container, AudioFragment.newInstance()).commit();
            }
        });


        /**
         * Set Video Mode Button and load {@link VideoFragment}
         */
        this.findViewById(R.id.btnModeVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_container);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack("VideoFragment");
                ft.replace(R.id.container, VideoFragment.newInstance()).commit();
            }
        });

        /**
         * Set Picture Mode and load {@link PictureFragment}
         */
        this.findViewById(R.id.btnModePicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_container);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack("VideoFragment");
                ft.replace(R.id.container, PictureFragment.newInstance()).commit();
            }
        });

        /**
         * Set ERNG Result Page
         */
        this.findViewById(R.id.btnResult).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.activity_container);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack("ERNGFragment");
                ft.replace(R.id.container, ERNGFragment.newInstance()).commit();
            }
        });

    }

}
