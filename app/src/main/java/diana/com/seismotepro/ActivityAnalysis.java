package diana.com.seismotepro;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/*import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;*/

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

//==========================================================================
public class ActivityAnalysis extends Activity {
    //==========================================================================
    private final static int REQUEST_DATA_FILE_SELECTION = 0;
    private final static int REQUEST_DATA_SIGNAL_SELECTION = 1;
    private final static int REQUEST_DATA_DIALOG_BACK = 2;
    private final static int REQUEST_DATA_DIALOG_HOME = 3;
    private final static int REQUEST_DATA_DIALOG_TOSEND = 4;
    private final static int REQUEST_DATA_DIALOG_OVERWRITEINTERVALS = 5;
    private final static int REQUEST_DATA_MODIFY_TIMES = 6;

    private TextView FileName_view;
    private FrameLayout BigFrame;
    private FrameLayout FrameGraph1;
    private FrameLayout FrameGraph2;
    private FrameLayout FrameGraph3;
    private Button PackErr_Button;

    private Button ChangeFile_button;
    private Button ReadFile_button;
    private Button ViewLastAcq_button;
    private Button GraphIt_button;
    private Button DoAnalysis_button;
    private Button Mediate_button;
    private Button FitView_button;
    private Button ResetView_button;
    private Button GetIntervals_button;
    private Button SendData_button;
    private Button Back_button;
    private Button SaveData_button;
    private Button ChangeDefaults_button;
    private TextView Defaults_textview;
    private Button EnlargeMean_button;
    private Button GoToChangeTimes_button;

    private Button AddMarker_button;
    private Button RmvMarker_button;

    private TextView TimeInfo;  // visualizzazione del tempo selezionato tra le due linee
    private Button PEP_button;
    private Button ICT_button;
    private Button LVET_button;
    private Button IRT_button;
    private Button PTT_button;
    private Button PAT_button;
    private Button PR_button;
    private Button QT_button;
    private Button QRS_button;
    private Button QTC_button;
    private TextView PEP_text = null;
    private TextView ICT_text = null;
    private TextView LVET_text = null;
    private TextView IRT_text = null;
    private TextView PTT_text = null;
    private TextView PAT_text = null;
    private TextView PR_text = null;
    private TextView QT_text = null;
    private TextView QRS_text = null;
    private TextView QTC_text = null;
    private TextView STI_text = null;
    private TextView TEI_text = null;

    private TextView HeartRate_text = null;

    private TextView analysis_phase_view = null;
    private TextView hints_view = null;
    private ProgressBar readfile_progressbar = null;

    private String SeismotePath;
    private String SeismotePath_Decoded;
    private String FileToBeAnalyzed;
    private byte[] FileContent;
    private int File_idx = 0;
    private FileInputStream FileReader = null;
    private File TheFile = null;

    private List<String> FileList;

    private long LastModifiedFile_Time; // time in milliseconds
    private int LastModifiedFile_Index;

    // per la decodifca del file
    /*private ByteDataExchange RawData_SharedMem;
    private IntDataExchange Out1_SharedMem;
    private IntDataExchange Out2_SharedMem;
    private IntDataExchange Out3_SharedMem;*/

    //per dati convertiti
    private long[] DecodedSignal1_Time = null;     // conterrà tutti i dati convertiti
    private int[] DecodedSignal1_Data = null;
    private long[] DecodedSignal2_Time = null;
    private int[] DecodedSignal2_Data = null;
    private long[] DecodedSignal3_Time = null;
    private int[] DecodedSignal3_Data = null;
    private int DecodedSignal1_idx = 0;
    private int DecodedSignal2_idx = 0;
    private int DecodedSignal3_idx = 0;
    private boolean isConverting = false;
    /*private long[] sample1 = new long[2];   //temporaneo per ricezione dati da decodifica raw
    private long[] sample2 = new long[2];
    private long[] sample3 = new long[2];*/

    private long[] MagicEvents;
    private int[] MagicEvents_displayUnits;
    private int event_cnt = 0;

    private long[] buf_t;
    private int[] buf_y;
    private long[] BufToAnalyze1_Time = null;  // qui ci andrà la copia dei dati da analizzare
    private int[] BufToAnalyze1_Data = null;
    private long[] BufToAnalyze2_Time = null;
    private int[] BufToAnalyze2_Data = null;
    private long[] BufToAnalyze3_Time = null;
    private int[] BufToAnalyze3_Data = null;

    private Settings UserSettings;
    private DataDecoder AnalysisDecoder;

    private BackgroundSave StoreData1;
    private BackgroundSave StoreData2;
    private BackgroundSave StoreData3;

    private String Ecg_FileName="";
    private String Seismo_FileName="";
    private String Pleth_FileName="";

    private boolean Thread1Working = false;
    private boolean Thread2Working = false;
    private boolean Thread3Working = false;
/*
    private long[] tmpl_1;
    private int[] tmpi_1;
    private long[] tmpl_2;
    private int[] tmpi_2;
    private long[] tmpl_3;
    private int[] tmpi_3;*/

    private AnalysisGraph graphic1 = null;
    private AnalysisGraph graphic2 = null;
    private AnalysisGraph graphic3 = null;
    private BigFrameHandler myBigFrame = null;

    private EcgProcessor myECGprocessor;

    /*RawToDecode_class RawToDecode = null;
    GetSignal1_class GetSignal1 = null;
    GetSignal2_class GetSignal2 = null;
    GetSignal3_class GetSignal3 = null;*/

    private int AnalysisStep = 0;   // 0 = idle;    1= read;    2=graph;    3=fit graph;    4=analysis; 5=mediate;  6=get time intervals;
    private DataDecoder_Offline OfflineDecoder;

    private int[] Signal1_Mean = null;
    private int[] Signal2_Mean = null;
    private int[] Signal3_Mean = null;

    private int DisplayWidth_pixel;
    private int DisplayHeight_pixel;

    private boolean thereWasInfoFile = false;
    private boolean SignalsAreChosen = false;

    private HeartIntervals TimeIntervals;
    private InfoFileHandler AcqDataSaver;

    final static int F_sample_200Hz = 200; //200HZ
    final static int T_sample_200Hz = 5; //5ms

    GlobalSettingsFileHandler AppSettings;
    private boolean TransfMode_Enabled = false;


