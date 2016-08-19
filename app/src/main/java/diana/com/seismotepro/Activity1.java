package diana.com.seismotepro;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Set;

// VISUALIZZAZIONE
//==========================================================================
public class Activity1 extends Activity {
    //==========================================================================
    TextView status_textview;
    Button StartStopButton;
    Button EventButton;
    Button RecButton;
    Button GoToSettingsButton;
    Button ChangeFileNameButton;
    TextView time_view;
    TextView byte_view;
    TextView vel_view;
    TextView filename_view;
    TextView info_view;
    TextView phase_view;
    TextView timer_rec_view;


    Button Zoom1up;
    Button Zoom1down;
    Button Zoom2up;
    Button Zoom2down;
    Button Zoom3up;
    Button Zoom3down;
    Button Zoom4up;
    Button Zoom4down;

    /*Button Shift1up;
    Button Shift1down;
    Button Shift2up;
    Button Shift2down;
    Button Shift3up;
    Button Shift3down;*/
    Button AutoBias1;
    Button AutoBias2;
    Button AutoBias3;
    Button AutoBias4;

    TextView PlotSignal1_view;
    TextView PlotSignal2_view;
    TextView PlotSignal3_view;
    TextView PlotSignal4_view;

    Button PackErr_button;
    Button FakeButton;

    long Tstart;
    long Tstop;
    long Tacquisition;
    Date Tstart_date;
    Date Tstop_date;

    private boolean isAcquiring = false;
    private boolean isRecording = false;

    private final static short GRAPH_POINTS = 1000;
    private final static short GRAPH_POINTS_TO_PLOT = 50;

    private myGraphs plotter1;
    private myGraphs plotter2;
    private myGraphs plotter3;
    private myGraphs plotter4;

    StoreThread SaveRawData;
    BackgroundSave RawDataStorer;
    BluetoothHandler myBT;
    DataDecoder myDataDecoder;

    WindowManager.LayoutParams NewLayoutParams = null;
    int DisplayWidth_pixel;
    int DisplayHeight_pixel;
    FrameLayout PlotFrame1;
    FrameLayout PlotFrame2;
    FrameLayout PlotFrame3;
    FrameLayout PlotFrame4;

    private final static int PIPE_DIMENSION_IN_BYTE = 1024*256;
    private final static int FILE_PART_DIMENSION_IN_BYTE = 1024*32;
    private final static int SIZE_SHARED_MEM_STORE = FILE_PART_DIMENSION_IN_BYTE*2;
    private final static int SIZE_SHARED_MEM_DECODE = 2*1024;
    //private final static int SIZE_SHARED_MEM_PLOT ;

    ByteDataExchange BTStore_SharedMem;
    ByteDataExchange BTPlot_SharedMem;
    IntDataExchange DecodePlot_SharedMem1;
    IntDataExchange DecodePlot_SharedMem2;
    IntDataExchange DecodePlot_SharedMem3;
    IntDataExchange DecodePlot_SharedMem4;

    private Settings UserSettings;
    private int REQUEST_DATA = 0;

    GlobalSettingsFileHandler AppSettings;

    @Override
    //==========================================================================
    protected void onCreate(Bundle savedInstanceState) {
        //==========================================================================
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_1);

        status_textview = (TextView)findViewById(R.id.textView_status);
        filename_view = (TextView)findViewById(R.id.textView_filename);
        time_view = (TextView)findViewById(R.id.textView2);
        byte_view = (TextView)findViewById(R.id.textView1);
        vel_view = (TextView)findViewById(R.id.textView3);
        info_view = (TextView)findViewById(R.id.textView_info);
        phase_view = (TextView)findViewById(R.id.textView_acquisition_phase);
        timer_rec_view = (TextView)findViewById(R.id.textView_timerec);

