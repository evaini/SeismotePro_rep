package diana.com.seismotepro;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Diana Scurati on 03/12/2015.
 */
//==========================================================================
public class Settings implements Serializable {
    //==========================================================================
    // classe da passare tra activity 1 e activity_Settings per impostare le caratteristiche del grafico e del salvataggio

    private byte NumOfPlot = 3; // numero dei plot da rapresentare a video, default=3

    public String FileName;
    public final static String AppFolderPath = getExternalStorageDirectory().getAbsolutePath() + "/Seismote/";

    public MagicSystem[] MyMagic;
    int i;  //indice di supporto


    // 1 per ogni grafico
    // default: GRAPH1 = ECG
    //          GRAPH2 = SISMO: MOTE5 ACC 1 Z AXES
    //          GRAPH3 = PPG: MOTE5 PPG_IR


    /*public int[] EnabledSource ={ 0, 2, 3};   // 0=magic, 1-5 motes
    public int[] EnabledSignal ={ 1, 0, 5}; // PER MAGIC--> 0=ACC, 1=ECG, 2=RESP
                                             //PER MOTES--> 0=ACC1, 1=ACC2, 2=ACC3, 3=ACC4, 4=ACC_MEAN, 5=PPG
    public int[] EnabledAxes ={ -1, 2, 1};    // PER ACC --> 0=X, 1=Y, 2=Z
                                              // PER PPG-->  0=RED, 1=IR*/
    // mod 15 feb 2016
    public int[] EnabledSource ={ 0, 1, 2, 3};   // 0=magic, 1-5 motes
    public int[] EnabledSignal ={ 1, 0, 0, 5}; // PER MAGIC--> 0=ACC, 1=ECG, 2=RESP
                                                //PER MOTES--> 0=ACC1, 1=ACC2, 2=ACC3, 3=ACC4, 4=ACC_MEAN, 5=PPG
    public int[] EnabledAxes ={ -1, 2, 2, 1};    // PER ACC --> 0=X, 1=Y, 2=Z
                                                // PER PPG-->  0=RED, 1=IR

    public boolean DeveloperMode = false;

    public static final int Max_T_Rec = 15 * 60;     // 15 min * 60 sec

    public String PatientParam1 = "";
    public String PatientParam2 = "";
    public String PatientParam3 = "";
    public String PatientParam4 = "";
    public String PatientParam5 = "";
    public String PatientParam6 = "";
    public String PatientParam7 = "";
    public String PatientParam8 = "";
    public String PatientParam9 = "";
    public String PatientParam10 = "";
    public String PatientNOTES = "";

    public boolean isTransferModeEnabled = false;// ABILITA LA POSSIBILITà DI TRASFERIRE I FILE DI REGISTRAZIONE NELL'ATTIVITà 3



    //==========================================================================
    public Settings(){
        //==========================================================================
        // inizializzo nome del file
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        Date now = new Date();
        FileName = formatter.format(now).toString();

        InitMagicPlot();
    }



    //==========================================================================
    public void ClearMagicPlot() {
        //==========================================================================
        MyMagic = new MagicSystem[4];//4 plot
        MyMagic[0] = new MagicSystem(); // 1 per ogni plot
        MyMagic[1] = new MagicSystem();
        MyMagic[2] = new MagicSystem();
        MyMagic[3] = new MagicSystem();
    }
    //==========================================================================
    public void InitMagicPlot(){
        //==========================================================================
        ClearMagicPlot();
        // inizializzo con segnali di default da plottare:

        // inizializzazione per prendere scg e pleti da mote 5
        /*MyMagic[0].MiniMagic.Ecg0 = 1;
        MyMagic[1].Motes[4].Acc[0].axes[2] = 2; // mote 5 acc 1 asse z su graph 2
        MyMagic[2].Motes[4].PPG.source[1] = 3;  //mote 5 ppg ir*/

        MyMagic[0].MiniMagic.Ecg0 = 1;
        MyMagic[1].Motes[0].Acc[0].axes[2] = 2; // mote 1 acc mean asse z su graph 2
        MyMagic[2].Motes[1].Acc[0].axes[2] = 3; // mote 2 acc mean asse z su graph 3
        MyMagic[3].Motes[2].PPG.source[1] = 4;  //mote 3 ppg ir su graph 4
    }

    //==========================================================================
    public class MagicSystem implements Serializable {
        //==========================================================================
        protected Mote[] Motes;
        protected Magic MiniMagic;
        public MagicSystem(){
            Motes = new Mote[5];
            MiniMagic = new Magic();

            Motes[0] = new Mote();
            Motes[1] = new Mote();
            Motes[2] = new Mote();
            Motes[3] = new Mote();
            Motes[4] = new Mote();
        }
    }
    //==========================================================================
    public class Magic implements Serializable {
        //==========================================================================
        protected int Ecg0 = 0;
        protected int Resp0 = 0;
        protected sample3axes Acc;

