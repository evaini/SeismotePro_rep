package diana.com.seismotepro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Diana Scurati on 29/01/2016.
 */
public class HeartIntervals implements Serializable {

    public final static String ColorCode_Green =    "#FFFF5D4B";
    public final static String ColorCode_Yellow =   "#FFFFFE54";
    public final static String ColorCode_Red =      "#FF88FF44";

    private final static String AppFolderPath = getExternalStorageDirectory().getAbsolutePath() + "/Seismote/";
    private String TimesFile;
    private String ReservedFolder;


    public HeartIntervals(){
        ReservedFolder = AppFolderPath + "Reserved/";
        TimesFile = ReservedFolder + "times.txt";
    }



    //**************************************
    // PEP (PRE EJECTION PERIOD)
    //**************************************
    public double PEP_InfLim_Yellow = 0;
    public double PEP_InfLim_Green = 0;
    public double PEP_SupLim_Green = 0;
    public double PEP_SupLim_Yellow = 0;


    //**************************************
    // ICT (ISOVOLUMIC CONTRACTION TIME)
    //**************************************
    public double ICT_InfLim_Yellow = 0;
    public double ICT_InfLim_Green = 0;
    public double ICT_SupLim_Green = 0;
    public double ICT_SupLim_Yellow = 0;


    //**************************************
    // LVET (LEFT VENTRICULAR EJECTION TIME)
    //**************************************
    public double LVET_InfLim_Yellow = 0;
    public double LVET_InfLim_Green = 0;
    public double LVET_SupLim_Green = 0;
    public double LVET_SupLim_Yellow = 0;


    //**************************************
    // IRT (ISOVOLUMIC RELAXATION TIME)
    //**************************************
    public double IRT_InfLim_Yellow = 0;
    public double IRT_InfLim_Green = 0;
    public double IRT_SupLim_Green = 0;
    public double IRT_SupLim_Yellow = 0;


    //**************************************
    // PTT (PULSE TRANSIT TIME)
    //**************************************
    public double PTT_InfLim_Yellow = 0;
    public double PTT_InfLim_Green = 0;
    public double PTT_SupLim_Green = 0;
    public double PTT_SupLim_Yellow = 0;


    //**************************************
    // PAT (PULSE ARRIVAL TIME)
    //**************************************
    public double PAT_InfLim_Yellow = 0;
    public double PAT_InfLim_Green = 0;
    public double PAT_SupLim_Green = 0;
    public double PAT_SupLim_Yellow = 0;


    //**************************************
    // PR ( INTERVALLO P-R)
    //**************************************
    public double PR_InfLim_Yellow = 0;
    public double PR_InfLim_Green = 0;
    public double PR_SupLim_Green = 0;
    public double PR_SupLim_Yellow = 0;


    //**************************************
    // QT (INTERVALLO Q-T)
    //**************************************
    public double QT_InfLim_Yellow = 0;
    public double QT_InfLim_Green = 0;
    public double QT_SupLim_Green = 0;
    public double QT_SupLim_Yellow = 0;


    //**************************************
    // QRS (INTERVALLO Q-R-S)
    //**************************************
    public double QRS_InfLim_Yellow = 0;
    public double QRS_InfLim_Green = 0;
    public double QRS_SupLim_Green = 0;
    public double QRS_SupLim_Yellow = 0;


    //**************************************
    // QTC (QT CORRECTED)
    //**************************************
    public double QTC_InfLim_Yellow = 0;
    public double QTC_InfLim_Green = 0;
    public double QTC_SupLim_Green = 0.42;
    public double QTC_SupLim_Yellow = 0.42;

    public String CheckRange_QTC(double interval){
        return CheckRange(interval, QTC_InfLim_Yellow, QTC_InfLim_Green, QTC_SupLim_Green, QTC_SupLim_Yellow);
    }


    //**************************************
    // TEI INDEX (MYOCARDICAL PERFORMANCE INDEX) = (ICT+IRT)/LVET
    //**************************************
    public double TEI_InfLim_Yellow = 0.34;
    public double TEI_InfLim_Green = 0.34;
    public double TEI_SupLim_Green = 0.44;
    public double TEI_SupLim_Yellow = 0.49;

    public String CheckRange_TEI(double interval){
        return CheckRange(interval, TEI_InfLim_Yellow, TEI_InfLim_Green, TEI_SupLim_Green, TEI_SupLim_Yellow);
    }


    //**************************************
    // STI RATIO (SISTOLIC TIME INDEX) = PEP/LVET
    //**************************************
    public double STI_InfLim_Yellow = 0.34;
    public double STI_InfLim_Green = 0.34;
    public double STI_SupLim_Green = 0.41;
    public double STI_SupLim_Yellow = 0.41;