        GoToSettingsButton = (Button) findViewById(R.id.settings_button);
        ChangeFileNameButton = (Button) findViewById(R.id.button_changefilename);
        StartStopButton = (Button) findViewById(R.id.start_button);
        RecButton = (Button) findViewById(R.id.button_rec);
        EventButton = (Button) findViewById(R.id.button_event);
        EventButton.setVisibility(View.INVISIBLE);
        Zoom1up = (Button)findViewById(R.id.button_zoom1up);
        Zoom1down = (Button)findViewById(R.id.button_zoom1down);
        Zoom2up = (Button)findViewById(R.id.button_zoom2up);
        Zoom2down = (Button)findViewById(R.id.button_zoom2down);
        Zoom3up = (Button)findViewById(R.id.button_zoom3up);
        Zoom3down = (Button)findViewById(R.id.button_zoom3down);
        Zoom4up = (Button)findViewById(R.id.button_zoom4up);
        Zoom4down = (Button)findViewById(R.id.button_zoom4down);

        /*Shift1up = (Button)findViewById(R.id.button_shift1up);
        Shift1down = (Button)findViewById(R.id.button_shift1down);
        Shift2up = (Button)findViewById(R.id.button_shift2up);
        Shift2down = (Button)findViewById(R.id.button_shift2down);
        Shift3up = (Button)findViewById(R.id.button_shift3up);
        Shift3down = (Button)findViewById(R.id.button_shift3down);*/
        AutoBias1 = (Button)findViewById(R.id.button_autobias1);
        AutoBias2 = (Button)findViewById(R.id.button_autobias2);
        AutoBias3 = (Button)findViewById(R.id.button_autobias3);
        AutoBias4 = (Button)findViewById(R.id.button_autobias4);

        PlotSignal1_view = (TextView)findViewById(R.id.textView_signal1);
        PlotSignal2_view = (TextView)findViewById(R.id.textView_signal2);
        PlotSignal3_view = (TextView)findViewById(R.id.textView_signal3);
        PlotSignal4_view = (TextView)findViewById(R.id.textView_signal4);



        BTStore_SharedMem = new ByteDataExchange(SIZE_SHARED_MEM_STORE);
        BTPlot_SharedMem = new ByteDataExchange(SIZE_SHARED_MEM_DECODE);
        DecodePlot_SharedMem1 = new IntDataExchange();
        DecodePlot_SharedMem2 = new IntDataExchange();
        DecodePlot_SharedMem3 = new IntDataExchange();
        DecodePlot_SharedMem4 = new IntDataExchange();


        UserSettings = new Settings();

        // legge le impostazioni dal file di settings globale della app
        AppSettings = new GlobalSettingsFileHandler(UserSettings);
        UserSettings = AppSettings.Read_AppSettingsInfo();
        if(UserSettings == null){
            UserSettings = new Settings();
            AppSettings = new GlobalSettingsFileHandler(UserSettings);
            AppSettings.Write_AppSettingsInfo();
        }

        UserSettings.ApplySignalChanges(UserSettings.EnabledSource, UserSettings.EnabledSignal, UserSettings.EnabledAxes);

        myBT = new BluetoothHandler(getApplicationContext(),BTStore_SharedMem, BTPlot_SharedMem);
        myDataDecoder = new DataDecoder( UserSettings, BTPlot_SharedMem, DecodePlot_SharedMem1, DecodePlot_SharedMem2, DecodePlot_SharedMem3, DecodePlot_SharedMem4);

        PlotFrame1 = (FrameLayout)findViewById(R.id.plot_frame1);
        PlotFrame2 = (FrameLayout)findViewById(R.id.plot_frame2);
        PlotFrame3 = (FrameLayout)findViewById(R.id.plot_frame3);
        PlotFrame4 = (FrameLayout)findViewById(R.id.plot_frame4);

        RawDataStorer = new BackgroundSave(UserSettings.FileName, "raw");
        filename_view.setText("File name: " + UserSettings.FileName);

        SetLayout();

        info_view.setTextColor(Color.BLUE);
        info_view.setText("Switch MagIC ON and press START");
        phase_view.setTextColor(Color.BLUE);
        phase_view.setText("PHASE: SET UP");

