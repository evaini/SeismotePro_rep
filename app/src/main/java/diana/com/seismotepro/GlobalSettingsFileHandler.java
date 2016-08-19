package diana.com.seismotepro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Diana Scurati on 08/02/2016.
 */
//==========================================================================
public class GlobalSettingsFileHandler {
    //==========================================================================

    private Settings UserSettings;

    private final static String AppFolderPath = getExternalStorageDirectory().getAbsolutePath() + "/Seismote/";
    private String ReservedFolder;
    private String GlobalSettingsFile;

    //==========================================================================
    public GlobalSettingsFileHandler(Settings us){
        //==========================================================================
        if(us != null)
            UserSettings = us;
        else
            UserSettings = new Settings();

        ReservedFolder = AppFolderPath + "Reserved/";
        GlobalSettingsFile = ReservedFolder + "app_settings.txt";
    }

    //==========================================================================
    public boolean Write_AppSettingsInfo(){
        //==========================================================================
        boolean res = false;
        File myFileFolder = new File(ReservedFolder);
        if (!(myFileFolder.exists())) {
            myFileFolder.mkdirs();
        }

        String StringToSave = "";
        FileOutputStream outputStream;
        File theFile = new File(GlobalSettingsFile);
        if(!theFile.exists()){ // se non esiste crea il file
            // costruisco la stringa da salvare
            if(UserSettings.isTransferModeEnabled)
                StringToSave = "TransferModeEnabled= \n" + "true" + "\n";
            else
                StringToSave = "TransferModeEnabled= \n" + "false" + "\n";

            StringToSave += "Signals: \n" +
                            get_EnabledSource_string() +
                            get_EnabledSignal_string() +
                            get_EnabledAxes_string();


            try {
                outputStream = new FileOutputStream(theFile, false); //true: append string to file(false:overwrite all)
                outputStream.write(StringToSave.getBytes());
                outputStream.close();
                res = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{//il file esiste già
            res = true;
        }
        return res;
    }

    //==========================================================================
    public Settings Read_AppSettingsInfo(){
        //==========================================================================

        File theFile = new File(GlobalSettingsFile);
        if (theFile.exists()) {
            // leggo tutto il file
            String line;
            List<String> records = new ArrayList<>();
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(theFile));
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
                if (records.get(i).equals("Signals: ")) {

                    // leggo i segnali che ho visualizzato durante l'acquisizione
                    String curString = records.get(i + 1);
                    String[] separated = curString.split("\t");
                    UserSettings.EnabledSource[0] = Integer.parseInt(separated[1]);
                    UserSettings.EnabledSource[1] = Integer.parseInt(separated[2]);
                    UserSettings.EnabledSource[2] = Integer.parseInt(separated[3]);
                    UserSettings.EnabledSource[3] = Integer.parseInt(separated[4]);

                    curString = records.get(i + 2);
                    separated = curString.split("\t");
                    UserSettings.EnabledSignal[0] = Integer.parseInt(separated[1]);
                    UserSettings.EnabledSignal[1] = Integer.parseInt(separated[2]);
                    UserSettings.EnabledSignal[2] = Integer.parseInt(separated[3]);
                    UserSettings.EnabledSignal[3] = Integer.parseInt(separated[4]);

                    curString = records.get(i + 3);
                    separated = curString.split("\t");
                    UserSettings.EnabledAxes[0] = Integer.parseInt(separated[1]);
                    UserSettings.EnabledAxes[1] = Integer.parseInt(separated[2]);
                    UserSettings.EnabledAxes[2] = Integer.parseInt(separated[3]);
                    UserSettings.EnabledAxes[3] = Integer.parseInt(separated[4]);

                    int j;
                    for(j=0;j<4;j++) {
                        if (UserSettings.EnabledSource[j] == 0) {//selez magic
                        }

                    }

                } else if(records.get(i).equals("TransferModeEnabled= ")){

                    if(records.get(i + 1).equals("true"))  // leggo il valore alla riga successiva
                        UserSettings.isTransferModeEnabled = true;
                    else
                        UserSettings.isTransferModeEnabled = false;
                }
            }

            return UserSettings;


        } else {//il file non esiste
            return null;
        }
    }

    //==========================================================================
    public boolean ChangeDefault(Settings NewSettings){
        //==========================================================================
        // cerca e elimina la linea da rimuovere all'interno di tutto il file
        File theFile = new File(GlobalSettingsFile);
        File tmp_file = new File(ReservedFolder + "tmp.txt");

        UserSettings = NewSettings;

        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(theFile));
            writer = new BufferedWriter(new FileWriter(tmp_file));

            if (theFile.exists()) {
                String currentLine;
                String newLine = "";

                while ((currentLine = reader.readLine()) != null) {
                    String[] separated = currentLine.split("\t");

                    // SOSTITUISCI QUESTE LINEE
                    if(separated[0].equals("source")) {
                        newLine = get_EnabledSource_string();
                        writer.write(newLine);
                    }else if(separated[0].equals("signal")) {
                        newLine = get_EnabledSignal_string();
                        writer.write(newLine);
                    }else if(separated[0].equals("axes")) {
                        newLine = get_EnabledAxes_string();
                        writer.write(newLine);

                    }else if(separated[0].equals("TransferModeEnabled= ")) {

                        newLine = "TransferModeEnabled= \n";
                        writer.write(newLine);

                        if(NewSettings.isTransferModeEnabled) {
                            newLine = "true" + "\n";
                            writer.write(newLine);
                        }

                        else {
                            newLine = "false" + "\n";
                            writer.write(newLine);
                        }

                        //skippo la prossima riga
                        currentLine = reader.readLine();

                    //MANTIENI LE ALTRE LINEE
                    }else {
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
        boolean successful = tmp_file.renameTo(theFile);
        return successful;
    }

    //==========================================================================
    private String get_EnabledSource_string(){
        //==========================================================================
        return ("source\t" +
                UserSettings.EnabledSource[0] + "\t" +  // 1 per ogni grafico
                UserSettings.EnabledSource[1] + "\t" +
                UserSettings.EnabledSource[2] + "\t" +
                UserSettings.EnabledSource[3] + "\n");
    }
    //==========================================================================
    private String get_EnabledSignal_string(){
        //==========================================================================
        return ("signal\t" +
                UserSettings.EnabledSignal[0] + "\t" +
                UserSettings.EnabledSignal[1] + "\t" +
                UserSettings.EnabledSignal[2] + "\t" +
                UserSettings.EnabledSignal[3] + "\n");
    }
    //==========================================================================
    private String get_EnabledAxes_string(){
        //==========================================================================
        return ("axes\t" +
                UserSettings.EnabledAxes[0] + "\t" +
                UserSettings.EnabledAxes[1] + "\t" +
                UserSettings.EnabledAxes[2] + "\t" +
                UserSettings.EnabledAxes[3] + "\n");
    }



}
