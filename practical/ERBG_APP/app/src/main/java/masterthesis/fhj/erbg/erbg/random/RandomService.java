package masterthesis.fhj.erbg.erbg.random;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import masterthesis.fhj.erbg.erbg.ERBGConstants;

public class RandomService {

    private static RandomService randomService_instance = null;
    private String content = "";
    private String inputFilepath = "";
    private String outputFilePath;
    private String filename;
    private String sessionId;
    private String[] results;
    private String usedCamera="Primary";

    private RandomService() {
        //intentionally left blank for Singleton behavior
    }

    public String getInputFilepath() {
        return inputFilepath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public String getFilename() {
        return filename;
    }

    public static RandomService getInstance() {
        if (randomService_instance == null)
            randomService_instance = new RandomService();
        return randomService_instance;
    }

    /**
     * Existing content will be overwritten by given input
     *
     * @param newContent
     */
    public void setContent(String newContent) {
        this.content = newContent;
    }

    /**
     * Adds input in addition to already existing content, no getContext needed
     *
     * @param additionalContent
     */
    public void addContent(String additionalContent) {
        this.content += additionalContent;
    }

    public void setInputFilepath(String inputFilepath) {
        this.inputFilepath = inputFilepath;
    }


    public void long2binary(long input) {
        throw new UnsupportedOperationException("This needs to be implemented first!");
    }

    public void short2binary(short input) {
        throw new UnsupportedOperationException("This needs to be implemented first!");
    }

    public void int2binary(int input) {
        throw new UnsupportedOperationException("This needs to be implemented first!");
    }

    public void byte2binary(byte input) {
        throw new UnsupportedOperationException("This needs to be implemented first!");
    }

    public byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }


    /**
     * Get or create the directory for the ERBG raw and random files
     * @param albumName
     * @return
     */
    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.mkdirs()) {
            Log.i(ERBGConstants.LOG_TAG, "Directory not created, maybe already existed");
        }
        return file;
    }


    /**
     *  Checks if external storage is available for read and write
     *
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if external storage is available to at least read
     *
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Generates or gets the filename for the generated random file that contains just 0 or 1's.
     *
     * @param prefix
     * @return
     */
    public String getFinalOutputFileName(String prefix) {
        if (!(this.filename == null))
            return this.filename;
        this.filename = "erbg_" + prefix + "_" + getSessionID() + "_"+usedCamera+"_"+getDeviceInfo()+".data";
        return this.filename;
    }


    /**
     * Used Camera (Front(Secondary) or Back (Primary))
     */
    public void setUsedCamera(String usedCamera) {
        this.usedCamera = usedCamera;
    }

    /**
     * Get and fetch model name and android version
     *
     */
    public String getDeviceInfo(){
        String reqString = "x";
        try {
            reqString = Build.MANUFACTURER
                    + "-" + Build.MODEL + "_" + Build.VERSION.RELEASE
                    + "_" + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName()
                    + "_API" +Build.VERSION.SDK_INT;
        } catch (Exception e1) {
            reqString = "noInfo";
        }

        return reqString.replace(" ","_");
    }



    /**
     * Generates or gets the filename for the RAW file i.e. the mp4 or mp3. with full content in it
     *
     * @param prefix
     * @return
     */
    public String getOutputFileNameForRaw(String prefix) {
        return ("erbg_" + prefix + "_" + getSessionID() + "_"+usedCamera+"_"+getDeviceInfo()+".data");
    }

    /**
     * Generates or gets the SessionID which is simply a timestamp.
     * This is needed for humanreadable purpuse to connect the raw files with the generates random files.
     *
     * @return
     */
    public String getSessionID(){
        if (sessionId==null || sessionId==""){
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            this.sessionId=timeStamp;
        }
        return this.sessionId;
    }

    /**
     * Return the calculated results from an previously generated random file. If the result is not ye ready the method returns null
     * @return
     */
    public String[] getResults(){
        if(this.results==null){
            return null;
        } else {
            return this.results;
        }
        
    }

    /**
     * Generates random bits 1/0 from the RandomService.inputfilepath defined source. This will be
     * executed in a background async task. Afterwards the result page will be shown to the user
     * filled with some details about the generated random sequence
     *
     * @param activity
     * @param prefix
     */
    public void generateRandomFromFile(Activity activity, String prefix) {
        //Define output file path
        getFinalOutputFileName(prefix);
        File outputFileDir = getPublicAlbumStorageDir(ERBGConstants.PUBLIC_FOLDER);
        File file = new File(outputFileDir, filename);

        String[] filePathArgs = new String[2];
        filePathArgs[0] = this.inputFilepath; // path from were the raw file should be loaded
        filePathArgs[1] = file.getAbsolutePath(); // path were the rng file should be stored

        RandomBitGeneratorFromBytes rng = new RandomBitGeneratorFromBytes(activity);
        rng.execute(filePathArgs);
    }

    public void setResults(String[] results) {
        this.results = results;
    }

    public void resetPaths() {
        this.inputFilepath=null;
        this.outputFilePath=null;
        this.sessionId=null;
    }
}

/**
 * Asynchronous Class for extracting random data from an raw file. This class handels not only the results but also resets the RandomService for in/outputFilePath and sessionID
 */
class RandomBitGeneratorFromBytes extends AsyncTask<String,Integer,String>{

    private String filename="";
    private String inputFilePath="";
    private String outputFilePath="";
    private RandomService mRandomService = RandomService.getInstance();
    private Context context;
    private int zero=0;
    private int one=0;
    private int firstBytesDumpCounter=0;