    public String CheckRange_STI(double interval){
        return CheckRange(interval, STI_InfLim_Yellow, STI_InfLim_Green, STI_SupLim_Green, STI_SupLim_Yellow);
    }



    //==========================================================================
    private String CheckRange(double interval, double InfLim_Yellow, double InfLim_Green, double SupLim_Green, double SupLim_Yellow ){
        //==========================================================================

        //..._____________||_____________________||_______________________||______________________||__________________________...
        //       Red               Yellow                  Green                   Yellow                     Red
        //          InfLim_Yellow           InfLim_Green            SupLim_Green            SupLim_Yellow
        // ...____________||_____________________||_______________________||______________________||__________________________...

        String area_color = "";
        if(interval < InfLim_Yellow){
            area_color = ColorCode_Red;
        }else if(interval < InfLim_Green){
            area_color = ColorCode_Green;
        }else if(interval < SupLim_Green){
            area_color = ColorCode_Green;
        }else if(interval < SupLim_Yellow){
            area_color = ColorCode_Yellow;
        }else{
            area_color = ColorCode_Red;
        }
        return area_color;
    }

    //==========================================================================
    public boolean CreateFileWithTimes(){
        //==========================================================================
        FileOutputStream outputStream;
        String StringToSave = "";
        boolean res = false;

        File myFileFolder = new File(ReservedFolder);
        if (!(myFileFolder.exists())) {
            myFileFolder.mkdirs();
        }

        File timesFile = new File(TimesFile);
        //if(!timesFile.exists()){ // se non esiste crea il file
            // costruisco la stringa da salvare
            StringToSave = "Interval" + "\t" + "InfLim_Yellow" + "\t" + "InfLim_Green" + "\t" +"SupLim_Green" + "\t" +"SupLim_Yellow" + "\n" +
            "PEP: " + "\t" + PEP_InfLim_Yellow + "\t" + PEP_InfLim_Green + "\t" + PEP_SupLim_Green + "\t" + PEP_SupLim_Yellow + "\n" +
            "ICT: " + "\t" + ICT_InfLim_Yellow + "\t" + ICT_InfLim_Green + "\t" + ICT_SupLim_Green + "\t" + ICT_SupLim_Yellow + "\n" +
            "LVET: " + "\t" + LVET_InfLim_Yellow + "\t" + LVET_InfLim_Green + "\t" + LVET_SupLim_Green + "\t" + LVET_SupLim_Yellow + "\n" +
            "IRT: " + "\t" + IRT_InfLim_Yellow + "\t" + IRT_InfLim_Green + "\t" + IRT_SupLim_Green + "\t" + IRT_SupLim_Yellow + "\n" +

            "PTT: " + "\t" + PTT_InfLim_Yellow + "\t" + PTT_InfLim_Green + "\t" + PTT_SupLim_Green + "\t" + PTT_SupLim_Yellow + "\n" +
            "PAT: " + "\t" + PAT_InfLim_Yellow + "\t" + PAT_InfLim_Green + "\t" + PAT_SupLim_Green + "\t" + PAT_SupLim_Yellow + "\n" +

            "PR: " + "\t" + PR_InfLim_Yellow + "\t" + PR_InfLim_Green + "\t" + PR_SupLim_Green + "\t" + PR_SupLim_Yellow + "\n" +
            "QT: " + "\t" + QT_InfLim_Yellow + "\t" + QT_InfLim_Green + "\t" + QT_SupLim_Green + "\t" + QT_SupLim_Yellow + "\n" +
            "QRS: " + "\t" + QRS_InfLim_Yellow + "\t" + QRS_InfLim_Green + "\t" + QRS_SupLim_Green + "\t" + QRS_SupLim_Yellow + "\n" +

            "STI: " + "\t" + STI_InfLim_Yellow + "\t" + STI_InfLim_Green + "\t" + STI_SupLim_Green + "\t" + STI_SupLim_Yellow + "\n" +
            "TEI: " + "\t" + TEI_InfLim_Yellow + "\t" + TEI_InfLim_Green + "\t" + TEI_SupLim_Green + "\t" + TEI_SupLim_Yellow + "\n" +
            "QTC: " + "\t" + QTC_InfLim_Yellow + "\t" + QTC_InfLim_Green + "\t" + QTC_SupLim_Green + "\t" + QTC_SupLim_Yellow + "\n" ;


            try {
                outputStream = new FileOutputStream(TimesFile, false); //true: append string to file(false:overwrite all)
                outputStream.write(StringToSave.getBytes());
                outputStream.close();
                res = true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        /*}else{
            res = true;
        }*/
        return res;
    }

    // ritorna vero se il file esiste già(e salva i valori nelle variabili), altrimenti ritorna falso
    //==========================================================================
    public boolean ReadFileWithTimes(){
        //==========================================================================
        FileOutputStream outputStream;
        String StringToSave = "";
        boolean res = false;

        File inputFile = new File(TimesFile);
        BufferedReader reader = null;

        File myFileFolder = new File(ReservedFolder);
        if (!(myFileFolder.exists())) {
            myFileFolder.mkdirs();
        }

        try {

            if (inputFile.exists()) {

                reader = new BufferedReader(new FileReader(inputFile));

                String currentLine;
                res = true; // il file esiste

                while ((currentLine = reader.readLine()) != null) {
                    String[] separated = currentLine.split("\t");
                    // SOSTITUISCI LE INFO TEMPORALI
                    try {
                        if (separated[0].equals("PEP: ")) {
                            PEP_InfLim_Yellow = Double.parseDouble(separated[1]);
                            PEP_InfLim_Green = Double.parseDouble(separated[2]);
                            PEP_SupLim_Green = Double.parseDouble(separated[3]);
                            PEP_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("ICT: ")) {
                            ICT_InfLim_Yellow = Double.parseDouble(separated[1]);
                            ICT_InfLim_Green = Double.parseDouble(separated[2]);
                            ICT_SupLim_Green = Double.parseDouble(separated[3]);
                            ICT_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("LVET: ")) {
                            LVET_InfLim_Yellow = Double.parseDouble(separated[1]);
                            LVET_InfLim_Green = Double.parseDouble(separated[2]);
                            LVET_SupLim_Green = Double.parseDouble(separated[3]);
                            LVET_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("IRT: ")) {
                            IRT_InfLim_Yellow = Double.parseDouble(separated[1]);
                            IRT_InfLim_Green = Double.parseDouble(separated[2]);
                            IRT_SupLim_Green = Double.parseDouble(separated[3]);
                            IRT_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("PTT: ")) {
                            PTT_InfLim_Yellow = Double.parseDouble(separated[1]);
                            PTT_InfLim_Green = Double.parseDouble(separated[2]);
                            PTT_SupLim_Green = Double.parseDouble(separated[3]);
                            PTT_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("PAT: ")) {
                            PAT_InfLim_Yellow = Double.parseDouble(separated[1]);
                            PAT_InfLim_Green = Double.parseDouble(separated[2]);
                            PAT_SupLim_Green = Double.parseDouble(separated[3]);
                            PAT_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("PR: ")) {
                            PR_InfLim_Yellow = Double.parseDouble(separated[1]);
                            PR_InfLim_Green = Double.parseDouble(separated[2]);
                            PR_SupLim_Green = Double.parseDouble(separated[3]);
                            PR_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("QT: ")) {
                            QT_InfLim_Yellow = Double.parseDouble(separated[1]);
                            QT_InfLim_Green = Double.parseDouble(separated[2]);
                            QT_SupLim_Green = Double.parseDouble(separated[3]);
                            QT_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("QRS: ")) {
                            QRS_InfLim_Yellow = Double.parseDouble(separated[1]);
                            QRS_InfLim_Green = Double.parseDouble(separated[2]);
                            QRS_SupLim_Green = Double.parseDouble(separated[3]);
                            QRS_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("STI: ")) {
                            STI_InfLim_Yellow = Double.parseDouble(separated[1]);
                            STI_InfLim_Green = Double.parseDouble(separated[2]);
                            STI_SupLim_Green = Double.parseDouble(separated[3]);
                            STI_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("TEI: ")) {
                            TEI_InfLim_Yellow = Double.parseDouble(separated[1]);
                            TEI_InfLim_Green = Double.parseDouble(separated[2]);
                            TEI_SupLim_Green = Double.parseDouble(separated[3]);
                            TEI_SupLim_Yellow = Double.parseDouble(separated[4]);

                        } else if (separated[0].equals("QTC: ")) {
                            QTC_InfLim_Yellow = Double.parseDouble(separated[1]);
                            QTC_InfLim_Green = Double.parseDouble(separated[2]);
                            QTC_SupLim_Green = Double.parseDouble(separated[3]);
                            QTC_SupLim_Yellow = Double.parseDouble(separated[4]);

                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                reader.close();
            }else{
                res = false; //il file non esiste
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }



}













