package at.fhj.masterthesis.erbg;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Main {

    private String filename;
    private String inputFilePath;
    private String outputFilePath;
    private String sessionId;

    private int zero=0;
    private int one=0;
    private int firstBytesDumpCounter=0;

    private int actRunZero=0;
    private int actRunOne=0;
    private int longestRunZero=0;
    private int longestRunOne=0;


    public static final String LOG_TAG = "ERBG_JAVA";
    public static final int FIRST_BYTE_DUMP_THRESHOLD = 4000;
    Log log=new Log(Log.INFO);

    public Main(String inputFilePath, String outputFilePath)  {
        setInputFilePath(inputFilePath);
        setOutputFilePath(outputFilePath);
        System.out.println(this.outputFilePath);

        try {
            generateFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkDistribution();
    }

    public static void main(String[] args) {
        if (args.length<1){
            System.out.println("usage: erbg_standalone <inputfilepath> [outputfilepath]");
            return;
        }
        String inputFilePath=args[0];
        String outputFilePath=null;
        if (args.length>1)
            outputFilePath=args[1];
        Main main = new Main(inputFilePath,outputFilePath);

    }


    /**
     * Getters & Setters
     *
     */

    /**
     *
     * @param inputFilePath
     */
    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    /**
     *
     * @param outputFilePath
     */
    public void setOutputFilePath(String outputFilePath) {
        if (outputFilePath==null)
            this.outputFilePath= System.getProperty("user.dir")+"/"+getFinalOutputFileName("javaStandalone");
        else
            this.outputFilePath = outputFilePath;
    }



    /**
     * From the given inputFilePath (i.e. the RAW file) the byte are reading in and handed over to for random extraction to 'byteArray2binary(byte[] input)'
     * @throws IOException
     */
    private void generateFromFile()throws IOException {
        log.d(LOG_TAG,"Start gathering Data from File");
        File file = new File(inputFilePath);
        InputStream ios = null;
        try {
            byte[] buffer = new byte[409600];
            ios = new FileInputStream(file);
            while (ios.read(buffer) != -1) {
                this.byteArray2binary(buffer);
            }
        }finally {
            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
                log.e(LOG_TAG,"Ups something with closing went wrong");
            }
        }
        log.d(LOG_TAG,"Finished data gathering from file");
    }


    /**
     * This is more or less the heart of the ERBG. It extracts zero or one by simply calculating 'byte modulo(2)' from any given byte array.
     * This Method is normally called by the method 'generateFromFile()'
     *
     * @param input
     */
    private void byteArray2binary(byte[] input){
        log.d(LOG_TAG,"There are " +input.length +" Bytes to process");
        StringBuilder sb = new StringBuilder();
        for (byte b:input) {
            if (firstBytesDumpCounter > FIRST_BYTE_DUMP_THRESHOLD) {
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
     * When all the data from an RAW file is processed this method is called and writes the given content from the StringBuilder to the given outputFilePath in append-mode
     * @param content
     */
    private void writeToFileExternal(StringBuilder content){
        File file = new File(outputFilePath);
        try {
            FileOutputStream f = new FileOutputStream(file,true);
            f.write(content.toString().getBytes());
            f.close();
            log.d(LOG_TAG,"File saved:" + outputFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.e(LOG_TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
            log.e(LOG_TAG,"Failed to save file" + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            log.e(LOG_TAG,"Failed to save file" + outputFilePath);
        }
    }

    /**
     * Reads a file from the given filepath and calculates the distribution of one' and zero's.
     *
     */
    public void checkDistribution() {
        String[] result = new String[10];

        double ratio = ((double) zero / (double) one) * 1.00000;
        int difference = (zero-one);
        int overall = zero+one;
        System.out.println("-------------------------------------------------");
        System.out.println("File:" + inputFilePath);
        System.out.println("Time:"+ sessionId);
        System.out.println("-------------------------------------------------");
        System.out.println("# Overall :\t\t"+Integer.toString(overall));
        System.out.println("# 0 : \t\t\t"+Integer.toString(zero));
        System.out.println("# 1 : \t\t\t"+Integer.toString(one));
        System.out.println("# Difference:\t\t"+Integer.toString(difference));
        System.out.println("% Diff to Overall\t"+Double.toString(((double)difference/overall)*100.00000));
        System.out.println("-------------------------------------------------");
        System.out.println("Distribution: \t\t"+Double.toString(ratio));
        System.out.println("-------------------------------------------------");
        System.out.println("# Longest Run 0: \t" + longestRunZero);
        System.out.println("# Longest Run 1: \t" + longestRunOne);
        System.out.println("#################################################\n\n");
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
        this.filename = "erbg_" + prefix + "_" + getSessionID() + ".data";
        return this.filename;
    }


    /**
     * Generates or gets the SessionID which is simply a timestamp.
     * This is needed for human readable purpose to connect the raw files with the generates random files.
     *
     * @return
     */
    private String getSessionID(){
        if (sessionId==null || sessionId==""){
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            this.sessionId=timeStamp;
        }
        return this.sessionId;
    }

}

class Log{

    public static int ERROR      = 0;
    public static int INFO       = 1;
    public static int DEBUG      = 2;
    public int logLevel;

    public Log(int logLevel) {
        this.logLevel = logLevel;
    }

    void i (String tag, String msg){
        if (logLevel>=Log.INFO)
            System.out.println("[INFO] "+tag+" : "+msg);
    }

    void d (String tag, String msg){
        if (logLevel>=Log.DEBUG)
            System.out.println("[DEBUG] "+tag+" : "+msg);
    }

    void e (String tag, String msg){
        if (logLevel>=Log.ERROR)
            System.out.println("[ERROR] "+tag+" : "+msg);
    }

}