    // con DataDecoder_offline, chiamato dopo la fine del processo di decodifica
    //==========================================================================
    AsyncResponse async_decode = new AsyncResponse() {
        //==========================================================================
        @Override
        public void processFinish(Boolean output) {
            // qui torna quando ha finito di decodificare e siamo pronti per graficare

            //tolgo barra avanzamento e pulsante di stop
            readfile_progressbar.setVisibility(View.INVISIBLE);
            Back_button.setVisibility(View.INVISIBLE);

            // mi copio i buffer del segnale 1
            DecodedSignal1_Time = new long[OfflineDecoder.get_Signal1_Size()];
            System.arraycopy(OfflineDecoder.OutputTime1, 0, DecodedSignal1_Time, 0, DecodedSignal1_Time.length);

            DecodedSignal1_Data = new int[OfflineDecoder.get_Signal1_Size()];
            System.arraycopy(OfflineDecoder.OutputSignal1, 0, DecodedSignal1_Data, 0, DecodedSignal1_Data.length);


            // mi copio i buffer del segnale 2
            DecodedSignal2_Time = new long[OfflineDecoder.get_Signal2_Size()];
            System.arraycopy(OfflineDecoder.OutputTime2, 0, DecodedSignal2_Time, 0, DecodedSignal2_Time.length);

            DecodedSignal2_Data = new int[OfflineDecoder.get_Signal2_Size()];
            System.arraycopy(OfflineDecoder.OutputSignal2, 0, DecodedSignal2_Data, 0, DecodedSignal2_Data.length);


            // mi copio i buffer del segnale 3
            DecodedSignal3_Time = new long[OfflineDecoder.get_Signal3_Size()];
            System.arraycopy(OfflineDecoder.OutputTime3, 0, DecodedSignal3_Time, 0, DecodedSignal3_Time.length);

            DecodedSignal3_Data = new int[OfflineDecoder.get_Signal3_Size()];
            System.arraycopy(OfflineDecoder.OutputSignal3, 0, DecodedSignal3_Data, 0, DecodedSignal3_Data.length);

            GraphSignals(null);

            if(!thereWasInfoFile && SignalsAreChosen){  // se non c'era il vecchio info file e ho selezionato i segnali da visualizzare
                InfoFileHandler AcqDataSaver = new InfoFileHandler(UserSettings);
                AcqDataSaver.StoreInfoFile(FileToBeAnalyzed, null, null, (DecodedSignal1_Time[DecodedSignal1_Time.length-1] - DecodedSignal1_Time[0]) );
            }
        }
    };
//todo
    /*private void GetDecodedSignalsAndPlot(){
        //tolgo barra avanzamento e pulsante di stop
        readfile_progressbar.setVisibility(View.INVISIBLE);
        Back_button.setVisibility(View.INVISIBLE);

        // mi copio i buffer del segnale 1
        DecodedSignal1_Time = new long[OfflineDecoder.get_Signal1_Size()];
        System.arraycopy(OfflineDecoder.OutputTime1, 0, DecodedSignal1_Time, 0, DecodedSignal1_Time.length);

        DecodedSignal1_Data = new int[OfflineDecoder.get_Signal1_Size()];
        System.arraycopy(OfflineDecoder.OutputSignal1, 0, DecodedSignal1_Data, 0, DecodedSignal1_Data.length);


        // mi copio i buffer del segnale 2
        DecodedSignal2_Time = new long[OfflineDecoder.get_Signal2_Size()];
        System.arraycopy(OfflineDecoder.OutputTime2, 0, DecodedSignal2_Time, 0, DecodedSignal2_Time.length);

        DecodedSignal2_Data = new int[OfflineDecoder.get_Signal2_Size()];
        System.arraycopy(OfflineDecoder.OutputSignal2, 0, DecodedSignal2_Data, 0, DecodedSignal2_Data.length);


        // mi copio i buffer del segnale 3
        DecodedSignal3_Time = new long[OfflineDecoder.get_Signal3_Size()];
        System.arraycopy(OfflineDecoder.OutputTime3, 0, DecodedSignal3_Time, 0, DecodedSignal3_Time.length);

        DecodedSignal3_Data = new int[OfflineDecoder.get_Signal3_Size()];
        System.arraycopy(OfflineDecoder.OutputSignal3, 0, DecodedSignal3_Data, 0, DecodedSignal3_Data.length);

        GraphSignals(null);

        if(!thereWasInfoFile && SignalsAreChosen){  // se non c'era il vecchio info file e ho selezionato i segnali da visualizzare
            InfoFileHandler AcqDataSaver = new InfoFileHandler(UserSettings);
            AcqDataSaver.StoreInfoFile(FileToBeAnalyzed, null, null, (DecodedSignal1_Time[DecodedSignal1_Time.length-1] - DecodedSignal1_Time[0]) );
        }
    }
*/
    @Override
    //==========================================================================
    protected void onCreate(Bundle savedInstanceState) {
        //==========================================================================
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        FileName_view = (TextView)findViewById(R.id.textView_name);
        PackErr_Button = (Button)findViewById(R.id.button_errors);
        PackErr_Button.setBackgroundColor(Color.GREEN);

        hints_view = (TextView)findViewById(R.id.textView_hints);
        hints_view.setTextColor(Color.BLUE);

        analysis_phase_view = (TextView)findViewById(R.id.textView_analysis_phase);
        analysis_phase_view.setTextColor(Color.BLUE);

        readfile_progressbar = (ProgressBar)findViewById(R.id.progressBar_readfile);
        readfile_progressbar.setProgress(0);
        readfile_progressbar.setMax(100);

        // trovo i bottoni per poterli rendere invisibili nelle varie fasi
        ChangeFile_button = (Button) findViewById(R.id.button_setanalysis);
        ReadFile_button = (Button) findViewById(R.id.button_readfile);
        ViewLastAcq_button = (Button)findViewById(R.id.button_viewlastanalysis);
        GraphIt_button = (Button) findViewById(R.id.button_dograph);
        DoAnalysis_button = (Button) findViewById(R.id.button_doanalysis);
        Mediate_button = (Button) findViewById(R.id.button_mean);
        FitView_button = (Button) findViewById(R.id.button_fit);
        ResetView_button = (Button) findViewById(R.id.button_reset_view);
        GetIntervals_button = (Button) findViewById(R.id.button_starttime);
        SendData_button = (Button) findViewById(R.id.button_fromanalysistotransfer);
        Back_button = (Button) findViewById(R.id.button_backtopreviousphase);
        SaveData_button = (Button) findViewById(R.id.button_savedata);
        ChangeDefaults_button = (Button) findViewById(R.id.button_changedefaults);
        Defaults_textview = (TextView) findViewById(R.id.textView_defaultsignals);
        EnlargeMean_button = (Button) findViewById(R.id.button_enlarge_mean);
        GoToChangeTimes_button = (Button) findViewById(R.id.button_gotochangetimes);

        AddMarker_button = (Button)findViewById(R.id.button_add_marker);
        RmvMarker_button = (Button)findViewById(R.id.button_rmv_marker);

        PEP_button = (Button) findViewById(R.id.button_pep);
        ICT_button = (Button) findViewById(R.id.button_ict);
        LVET_button = (Button) findViewById(R.id.button_lvet);
        IRT_button = (Button) findViewById(R.id.button_irt);
        PTT_button = (Button) findViewById(R.id.button_ptt);
        PAT_button = (Button) findViewById(R.id.button_pat);
        PR_button = (Button) findViewById(R.id.button_pq);
        QT_button = (Button) findViewById(R.id.button_qt);
        QRS_button = (Button) findViewById(R.id.button_qrs);
        //QTC_button = (Button) findViewById(R.id.button_qtc);

        PEP_text = (TextView)findViewById(R.id.textView_pep);
        ICT_text = (TextView)findViewById(R.id.textView_ict);
        LVET_text = (TextView)findViewById(R.id.textView_lvet);
        IRT_text = (TextView)findViewById(R.id.textView_irt);
        PTT_text = (TextView)findViewById(R.id.textView_ptt);
        PAT_text = (TextView)findViewById(R.id.textView_pat);
        PR_text = (TextView)findViewById(R.id.textView_pq);
        QT_text = (TextView)findViewById(R.id.textView_qt);
        QRS_text = (TextView)findViewById(R.id.textView_qrs);
        QTC_text = (TextView)findViewById(R.id.textView_qtc);
        STI_text = (TextView)findViewById(R.id.textView_stiratio);
        TEI_text = (TextView)findViewById(R.id.textView_teiindex);


        HeartRate_text = (TextView)findViewById(R.id.textView_heartrate);

        TimeInfo = (TextView)findViewById(R.id.textView_time_live);

        BigFrame = (FrameLayout) findViewById(R.id.frame_layout);
        FrameGraph1 = (FrameLayout) findViewById(R.id.frame_layout1);
        FrameGraph2 = (FrameLayout) findViewById(R.id.frame_layout2);
        FrameGraph3 = (FrameLayout) findViewById(R.id.frame_layout3);

        BigFrame.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        FrameGraph1.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        FrameGraph2.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        FrameGraph3.setLayerType(View.LAYER_TYPE_HARDWARE, null);



        SeismotePath = getExternalStorageDirectory().getAbsolutePath() + "/Seismote/";
        SeismotePath_Decoded = SeismotePath + "Decoded/";

        FileToBeAnalyzed = GetLastFileName();
        FileName_view.setText("  Current File: "+FileToBeAnalyzed);   // faccio vedere il nome del file di default da decodificare

        UserSettings = new Settings();

        // legge le impostazioni dal file di settings globale della app
        AppSettings = new GlobalSettingsFileHandler(UserSettings);
        UserSettings = AppSettings.Read_AppSettingsInfo();
        if(UserSettings == null){
            UserSettings = new Settings();
            AppSettings = new GlobalSettingsFileHandler(UserSettings);
            AppSettings.Write_AppSettingsInfo();
        }
        TransfMode_Enabled = UserSettings.isTransferModeEnabled;

        AcqDataSaver = new InfoFileHandler(UserSettings);
        UserSettings = AcqDataSaver.GetVisualizedSignals(GetLastFileName());

        if (UserSettings == null){  //non c'è il file info e alla fine del file selection lo creo per le prossime volte
            thereWasInfoFile = false;
            SignalsAreChosen = false;
            UserSettings = new Settings();
            Defaults_textview.setText(  "Current File:\n\t" + FileToBeAnalyzed + "\n\n" +
                                        "Current Signals:" +        "\n\tGraph 1:\t" + "to be chosen" +
                                                                    "\n\tGraph 2:\t" + "to be chosen" +
                                                                    "\n\tGraph 3:\t" + "to be chosen");

        }else {
            thereWasInfoFile = true;
            SignalsAreChosen = true;
            Defaults_textview.setText(  "Current File:\n\t" + FileToBeAnalyzed + "\n\n" +
                                        "Current Signals:" +        "\n\tGraph 1:\t" + UserSettings.get_SignalName(0, false) +
                                                                    "\n\tGraph 2:\t" + UserSettings.get_SignalName(1, false) +
                                                                    "\n\tGraph 3:\t" + UserSettings.get_SignalName(2, false)  );
            UserSettings.ApplySignalChanges(UserSettings.EnabledSource, UserSettings.EnabledSignal, UserSettings.EnabledAxes);
        }

        if (GetLastFileName().equals("")){ //se non ci sono file
            SetUpForPhaseNum(-1);   //-1 è una fase inesistente e quindi non mostrerà alcun tasto
        }else {
            SetUpForPhaseNum(FILE_SELECTION_PHASE);
        }
       /*   TABLET GALAXY TAB 10.1 = 64 MB
       ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        call_toast("memory class = " + memoryClass);*/


        DisplayMetrics DisplayMetrics = this.getResources().getDisplayMetrics();
        DisplayWidth_pixel = DisplayMetrics.widthPixels;
        DisplayHeight_pixel = DisplayMetrics.heightPixels;

        PrintTime = new PrintTimeClass();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_analysis, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    // forza l'orientazione del tablet sempre in modalità landscape
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation/keyboard change
        super.onConfigurationChanged(newConfig);
    }

    @Override
    //==========================================================================
    public void onStop() {
        //==========================================================================
        super.onStop();

    }