        public Magic(){
            Acc = new sample3axes();
        }
    }
    //==========================================================================
    public class Mote implements Serializable {
        //==========================================================================
        protected sample3axes[] Acc;
        protected ppg PPG;

        public Mote(){
            Acc = new sample3axes[5];   // 5 accelerometri per ciascun mote
            PPG = new ppg();            // 1 pletismografo per ogni mote
            for (i=0; i<5;i++)
                Acc[i] = new sample3axes();
        }
    }
    //==========================================================================
    public class sample3axes implements Serializable {
        //==========================================================================
        protected  byte[] axes;
        public sample3axes(){
            axes = new byte[3]; // 0=x  1=y 2=z
        }
    }
    //==========================================================================
    public class ppg implements Serializable {
        //==========================================================================
        protected byte[] source;
        public ppg(){
            source = new byte[3]; // 0 = red, 1 = ir, 2= mean
        }
    }
    //==========================================================================
    public String get_SignalName(int GraphNum, boolean withLineBreak){
        //==========================================================================
        // graph num = 0, 1, 2, 3
        String SignalName = "";
        //magic
        if(EnabledSource[GraphNum] == 0) {
            if(withLineBreak)
                SignalName = "Magic\n";
            else
                SignalName = "Magic ";
            if(EnabledSignal[GraphNum] == 0){//accelerometro della magic
                if(EnabledAxes[GraphNum] == 0)
                    SignalName += " Acc X";
                if(EnabledAxes[GraphNum] == 1)
                    SignalName += " Acc Y";
                if(EnabledAxes[GraphNum] == 2)
                    SignalName += " Acc Z";
            } else if(EnabledSignal[GraphNum] == 1) {  // ecg
               // SignalName += " ECG";
                SignalName = "ECG";
            } else if(EnabledSignal[GraphNum] == 2) {  // resp
                SignalName += " Resp";
            }

            //motes
        }else if(EnabledSource[GraphNum] != -1 && EnabledSource[GraphNum] <=5) {
            if(withLineBreak)
                SignalName = "Mote" + EnabledSource[GraphNum] + "\n";
            else
                SignalName = "Mote" + EnabledSource[GraphNum] + " ";

            if(EnabledSignal[GraphNum] == 0 ||
                    EnabledSignal[GraphNum] == 1 ||
                    EnabledSignal[GraphNum] == 2 ||
                    EnabledSignal[GraphNum] == 3 ||
                    EnabledSignal[GraphNum] == 4 ){   // accelerometri del mote

                if(EnabledSignal[GraphNum] == 4)
                    SignalName += " Acc mean";
                else
                    SignalName += " Acc" + (EnabledSignal[GraphNum]+1);

                if(EnabledAxes[GraphNum] == 0)
                    SignalName += " X";
                if(EnabledAxes[GraphNum] == 1)
                    SignalName += " Y";
                if(EnabledAxes[GraphNum] == 2) {
                    if(withLineBreak)
                        SignalName = "SCG\n" + "(" + SignalName + " Z)";
                    else
                        SignalName = "SCG" + "(" + SignalName + " Z)";
                    //SignalName += " Z";
                }
            }else if(EnabledSignal[GraphNum] == 5){// pletismografo
                if(EnabledAxes[GraphNum] == 0)
                    SignalName += " PPG RED";
                if(EnabledAxes[GraphNum] == 1){
                    if(withLineBreak)
                        SignalName = "PPG\n" + "(" + SignalName + " IR)";
                    else
                        SignalName = "PPG" + "(" + SignalName + " IR)";
                    //SignalName += " PPG_IR";

                }
            }
        }
        return SignalName;
    }

    //==========================================================================
    public void SetFileName (String new_filename){
        //==========================================================================
        FileName = new_filename;    // da mandare senza l'estensione
    }

    //==========================================================================
    public void ApplySignalChanges(int[] new_source, int[] new_signal, int[] new_axes){
        //==========================================================================
        int i;
        for (i=0; i<4; i++) { //ciclo sui quattro canali o grafici

            if(new_source[i] == 0){ //magic

                if(new_signal[i] == 0){//acc magic
                    MyMagic[i].MiniMagic.Acc.axes[new_axes[i]] = (byte)(i+1);
                }else if(new_signal[i] == 1){//ecg
                    MyMagic[i].MiniMagic.Ecg0 = (i+1);
                }else if(new_signal[i] == 2){//resp
                    MyMagic[i].MiniMagic.Resp0 = (i+1);
                }

            }else if(new_source[i]>=1 && new_source[i]<=5){//mote

                if(new_signal[i]>= 0 && new_signal[i]<=4) {//accelerometri
                    MyMagic[i].Motes[new_source[i]-1].Acc[new_signal[i]].axes[new_axes[i]] = (byte)(i+1);
                }else if(new_signal[i] == 5){//ppg
                    MyMagic[i].Motes[new_source[i]-1].PPG.source[new_axes[i]] = (byte)(i+1);
                }




            }
        }
    }


}