    private int runbefore=2;
    private int actRunZero=0;
    private int actRunOne=0;
    private int longestRunZero=0;
    private int longestRunOne=0;


    /**
     * Asynchronous Class for extracting random data from an raw file. This class handel not only the results but also resets the RandomService for in/outputFilePath and sessionID
     * @param context
     */
    RandomBitGeneratorFromBytes(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String[] filePaths) {
        Log.i(ERBGConstants.LOG_TAG_ASYNC,"doInBackground args" + filePaths.length + " :inoutFilePath:" + filePaths[0] + ":outputFilePath:" + filePaths[1]);
        this.inputFilePath=filePaths[0];
        this.outputFilePath=filePaths[1];

        try {
            generateFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkDistribution(outputFilePath);
        return this.outputFilePath;

    }

    @Override
    protected void onProgressUpdate(Integer[] values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String o) {
        super.onPostExecute(o);
        mRandomService.resetPaths();
        Log.i(ERBGConstants.LOG_TAG_ASYNC,"WUUUHUUU!!! random file generated and basic statistic finished, congrats!");
        Toast.makeText(context, "Your ERBG results are ready ", Toast.LENGTH_SHORT).show();
        this.context=null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        this.context=null;
        Log.i(ERBGConstants.LOG_TAG_ASYNC,"DoInBackground RNG generation was cancelled");
    }

    /**
     * This is more or less the heart of the ERBG. It extracts zero or one by simply calculating
     * 'byte modulo(2)' from any given byte array.
     * This Method is normally called by the method 'generateFromFile()'
     *
     * @param input
     */
    private void byteArray2binary(byte[] input){
        Log.i(ERBGConstants.LOG_TAG_ASYNC,"There are " +input.length +" Bytes to process");
        StringBuilder sb = new StringBuilder();
        for (byte b:input) {
            if (firstBytesDumpCounter > ERBGConstants.FIRST_BYTE_DUMP_THRESHOLD) {
                int i = Math.abs(b)%2;
                if (i==0) {
                    zero++;
                    actRunZero++;
                    actRunOne = 0;
                    if(actRunZero>longestRunZero) {
                        longestRunZero = actRunZero;
                    }
                }
                else {
                    one++;
                    actRunOne++;
                    actRunZero = 0;
                    if(actRunOne>longestRunOne) {
                        longestRunOne = actRunOne;
                    }
                }
                sb.append(i);
            }
            firstBytesDumpCounter++;
        }
        sb.append("\n");
        writeToFileExternal(sb);
    }

    /**
     * From the given inputFilePath (i.e. the RAW file) the byte are reading in and handed over to for random extraction to 'byteArray2binary(byte[] input)'
     * @throws IOException
     */
    private void generateFromFile()throws IOException {
        Log.i(ERBGConstants.LOG_TAG_ASYNC,"Start gathering Data from File");
        File file = new File(inputFilePath);
        InputStream ios = null;
        try {
            byte[] buffer = new byte[409600];
            ios = new FileInputStream(file);
            while (ios.read(buffer) != -1) {
                this.byteArray2binary(buffer); // DEMO Start point
            }
        }finally {
            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
                Log.i(ERBGConstants.LOG_TAG_ASYNC,"Ups something with closing went wrong");
            }
        }
        Log.i(ERBGConstants.LOG_TAG_ASYNC,"Finished data gathering from file");
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

    /**
     * Reads a file from the given filepath and calculates the distribution of one' and zero's.
     * The result of this will be directly written to the {@link RandomService} Singelton
     * @param filepath
     */
    public void checkDistribution(String filepath) {
        String[] result = new String[20];
        double ratio = ((double) zero / (double) one) * 1.00000;
        int i=0;
        int difference = (zero-one);
        int overall = zero+one;

        result[i++] = "-------------------------------------------------";
        result[i++] = "File:" + inputFilePath;
        result[i++] = "Time:"+ mRandomService.getSessionID();
        result[i++] = "-------------------------------------------------";
        result[i++] = "# Overall :\t\t"+Integer.toString(overall);
        result[i++] = "# 0 : \t\t\t"+Integer.toString(zero);
        result[i++] = "# 1 : \t\t\t"+Integer.toString(one);
        result[i++] = "# Difference:\t\t"+Integer.toString(difference);
        result[i++] = "% Diff to Overall\t"+Double.toString(((double)difference/overall)*100.00000);
        result[i++] = "-------------------------------------------------";
        result[i++] = "Distribution: \t\t"+Double.toString(ratio);
        result[i++] = "-------------------------------------------------";
        result[i++] = "# Longest Run 0: \t" + longestRunZero;
        result[i++] = "# Longest Run 1: \t" + longestRunOne;
        result[i++] = "#################################################\n\n";


//        result[i++] = "# Overall :\t\t"+Integer.toString((zero+one));
//        result[i++] = "# 0 : \t\t"+Integer.toString(zero);
//        result[i++] = "# 1 : \t\t"+Integer.toString(one);
//        result[i++] = "# Difference: \t" + Integer.toString();
//        result[i++] = "% Diff to Overall";
//        result[i++] = "Distribution: \t\t"+Double.toString(ratio);
//        result[i++] = "# Longest Run 0: " + longestRunZero;
//        result[i++] = "# Longest Run 1: " + longestRunOne;
        Log.i(ERBGConstants.LOG_TAG_ASYNC,  "# Overall: '"+(zero+one)+"' -- # 0: '" + zero + "' -- 1:'" + one + "' -- Ratio 0/1:'" + String.format("%.12f", ratio) + "'");
        mRandomService.setResults(result);

    }
    
    
}