    private static final int FILE_SELECTION_PHASE = 0;
    private static final int SEGMENT_SELECTION_PHASE = 1;
    private static final int R_PEAK_DETECTION_PHASE = 2;
    private static final int FEATURE_EXTRACTION_PHASE = 3;
    private int Phase = 0;
    //==========================================================================
    private void SetUpForPhaseNum( int phase_num) {
        //==========================================================================
        final int PhaseNum = phase_num;
        Phase = phase_num;
        // FA PARTIRE THEAD E IMPOSTA VISIBILITà OGGETTI NELLE VARIE FASI
        runOnUiThread(new Runnable() {
            public void run() {
                ChangeFile_button.setVisibility(View.INVISIBLE);
                ReadFile_button.setVisibility(View.INVISIBLE);
                ViewLastAcq_button.setVisibility(View.INVISIBLE);
                GraphIt_button.setVisibility(View.INVISIBLE);
                DoAnalysis_button.setVisibility(View.INVISIBLE);
                Mediate_button.setVisibility(View.INVISIBLE);
                FitView_button.setVisibility(View.INVISIBLE);
                ResetView_button.setVisibility(View.INVISIBLE);
                GetIntervals_button.setVisibility(View.INVISIBLE);
                SendData_button.setVisibility(View.INVISIBLE);
                Back_button.setVisibility(View.INVISIBLE);
                SaveData_button.setVisibility(View.INVISIBLE);
                ChangeDefaults_button.setVisibility(View.INVISIBLE);
                Defaults_textview.setVisibility(View.INVISIBLE);
                EnlargeMean_button.setVisibility(View.INVISIBLE);
                GoToChangeTimes_button.setVisibility(View.INVISIBLE);

                AddMarker_button.setVisibility(View.INVISIBLE);
                RmvMarker_button.setVisibility(View.INVISIBLE);

                PEP_button.setVisibility(View.INVISIBLE);
                ICT_button.setVisibility(View.INVISIBLE);
                LVET_button.setVisibility(View.INVISIBLE);
                IRT_button.setVisibility(View.INVISIBLE);
                PTT_button.setVisibility(View.INVISIBLE);
                PAT_button.setVisibility(View.INVISIBLE);
                PR_button.setVisibility(View.INVISIBLE);
                QT_button.setVisibility(View.INVISIBLE);
                QRS_button.setVisibility(View.INVISIBLE);

                PEP_text.setVisibility(View.INVISIBLE);
                ICT_text.setVisibility(View.INVISIBLE);
                LVET_text.setVisibility(View.INVISIBLE);
                IRT_text.setVisibility(View.INVISIBLE);
                PTT_text.setVisibility(View.INVISIBLE);
                PAT_text.setVisibility(View.INVISIBLE);
                PR_text.setVisibility(View.INVISIBLE);
                QT_text.setVisibility(View.INVISIBLE);
                QRS_text.setVisibility(View.INVISIBLE);
                QTC_text.setVisibility(View.INVISIBLE);
                STI_text.setVisibility(View.INVISIBLE);
                TEI_text.setVisibility(View.INVISIBLE);


                HeartRate_text.setVisibility(View.INVISIBLE);

                PackErr_Button.setVisibility(View.INVISIBLE);
                readfile_progressbar.setVisibility(View.INVISIBLE);
                TimeInfo.setBackgroundColor(Color.LTGRAY);
                TimeInfo.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                TimeInfo.setVisibility(View.INVISIBLE);

                Back_button.setText("BACK");


                FileName_view.setBackgroundColor(Color.TRANSPARENT);
                ReadFile_button.setBackgroundResource(android.R.drawable.btn_default);

                switch(PhaseNum){
                    //**************************************************
                    case FILE_SELECTION_PHASE:

                        //FileName_view.setBackgroundColor(Color.YELLOW);//giallino
                        FileName_view.setVisibility(View.INVISIBLE);

                        ReadFile_button.setBackgroundColor(Color.YELLOW);

                        analysis_phase_view.setText("PHASE: FILE SELECTION");
                        hints_view.setText("Press Read File, Change File or Change Signals");
                        ChangeFile_button.setVisibility(View.VISIBLE);
                        ReadFile_button.setVisibility(View.VISIBLE);
                        //GraphIt_button.setVisibility(View.VISIBLE);   // unite le funzioni analysis e graphit!
                        PackErr_Button.setVisibility(View.VISIBLE);
                        //readfile_progressbar.setVisibility(View.VISIBLE); visibile solo durante lettura file
                        ChangeDefaults_button.setVisibility(View.VISIBLE);
                        Defaults_textview.setVisibility(View.VISIBLE);
                        /*Defaults_textview.setText("Current File:\n\t" + FileToBeAnalyzed + "\n\n" +
                                                  "Current Signals:" +      "\n\tGraph 1:\t" + UserSettings.get_SignalName(0, false) +
                                                                            "\n\tGraph 2:\t" + UserSettings.get_SignalName(1, false) +
                                                                            "\n\tGraph 3:\t" + UserSettings.get_SignalName(2, false)  );*/

                        break;
                    //**************************************************
                    case SEGMENT_SELECTION_PHASE:
                        FileName_view.setVisibility(View.VISIBLE);
                        analysis_phase_view.setText("PHASE: SEGMENT SELECTION");
                        hints_view.setText("Select and Zoom Signals using lines and buttons, then select Segmentation");//Move the two lines to select the segment to analyze, then Fit, and finally Segmentation");
                        FitView_button.setVisibility(View.VISIBLE);
                        FitView_button.setText("SELECT");
                        //ResetView_button.setVisibility(View.VISIBLE);  visibile solo dopo prima zoomata
                        //DoAnalysis_button.setVisibility(View.VISIBLE);
                        Back_button.setVisibility(View.VISIBLE);
                        TimeInfo.setVisibility(View.VISIBLE);

                        break;
                    //**************************************************
                    case R_PEAK_DETECTION_PHASE:
                        FileName_view.setVisibility(View.VISIBLE);
                        analysis_phase_view.setText("PHASE: R-PEAK DETECTION");
                        hints_view.setText("Press Averaging to view mean signals");
                        Mediate_button.setVisibility(View.VISIBLE);
                        Back_button.setVisibility(View.VISIBLE);
                        TimeInfo.setVisibility(View.VISIBLE);

                        break;
                    //**************************************************
                    case FEATURE_EXTRACTION_PHASE:
                        FileName_view.setVisibility(View.VISIBLE);
                        analysis_phase_view.setText("PHASE: FEATURE EXTRACTION");
                        hints_view.setText("Get time intervals using lines and buttons, then save them");
                        //GetIntervals_button.setVisibility(View.VISIBLE); è diventato automatico
                        Back_button.setVisibility(View.VISIBLE);
                        if(TransfMode_Enabled) {
                            SendData_button.setVisibility(View.VISIBLE);
                        }
                        EnlargeMean_button.setVisibility(View.VISIBLE);
                        GoToChangeTimes_button.setVisibility(View.VISIBLE);

                        PEP_button.setVisibility(View.VISIBLE);
                        ICT_button.setVisibility(View.VISIBLE);
                        LVET_button.setVisibility(View.VISIBLE);
                        IRT_button.setVisibility(View.VISIBLE);
                        PTT_button.setVisibility(View.VISIBLE);
                        PAT_button.setVisibility(View.VISIBLE);
                        PR_button.setVisibility(View.VISIBLE);
                        QT_button.setVisibility(View.VISIBLE);
                        QRS_button.setVisibility(View.VISIBLE);

                        AddMarker_button.setVisibility(View.VISIBLE);
                        RmvMarker_button.setVisibility(View.VISIBLE);

                        PEP_text.setVisibility(View.VISIBLE);
                        ICT_text.setVisibility(View.VISIBLE);
                        LVET_text.setVisibility(View.VISIBLE);
                        IRT_text.setVisibility(View.VISIBLE);
                        PTT_text.setVisibility(View.VISIBLE);
                        PAT_text.setVisibility(View.VISIBLE);
                        PR_text.setVisibility(View.VISIBLE);
                        QT_text.setVisibility(View.VISIBLE);
                        QRS_text.setVisibility(View.VISIBLE);

                        TimeInfo.setVisibility(View.VISIBLE);
                        SaveData_button.setVisibility(View.VISIBLE);
                        SaveData_button.setBackgroundColor(Color.YELLOW);
                        HeartRate_text.setVisibility(View.VISIBLE);


                        boolean result;
                        TimeIntervals = new HeartIntervals();
                        result = TimeIntervals.ReadFileWithTimes();
                        if(result == true){
                            // il file esiste, ha trovato i tempi e li ha salvati nelle var

                        }else{
                            result = TimeIntervals.CreateFileWithTimes();

                        }

                        break;
                }
            }
        });
    }
    //==========================================================================
    public void BackButtonPressed(View v) {
    //==========================================================================
        Phase -= 1; // torno indietro alla fase precedente
        if(Phase <= FILE_SELECTION_PHASE){//se siamo tornati alla fase di lettura del file
            // chiedi se davvero vuoi tornare indietro
            GoToDialogActivity(REQUEST_DATA_DIALOG_BACK, "Change file?", "You will lose this data!", "OK", "CANCEL");

        }else {
            SetUpForPhaseNum(Phase);
            if (Phase == R_PEAK_DETECTION_PHASE){
                //arrivo dai segnali medi e torno alla visualizzazione di tutto il segnale
                Phase-=1;
                myBigFrame.doDrawVerticalLine = false;  // cancello la linea sul picco r del segnale medio
                myBigFrame.GraphRedraw();
                SetUpForPhaseNum(Phase);
                ClearTimes();

                PrintTime.StopThread();
                isTimeExtractionEnabled = false;
                graphic1.ClearBalloons();
                graphic2.ClearBalloons();
                graphic3.ClearBalloons();

                ResetView(null);
            }
            if(Phase == SEGMENT_SELECTION_PHASE){
                Back_button.setText("DECODE \n ANOTHER FILE");
            }
        }
    }

        //==========================================================================
    private String GetLastFileName(){
        //==========================================================================
        FileList = new ArrayList<>();

        File Folder = new File(SeismotePath);
        File SeismoteFiles[] = Folder.listFiles();

        // cerco il file più recente
        int i;
        if (SeismoteFiles.length == 0){
            call_toast("No Files Available. Make an acquisition before.");
            FileToBeAnalyzed = ""; // ritorno una stringa null
        }else {
            LastModifiedFile_Time = SeismoteFiles[0].lastModified();    // time in milliseconds since January 1st, 1970, midnight
            LastModifiedFile_Index = 0;
            for (i = 0; i < SeismoteFiles.length; i++) {
                FileList.add(SeismoteFiles[i].getName());   // aggiungo i file alla lista se serve doopo per la scelta di un file diverso
                if ((SeismoteFiles[i].lastModified() > LastModifiedFile_Time) && (SeismoteFiles[i].isFile())) {    // ho trovato un file più recente
                    String extension = SeismoteFiles[i].getName().substring((SeismoteFiles[i].getName().length() - 3), (SeismoteFiles[i].getName().length()));
                    if (extension.equals("raw")) {  // se è un file raw
                        LastModifiedFile_Time = SeismoteFiles[i].lastModified();
                        LastModifiedFile_Index = i;
                    }
                }
            }
            FileToBeAnalyzed = SeismoteFiles[LastModifiedFile_Index].getName();
        }
        return FileToBeAnalyzed;
    }

    boolean isTheFirstDecode = true;
    // fa partire thread per la lettura e la decodifica del file raw salvato runtime da bt
    //==========================================================================
    public void DecodeFile(View v){
        //==========================================================================

        if(!isConverting) { // se non sta gia convertendo
            if(isTheFirstDecode) {
                if(SignalsAreChosen) {// se sono stati selezionati i segnali da visualizzare

                    hints_view.setText("Reading File...");
                    isConverting = true;

                    // tolgo quello che non serve più
                    ChangeFile_button.setVisibility(View.INVISIBLE);
                    ViewLastAcq_button.setVisibility(View.INVISIBLE);
                    ReadFile_button.setVisibility(View.INVISIBLE);
                    ChangeDefaults_button.setVisibility(View.INVISIBLE);
                    Defaults_textview.setVisibility(View.INVISIBLE);

                    //aggiungo quello che serve
                    readfile_progressbar.setVisibility(View.VISIBLE);
                    Back_button.setText("Stop decode");
                    Back_button.setVisibility(View.VISIBLE);

                    FileContent = ReadTheWholeFile(SeismotePath + FileToBeAnalyzed);  //file raw da decodificare

                    OfflineDecoder = new DataDecoder_Offline(UserSettings, FileContent, async_decode);
                    OfflineDecoder.put_IO_Buffers_Signal1(UserSettings.Max_T_Rec * F_sample_200Hz);//256 * 1024);    // 20 min di registrazione circa
                    OfflineDecoder.put_IO_Buffers_Signal2(UserSettings.Max_T_Rec * F_sample_200Hz);//256 * 1024);
                    OfflineDecoder.put_IO_Buffers_Signal3(UserSettings.Max_T_Rec * F_sample_200Hz);//256 * 1024);
                    OfflineDecoder.execute();


                    Ecg_FileName = AcqDataSaver.get_signal1_filename(FileToBeAnalyzed);
                    Seismo_FileName = AcqDataSaver.get_signal2_filename(FileToBeAnalyzed);
                    Pleth_FileName = AcqDataSaver.get_signal3_filename(FileToBeAnalyzed);
                   /* Ecg_FileName = FileToBeAnalyzed.substring(0, FileToBeAnalyzed.length() - 4) + "_ECG";
                    Seismo_FileName = FileToBeAnalyzed.substring(0, FileToBeAnalyzed.length() - 4) + "_SEISMO";
                    Pleth_FileName = FileToBeAnalyzed.substring(0, FileToBeAnalyzed.length() - 4) + "_PLETH";*/

                    handler.postDelayed(r, 100);    // faccio partire runner per visualizzazione progresso lettura
                }else if(!SignalsAreChosen){
                    call_toast("Select signals to plot before!");
                }
            }else{

            }
        } else{
            call_toast("Wait End of Conversion.");
        }
    }

