package diana.com.seismotepro;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Diana Scurati on 10/11/2015.
 */
//==========================================================================
public class BackgroundSave {
    //==========================================================================

    private byte BufToSave[];
    private int  BufDim;

    private String StringToSave = "";


    private String FileName;// = "data";
    private final static String FolderPath = getExternalStorageDirectory().getAbsolutePath() + "/Seismote/";
    private String FilePath;// =FolderPath + FileName + ".raw";
    private String TextFilePath =FolderPath + FileName + ".txt";

    String FileFormat;

    private boolean isBusy = false;

    private FileOutputStream BinaryOutputStream = null;
    private BufferedOutputStream bin_out = null;


    //costruttore
    //==========================================================================
    public BackgroundSave(String filename, String file_format){
        //==========================================================================
        FileName = filename;
        FileFormat = file_format;   // "raw" per dati provenienti da bt, "wave" per segnali mediati
        if(FileFormat.equals("raw"))
            FilePath = FolderPath + FileName + ".raw";
        else if(FileFormat.equals("wave"))
            FilePath = FolderPath + FileName + ".wave";
        else if(FileFormat.equals("txt"))
            FilePath = FolderPath + FileName + ".txt";

    }

    // per salvataggio dati NON in formato binario
    //==========================================================================
    public void OpenFile(){
        //==========================================================================
        // crea la cartella se non esiste
        File myFileFolder = new File(FolderPath);
        if (!(myFileFolder.exists())) {
            myFileFolder.mkdirs();
        }
        try {
            if(!FileFormat.equals("txt")) {
                // apre il file
                BinaryOutputStream = new FileOutputStream(FilePath, true); //true: append to file
                bin_out = new BufferedOutputStream(BinaryOutputStream);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //==========================================================================
    public void CloseFile() {
        //==========================================================================
        try {
            bin_out.flush();
            BinaryOutputStream.close(); //close binary file
        } catch (IOException e) {
            e.printStackTrace();
        }
        BinaryOutputStream = null;
        bin_out = null;

    }
    // salva buffer di byte
    //==========================================================================
    public void StoreData(byte[] buff) {
        //==========================================================================
        isBusy = true;

        if(BinaryOutputStream == null || bin_out == null){
            OpenFile();
        }
        BinaryStorer storeThis = new BinaryStorer(buff, buff.length, bin_out);
        storeThis.execute();
    }
    // salva buffer di long( per i dati decodificati in fase di analisi
    //==========================================================================
    public void StoreData(int[] buff) {
        //==========================================================================
        isBusy = true;

        if(BinaryOutputStream == null || bin_out == null){
            OpenFile();
        }
        int i;
        int j=0;
        byte[] byte_buff = new byte[buff.length*4];

        for(i=0; i<(byte_buff.length-3); i+=4) {
            byte_buff[i] =   (byte)((buff[j] & 0xff000000) >> 24);
            byte_buff[i+1] = (byte)((buff[j] & 0x00ff0000) >> 16);
            byte_buff[i+2] = (byte)((buff[j] & 0x0000ff00) >> 8);
            byte_buff[i+3] = (byte)((buff[j++] & 0x000000ff) );
        }
        BinaryStorer storeThis = new BinaryStorer(byte_buff, byte_buff.length, bin_out);
        storeThis.execute();
        /*isBusy = true;

        if(BinaryOutputStream == null || bin_out == null){
            OpenFile();
        }
        int i;
        ByteBuffer byte_buff = ByteBuffer.allocate(buff.length*Integer.SIZE/8); //Integer.SIZE = 32 bit

        for(i=0;i<buff.length;i++) {
            byte_buff.putInt(buff[i]);
        }
        BinaryStorer storeThis = new BinaryStorer(byte_buff.array(), byte_buff.capacity(), bin_out);
        storeThis.execute();*/
    }

    // classe asynctask
    //==========================================================================
    //==========================================================================
    //==========================================================================
    private class BinaryStorer extends AsyncTask<Void, Boolean, Boolean> {
        //==========================================================================
        //==========================================================================
        //==========================================================================


        BufferedOutputStream bin_out_local;
        //==========================================================================
        public BinaryStorer (byte[] buftosave, int bufdim, BufferedOutputStream bos){
            //==========================================================================
            BufToSave = buftosave;
            BufDim = bufdim;
            bin_out_local = bos;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        //==========================================================================
        protected Boolean doInBackground(Void... params) {
            //==========================================================================
            boolean outcome = false; // vero: tutto ok, falso: salvataggio non andato bene
            // crea il file se non esiste
            /*File myFileFolder = new File(FolderPath);
            if (!(myFileFolder.exists())) {
                myFileFolder.mkdirs();
            }*/

            // open binary file
            try {
                /* spostato nel open file
                BinaryOutputStream = new FileOutputStream(FilePath, true); //true: append to file
                bin_out = new BufferedOutputStream(BinaryOutputStream);*/

                bin_out_local.write(BufToSave, 0, BufDim);
                bin_out_local.flush();
                /* spostato nel close file
                bin_out.flush();
                BinaryOutputStream.close(); //close binary file
*/
                outcome = true;
            } catch (IOException e) {
                e.printStackTrace();
                outcome = false;
            }
            return outcome;
        }

        @Override
        //==========================================================================
        protected void onPostExecute(Boolean outcome){
            //==========================================================================
            isBusy = false;
            if(outcome == true){
                // salvataggio ok
            }else{
                //errore di salvataggio
            }
        }


    }//fine classe mystorer





    //*****************************************************************
    // salvataggio di dati in formato testo
    //*****************************************************************
    //==========================================================================
    public void AddDataToString(String to_add) {
        //==========================================================================
        StringToSave += to_add;
    }
    //==========================================================================
    public void StoreTextData() {
        //==========================================================================
        TextStorer storeThisString = new TextStorer(FilePath, StringToSave, StringToSave.length());
        storeThisString.execute();
    }
    // classe asynctask
    //==========================================================================
    //==========================================================================
    //==========================================================================
    private class TextStorer extends AsyncTask<Void, Boolean, Boolean> {
        //==========================================================================
        //==========================================================================
        //==========================================================================
        String ThisFileName;

        //==========================================================================
        public TextStorer (String filename, String string_to_save, int bufdim){
            //==========================================================================
            StringToSave = string_to_save;
            BufDim = bufdim;
            ThisFileName = filename;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        //==========================================================================
        protected Boolean doInBackground(Void... params) {
            //==========================================================================
            boolean outcome = false; // vero: tutto ok, falso: salvataggio non andato bene
            FileOutputStream outputStream;

            try {
                outputStream = new FileOutputStream(ThisFileName, true); //true: append string to file (non-binary file)
                outputStream.write(StringToSave.getBytes());
                outputStream.close();
                outcome = true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return outcome;
        }

        @Override
        //==========================================================================
        protected void onPostExecute(Boolean outcome){
            //==========================================================================
            isBusy = false;
            if(outcome == true){
                // salvataggio ok
            }else{
                //errore di salvataggio
            }
        }


    }//fine classe textstorer

    //==========================================================================
    public boolean get_isBusy(){
        //==========================================================================
        return isBusy;
    }

    public String get_FileName(){return FileName;}
    public void put_FileName(String newName){
        FileName = newName;   }

}