        FakeButton = (Button)findViewById(R.id.fake_button);


    }

    //==========================================================================
    private void SetLayout(){
        //==========================================================================
        DisplayMetrics DisplayMetrics = this.getResources().getDisplayMetrics();
        DisplayWidth_pixel = DisplayMetrics.widthPixels;
        DisplayHeight_pixel = DisplayMetrics.heightPixels;

        int TopFreeSpace = 30;
        int BottomFreeSpace = StartStopButton.getMeasuredHeight() +  info_view.getMeasuredHeight();
        if(BottomFreeSpace < 120){//== 0){
            BottomFreeSpace = 120;
        }

        int BoxHeigth = (DisplayHeight_pixel - BottomFreeSpace) / 4;



        RelativeLayout.LayoutParams Frame1_lp = new RelativeLayout.LayoutParams(DisplayWidth_pixel,BoxHeigth);
        Frame1_lp.setMargins(0, TopFreeSpace, 0, 0);
        PlotFrame1.setLayoutParams(Frame1_lp);
        PlotFrame1.setBackgroundColor(Color.WHITE);
        PlotFrame1.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        RelativeLayout.LayoutParams Frame2_lp = new RelativeLayout.LayoutParams(DisplayWidth_pixel,BoxHeigth);
        Frame2_lp.setMargins(0, BoxHeigth + TopFreeSpace, 0, 0);
        PlotFrame2.setLayoutParams(Frame2_lp);
        PlotFrame2.setBackgroundColor(Color.WHITE);
        PlotFrame2.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        RelativeLayout.LayoutParams Frame3_lp = new RelativeLayout.LayoutParams(DisplayWidth_pixel,BoxHeigth);
        Frame3_lp.setMargins(0, 2 * (BoxHeigth) + TopFreeSpace, 0, 0);
        PlotFrame3.setLayoutParams(Frame3_lp);
        PlotFrame3.setBackgroundColor(Color.WHITE);
        PlotFrame3.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        RelativeLayout.LayoutParams Frame4_lp = new RelativeLayout.LayoutParams(DisplayWidth_pixel,BoxHeigth);
        Frame4_lp.setMargins(0, 3 * (BoxHeigth) + TopFreeSpace, 0, 0);
        PlotFrame4.setLayoutParams(Frame4_lp);
        PlotFrame4.setBackgroundColor(Color.WHITE);
        PlotFrame4.setLayerType(View.LAYER_TYPE_HARDWARE, null);


        plotter1 = new myGraphs(this, DisplayWidth_pixel, BoxHeigth, DecodePlot_SharedMem1, 0, UserSettings);
        plotter2 = new myGraphs(this, DisplayWidth_pixel, BoxHeigth, DecodePlot_SharedMem2, 1, UserSettings);
        plotter3 = new myGraphs(this, DisplayWidth_pixel, BoxHeigth, DecodePlot_SharedMem3, 2, UserSettings);
        plotter4 = new myGraphs(this, DisplayWidth_pixel, BoxHeigth, DecodePlot_SharedMem4, 3, UserSettings);
        PlotFrame1.addView(plotter1);   // lego plotter1 a plotframe1
        PlotFrame2.addView(plotter2);   // lego plotter1 a plotframe2
        PlotFrame3.addView(plotter3);   // lego plotter1 a plotframe3
        PlotFrame4.addView(plotter4);   // lego plotter1 a plotframe4


        positionControls(Zoom1up, TopFreeSpace, "left");
        positionControls(Zoom2up, BoxHeigth + TopFreeSpace, "left");
        positionControls(Zoom3up, BoxHeigth*2+TopFreeSpace, "left");
        positionControls(Zoom4up, BoxHeigth*3+TopFreeSpace, "left");

        /*positionControls(Shift1up, TopFreeSpace, "right");
        positionControls(Shift2up, BoxHeigth+TopFreeSpace, "right");
        positionControls(Shift3up, BoxHeigth*2+TopFreeSpace, "right");*/

        positionControls(AutoBias1, TopFreeSpace+(BoxHeigth/4), "right");
        positionControls(AutoBias2, BoxHeigth+TopFreeSpace+(BoxHeigth/4), "right");
        positionControls(AutoBias3, (BoxHeigth*2)+TopFreeSpace+(BoxHeigth/4), "right");
        positionControls(AutoBias4, (BoxHeigth*3)+TopFreeSpace+(BoxHeigth/4), "right");

        plotter1.GraphRedraw();
        plotter2.GraphRedraw();
        plotter3.GraphRedraw();
        plotter4.GraphRedraw();
    }

    //==========================================================================
    private void positionControls(Button button, int topMargin, String align){
        //==========================================================================
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                ((int) ViewGroup.LayoutParams.WRAP_CONTENT, (int) ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = topMargin;
        if(align.equals("right")){
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else if (align.equals("left")){
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        button.setLayoutParams(params);
    }

    //==========================================================================
    public void ZoomEvent(View v){
        //==========================================================================
        if(v.getId() == Zoom1up.getId()){
            plotter1.SetZoomingScale(1);
        }else if(v.getId() == Zoom1down.getId()){
            plotter1.SetZoomingScale(-1);
        }else if(v.getId() == Zoom2up.getId()){
            plotter2.SetZoomingScale(1);
        }else if(v.getId() == Zoom2down.getId()){
            plotter2.SetZoomingScale(-1);
        }else if(v.getId() == Zoom3up.getId()){
            plotter3.SetZoomingScale(1);
        }else if(v.getId() == Zoom3down.getId()) {
            plotter3.SetZoomingScale(-1);
        }else if(v.getId() == Zoom4up.getId()){
            plotter4.SetZoomingScale(1);
        }else if(v.getId() == Zoom4down.getId()) {
            plotter4.SetZoomingScale(-1);
        }
    }
    //==========================================================================
    public void OffsetEvent(View v){
        //==========================================================================
        /*if(v.getId() == Shift1up.getId()){
            plotter1.SetOffset(1);
        }else if(v.getId() == Shift1down.getId()){
            plotter1.SetOffset(-1);
        }else if(v.getId() == Shift2up.getId()){
            plotter2.SetOffset(1);
        }else if(v.getId() == Shift2down.getId()){
            plotter2.SetOffset(-1);
        }else if(v.getId() == Shift3up.getId()){
            plotter3.SetOffset(1);
        }else if(v.getId() == Shift3down.getId()) {
            plotter3.SetOffset(-1);
        }*/
    }
    //==========================================================================
    public void AutoBias(View v){
        //==========================================================================
        if(v.getId() == AutoBias1.getId()){
            plotter1.AutoBias();
        }else if(v.getId() == AutoBias2.getId()){
            plotter2.AutoBias();
        }else if(v.getId() == AutoBias3.getId()){
            plotter3.AutoBias();
        }else if(v.getId() == AutoBias4.getId()){
            plotter4.AutoBias();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity1, menu);
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
    //==========================================================================
    public void onResume() {
        //==========================================================================
        super.onResume();
        status_textview = (TextView)findViewById(R.id.textView_status);
        filename_view = (TextView)findViewById(R.id.textView_filename);
        time_view = (TextView)findViewById(R.id.textView2);
        byte_view = (TextView)findViewById(R.id.textView1);
        vel_view = (TextView)findViewById(R.id.textView3);
        info_view = (TextView)findViewById(R.id.textView_info);
        phase_view = (TextView)findViewById(R.id.textView_acquisition_phase);
        timer_rec_view = (TextView)findViewById(R.id.textView_timerec);

        GoToSettingsButton = (Button) findViewById(R.id.settings_button);
        ChangeFileNameButton = (Button) findViewById(R.id.button_changefilename);
        StartStopButton = (Button) findViewById(R.id.start_button);
        RecButton = (Button) findViewById(R.id.button_rec);
        EventButton = (Button) findViewById(R.id.button_event);
        EventButton.setVisibility(View.INVISIBLE);
        Zoom1up = (Button)findViewById(R.id.button_zoom1up);
        Zoom1down = (Button)findViewById(R.id.button_zoom1down);
        Zoom2up = (Button)findViewById(R.id.button_zoom2up);
        Zoom2down = (Button)findViewById(R.id.button_zoom2down);
        Zoom3up = (Button)findViewById(R.id.button_zoom3up);
        Zoom3down = (Button)findViewById(R.id.button_zoom3down);
        Zoom4up = (Button)findViewById(R.id.button_zoom4up);
        Zoom4down = (Button)findViewById(R.id.button_zoom4down);

        /*Shift1up = (Button)findViewById(R.id.button_shift1up);
        Shift1down = (Button)findViewById(R.id.button_shift1down);
        Shift2up = (Button)findViewById(R.id.button_shift2up);
        Shift2down = (Button)findViewById(R.id.button_shift2down);
        Shift3up = (Button)findViewById(R.id.button_shift3up);
        Shift3down = (Button)findViewById(R.id.button_shift3down);*/
        AutoBias1 = (Button)findViewById(R.id.button_autobias1);
        AutoBias2 = (Button)findViewById(R.id.button_autobias2);
        AutoBias3 = (Button)findViewById(R.id.button_autobias3);
        AutoBias4 = (Button)findViewById(R.id.button_autobias4);

        PlotSignal1_view = (TextView)findViewById(R.id.textView_signal1);
        PlotSignal2_view = (TextView)findViewById(R.id.textView_signal2);
        PlotSignal3_view = (TextView)findViewById(R.id.textView_signal3);
        PlotSignal4_view = (TextView)findViewById(R.id.textView_signal4);


        PlotSignal1_view.setTextColor(Color.BLACK);
        PlotSignal2_view.setTextColor(Color.BLACK);
        PlotSignal3_view.setTextColor(Color.BLACK);
        PlotSignal4_view.setTextColor(Color.BLACK);
        PlotSignal1_view.setTextSize(17);
        PlotSignal2_view.setTextSize(17);
        PlotSignal3_view.setTextSize(17);
        PlotSignal4_view.setTextSize(17);
        PlotSignal1_view.setText(UserSettings.get_SignalName(0, true));
        PlotSignal2_view.setText(UserSettings.get_SignalName(1, true));
        PlotSignal3_view.setText(UserSettings.get_SignalName(2, true));
        PlotSignal4_view.setText(UserSettings.get_SignalName(3, true));

        // VISUALIZZO TESTI DI DEBUG SOLO SE SIAMO IN MODALITà DEVELOPER
        if(UserSettings.DeveloperMode == true){
            FakeButton.setVisibility(View.VISIBLE);
            status_textview.setVisibility(View.VISIBLE);
            time_view.setVisibility(View.VISIBLE);
            byte_view.setVisibility(View.VISIBLE);
            vel_view.setVisibility(View.VISIBLE);

        }else{  // modalità medico: nascondi tasti e campi di testo di debug
            FakeButton.setVisibility(View.INVISIBLE);
            status_textview.setVisibility(View.INVISIBLE);
            time_view.setVisibility(View.INVISIBLE);
            byte_view.setVisibility(View.INVISIBLE);
            vel_view.setVisibility(View.INVISIBLE);

        }

    }
    @Override
    //==========================================================================
    public void onStop() {
        //==========================================================================
        super.onStop();

    }


    //==========================================================================
    public void Act1_BackToActivity0(View v){
        //==========================================================================
        // chiude questa attività e torna alla attività 0
        Intent intent = new Intent(Activity1.this, Activity0.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    //==========================================================================
    public void Act1_GoToActivityAnalysis(View v){
        //==========================================================================
        if (isAcquiring){
            StopAcquire();
        }
        Intent intent = new Intent(Activity1.this, ActivityAnalysis.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    // forza l'orientazione del tablet sempre in modalità landscape
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation/keyboard change
        super.onConfigurationChanged(newConfig);
    }

    private boolean isTheFirstAcquisition = true;
    //==========================================================================
    public void StartStopAcquisition(View v){
        //==========================================================================
        if(isAcquiring){    // spegni visualizzazione

            Tstop_date = new Date();
            StopAcquire();
            Tstop =  SystemClock.elapsedRealtime();
            Tacquisition = Tstop - Tstart;

            time_view.setText("" + Tacquisition + " ms_ ");
            byte_view.setText("" + myBT.BytesReceived + " B_ ");
            if(Tacquisition!=0) {
                long velocità = myBT.BytesReceived / (Tacquisition / 1000);
                vel_view.setText("-->" + velocità + " B/s_ ");
            }
            isAcquiring = false;
            info_view.setText("Press START to begin another acquisition");
            phase_view.setText("PHASE: NEW SETUP");


        }else if(!isAcquiring){ //accendi visualizzazione
            ChangeFileNameButton.setVisibility(View.INVISIBLE);

            if(isTheFirstAcquisition) {  // toppa per evitare grafici brutti alla seconda acquisizione
                isTheFirstAcquisition = false;

                if (StartAcquire() == true) {

                    info_view.setText("To record data, press REC");

                    Tstart = SystemClock.elapsedRealtime();
                    Tstart_date = new Date();
                    isAcquiring = true;
                    phase_view.setText("PHASE: SIGNAL VISUALIZATION (not recording)");
                }
            } else {// è la seconda acquisizione
                startActivity(new Intent(Activity1.this, Activity1.class));
            finish();
            }
        }
    }

    //==========================================================================
    public void StartStopRecording(View v){
        //==========================================================================
        if(isRecording){
            //STOP RECORDINGS
            isRecording = false;
            myBT.put_isRecording(false);
            StopRec();
            phase_view.setText("PHASE: SIGNAL VISUALIZATION");
            info_view.setText("Press STOP to end acquisition");

            InfoFileHandler InfoFileCreator = new InfoFileHandler(UserSettings);
            InfoFileCreator.StoreInfoFile( UserSettings.FileName, Tstart_date, Tstop_date, Tacquisition);

        }else if(!isRecording && isAcquiring) { // se è gia partira l'acquisizione
            // START RECORDING
            isRecording = true;
            myBT.put_isRecording(true);
            StartRec();
            info_view.setText("Press REC to end recording or STOP to end acquisition");
            phase_view.setText("PHASE: SIGNAL RECORDING");
        } else{
            call_toast("Start acquisition before");
        }
    }
    //==========================================================================
    private void StartRec(){
        //==========================================================================
        BTStore_SharedMem.Reset();

        // thread per il salvataggio in mem
        SaveRawData = new StoreThread(BTStore_SharedMem);//Pin_BT, BTStore_SharedMem);
        RawDataStorer.OpenFile(); // apre o crea il file in append
        SaveRawData.StartThread();

        RecButton.setBackgroundColor(Color.RED);
        RecButton.setText("STOP REC");

        plotter1.set_PlotPaint_RecColor();
        plotter2.set_PlotPaint_RecColor();
        plotter3.set_PlotPaint_RecColor();
        plotter4.set_PlotPaint_RecColor();

        // faccio partire il timer che visualizza il tempo di registrazione
        timer_seconds = 0;
        timer_minutes = 0;
        handler.postDelayed(r, 1000);
    }

    //==========================================================================
    private void StopRec(){
        //==========================================================================
        handler.removeCallbacks(r);

        RawDataStorer.CloseFile(); //chiude il file
        SaveRawData.StopThread();
        SaveRawData.StoreEndOfFile();

        RecButton.setBackgroundResource(android.R.drawable.btn_default);
        RecButton.setText("REC");

        plotter1.set_PlotPaint_AcqColor();
        plotter2.set_PlotPaint_AcqColor();
        plotter3.set_PlotPaint_AcqColor();
        plotter4.set_PlotPaint_AcqColor();
    }
    //==========================================================================
    private boolean StartAcquire(){
        //==========================================================================

        boolean res = false;

        if(!isAcquiring) {
            EventButton.setVisibility(View.VISIBLE);
            BTPlot_SharedMem.Reset();
            DecodePlot_SharedMem1.Reset();
            DecodePlot_SharedMem2.Reset();
            DecodePlot_SharedMem3.Reset();
            DecodePlot_SharedMem4.Reset();

            GoToSettingsButton.setVisibility(View.INVISIBLE);

            myBT.StartBT();
            Set<BluetoothDevice> bonded = myBT.GetBondedDevices();
            myBT.SearchBondedBTDevices(this);

            // se ci siamo collegati fai partire l'acquisizione
            if (myBT.Connect(0) == true) {   // mi collego per ora al primo e unico che trovo
                // se tutto è andato bene
                myBT.startThread();         // fa partire il thread che legge i dati

                // parte il thread che riceve i dati grezzi dal bluetooth per la decodifica
                myDataDecoder = new DataDecoder(UserSettings, BTPlot_SharedMem, DecodePlot_SharedMem1, DecodePlot_SharedMem2, DecodePlot_SharedMem3, DecodePlot_SharedMem4);
                myDataDecoder.StartThread();

                plotter1.StartPlot();
                plotter2.StartPlot();
                plotter3.StartPlot();
                plotter4.StartPlot();

                //StartStopButton.setBackgroundColor(Color.RED);
                StartStopButton.setText("DISCONNECT");
                SetScreenOn();
                res = true;
            }
        }
        return  res;
    }
    //==========================================================================
    private void StopAcquire(){
        //==========================================================================
        // stop acquisizione

        if(isAcquiring) {
            EventButton.setVisibility(View.INVISIBLE);
            isAcquiring = false;
            myBT.stopThread(); // spegne il thread bt (spegnere tutti i listener prima)
            myBT.StopBT();      // chiude le comunicazioni bt

            if (isRecording) {
                /*isRecording = false;
                StopRec();*/
                StartStopRecording(null);
            }

            myDataDecoder.StopThread();

            plotter1.StopPlot();
            plotter2.StopPlot();
            plotter3.StopPlot();
            plotter4.StopPlot();

            ResetScreen();
            StartStopButton.setBackgroundResource(android.R.drawable.btn_default);
            StartStopButton.setText("START ANOTHER");
        }
    }


    //==========================================================================
    private class StoreThread extends Thread {
        //==========================================================================
       // private DataInputStream PipeInput;
        private byte[] tmp = new byte[FILE_PART_DIMENSION_IN_BYTE];
        int tmp_index=0;
        int pippo =0;
        int piciu =0;
        ByteDataExchange SharedMem;
        //DataExchangeControl ProvaSynchro;

        public StoreThread(ByteDataExchange prova){//InputStream is, ByteDataExchange prova){//DataExchangeControl prova) {
            //PipeInput = new DataInputStream(is);
            SharedMem = prova;
        }

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

        int k=0;
        @Override
        public void run() {
        //==========================================================================
            while(true) {

                //  riceve dati da bluetooth con mem condivisa
                if(SharedMem.getAvailable()>0){
                    pippo = SharedMem.get();
                    if(pippo!=1000) {   //1000= codice errore della shared mem
                        tmp[tmp_index++] = (byte) pippo;
                        if (tmp_index >= tmp.length) {  //tmp=32kB
                            tmp_index = 0;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    RawDataStorer.StoreData(tmp);
                                    piciu++;
                                    status_textview.setText("n: " + piciu);
                                }
                            });
                        }
                    }
                }
            }
        }


        //==========================================================================
        public void StoreEndOfFile(){
            //==========================================================================
            while(SharedMem.getAvailable()>0){
                tmp[tmp_index++] = (byte) SharedMem.get();
                if (tmp_index >= tmp.length) {  // salvo se riempio il buffer
                    tmp_index = 0;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            RawDataStorer.StoreData(tmp);
                            piciu++;
                            status_textview.setText("n: " + piciu);
                        }
                    });
                }
            }
            if(tmp_index>0) {
                runOnUiThread(new Runnable() {
                    public void run() { // salvo il buffer se parzialemnte pieno alla fine dell'acquisizione
                        byte[] tosave = new byte[tmp_index];
                        System.arraycopy(tmp, 0, tosave, 0, tmp_index);
                        RawDataStorer.StoreData(tosave);
                        piciu++;
                        status_textview.setText("n: " + piciu + "_stop");
                    }
                });
            }
        }
    }
    private boolean erosso = false;
    //==========================================================================
    public void ResetPacketErrors(View v){
        //==========================================================================
        if(myDataDecoder!=null) {
            myDataDecoder.CorruptedHeaders = 0;
            myDataDecoder.CorruptedPayload = 0;
            PackErr_button.setBackgroundColor(Color.GREEN);
            erosso = false;
        }
    }

    //==========================================================================
    public void SetScreenOn(){
        //==========================================================================
        NewLayoutParams = new WindowManager.LayoutParams();
        NewLayoutParams = getWindow().getAttributes();

        NewLayoutParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        //NewLayoutParams.screenBrightness = 0;
        getWindow().setAttributes(NewLayoutParams);
    }
    //==========================================================================
    public void ResetScreen() {
        //==========================================================================
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //==========================================================================
    public void FakeButtonClicked (View v){
    //==========================================================================
        call_toast("sono vivo");
        Intent i = new Intent(Activity1.this, DialogActivity.class);
        startActivity(i);

    }

    //==========================================================================
    public void StartSettingsActivity(View v){
//==========================================================================

        StopAcquire();
        Intent intent = new Intent(Activity1.this, ActivitySettings.class);
        intent.putExtra("user", UserSettings);
        if(v.getId() == GoToSettingsButton.getId() )
            intent.putExtra("mode", "Complete");    // chiamata dal tasto settings
        else if(v.getId() == ChangeFileNameButton.getId())
            intent.putExtra("mode", "changeName");  // attività chiamata dal tasto cambia nome

        startActivityForResult(intent, REQUEST_DATA);
    }
    // metodo che viene chiamato dalla fine della settingsactivity
    @Override
    //==========================================================================
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //==========================================================================
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DATA) {
            UserSettings = (Settings) data.getSerializableExtra("new_settings");    // mi salvo le eventuali modifiche
            ApplySettings();
        }

    }
    //==========================================================================
    private void ApplySettings(){
        //==========================================================================
        filename_view.setText("File name: " + UserSettings.FileName);
        RawDataStorer.put_FileName(UserSettings.FileName);
        plotter1.putNewSettings(UserSettings);
        plotter2.putNewSettings(UserSettings);
        plotter3.putNewSettings(UserSettings);
        plotter4.putNewSettings(UserSettings);
        plotter1.GraphRedraw();
        plotter2.GraphRedraw();
        plotter3.GraphRedraw();
        plotter4.GraphRedraw();

        AppSettings.ChangeDefault(UserSettings);
    }


    //==========================================================================
    // handler per il timer che mostra il tempo di registrazione sul textview
    private Handler handler = new Handler();
    private int timer_seconds = 0;
    private int timer_minutes = 0;

    final Runnable r = new Runnable()
    {
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    timer_seconds++;
                    if(timer_seconds >= 60){
                        timer_seconds = 0;
                        timer_minutes++;
                        if (timer_minutes >= UserSettings.Max_T_Rec){
                            call_toast("End of File Reached.");
                            StopRec();
                        }
                    }

                    timer_rec_view.setText(String.format("%02d", timer_minutes) + ":" + String.format("%02d", timer_seconds) );

                }
            });
            handler.postDelayed(this, 1000);    //scatta ogni 1000 ms = 1s
        }
    };


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

    //==========================================================================
    public void EventButton_Pressed(View v){
        //==========================================================================
        if(myBT != null) {
            myBT.SendCommand30((byte)MagicCommProtocol.CMD_SET_EVENT, (byte) 0, (int) 0);
        }
    }


}//FINE ACTIVITY 1