    //==========================================================================
    // handler per la progress bar per il progresso della lettura dal file
    private Handler handler = new Handler();
    private int progress = 0;
    final Runnable r = new Runnable()
    {
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    if(FileContent != null) {


                        progress = (int) (((double) OfflineDecoder.get_input_index() / FileContent.length) * 100);

                        if(progress % 5 == 0 && progress < 100) {  // se è un multiplo di 5
                            readfile_progressbar.setProgress(progress);
                            hints_view.setText("Decoding File... " + progress + "%");
                        }
                        else if(progress == 100){
                            //todo

                            //GetDecodedSignalsAndPlot();
                        }

                        // controllo numero di errori su payload / header nella classe di decodifica
                        if (OfflineDecoder.CorruptedPayload != 0 || OfflineDecoder.CorruptedHeaders != 0) {
                            if (!erosso) {
                                PackErr_Button.setBackgroundColor(Color.RED);
                                erosso = true;
                            }
                        }
                    }
                }
            });
            handler.postDelayed(this, 200);    //scatta ogni 300 ms
        }
    };

    //==========================================================================
    public byte[] ReadTheWholeFile(String FilePath){
        //==========================================================================
        try {
            TheFile = new File(FilePath);
            //TheFile = new File(SeismotePath + FileToBeAnalyzed);
            FileContent = new byte[(int)TheFile.length()];
            File_idx = 0;

            FileReader = new FileInputStream(TheFile);
            FileReader.read(FileContent);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }finally{
            if(FileReader!=null){
                try {
                    FileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return FileContent;
        }
    }




    private boolean IsViewingAllTheAcquisition = false;
    //chiamato dalla pressione del tasto graph it! oppure dal tasto zoom out
    //==========================================================================
    public void GraphSignals(View v){
        //==========================================================================

        if(!Thread1Working && !Thread2Working && !Thread3Working ) {
            FileContent = null;
            handler.removeCallbacks(r);

            if(Ecg_FileName.equals("") || Seismo_FileName.equals("") || Pleth_FileName.equals("")){
                call_toast("Read a file before");
            }else {

                if(!IsViewingAllTheAcquisition){
                    IsViewingAllTheAcquisition = true;

                    //linee verdi in corrispondenza degli eventi magic
                    MagicEvents = new long[OfflineDecoder.evnt_nmb];
                    System.arraycopy(OfflineDecoder.MagicEvents, 0, MagicEvents, 0, MagicEvents.length);
                    MagicEvents_displayUnits = new int[MagicEvents.length];

                    //pulisce le strutture dati del decoder
                    OfflineDecoder.clearBuffers();
//                    OfflineDecoder = null;


                    init_Graphs_Layouts();

                    PrintTime.StartThread();


                    //==========================================================================
                    new Thread(new Runnable() {
                        public void run() {

                        if (DecodedSignal1_Data != null && DecodedSignal2_Data != null && DecodedSignal3_Data != null) {
                            if (DecodedSignal1_Data.length > 0) {
                                // prima visualizzazione: mostro tutta la acquisizione
                                // segnale 1*******************************
                                graphic1.PutAndCentreSignal(DecodedSignal1_Time, DecodedSignal1_Data, (DecodedSignal1_Time.length * T_sample_200Hz));     //numero campioni * millisec a campione
                            }

                            if (DecodedSignal2_Data.length > 0) {
                                // segnale 2******************************
                                graphic2.PutAndCentreSignal(DecodedSignal2_Time, DecodedSignal2_Data, (DecodedSignal2_Data.length * T_sample_200Hz));     //numero campioni * millisec a campione
                            }

                            if (DecodedSignal3_Data.length > 0) {
                                // segnale 3*******************************
                                graphic3.PutAndCentreSignal(DecodedSignal3_Time, DecodedSignal3_Data, (DecodedSignal3_Data.length * T_sample_200Hz));     //numero campioni * millisec a campione
                            }

                            if (DecodedSignal1_Data.length > 0 || DecodedSignal2_Data.length > 0 || DecodedSignal3_Data.length > 0) {
                                SetUpForPhaseNum(SEGMENT_SELECTION_PHASE);//mi metto in fase di selezione del segmento da analizzare
                            } else {
                                call_toast("nessun segnale");
                            }

                            if(MagicEvents != null && MagicEvents.length > 0) {
                                int i;
                                for (i = 0; i < MagicEvents.length; i++) {
                                    MagicEvents_displayUnits[i] = (int) graphic1.RemapData_X(MagicEvents[i]);
                                }
                                myBigFrame.DrawVerticalLinesOnEvents(MagicEvents_displayUnits);
                            }

                        } else {
                            hints_view.setText("Empty file. Select another.");
                        }
                    }}).start();
                }
            }

        }else{
            call_toast("Wait End of Conversion.");
        }
    }

    //==========================================================================
    public void init_Graphs_Layouts(){
        //==========================================================================
        int FrameWidth = BigFrame.getWidth();
        int FrameHeight = BigFrame.getHeight() / 3 - 10;    // il -15 è per farci stare i pallini delle linee di selezione segnmenti

        if (graphic1 != null)
            graphic1.ClearAll();
        if (graphic2 != null)
            graphic2.ClearAll();
        if (graphic3 != null)
            graphic3.ClearAll();


        myBigFrame = new BigFrameHandler(getApplicationContext(), BigFrame.getWidth(), BigFrame.getHeight());
        graphic1 = new AnalysisGraph(getApplicationContext(), FrameWidth, FrameHeight, 0, UserSettings);
        graphic2 = new AnalysisGraph(getApplicationContext(), FrameWidth, FrameHeight, 1, UserSettings);
        graphic3 = new AnalysisGraph(getApplicationContext(), FrameWidth, FrameHeight, 2, UserSettings);

        myBigFrame.put_graph1_ref(graphic1);

        FrameLayout.LayoutParams Frame1_lp = new FrameLayout.LayoutParams(FrameWidth, FrameHeight);
        FrameGraph1.setLayoutParams(Frame1_lp);
        FrameGraph1.setBackgroundColor(Color.WHITE);

        FrameLayout.LayoutParams Frame2_lp = new FrameLayout.LayoutParams(FrameWidth, FrameHeight);
        Frame2_lp.setMargins(0, FrameHeight, 0, 0);
        FrameGraph2.setLayoutParams(Frame2_lp);
        FrameGraph2.setBackgroundColor(Color.WHITE);

        FrameLayout.LayoutParams Frame3_lp = new FrameLayout.LayoutParams(FrameWidth, FrameHeight);
        Frame3_lp.setMargins(0, 2 * FrameHeight, 0, 0);
        FrameGraph3.setLayoutParams(Frame3_lp);
        FrameGraph3.setBackgroundColor(Color.WHITE);

        FrameGraph1.addView(graphic1);
        FrameGraph2.addView(graphic2);
        FrameGraph3.addView(graphic3);
        BigFrame.addView(myBigFrame);
        myBigFrame.ClearAll();
        graphic1.ClearAll();
        graphic2.ClearAll();
        graphic3.ClearAll();
    }

    //==========================================================================
    private void call_toast(final CharSequence text){
        //==========================================================================
        // SETS A KIND OF POP-UP MESSAGE
        runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }


    private boolean erosso = false;
    // chimato dalla pressione del quadratino rosso durante la decodifica del file
    //==========================================================================
    public void ResetPacketErrors(View v){
        //==========================================================================
        /*if(AnalysisDecoder!=null) {
            AnalysisDecoder.CorruptedHeaders = 0;
            AnalysisDecoder.CorruptedPayload = 0;
            PackErr_Button.setBackgroundColor(Color.GREEN);
            erosso = false;
        }*/
        if(OfflineDecoder!=null) {
            OfflineDecoder.CorruptedHeaders = 0;
            OfflineDecoder.CorruptedPayload = 0;
            PackErr_Button.setBackgroundColor(Color.GREEN);
            erosso = false;
        }
    }


    //==========================================================================
    public void StartAnalyzeEcg(View v){
        //==========================================================================
        if(isSegmentSelected) {
            isSegmentSelected = false;
            myECGprocessor = new EcgProcessor(F_sample_200Hz, BufToAnalyze1_Time.length);

            new Thread(new Runnable() {
                public void run() {
                    int i;
                    // ANALIZZO SEGNALE ECG
                    for (i = 0; i < BufToAnalyze1_Data.length; i++) {
                        myECGprocessor.AddData(BufToAnalyze1_Data[i]);
                    }

                    //RACCOLGO I RISULTATI
                    //call_toast("num peak = " + myECGprocessor.myPeak.conta_picchi);
                    graphic1.DrawLinesOnPeaks(myECGprocessor.myPeak.peak_pointer,  myECGprocessor.myPeak.conta_picchi);
                /*for (i=0; i<myECGprocessor.myPeak.conta_picchi; i++){
                    call_toast(""+(double)(myECGprocessor.myPeak.peak_pointer[i]/200.0));
                }*/
                    if(myECGprocessor.myPeak.conta_picchi>=2) {
                        SetUpForPhaseNum(R_PEAK_DETECTION_PHASE);
                    }
                    else{
                        call_toast("Select AT LEAST FOUR BEATS");
                    }
                }
            }).start();
        }else{
            call_toast("Select a Segment Before Analyze");
        }

    }


    private long[] time_mean;
    private int peak_position;
    private double RR_MEAN = 0;
    private int BPM;
    private int NumberOfBeats;
    //==========================================================================
    public void SegmentAndMediate(View v) {
        //==========================================================================
        double tmp = ComputeRRmean(myECGprocessor.myPeak.peak_pointer, myECGprocessor.myPeak.conta_picchi);

        final int RR_Mean_sample = (int)tmp;               // intervallo rr in numero di campioni
        final int RR_Mean = (int)(1000.0/F_sample_200Hz * tmp);     // media RR espressa in mili secondi
        RR_MEAN = RR_Mean;
        BPM = (int)(60000.0 / (double)RR_Mean);             //media RR in battiti al minuto

        int peak_num = myECGprocessor.myPeak.conta_picchi;  //numero righe = numero picchi R
        NumberOfBeats = peak_num;

        AcqDataSaver.put_BeatsInfo(RR_MEAN, BPM, NumberOfBeats);
        HeartRate_text.setText(peak_num + " beats\n" + "HR=" + BPM + " bpm\n" + "RR=" + RR_Mean + "ms");

        time_mean = new long[RR_Mean_sample];
        int i;
        // mi creo array temporale farlocco per visualizzazione dei risultati della media
        for(i=0; i<RR_Mean_sample; i++){
            time_mean[i] = i*T_sample_200Hz;  // simulo un campione ogni 5 ms
        }

        new Thread(new Runnable() {
            public void run() {
                int i;

                if(DecodedSignal1_Data != null && BufToAnalyze1_Data != null) {
                    if (DecodedSignal1_Data.length > 0 || BufToAnalyze1_Data.length > 0) {
                        Signal1_Mean = ComputeMeanSignal(BufToAnalyze1_Data, myECGprocessor.myPeak.conta_picchi, RR_Mean_sample, myECGprocessor.myPeak.peak_pointer);
                        graphic1.PutAndCentreMeanSignal(time_mean, Signal1_Mean, RR_Mean * 2);

                        peak_position = 40;//da qui cerco la posizione esatta del picco r nei 100 ms prima e dopo della posiz iniziale
                        for ( i=-20; i<20; i++){
                            if(Signal1_Mean[i + 40] > Signal1_Mean[peak_position]){
                                peak_position = i+40;
                            }
                        }
                        // disegno linea verde su picco r
                        myBigFrame.DrawVerticalLine((int) graphic1.RemapData_X((long) (graphic1.ReverseRemapData_X((int) graphic1.deltaT) + peak_position * T_sample_200Hz)));//linea su picco r ( posizione = delta t di visualizzazione + 200 millisecondi
                    }
                }
                if(DecodedSignal2_Data != null && BufToAnalyze2_Data != null) {
                    if (DecodedSignal2_Data.length > 0 || BufToAnalyze2_Data.length > 0) {
                        Signal2_Mean = ComputeMeanSignal(BufToAnalyze2_Data, myECGprocessor.myPeak.conta_picchi, RR_Mean_sample, myECGprocessor.myPeak.peak_pointer);
                        graphic2.PutAndCentreMeanSignal(time_mean, Signal2_Mean, RR_Mean * 2);
                    }
                }
                if(DecodedSignal3_Data != null && BufToAnalyze3_Data != null) {
                    if (DecodedSignal3_Data.length > 0 || BufToAnalyze3_Data.length > 0) {
                        Signal3_Mean = ComputeMeanSignal(BufToAnalyze3_Data, myECGprocessor.myPeak.conta_picchi, RR_Mean_sample, myECGprocessor.myPeak.peak_pointer);
                        graphic3.PutAndCentreMeanSignal(time_mean, Signal3_Mean, RR_Mean * 2);   // *2 = ce ne starebbero 2
                    }
                }
                if(DecodedSignal1_Data.length > 0 || DecodedSignal2_Data.length > 0 || DecodedSignal3_Data.length > 0) {
                    SetUpForPhaseNum(FEATURE_EXTRACTION_PHASE);
                    EnableTimeTasks(null);
                }
            }
        }).start();
    }

    //==========================================================================
    private double ComputeRRmean(int[] peak_position, int num_peak){
        //==========================================================================
        // rwstituisce il valore dell'itervallo rr medio, approssimato a int
        final int[] PeakPositions = peak_position;
        final int NumPeak = num_peak;

        double RRmean = 0;
        int i;
        RRmean = PeakPositions[1] - PeakPositions[0];
        for (i=1; i < NumPeak-1; i++) {
            RRmean = RRmean + (PeakPositions[i+1] - PeakPositions[i]);
        }
        RRmean /= (NumPeak - 1);
        // rrmean ora contiene info riguardo alla distanza tra due picchi R in numero di campiioni
        //call_toast("RRmedio= " + (RRmean / 200.0) + " sec");

        return RRmean;

    }
    //==========================================================================
    private int[] ComputeMeanSignal(int[] Signal, int peak_num, int RRmean_sample, int[] peak_ptr) {
        //==========================================================================
        // param: segnale da mediare, numero di picchi trovati, rr medio in numero di campioni, puntatori alle posizioni dei picchi
        int[] Signal_mean = new int[RRmean_sample]; // battito medio

        int i;
        int j;
        int first_beat_num = 0; // numero del primo battito intero
        int last_beat_num = 0; // numero dell'ultimo battito intero

        // -40 = apro la finestra del battito 40 campioni = 200 ms prima del picco R
        if(peak_ptr[0] - 40 >= 0){
            first_beat_num = 0;
        }else   // se il primo picco non corrispone a un battito interamente contenuto nel segnale allora parto dal secondo
            first_beat_num = 1;

        // chiudo la finestra in modo tale da avere l'ultimo battito per intero
        if((peak_ptr[peak_num-2] + RRmean_sample - 40) > Signal.length){    // se l'ultimo battito non è contenuto in Signal
            last_beat_num = peak_num-2;
        }else{
            last_beat_num = peak_num-1;//scarto comunque l'ultimo picco che è sbagliato
        }


        // calcolo la media di ciascun battito che verrà sottratta ai singoli campioni del battito in fase di media
        int[] BeatMean = new int[peak_num]; // arrai contenente la media di ciascun battito
        int mean_tmp = 0;

        for( i=first_beat_num; i<last_beat_num; i++){//scorro sul num dei battiti con i ed escludo ultimo battito che in genere è sbagliato
            for( j=0; j< RRmean_sample; j++) {//scorro all'interno del singolo battito con j
                if((peak_ptr[i+first_beat_num] - 40 + j) < Signal.length )
                    mean_tmp += Signal[peak_ptr[i+first_beat_num] - 40 + j ];
            }
            BeatMean[i] = mean_tmp / RRmean_sample; // j scorre all'interno del signolo battito
            mean_tmp = 0;
        }

        // calcolo il battito medio aggiorno da primo battito valido
        for( i=first_beat_num; i<last_beat_num; i++){  //scorro sul num dei battiti con i
            for( j=0; j< RRmean_sample; j++) {  //scorro all'interno del singolo battito con j
                if((peak_ptr[i+first_beat_num] - 40 + j) < Signal.length )
                    Signal_mean[j] += Signal[peak_ptr[i+first_beat_num] - 40 + j ] - BeatMean[i];
            }
        }
        for( i=0; i<Signal_mean.length; i++){
            Signal_mean[i] /= (last_beat_num-first_beat_num+1);    // calcolo media per ciascun campione del battito
        }

        return Signal_mean;
    }

    private boolean enlarged = false;
    //funziona usata per zoomare il segnale medio sulle x fino a farlo diventare largo quanto i box
    //==========================================================================
    public void EnlargeMeanSignal(View v) {
        //==========================================================================
        if(!enlarged){
            enlarged = true;
            EnlargeMean_button.setText("→←");   // il testo mostra che se si schiaccia il bottone il plot de-zooma

            new Thread(new Runnable() {
                public void run() {

                    if(Signal1_Mean != null) {
                        graphic1.PutAndCentreMeanSignal(time_mean, Signal1_Mean, Signal1_Mean.length * T_sample_200Hz);
                        myBigFrame.DrawVerticalLine((int) graphic1.RemapData_X((long) (graphic1.ReverseRemapData_X((int) graphic1.deltaT) + peak_position * T_sample_200Hz)));
                    }
                    if(Signal2_Mean != null) {
                        graphic2.PutAndCentreMeanSignal(time_mean, Signal2_Mean, Signal2_Mean.length * T_sample_200Hz);
                    }
                    if(Signal3_Mean != null) {
                        graphic3.PutAndCentreMeanSignal(time_mean, Signal3_Mean, Signal3_Mean.length * T_sample_200Hz);   // così si mostra tutto
                    }
                }
            }).start();

        }else{
            enlarged = false;
            EnlargeMean_button.setText("←→");
            new Thread(new Runnable() {
                public void run() {

                    if(Signal1_Mean != null) {
                        graphic1.PutAndCentreMeanSignal(time_mean, Signal1_Mean, Signal1_Mean.length * T_sample_200Hz * 2);
                        myBigFrame.DrawVerticalLine((int) graphic1.RemapData_X((long) (graphic1.ReverseRemapData_X((int) graphic1.deltaT) + peak_position * T_sample_200Hz)));
                    }
                    if(Signal2_Mean != null) {
                        graphic2.PutAndCentreMeanSignal(time_mean, Signal2_Mean, Signal2_Mean.length * T_sample_200Hz * 2);
                    }
                    if(Signal3_Mean != null) {
                        graphic3.PutAndCentreMeanSignal(time_mean, Signal3_Mean, Signal3_Mean.length * T_sample_200Hz * 2);   // *2 = così ce ne starebbero 2
                    }
                }
            }).start();
        }

    }

    private boolean isSegmentSelected = false;
    int time1_position_sign1 = 0; //sua posizione nel buffer dei timestamp
    int time1_position_sign2 = 0;
    int time1_position_sign3 = 0;
    int time2_position_sign1 = 0;
    int time2_position_sign2 = 0;
    int time2_position_sign3 = 0;
    long time1 = 0;
    long time2 = 0;
    int x1 = 0;
    int x2 = 0;
    int[] tmp_long = null;
    //==========================================================================
   public void FitDataInBox(View v){
       //==========================================================================

       IsViewingAllTheAcquisition = false;

       x1 = myBigFrame.get_Line1_X();
       x2 = myBigFrame.get_Line2_X();
       boolean time_interval_ok = false;

       if( x1 < x2){ //ok
           isSegmentSelected = true;
           time1 = graphic1.ReverseRemapData_X(x1); //ottengo il valore in ms
           time2 = graphic1.ReverseRemapData_X(x2);

           if((time2 - time1) > T_sample_200Hz * 1000){  // ho preso almeno 5 secondi di acquisizione
               time_interval_ok = true;
           }else{
               call_toast("select at least 5 sec");
           }

           if(time_interval_ok) {

               int i;
               float signal1_average=0;
               float signal2_average=0;
               float signal3_average=0;

               DoAnalysis_button.setVisibility(View.VISIBLE);
               ResetView_button.setVisibility(View.VISIBLE);
               FitView_button.setText("ZOOM IN");

               // cerco quale timestamp è il più vicin a questi due numeri trovati
               // per x1
               for (i = 0; i < DecodedSignal1_Time.length; i++) {
                   if (Math.abs(time1 - DecodedSignal1_Time[i]) <= T_sample_200Hz) {
                       time1_position_sign1 = i;
                   }
                   if (Math.abs(time2 - DecodedSignal1_Time[i]) <= T_sample_200Hz) {
                       time2_position_sign1 = i;
                   }
                   // calcolo media
                   signal1_average += DecodedSignal1_Data[i];
               }
               signal1_average /= DecodedSignal1_Data.length;

               for (i = 0; i < DecodedSignal2_Time.length; i++) {
                   if (Math.abs(time1 - DecodedSignal2_Time[i]) <= T_sample_200Hz) {
                       time1_position_sign2 = i;
                   }
                   if (Math.abs(time2 - DecodedSignal2_Time[i]) <= T_sample_200Hz) {
                       time2_position_sign2 = i;
                   }
                   // calcolo media
                   signal2_average += DecodedSignal2_Data[i];
               }
               signal2_average /= DecodedSignal2_Data.length;

               for (i = 0; i < DecodedSignal3_Time.length; i++) {
                   if (Math.abs(time1 - DecodedSignal3_Time[i]) <= T_sample_200Hz) {
                       time1_position_sign3 = i;
                   }
                   if (Math.abs(time2 - DecodedSignal3_Time[i]) <= T_sample_200Hz) {
                       time2_position_sign3 = i;
                   }
                   // calcolo media
                   signal3_average += DecodedSignal3_Data[i];
               }
               signal3_average /= DecodedSignal3_Data.length;
                // ci metto i valori corretti
               time1 = DecodedSignal1_Time[time1_position_sign1];
               time2 = DecodedSignal1_Time[time2_position_sign1];



               if (time1_position_sign1 > 0 && time2_position_sign1 > 0 && time2_position_sign1 > time1_position_sign1) {
                   // aggiorno i buffer da analizzare

                   new Thread(new Runnable() {
                       public void run() {

                           if (DecodedSignal1_Data.length > 0) {
                               BufToAnalyze1_Time = new long[time2_position_sign1 - time1_position_sign1];
                               BufToAnalyze1_Data = new int[time2_position_sign1 - time1_position_sign1];

                               System.arraycopy(DecodedSignal1_Time, time1_position_sign1, BufToAnalyze1_Time, 0, (time2_position_sign1 - time1_position_sign1));
                               System.arraycopy(DecodedSignal1_Data, time1_position_sign1, BufToAnalyze1_Data, 0, (time2_position_sign1 - time1_position_sign1));

                               graphic1.PutAndCentreSignal(BufToAnalyze1_Time, BufToAnalyze1_Data, (int)(time2 - time1));//,(int)signal1_average );
                           }

                           if (DecodedSignal2_Data.length > 0) {
                               BufToAnalyze2_Time = new long[time2_position_sign2 - time1_position_sign2];
                               BufToAnalyze2_Data = new int[time2_position_sign2 - time1_position_sign2];

                               System.arraycopy(DecodedSignal2_Time, time1_position_sign2, BufToAnalyze2_Time, 0, (time2_position_sign2 - time1_position_sign2));
                               System.arraycopy(DecodedSignal2_Data, time1_position_sign2, BufToAnalyze2_Data, 0, (time2_position_sign2 - time1_position_sign2));

                               graphic2.PutAndCentreSignal(BufToAnalyze2_Time, BufToAnalyze2_Data, (int) (time2 - time1));//, (int)signal2_average);
                           }

                           if (DecodedSignal3_Data.length > 0) {
                               BufToAnalyze3_Time = new long[time2_position_sign3 - time1_position_sign3];
                               BufToAnalyze3_Data = new int[time2_position_sign3 - time1_position_sign3];

                               System.arraycopy(DecodedSignal3_Time, time1_position_sign3, BufToAnalyze3_Time, 0, (time2_position_sign3 - time1_position_sign3));
                               System.arraycopy(DecodedSignal3_Data, time1_position_sign3, BufToAnalyze3_Data, 0, (time2_position_sign3 - time1_position_sign3));


                               graphic3.PutAndCentreSignal(BufToAnalyze3_Time, BufToAnalyze3_Data, (int) (time2 - time1));//, (int)signal3_average);
                           }

                           //seleziono gli eventi da mandare
                           int i;
                           if(MagicEvents.length >0){
                               tmp_long = new  int[MagicEvents.length];
                               for(i=0; i<MagicEvents.length; i++){
                                   if((MagicEvents[i] > time1)&&(MagicEvents[i]<time2)){//se è compreso tra min e max
                                       tmp_long[i] = (int)graphic1.RemapData_X(MagicEvents[i]);

                                   }else if(MagicEvents[i] > time2){
                                       break;
                                   }
                               }
                               int[] EventsToPlot = new int[i];
                               System.arraycopy(tmp_long, 0, EventsToPlot, 0, i);   //tmp_long contiene solo i valori copiati  e non gli eventuali zeri finali
                               myBigFrame.DrawVerticalLinesOnEvents(EventsToPlot);
                           }


                       }
                   }).start();

               }
               myBigFrame.PutLinesAtBorders();
           }// fine se ho almeno 5 secondi


       }else{ //linea 2 prima di linea 1 --> errore
           call_toast("Line 1 must come BEFORE Line 2");
       }
   }

    //==========================================================================
    private int getArrayAverage(int[] data){
        //==========================================================================
        int i;
        float mean = 0;
        for(i=0; i<data.length;i++){
            mean += data[i];
        }
        mean /= data.length;
        return (int)mean;
    }

    //==========================================================================
    public void ResetView(View v) {
        //==========================================================================
        if(!IsViewingAllTheAcquisition) {
            IsViewingAllTheAcquisition = true;
            isSegmentSelected = false;

            new Thread(new Runnable() {

                public void run() {
                    if (DecodedSignal1_Data.length > 0) {
                        BufToAnalyze1_Data = null;
                        BufToAnalyze1_Time = null;
                        graphic1.PutAndCentreSignal(DecodedSignal1_Time, DecodedSignal1_Data, DecodedSignal1_Data.length * T_sample_200Hz);
                    }
                    if (DecodedSignal2_Data.length > 0) {
                        BufToAnalyze2_Data = null;
                        BufToAnalyze2_Time = null;
                        graphic2.PutAndCentreSignal(DecodedSignal2_Time, DecodedSignal2_Data, DecodedSignal2_Data.length * T_sample_200Hz);
                    }
                    if (DecodedSignal3_Data.length > 0) {
                        BufToAnalyze3_Data = null;
                        BufToAnalyze3_Time = null;
                        graphic3.PutAndCentreSignal(DecodedSignal3_Time, DecodedSignal3_Data, DecodedSignal3_Data.length * T_sample_200Hz);
                    }
                    if(MagicEvents_displayUnits.length > 0){
                        myBigFrame.DrawVerticalLinesOnEvents(MagicEvents_displayUnits);
                    }
                }

            }).start();
        }
    }

    private boolean isTimeExtractionEnabled = false;
    PrintTimeClass PrintTime;
    //==========================================================================
    public void EnableTimeTasks(View v){
        //==========================================================================
        if(!isTimeExtractionEnabled) {
            isTimeExtractionEnabled = true;
            PrintTime.StartThread();
        }
    }

    //==========================================================================
    private class PrintTimeClass extends Thread {
        //==========================================================================
        private Thread theThread;
        private boolean ThreadRunning = false;

        //==========================================================================
        public void StartThread(){
            //==========================================================================
            // fa partire il runnable
            if(!ThreadRunning) {
                theThread = new Thread(this);
                theThread.start();

                ThreadRunning = true;
            }
        }
        //==========================================================================
        public void StopThread(){
            //==========================================================================
            if(ThreadRunning) {
                // fa fermare il runnable
                theThread.interrupt();
                ThreadRunning = false;
            }
        }

        long time1_local = 0;
        long time2_local = 0;
        int x1_local = 0;
        int x2_local = 0;
        @Override
        //==========================================================================
        public void run() {
            //==========================================================================
            while(true) {

                x1_local = myBigFrame.get_Line1_X();//in unità display
                x2_local = myBigFrame.get_Line2_X();


                if (x1_local < x2_local) { //ok
                    time1_local = graphic1.ReverseRemapData_X(x1_local); //in millisecondi
                    time2_local = graphic1.ReverseRemapData_X(x2_local);
                }
                if((time2_local-time1_local)!= deltaT){
                    deltaT = (time2_local-time1_local) ;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            TimeInfo.setText(deltaT + " ms");
                        }
                    });

                    if(isTimeExtractionEnabled) {
                        graphic1.PutDataForBalloons(time1_local, time2_local);
                        graphic2.PutDataForBalloons(time1_local, time2_local);
                        graphic3.PutDataForBalloons(time1_local, time2_local);
                    }
                }
            }
        }
    }//fine sottoclasse



    private void get_time1_time2(){
        int x1 = myBigFrame.get_Line1_X();//in unità display
        int x2 = myBigFrame.get_Line2_X();
        time1 = graphic1.ReverseRemapData_X((int) (x1));
        time2 = graphic1.ReverseRemapData_X((int) (x2));
        deltaT = time2 - time1;
    }

    private double deltaT = 1;

    private double STI_RATIO = -1;
    private double TEI_INDEX = -1;

    private double PEP = -1;
    private double PEP_Start = -1;
    private double PEP_Stop = -1;
    //==========================================================================
    public void PEP_button_pressed(View v){
        //==========================================================================
        if(isTimeExtractionEnabled){
            x1 = myBigFrame.get_Line1_X();//in unità display
            x2 = myBigFrame.get_Line2_X();
            time1 = graphic1.ReverseRemapData_X((int) (x1));
            time2 = graphic1.ReverseRemapData_X((int) (x2));
            PEP_Start = time1 - time_mean[0] - graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            PEP_Stop = time2 - time_mean[0]- graphic1.ReverseRemapData_X((int)graphic1.deltaT);

            PEP_text.setText("" + deltaT);
            PEP = deltaT;

            STI_RATIO_set();
            AcqDataSaver.put_PEP_times(deltaT, PEP_Start, PEP_Stop);
        }else{
            if(AcqDataSaver.PEP_Start!=-1 && AcqDataSaver.PEP_Stop!=-1){
                myBigFrame.PutLinesAt((long)(graphic1.RemapData_X((long)AcqDataSaver.PEP_Start)+graphic1.deltaT),
                        (long)(graphic1.RemapData_X((long)AcqDataSaver.PEP_Stop)+graphic1.deltaT));
            }
        }
    }
    private double ICT = -1;
    private double ICT_Start = -1;
    private double ICT_Stop = -1;
    //==========================================================================
    public void ICT_button_pressed(View v){
        //==========================================================================
        if(isTimeExtractionEnabled){
            get_time1_time2();
            ICT_Start = time1 - time_mean[0] - graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            ICT_Stop = time2 - time_mean[0]- graphic1.ReverseRemapData_X((int)graphic1.deltaT);

            ICT_text.setText("" + deltaT);
            ICT = deltaT;
            TEI_INDEX_set();
            AcqDataSaver.put_ICT_times(deltaT, ICT_Start, ICT_Stop);
        }else{
            if(AcqDataSaver.ICT_Start!=-1 && AcqDataSaver.ICT_Stop!=-1){
                myBigFrame.PutLinesAt((long)(graphic1.RemapData_X((long)AcqDataSaver.ICT_Start)+graphic1.deltaT),
                        (long)(graphic1.RemapData_X((long)AcqDataSaver.ICT_Stop)+graphic1.deltaT));
            }
        }
    }
    private double LVET = -1;
    private double LVET_Start = -1;
    private double LVET_Stop = -1;
    //==========================================================================
    public void LVET_button_pressed(View v){
        //==========================================================================
        if(isTimeExtractionEnabled){
            get_time1_time2();
            LVET_Start = time1 - time_mean[0] - graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            LVET_Stop = time2 - time_mean[0]- graphic1.ReverseRemapData_X((int)graphic1.deltaT);

            LVET_text.setText("" + deltaT);
            LVET = deltaT;
            STI_RATIO_set();
            TEI_INDEX_set();
            AcqDataSaver.put_LVET_times(deltaT, LVET_Start, LVET_Stop);
        }else{
            if(AcqDataSaver.LVET_Start!=-1 && AcqDataSaver.LVET_Stop!=-1){
                myBigFrame.PutLinesAt((long)(graphic1.RemapData_X((long)AcqDataSaver.LVET_Start)+graphic1.deltaT),
                        (long)(graphic1.RemapData_X((long)AcqDataSaver.LVET_Stop)+graphic1.deltaT));
            }
        }
    }
    private double IRT = -1;
    private double IRT_Start = -1;
    private double IRT_Stop = -1;
    //==========================================================================
    public void IRT_button_pressed(View v){
        //==========================================================================
        if(isTimeExtractionEnabled){
            get_time1_time2();
            IRT_Start = time1 - time_mean[0] - graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            IRT_Stop = time2 - time_mean[0]- graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            IRT_text.setText("" + deltaT);
            IRT = deltaT;
            TEI_INDEX_set();
            AcqDataSaver.put_IRT_times(deltaT, IRT_Start, IRT_Stop);
        }else{
            if(AcqDataSaver.IRT_Start!=-1 && AcqDataSaver.IRT_Stop!=-1){
                myBigFrame.PutLinesAt((long)(graphic1.RemapData_X((long)AcqDataSaver.IRT_Start)+graphic1.deltaT),
                        (long)(graphic1.RemapData_X((long)AcqDataSaver.IRT_Stop)+graphic1.deltaT));
            }
        }
    }
    private double PTT = -1;
    private double PTT_Start = -1;
    private double PTT_Stop = -1;
    //==========================================================================
    public void PTT_button_pressed(View v){
        //==========================================================================
        if(isTimeExtractionEnabled){
            get_time1_time2();
            PTT_Start = time1 - time_mean[0] - graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            PTT_Stop = time2 - time_mean[0]- graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            PTT_text.setText("" + deltaT);
            PTT = deltaT;
            AcqDataSaver.put_PTT_times(deltaT, PTT_Start, PTT_Stop);
        }else{
            if(AcqDataSaver.PTT_Start!=-1 && AcqDataSaver.PTT_Stop!=-1){
                myBigFrame.PutLinesAt((long) (graphic1.RemapData_X((long) AcqDataSaver.PTT_Start) + graphic1.deltaT),
                        (long) (graphic1.RemapData_X((long) AcqDataSaver.PTT_Stop) + graphic1.deltaT));
            }
        }
    }
    private double PAT = -1;
    private double PAT_Start = -1;
    private double PAT_Stop = -1;
    //==========================================================================
    public void PAT_button_pressed(View v){
        //==========================================================================
        if(isTimeExtractionEnabled){
            get_time1_time2();
            PAT_Start = time1 - time_mean[0] - graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            PAT_Stop = time2 - time_mean[0]- graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            PAT_text.setText("" + deltaT);
            PAT = deltaT;
            AcqDataSaver.put_PAT_times(deltaT, PAT_Start, PAT_Stop);
        }else{
            if(AcqDataSaver.PAT_Start!=-1 && AcqDataSaver.PAT_Stop!=-1){
                myBigFrame.PutLinesAt((long)(graphic1.RemapData_X((long)AcqDataSaver.PAT_Start)+graphic1.deltaT),
                        (long)(graphic1.RemapData_X((long)AcqDataSaver.PAT_Stop)+graphic1.deltaT));
            }
        }
    }
    private double PR = -1;
    private double PR_Start = -1;
    private double PR_Stop = -1;
    //==========================================================================
    public void PR_button_pressed(View v){
        //==========================================================================
        if(isTimeExtractionEnabled){
            get_time1_time2();
            PR_Start = time1 - time_mean[0] - graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            PR_Stop = time2 - time_mean[0]- graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            PR_text.setText("" + deltaT);
            PR = deltaT;
            AcqDataSaver.put_PR_times(deltaT, PR_Start, PR_Stop);
        }else{
            if(AcqDataSaver.PR_Start!=-1 && AcqDataSaver.PR_Stop!=-1){
                myBigFrame.PutLinesAt((long)(graphic1.RemapData_X((long)AcqDataSaver.PR_Start)+graphic1.deltaT),
                        (long)(graphic1.RemapData_X((long)AcqDataSaver.PR_Stop)+graphic1.deltaT));
            }
        }
    }

    private double QTC = 0;
    private double QT = 0;
    private double QT_Start = -1;
    private double QT_Stop = -1;
    //==========================================================================
    public void QT_button_pressed(View v){
        //==========================================================================
        if(isTimeExtractionEnabled){
            get_time1_time2();
            QT_Start = time1 - time_mean[0] - graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            QT_Stop = time2 - time_mean[0]- graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            QT = deltaT;
            QT_text.setText("" + QT);
            QTC = (QT/1000.0) / Math.sqrt(RR_MEAN / 1000.0);
            AcqDataSaver.put_QTC_times(QTC);
            AcqDataSaver.put_QT_times(QT, QT_Start, QT_Stop);
            QTC_text.setText("QTC\n" + String.format("%.02f", QTC) + " \n");

            String text_color = TimeIntervals.CheckRange_QTC(QTC);
            QTC_text.setBackgroundColor(Color.parseColor(text_color));

            QTC_text.setVisibility(View.VISIBLE);
        }else{
            if(AcqDataSaver.QT_Start!=-1 && AcqDataSaver.QT_Stop!=-1){
                myBigFrame.PutLinesAt((long)(graphic1.RemapData_X((long)AcqDataSaver.QT_Start)+graphic1.deltaT),
                        (long)(graphic1.RemapData_X((long)AcqDataSaver.QT_Stop)+graphic1.deltaT));
            }
        }
    }


    private double QRS = -1;
    private double QRS_Start = -1;
    private double QRS_Stop = -1;
    //==========================================================================
    public void QRS_button_pressed(View v){
        //==========================================================================
        if(isTimeExtractionEnabled){
            get_time1_time2();
            QRS_Start = time1 - time_mean[0] - graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            QRS_Stop = time2 - time_mean[0]- graphic1.ReverseRemapData_X((int)graphic1.deltaT);
            QRS_text.setText("" + deltaT);
            QRS = deltaT;
            AcqDataSaver.put_QRS_times(deltaT, QRS_Start, QRS_Stop);
        }else{
            if(AcqDataSaver.QRS_Start!=-1 && AcqDataSaver.QRS_Stop!=-1){
                myBigFrame.PutLinesAt((long)(graphic1.RemapData_X((long)AcqDataSaver.QRS_Start)+graphic1.deltaT),
                        (long)(graphic1.RemapData_X((long)AcqDataSaver.QRS_Stop)+graphic1.deltaT));
            }
        }
    }

    //==========================================================================
    private void STI_RATIO_set(){
        //==========================================================================
        if(PEP != -1 && LVET > 0){
            STI_RATIO = (float)PEP/(float)LVET;
            STI_text.setText("STI RATIO\n" + String.format("%.02f", STI_RATIO) +" \n");
            STI_text.setVisibility(View.VISIBLE);

            AcqDataSaver.put_STI_times(STI_RATIO);

            String text_color = TimeIntervals.CheckRange_STI(STI_RATIO);
            STI_text.setBackgroundColor(Color.parseColor(text_color));


        }
    }
    //==========================================================================
    private void TEI_INDEX_set(){
        //==========================================================================
        if(ICT != -1 && IRT != -1 && LVET > 0){
            TEI_INDEX = (float)(ICT + IRT)/(float)LVET;
            TEI_text.setText("TEI INDEX\n" + String.format("%.02f", TEI_INDEX) + " \n");
            TEI_text.setVisibility(View.VISIBLE);

            AcqDataSaver.put_TEI_times(TEI_INDEX);

            String text_color = TimeIntervals.CheckRange_STI(TEI_INDEX);
            TEI_text.setBackgroundColor(Color.parseColor(text_color));
        }
    }
    //==========================================================================
    private void ClearTimes(){
        //==========================================================================
        PEP = -1;
        ICT = -1;
        LVET = -1;
        IRT = -1;
        PTT = -1;
        PAT = -1;
        PR = -1;
        QT = -1;
        QTC = -1;
        STI_RATIO = -1;
        TEI_INDEX = -1;
    }

    String InfoFile = "";
    //==========================================================================
    public void SaveData(View v){
        //==========================================================================
        InfoFile = FileToBeAnalyzed.substring(0, FileToBeAnalyzed.length()-4) + "_inf";


        boolean thereIsOldTimeInfo = AcqDataSaver.thereIsOldTimeInfo(FileToBeAnalyzed);
        if(thereIsOldTimeInfo){
            GoToDialogActivity(REQUEST_DATA_DIALOG_OVERWRITEINTERVALS, "Intervals have been already stored! Overwrite them?", "Press OK to overwrite data, or CANCEL to continue without saving", "OK", "CANCEL");
        }else{
            // salvo i parametri di tempo accodando al vecchio inf
            //accodo al FileToBeAnalyzed + "_inf"
            if(isTimeExtractionEnabled) {   // salvo dati temporali solo se ho attivato la funzioneù
                AcqDataSaver.StoreTimes(FileToBeAnalyzed);
            }

            // salvo i segnali medi formato .wave
            if(Signal1_Mean != null) {
                StoreData1 = new BackgroundSave("Decoded/" + Ecg_FileName, "wave");
                StoreData1.OpenFile();
                StoreData1.StoreData(Signal1_Mean);
                // StoreData1.CloseFile();
            }

            if(Signal2_Mean != null) {
                StoreData2 = new BackgroundSave("Decoded/" + Seismo_FileName, "wave");
                StoreData2.OpenFile();
                StoreData2.StoreData(Signal2_Mean);
                //StoreData2.CloseFile();
            }

            if(Signal3_Mean != null) {
                StoreData3 = new BackgroundSave("Decoded/" + Pleth_FileName, "wave");
                StoreData3.OpenFile();
                StoreData3.StoreData(Signal3_Mean);
                //StoreData3.CloseFile();
            }

            call_toast("Save done.");
        }







    }


    // per la selezione del file da decodificare
    //==========================================================================
    public void GoToAnalysisSettings(View v){
        //==========================================================================
        if(!isConverting) {
            Intent intent = new Intent(ActivityAnalysis.this, Activity2.class);
            startActivityForResult(intent, REQUEST_DATA_FILE_SELECTION);
        }else{
            call_toast("Wait End of Conversion.");
        }
    }

    // per la modifca dei segnali sdi default da mostrare dopo la decodifica
    //==========================================================================
    public void GoToChangeDefaultsActivity(View v){
//==========================================================================
        if(!isConverting) {
            Intent intent = new Intent(ActivityAnalysis.this, ActivitySettings.class);
            intent.putExtra("user", UserSettings);
            intent.putExtra("mode", "signalSelection");
            startActivityForResult(intent, REQUEST_DATA_SIGNAL_SELECTION);
        }else{
            call_toast("Wait End of Conversion.");
        }
    }

    //==========================================================================
    public void GoToSendDataActivity(View v) {
        //==========================================================================

        GoToDialogActivity(REQUEST_DATA_DIALOG_TOSEND, "Leave this page?", "Press CANCEL to save data or OK to go to file transfer without saving", "OK", "CANCEL");

    }

    //==========================================================================
    public void GoToDialogActivity(int request, String Title, String Hint, String PositiveButtonText, String NegativeButtonText) {
        //==========================================================================
        Intent intent = new Intent(ActivityAnalysis.this, DialogActivity.class);
        intent.putExtra("title", Title);
        intent.putExtra("hint", Hint);
        intent.putExtra("pos", PositiveButtonText);
        intent.putExtra("neg", NegativeButtonText);


        startActivityForResult(intent, request);
    }

    //==========================================================================
    public void GoToChangeTimes(View v) {
        //==========================================================================
        Intent intent = new Intent(ActivityAnalysis.this, ActivityTimes.class);
        intent.putExtra("times", (Serializable) TimeIntervals);

        startActivityForResult(intent, REQUEST_DATA_MODIFY_TIMES);
    }

    //==========================================================================
    public void ActAnalysis_BackToActivity0(View v){
        //==========================================================================
        // chiedi se davvero vuoi tornare a home
        GoToDialogActivity(REQUEST_DATA_DIALOG_HOME, "Return to Home?", "You'll exit analysis", "OK", "CANCEL");
    }

    // metodo che viene chiamato dalla fine della settingsactivity
    @Override
    //==========================================================================
    //==========================================================================
    //==========================================================================
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //==========================================================================
        //==========================================================================
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DATA_FILE_SELECTION) {
            FileToBeAnalyzed = (String) data.getSerializableExtra("new_filename");    // mi salvo le eventuali modifiche
            UserSettings.FileName = FileToBeAnalyzed;

            FileName_view.setText("  Current File: " + FileToBeAnalyzed);


            AcqDataSaver = new InfoFileHandler(UserSettings);
            UserSettings = AcqDataSaver.GetVisualizedSignals(FileToBeAnalyzed);

            if (UserSettings == null){  //non c'è il file info e alla fine del file selection lo creo per le prossime volte
                thereWasInfoFile = false;
                SignalsAreChosen = false;
                UserSettings = new Settings();
                Defaults_textview.setText(  "Current File:\n\t" + FileToBeAnalyzed + "\n\n" +
                        "Current Signals:" +        "\n\tGraph 1:\t" + "to be chosen" +
                        "\n\tGraph 2:\t" + "to be chosen" +
                        "\n\tGraph 3:\t" + "to be chosen");

            }else {
                thereWasInfoFile = true;
                SignalsAreChosen = true;
                Defaults_textview.setText(  "Current File:\n\t" + FileToBeAnalyzed + "\n\n" +
                        "Current Signals:" +        "\n\tGraph 1:\t" + UserSettings.get_SignalName(0, false) +
                        "\n\tGraph 2:\t" + UserSettings.get_SignalName(1, false) +
                        "\n\tGraph 3:\t" + UserSettings.get_SignalName(2, false)  );
                UserSettings.ApplySignalChanges(UserSettings.EnabledSource, UserSettings.EnabledSignal, UserSettings.EnabledAxes);

                // se oltre al file info ci sono anche le vecchie analisi dai la possibilità di visualizzarle
                boolean thereIsOldAnalysis = AcqDataSaver.CheckOldAnalysis(FileToBeAnalyzed);
                if(thereIsOldAnalysis){
                    ViewLastAcq_button.setVisibility(View.VISIBLE);// do la possibilità di visualizzaro abilitando il tasto
                }
            }


            //==========================================================================
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DATA_SIGNAL_SELECTION) {
            UserSettings = (Settings) data.getSerializableExtra("new_settings");    // mi salvo le eventuali modifiche
            if(UserSettings != null) {
                SignalsAreChosen = true;
                Defaults_textview.setText("Current File:\n\t" + FileToBeAnalyzed + "\n\n" +
                                            "Current Signals:" + "\n\tGraph 1:\t" + UserSettings.get_SignalName(0, false) +
                                                                 "\n\tGraph 2:\t" + UserSettings.get_SignalName(1, false) +
                                                                 "\n\tGraph 3:\t" + UserSettings.get_SignalName(2, false));
            }

            //==========================================================================
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DATA_DIALOG_BACK) {
            boolean UserChoose = (boolean) data.getSerializableExtra("exit");
            if(UserChoose == true){
                //VUOLE DAVVERO TORNARE INDIETRO
                startActivity(new Intent(ActivityAnalysis.this, ActivityAnalysis.class));
                finish();
            }else{

            }

            //==========================================================================
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DATA_DIALOG_HOME) {
            boolean UserChoose = (boolean) data.getSerializableExtra("exit");
            if(UserChoose == true){
                //VUOLE DAVVERO TORNARE INDIETRO alla home
                startActivity(new Intent(ActivityAnalysis.this, Activity0.class));
                finish();
            }else{

            }

            //==========================================================================
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DATA_DIALOG_TOSEND) {
            boolean UserChoose = (boolean) data.getSerializableExtra("exit");
            if(UserChoose == true){
                //VUOLE DAVVERO andare al send data
                Intent intent = new Intent(ActivityAnalysis.this, Activity3.class);

                if(isTimeExtractionEnabled)
                    intent.putExtra("tempi", ("Decoded/" + InfoFile + ".txt"));
                else
                    intent.putExtra("tempi", "");


                //call_toast(Ecg_FileName);
                if(Signal1_Mean != null)
                    intent.putExtra("ecg", "Decoded/" + Ecg_FileName + ".wave");
                else
                    intent.putExtra("ecg", "");


                if(Signal2_Mean != null)
                    intent.putExtra("seismo", "Decoded/" + Seismo_FileName + ".wave");
                else
                    intent.putExtra("seismo", "");


                if(Signal2_Mean != null)
                    intent.putExtra("pleth", "Decoded/" + Pleth_FileName + ".wave");
                else
                    intent.putExtra("pleth", "");

                if(!thereWasInfoFile){
                    // costruisci info file per le prossime letture
                    UserSettings.SetFileName(FileToBeAnalyzed.substring(0, FileToBeAnalyzed.length() - 4)); // salvo il nome del file senza estensioni
                    InfoFileHandler InfoFileCreator = new InfoFileHandler(UserSettings);
                    InfoFileCreator.StoreInfoFile(FileToBeAnalyzed, null, null, (DecodedSignal1_Time[DecodedSignal1_Time.length-1] - DecodedSignal1_Time[0]) );
                }


                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }else{

            }
            //==========================================================================
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DATA_DIALOG_OVERWRITEINTERVALS) {
            boolean UserChoose = (boolean) data.getSerializableExtra("exit");
            if(UserChoose == true){
                // ha cliccato ok: vuole sovrascrivere i dati
                AcqDataSaver.OverwriteOldTimes(FileToBeAnalyzed);
                call_toast("Save done.");

            }else{
                // non salvo, non vuole sovrascrivere
            }
        }else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DATA_MODIFY_TIMES) {
            TimeIntervals = (HeartIntervals) data.getSerializableExtra("modified");
        }
    }

    //==========================================================================
    public void ViewLastAnalysis(View v){
        //==========================================================================
        int i;
        int j = 0;
        byte[] tmp = ReadTheWholeFile(SeismotePath_Decoded + AcqDataSaver.get_signal1_filename(FileToBeAnalyzed) + ".wave");
        Signal1_Mean = new int[tmp.length/4];
        time_mean = new long[Signal1_Mean.length];
        for(i=0; i< tmp.length-3; i+=4){ //conversione byte(8bit) --> int(4byte)
            Signal1_Mean[j] = ((int)(tmp[i])* 16777216) + (ConvertByteToUnsignedInt(tmp[i+1])* 65536) + (ConvertByteToUnsignedInt(tmp[i+2]) * 256) + ConvertByteToUnsignedInt(tmp[i+3]);
            time_mean[j] = j*T_sample_200Hz;//simulo un campione ogni 5 ms
            j++;
        }

        j=0;
        tmp = ReadTheWholeFile(SeismotePath_Decoded + AcqDataSaver.get_signal2_filename(FileToBeAnalyzed) + ".wave");
        Signal2_Mean = new int[tmp.length/4];
        //long[] Signal2_Mean_Time = new long[Signal2_Mean.length];
        for(i=0; i< tmp.length-3; i+=4){ //conversione byte(8bit) --> int(4byte)
            Signal2_Mean[j] = ((int)(tmp[i])* 16777216) + (ConvertByteToUnsignedInt(tmp[i+1])* 65536) + (ConvertByteToUnsignedInt(tmp[i+2]) * 256) + ConvertByteToUnsignedInt(tmp[i+3]);
            //Signal2_Mean_Time[j] = j*T_sample_200Hz;
            j++;
        }

        j=0;
        tmp = ReadTheWholeFile(SeismotePath_Decoded + AcqDataSaver.get_signal3_filename(FileToBeAnalyzed) + ".wave");
        Signal3_Mean = new int[tmp.length/4];
        //long[] Signal3_Mean_Time = new long[Signal3_Mean.length];
        for(i=0; i< tmp.length-3; i+=4){ //conversione byte(8bit) --> int(4byte)
            Signal3_Mean[j] = ((int)(tmp[i])* 16777216) + (ConvertByteToUnsignedInt(tmp[i+1])* 65536) + (ConvertByteToUnsignedInt(tmp[i+2]) * 256) + ConvertByteToUnsignedInt(tmp[i+3]);
            //Signal3_Mean_Time[j] = j*T_sample_200Hz;
            j++;
        }

        init_Graphs_Layouts();
        SetUpForPhaseNum(FEATURE_EXTRACTION_PHASE);
        analysis_phase_view.setText("PHASE: FEATURE EXTRACTION - REPLAY/MODIFY");
        Back_button.setVisibility(View.INVISIBLE);//non ho niente da mostrare nel back, nel caso usa home
        graphic1.PutAndCentreMeanSignal(time_mean, Signal1_Mean, Signal1_Mean.length * T_sample_200Hz * 2);
        graphic2.PutAndCentreMeanSignal(time_mean, Signal2_Mean, Signal2_Mean.length * T_sample_200Hz * 2);
        graphic3.PutAndCentreMeanSignal(time_mean, Signal3_Mean, Signal3_Mean.length * T_sample_200Hz * 2);

        // leggi gli intervalli salvati e mostrali nei box
        AcqDataSaver.ReadTimes(FileToBeAnalyzed);
        PEP_text.setText(String.format("%.02f", AcqDataSaver.PEP));
        PEP = AcqDataSaver.PEP;
        ICT_text.setText(String.format("%.02f", AcqDataSaver.ICT));
        ICT = AcqDataSaver.ICT;
        LVET_text.setText(String.format("%.02f", AcqDataSaver.LVET));
        LVET = AcqDataSaver.LVET;
        IRT_text.setText(String.format("%.02f", AcqDataSaver.IRT));
        IRT = AcqDataSaver.IRT;
        PTT_text.setText(String.format("%.02f", AcqDataSaver.PTT));
        PTT = AcqDataSaver.PTT;
        PAT_text.setText(String.format("%.02f", AcqDataSaver.PAT));
        PAT = AcqDataSaver.PAT;
        PR_text.setText(String.format("%.02f", AcqDataSaver.PR));
        PR = AcqDataSaver.PR;
        QT_text.setText(String.format("%.02f", AcqDataSaver.QT));
        QT = AcqDataSaver.QT;
        QRS_text.setText(String.format("%.02f", AcqDataSaver.QRS));
        QRS = AcqDataSaver.QRS;

        // mostra indici calcolati se presenti
        if(AcqDataSaver.QTC != -1){
            QTC = AcqDataSaver.QTC;
            QTC_text.setText("QTC\n" + String.format("%.02f", AcqDataSaver.QTC));
            QTC_text.setVisibility(View.VISIBLE);
            TimeIntervals.CheckRange_QTC(QTC);
        }
        if(AcqDataSaver.TEI != -1){
            TEI_INDEX = AcqDataSaver.TEI;
            TEI_text.setText("TEI INDEX\n" + String.format("%.02f", AcqDataSaver.TEI));
            TEI_text.setVisibility(View.VISIBLE);
            TimeIntervals.CheckRange_TEI(TEI_INDEX);
        }
        if(AcqDataSaver.STI != -1){
            STI_RATIO = AcqDataSaver.STI;
            STI_text.setText("STI RATIO\n" + String.format("%.02f", AcqDataSaver.STI));
            STI_text.setVisibility(View.VISIBLE);
        }
        // riga verde su picco r
        peak_position = 40;//da qui cerco la posizione esatta del picco r nei 100 ms prima e dopo della posiz iniziale
        for ( i=-20; i<20; i++){
            if(Signal1_Mean[i + 40] > Signal1_Mean[peak_position]){
                peak_position = i+40;
            }
        }
        myBigFrame.DrawVerticalLine((int)( graphic1.RemapData_X(peak_position*T_sample_200Hz)+ graphic1.deltaT));//linea su picco r ( posizione = delta t di visualizzazione + 200 millisecondi

        //dai la possibilità di modificare i tempi pep ecc che sono stati salvati alla scorsa acquisizione
        GetIntervals_button.setVisibility(View.VISIBLE);
        GetIntervals_button.setText("MODIFY TIMES");

    }

    //==========================================================================
    private int ConvertByteToUnsignedInt(byte toConvert){
        //==========================================================================
        if(toConvert<0){
            return (int)(toConvert + 256);
        }
        else
            return (int)toConvert;
    }
    //==========================================================================
    public void AddMrkBttn_Click(View v){
        //==========================================================================

        myBigFrame.AddMarker();
    }
    //==========================================================================
    public void RmvMrkBttn_Click(View v){
        //==========================================================================
        myBigFrame.ClearMarkers();
    }
}























