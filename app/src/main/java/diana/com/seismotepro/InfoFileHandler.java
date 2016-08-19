package diana.com.seismotepro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Diana Scurati on 27/01/2016.
 */
//==========================================================================
public class InfoFileHandler {
    //==========================================================================
    Settings UserSettings;
    Date Tstart_date;
    Date Tstop_date;
    long Tacquisition;
    String InfoFile;
    String SeismotePath;
    String SeismotePath_Decoded;

    //==========================================================================
    public InfoFileHandler(Settings sett) {
        //==========================================================================
        UserSettings = sett;
        SeismotePath = getExternalStorageDirectory().getAbsolutePath() + "/Seismote/";
        SeismotePath_Decoded = SeismotePath + "Decoded/";
    }


    // prima parte del file
    //==========================================================================
    public void StoreInfoFile(String AcqFileName, Date tstart, Date tstop, long time_acquisition) {
        //==========================================================================
        Tstart_date = tstart;
        Tstop_date = tstop;
        Tacquisition = time_acquisition;

        InfoFile = AcqFileName.substring(0, AcqFileName.length() - 4);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");   // per formattare tempi di inizio e fine registrazione
        BackgroundSave SaveInfoFile = new BackgroundSave("Decoded/" + InfoFile + "_inf", "txt");

        if (Tstart_date != null) {// è null se l'acquisizione non l'ha fatta il tablet e non si sa quando l'ha fatta
            SaveInfoFile.AddDataToString("Start of acquisition: \t" + formatter.format(Tstart_date).toString() + "\n");
        } else {
            SaveInfoFile.AddDataToString("Start of acquisition: \t" + "not available" + "\n");
        }
        if (Tstop_date != null) {
            SaveInfoFile.AddDataToString("Stop of acquisition: \t" + formatter.format(Tstop_date).toString() + "\n");
        } else {
            SaveInfoFile.AddDataToString("Stop of acquisition: \t" + "not available" + "\n");
        }

        SaveInfoFile.AddDataToString("Acquisition time: \t" + Tacquisition + " ms\n");

        SaveInfoFile.AddDataToString("Visualized signals: \n");
        SaveInfoFile.AddDataToString("source\t" + UserSettings.EnabledSource[0] + "\t" + UserSettings.EnabledSource[1] + "\t" + UserSettings.EnabledSource[2] + "\n");
        SaveInfoFile.AddDataToString("signal\t" + UserSettings.EnabledSignal[0] + "\t" + UserSettings.EnabledSignal[1] + "\t" + UserSettings.EnabledSignal[2] + "\n");
        SaveInfoFile.AddDataToString("axes\t" + UserSettings.EnabledAxes[0] + "\t" + UserSettings.EnabledAxes[1] + "\t" + UserSettings.EnabledAxes[2] + "\n");

        if(!UserSettings.PatientParam1.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientParam1 + "\n");
        }
        if(!UserSettings.PatientParam2.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientParam2 + "\n");
        }
        if(!UserSettings.PatientParam3.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientParam3 + "\n");
        }
        if(!UserSettings.PatientParam4.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientParam4 + "\n");
        }
        if(!UserSettings.PatientParam5.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientParam5 + "\n");
        }
        if(!UserSettings.PatientParam6.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientParam6 + "\n");
        }
        if(!UserSettings.PatientParam7.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientParam7 + "\n");
        }
        if(!UserSettings.PatientParam8.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientParam8 + "\n");
        }
        if(!UserSettings.PatientParam9.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientParam9 + "\n");
        }
        if(!UserSettings.PatientParam10.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientParam10 + "\n");
        }
        if(!UserSettings.PatientNOTES.equals("")){
            SaveInfoFile.AddDataToString(UserSettings.PatientNOTES + "\n");
        }




        SaveInfoFile.StoreTextData();
    }

    // per salvarli se non ci sono già
    //==========================================================================
    public void StoreTimes(String AcqFileName){
        //==========================================================================
        InfoFile = AcqFileName.substring(0, AcqFileName.length() - 4);
        BackgroundSave SaveInfoFile = new BackgroundSave("Decoded/" + InfoFile + "_inf", "txt");
        SaveInfoFile.AddDataToString(get_PEP_line());
        SaveInfoFile.AddDataToString(get_ICT_line());
        SaveInfoFile.AddDataToString(get_LVET_line());
        SaveInfoFile.AddDataToString(get_IRT_line());
        SaveInfoFile.AddDataToString(get_PTT_line());
        SaveInfoFile.AddDataToString(get_PAT_line());
        SaveInfoFile.AddDataToString(get_PR_line());
        SaveInfoFile.AddDataToString(get_QT_line());
        SaveInfoFile.AddDataToString(get_QRS_line());
        SaveInfoFile.AddDataToString(get_STI_line());
        SaveInfoFile.AddDataToString(get_TEI_line());
        SaveInfoFile.AddDataToString(get_QTC_line());
        //info dell'heart rate
        SaveInfoFile.AddDataToString(get_BEATS_line());


        SaveInfoFile.StoreTextData();
    }


    // vedi dal file info quali segnali sono stati visualizzati i fase di acquisizione
    //==========================================================================
    public Settings GetVisualizedSignals(String acquisition_file) {
        //==========================================================================
        String InfoFile = acquisition_file.substring(0, acquisition_file.length() - 4);
        InfoFile = "Decoded/" + InfoFile + "_inf.txt";
        UserSettings = new Settings();
        InfoFile = UserSettings.AppFolderPath + InfoFile;

        File thefile = new File(InfoFile);
        if (thefile.exists()) {
            // leggo tutto il file
            String line;
            List<String> records = new ArrayList<>();
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(InfoFile));
                // the readLine method returns null when there is nothing else to read.
                while ((line = bufferedReader.readLine()) != null) {
                    //legge dalla prima all'ultima riga
                    records.add(line);
                }
                bufferedReader.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            int i;
            for (i = 0; i < records.size(); i++) {
                if (records.get(i).equals("Visualized signals: ")) {

                    // leggo i segnali che ho visualizzato durante l'acquisizione
                    String curString = records.get(i + 1);
                    String[] separated = curString.split("\t");
                    UserSettings.EnabledSource[0] = Integer.parseInt(separated[1]);
                    UserSettings.EnabledSource[1] = Integer.parseInt(separated[2]);
                    UserSettings.EnabledSource[2] = Integer.parseInt(separated[3]);

                    curString = records.get(i + 2);
                    separated = curString.split("\t");
                    UserSettings.EnabledSignal[0] = Integer.parseInt(separated[1]);
                    UserSettings.EnabledSignal[1] = Integer.parseInt(separated[2]);
                    UserSettings.EnabledSignal[2] = Integer.parseInt(separated[3]);

                    curString = records.get(i + 3);
                    separated = curString.split("\t");
                    UserSettings.EnabledAxes[0] = Integer.parseInt(separated[1]);
                    UserSettings.EnabledAxes[1] = Integer.parseInt(separated[2]);
                    UserSettings.EnabledAxes[2] = Integer.parseInt(separated[3]);

                    break;
                }
            }

            return UserSettings;


        } else { //il file non esiste e viene da una registrazione non fatta dal tablet
            //thereWasInfoFile = false;
            return null;
        }
    }


    // vedo se ho già scritto le informazioni temporali
    //==========================================================================
    public boolean thereIsOldTimeInfo(String acquisition_file) {
        //==========================================================================
        boolean res = false;
        String InfoFile = acquisition_file.substring(0, acquisition_file.length() - 4);
        InfoFile = "Decoded/" + InfoFile + "_inf.txt";
        UserSettings = new Settings();
        InfoFile = UserSettings.AppFolderPath + InfoFile;

        File thefile = new File(InfoFile);
        if (thefile.exists()) {
            // leggo tutto il file
            String line;
            List<String> records = new ArrayList<>();
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(InfoFile));
                // the readLine method returns null when there is nothing else to read.
                while ((line = bufferedReader.readLine()) != null) {
                    //legge dalla prima all'ultima riga
                    records.add(line);
                }
                bufferedReader.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            int i;

            //cerco la parte che interessa
            for (i = 0; i < records.size(); i++) {
                line = records.get(i);
                String[] separated = line.split("\t");  // separo in base al tab
                if (separated[0].equals("PEP: ")) {
                    res = true; //ho trovato informazioni temporali che ho salvato precedentemente
                    break;
                }
            }
        }
        return res;
    }

    //==========================================================================
    public boolean OverwriteOldTimes(String acquisition_file){
        //==========================================================================
        // cerca e elimina la linea da rimuovere all'interno di tutto il file
        String InfoFile = acquisition_file.substring(0, acquisition_file.length() - 4);

        String tmp_file = "Decoded/" + InfoFile + "_tmp.txt";
        InfoFile = "Decoded/" + InfoFile + "_inf.txt";

        UserSettings = new Settings();
        InfoFile = UserSettings.AppFolderPath + InfoFile;
        tmp_file = UserSettings.AppFolderPath + tmp_file;

        File inputFile = new File(InfoFile);
        File tempFile = new File(tmp_file);

        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
            writer = new BufferedWriter(new FileWriter(tempFile));

            if (inputFile.exists()) {
                String currentLine;
                String newLine;

                while ((currentLine = reader.readLine()) != null) {
                    String[] separated = currentLine.split("\t");
                    // SOSTITUISCI LE LINEE CONTENENTI INFO TEMPORALI
                    if(separated[0].equals("PEP: ")){
                        newLine = get_PEP_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("ICT: ")) {
                        newLine = get_ICT_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("LVET: ")) {
                        newLine = get_LVET_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("IRT: ")) {
                        newLine = get_IRT_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("PTT: ")) {
                        newLine = get_PTT_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("PAT: ")) {
                        newLine = get_PAT_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("PR: ")) {
                        newLine = get_PR_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("QT: ")) {
                        newLine = get_QT_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("QRS: ")) {
                        newLine = get_QRS_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("STI: ")) {
                        newLine = get_STI_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("TEI: ")) {
                        newLine = get_TEI_line();
                        writer.write(newLine);
                    }else if(separated[0].equals("QTC: ")) {
                        newLine = get_QTC_line();
                        writer.write(newLine);

                        //MANTIENI LE LINEE NON CONTENENTI INFO TEMPORALI
                    }else  {
                        writer.write(currentLine + System.getProperty("line.separator"));
                    }
                }
                writer.close();
                reader.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean successful = tempFile.renameTo(inputFile);
        return successful;
    }

    //==========================================================================
    public boolean ReadTimes(String acquisition_file){
        //==========================================================================
        // cerca e elimina la linea da rimuovere all'interno di tutto il file
        String InfoFile = acquisition_file.substring(0, acquisition_file.length() - 4);

        InfoFile = "Decoded/" + InfoFile + "_inf.txt";

        UserSettings = new Settings();
        InfoFile = UserSettings.AppFolderPath + InfoFile;

        File inputFile = new File(InfoFile);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(inputFile));

            if (inputFile.exists()) {
                String currentLine;

                while ((currentLine = reader.readLine()) != null) {
                    String[] separated = currentLine.split("\t");
                    // SOSTITUISCI LE LINEE CONTENENTI INFO TEMPORALI
                    if(separated[0].equals("PEP: ")){
                        PEP = Double.parseDouble(separated[1]);
                        PEP_Start = Double.parseDouble(separated[2]);
                        PEP_Stop = Double.parseDouble(separated[3]);

                    }else if(separated[0].equals("ICT: ")) {
                        ICT = Double.parseDouble(separated[1]);
                        ICT_Start = Double.parseDouble(separated[2]);
                        ICT_Stop = Double.parseDouble(separated[3]);

                    }else if(separated[0].equals("LVET: ")) {
                        LVET = Double.parseDouble(separated[1]);
                        LVET_Start = Double.parseDouble(separated[2]);
                        LVET_Stop = Double.parseDouble(separated[3]);

                    }else if(separated[0].equals("IRT: ")) {
                        IRT = Double.parseDouble(separated[1]);
                        IRT_Start = Double.parseDouble(separated[2]);
                        IRT_Stop = Double.parseDouble(separated[3]);

                    }else if(separated[0].equals("PTT: ")) {
                        PTT = Double.parseDouble(separated[1]);
                        PTT_Start = Double.parseDouble(separated[2]);
                        PTT_Stop = Double.parseDouble(separated[3]);

                    }else if(separated[0].equals("PAT: ")) {
                        PAT = Double.parseDouble(separated[1]);
                        PAT_Start = Double.parseDouble(separated[2]);
                        PAT_Stop = Double.parseDouble(separated[3]);

                    }else if(separated[0].equals("PR: ")) {
                        PR = Double.parseDouble(separated[1]);
                        PR_Start = Double.parseDouble(separated[2]);
                        PR_Stop = Double.parseDouble(separated[3]);

                    }else if(separated[0].equals("QT: ")) {
                        QT = Double.parseDouble(separated[1]);
                        QT_Start = Double.parseDouble(separated[2]);
                        QT_Stop = Double.parseDouble(separated[3]);

                    }else if(separated[0].equals("QRS: ")) {
                        QRS = Double.parseDouble(separated[1]);
                        QRS_Start = Double.parseDouble(separated[2]);
                        QRS_Stop = Double.parseDouble(separated[3]);

                    }else if(separated[0].equals("STI: ")) {
                        STI = Double.parseDouble(separated[1]);

                    }else if(separated[0].equals("TEI: ")) {
                        TEI = Double.parseDouble(separated[1]);

                    }else if(separated[0].equals("QTC: ")) {
                        QTC = Double.parseDouble(separated[1]);

                    }else if(separated[0].equals("BeatNum: ")) {
                        NumberOfBeats = Integer.parseInt(separated[1]);
                        BPM = Integer.parseInt(separated[3]);
                        RR = Double.parseDouble(separated[5]);

                    }
                }
                reader.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    public double PEP = -1;
    public double PEP_Start = -1;
    public double PEP_Stop = -1;
    //==========================================================================
    public void put_PEP_times(double interval, double Tstart, double Tstop){
        PEP = interval;
        PEP_Start = Tstart;
        PEP_Stop = Tstop;
    }
    private String get_PEP_line(){
        return  "PEP: \t" + PEP + "\t" + PEP_Start + "\t" + PEP_Stop + "\n";
    }

    public double ICT = -1;
    public double ICT_Start = -1;
    public double ICT_Stop = -1;
    private String get_ICT_line(){ return  "ICT: \t" + ICT + "\t" + ICT_Start + "\t" + ICT_Stop  + "\n";}
    //==========================================================================
    public void put_ICT_times(double interval, double Tstart, double Tstop){
        ICT = interval;
        ICT_Start = Tstart;
        ICT_Stop = Tstop;
    }

    public double LVET = -1;
    public double LVET_Start = -1;
    public double LVET_Stop = -1;
    private String get_LVET_line(){ return  "LVET: \t" + LVET + "\t" + LVET_Start + "\t" + LVET_Stop + "\n";}
    //==========================================================================
    public void put_LVET_times(double interval, double Tstart, double Tstop){
        LVET = interval;
        LVET_Start = Tstart;
        LVET_Stop = Tstop;
    }

    public double IRT = -1;
    public double IRT_Start = -1;
    public double IRT_Stop = -1;
    private String get_IRT_line(){ return  "IRT: \t" + IRT + "\t" + IRT_Start + "\t" + IRT_Stop  + "\n";}
    //==========================================================================
    public void put_IRT_times(double interval, double Tstart, double Tstop){
        IRT = interval;
        IRT_Start = Tstart;
        IRT_Stop = Tstop;
    }

    public double PTT = -1;
    public double PTT_Start = -1;
    public double PTT_Stop = -1;
    private String get_PTT_line(){ return  "PTT: \t" + PTT + "\t" + PTT_Start + "\t" + PTT_Stop  + "\n";}
    //==========================================================================
    public void put_PTT_times(double interval, double Tstart, double Tstop){
        PTT = interval;
        PTT_Start = Tstart;
        PTT_Stop = Tstop;
    }

    public double PAT = -1;
    public double PAT_Start = -1;
    public double PAT_Stop = -1;
    private String get_PAT_line(){ return  "PAT: \t" + PAT + "\t" + PAT_Start + "\t" + PAT_Stop  + "\n";}
    //==========================================================================
    public void put_PAT_times(double interval, double Tstart, double Tstop){
        PAT = interval;
        PAT_Start = Tstart;
        PAT_Stop = Tstop;
    }

    public double PR = -1;
    public double PR_Start = -1;
    public double PR_Stop = -1;
    private String get_PR_line(){ return  "PR: \t" + PR + "\t" + PR_Start + "\t" + PR_Stop  + "\n";}
    //==========================================================================
    public void put_PR_times(double interval, double Tstart, double Tstop){
        PR = interval;
        PR_Start = Tstart;
        PR_Stop = Tstop;
    }

    public double QT = -1;
    public double QT_Start = -1;
    public double QT_Stop = -1;
    private String get_QT_line(){ return  "QT: \t" + QT + "\t" + QT_Start + "\t" + QT_Stop  + "\n";}
    //==========================================================================
    public void put_QT_times(double interval, double Tstart, double Tstop){
        QT = interval;
        QT_Start = Tstart;
        QT_Stop = Tstop;
    }

    public double QRS = -1;
    public double QRS_Start = -1;
    public double QRS_Stop = -1;
    private String get_QRS_line(){ return  "QRS: \t" + QRS + "\t" + QRS_Start + "\t" + QRS_Stop  + "\n";}
    //==========================================================================
    public void put_QRS_times(double interval, double Tstart, double Tstop){
        QRS = interval;
        QRS_Start = Tstart;
        QRS_Stop = Tstop;
    }

    public double STI = -1;
    private String get_STI_line(){ return  "STI: \t" + STI + "\n";}
    //==========================================================================
    public void put_STI_times(double interval){
        STI = interval;
    }

    public double TEI = -1;
    private String get_TEI_line(){ return  "TEI: \t" + TEI + "\n";}
    //==========================================================================
    public void put_TEI_times(double interval){
        TEI = interval;
    }

    public double QTC = -1;
    private String get_QTC_line(){ return  "QTC: \t" + QTC + "\n";}
    //==========================================================================
    public void put_QTC_times(double interval){
        QTC = interval;
    }

    public double RR = -1;
    public int BPM = -1;
    public int NumberOfBeats = -1;
    //==========================================================================
    public void put_BeatsInfo(double rr_ms, int bpm, int number_of_beats){
        //==========================================================================
        RR = rr_ms;
        BPM = bpm;
        NumberOfBeats = number_of_beats;
    }
    private String get_BEATS_line(){ return  "BeatNum: \t" + NumberOfBeats + "\t" + "BPM: " + "\t" + BPM + "\t" + "RR: \t" + RR + "\n";}


    //==========================================================================
    public String get_signal1_filename(String AcqFile){
        return  AcqFile.substring(0, AcqFile.length() - 4) + "_SIGNAL_1";
    }
    //==========================================================================
    public String get_signal2_filename(String AcqFile){
        return  AcqFile.substring(0, AcqFile.length() - 4) + "_SIGNAL_2";
    }
    //==========================================================================
    public String get_signal3_filename(String AcqFile){
        return  AcqFile.substring(0, AcqFile.length() - 4) + "_SIGNAL_3";
    }

    // ritorna vero se ci sono i file di tutti e tre i segnali medi
    //==========================================================================
    public boolean CheckOldAnalysis(String AcqFile){
        //==========================================================================
        boolean res = false;
        File Signal1_file = new File(SeismotePath_Decoded + get_signal1_filename(AcqFile) + ".wave");
        File Signal2_file = new File(SeismotePath_Decoded + get_signal2_filename(AcqFile) + ".wave");
        File Signal3_file = new File(SeismotePath_Decoded + get_signal3_filename(AcqFile) + ".wave");
        if(Signal1_file.exists() && Signal2_file.exists() && Signal3_file.exists()){
            res = true;
        }
        return res;
    }


}
