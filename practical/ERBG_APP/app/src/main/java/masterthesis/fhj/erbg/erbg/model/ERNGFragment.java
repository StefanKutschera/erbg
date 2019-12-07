package masterthesis.fhj.erbg.erbg.model;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import masterthesis.fhj.erbg.erbg.ERBGConstants;
import masterthesis.fhj.erbg.erbg.R;
import masterthesis.fhj.erbg.erbg.random.RandomService;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class ERNGFragment extends Fragment {


    private RandomService mRandomService = RandomService.getInstance();
    private EditText mEditText;
    private TextView mtxtLabelFileName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_erng, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        this.getView().setBackgroundColor(Color.WHITE);
        Log.i(ERBGConstants.LOG_TAG,"onViewCreated");
        mEditText = view.findViewById(R.id.txtStats);
        mtxtLabelFileName = view.findViewById(R.id.txtLabelFileName);

        String[] result = mRandomService.getResults();

        String fileName= mRandomService.getFilename();
        if(fileName!=null)
            mtxtLabelFileName.setText(fileName);

        String resultText="";
        if (result!=null){
            for (String line:result) {
                if(line!=null)
                    resultText+=line+"\n";
            }
        }

        if (result!=null)
            mEditText.setText(resultText);
        else
            mEditText.setText("Wuups, here should be your result\n\nFor some reason it isn't\n\nPlease pray for a bugfix!");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(ERBGConstants.LOG_TAG,"onResume");

    }

    public static ERNGFragment newInstance() {
        return new ERNGFragment();
    }

}
