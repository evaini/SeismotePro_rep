package diana.com.seismotepro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Diana Scurati on 09/11/2015.
 */


// da usarsi per visualizzazione in real time dei dati provenienti dal bluetooth
//==========================================================================
public class myGraphs extends View implements Runnable {
    //==========================================================================
    private Context context;

    int Frame_Width = 0;
    int Frame_Height = 0;
    int PlotBox_Width = 0;
    int PlotBox_Height = 0;
    int PlotBox_TopLeftCorner_X = 0;
    int PlotBox_TopLeftCorner_Y = 0;

    private Paint GraphPaint;
    private Paint TextPaint;
    private Paint PlotPaint;

    private Thread myPlotThread;
    private boolean isPlotting = false;
    private boolean isAcquisitionPhase = false;
    private boolean isRecordingPhase = false;
    private boolean checkSignalCommand = false;

    private Path ThePath;
    private float[] ECG0_array;
    private int ECG0_array_write_idx = 0;
    private int ECG0_array_read_idx = 0;
    private int ECG0_data_avail = 0;
    private int ECG0_data_cnt = 0;

    private static final int SIZE_DATA_ARRAY = 1024;
    private static final int SIZE_POINTS_TO_PLOT = 32;//64*2;

    private IntDataExchange DecodePlot_SharedMem;
    //private Activity1.IntDataExchange DecodePlot_SharedMem;

    private static final int FULL_SCALE_ECG0 = 4096;              // 2^12: segnali a 12 bit
    private static final int FULL_SCALE_RESP0 = 4096;             // 2^12: segnali a 12 bit
    private static final int FULL_SCALE_ACC_14BIT = 16384;        // 2^14: segnali a 14 bit: accelerometro magic
    //private static final int FULL_SCALE_ACC_16BIT = 65536;        // 2^16: segnali a 16 bit: accelerometro motes
    private static final int FULL_SCALE_PPG_16BIT = 65536;        // 2^16: segnali a 16 bit
    private static final int FULL_SCALE_TIME = 10000;             // millisecondi da rappresentare nel grafico

    private long x_in_start_box = 0;      // timestamp che corrisponde al primo campione da metere nel plot ogni volta

    private int FSR_Y;  // full scale range sulla y
    private int FSR_Y_MAX;
    private String MeasureUnit;
    private double Acc_gain = 4*6.103515625E-02;  // guadagno per passare da count a unità milli g(todo valore di manu: 6.103515625E-05)
    private double Ecg_resp_gain = 3300.0/4096.0;  // guadagno per passare da count a unità milli volt
    private double graph_gain;


    public static final int ACC_SIGNAL = 0;
    public static final int ECG_SIGNAL = 1;
    private String SignalName;

    Settings UserSettings;

    private int GraphNum;   // 0, 1, 2, 3

    // per il calcolo automatico dell'offset
    private int Ymean=0;
    private short ActualZoom = 1;

    private int Ymax = 0;
    private int Ymin = 0;
    private int Ymax_ever = 0;
    private int Ymin_ever = 0;  // massimo e minimo rappresentabile, oltre al quale non si va


    //==========================================================================
    public myGraphs(Context context, int frame_width, int frame_height, IntDataExchange in, int graph_num, Settings us){
        //==========================================================================
        super(context);

        DecodePlot_SharedMem = in;
        Frame_Height = frame_height;
        Frame_Width = frame_width;
        UserSettings = us;
        GraphNum = graph_num;       // 0     1     2    3

        GraphPaint = new Paint();
        GraphPaint.setDither(true);
        GraphPaint.setAntiAlias(true);
        GraphPaint.setStyle(Paint.Style.STROKE);
        GraphPaint.setStrokeWidth(1);
        GraphPaint.setColor(Color.BLACK);

        TextPaint = new Paint();
        TextPaint.setTextSize(17);
        TextPaint.setStrokeWidth(1);

        PlotPaint = new Paint();
        //PlotPaint.setColor(Color.BLUE);
        set_PlotPaint_AcqColor();
        PlotPaint.setStyle(Paint.Style.STROKE);
        PlotPaint.setStrokeWidth(2);

        CheckSignal(graph_num);

        PlotBox_Height = (int)(frame_height *90.0/100.0);
        PlotBox_Width = (int)(frame_width * 83.0/100.0); // 90% della larghezza

        PlotBox_TopLeftCorner_X = (int)(frame_width *10.0/100.0);
        PlotBox_TopLeftCorner_Y = (int)(frame_height *1.0/100.0);

        ResetGraph();

        Ymean = FSR_Y/2;
    }
    //==========================================================================
    public void CheckSignal(int graph_num){
        //==========================================================================
        // CAPIAMO QUALE SEGNALE DEVE ESSER PLOTTATO
        //magic
        if(UserSettings.EnabledSource[graph_num] == 0) {
            SignalName = "Magic\n";
            if(UserSettings.EnabledSignal[graph_num] == 0){//accelerometro della magic
                FSR_Y_MAX = (int)(FULL_SCALE_ACC_14BIT * Acc_gain);
                FSR_Y = FSR_Y_MAX;
                Ymax = FSR_Y / 2;
                Ymin = - ( FSR_Y / 2);
                MeasureUnit = "mg";
                graph_gain = Acc_gain;

                if(UserSettings.EnabledAxes[graph_num] == 0)
                    SignalName += " Acc X";
                if(UserSettings.EnabledAxes[graph_num] == 1)
                    SignalName += " Acc Y";
                if(UserSettings.EnabledAxes[graph_num] == 2)
                    SignalName += " Acc Z";
            } else if(UserSettings.EnabledSignal[graph_num] == 1) {  // ecg
                FSR_Y_MAX = (int)(FULL_SCALE_ECG0*Ecg_resp_gain);
                FSR_Y = FSR_Y_MAX;
                graph_gain = Ecg_resp_gain;

                MeasureUnit = "mV";
                SignalName += " ECG";
                Ymax = FSR_Y;
                Ymin = 0;

            } else if(UserSettings.EnabledSignal[graph_num] == 2) {  // resp
                FSR_Y_MAX = (int)(FULL_SCALE_RESP0*Ecg_resp_gain);
                FSR_Y = FSR_Y_MAX;
                graph_gain = Ecg_resp_gain;

                MeasureUnit = "mV";
                SignalName += " Resp";
                Ymax = FSR_Y;
                Ymin = 0;
            }


            //motes
        }else if(UserSettings.EnabledSource[graph_num] != -1 && UserSettings.EnabledSource[graph_num] <=5) {
            SignalName = "Mote\n" + UserSettings.EnabledSource[graph_num];

            if(UserSettings.EnabledSignal[graph_num] == 0 ||
                    UserSettings.EnabledSignal[graph_num] == 1 ||
                    UserSettings.EnabledSignal[graph_num] == 2 ||
                    UserSettings.EnabledSignal[graph_num] == 3 ||
                    UserSettings.EnabledSignal[graph_num] == 4 ){   // accelerometri del mote
                SignalName += " Acc" + (UserSettings.EnabledSignal[graph_num]+1);
                FSR_Y_MAX = (int)(FULL_SCALE_ACC_14BIT*Acc_gain);
                FSR_Y = FSR_Y_MAX;
                graph_gain = Acc_gain;

                MeasureUnit = "mg";
                Ymax = FSR_Y / 2;
                Ymin = - ( FSR_Y / 2);
                if(UserSettings.EnabledAxes[graph_num] == 0)
                    SignalName += " X";
                if(UserSettings.EnabledAxes[graph_num] == 1)
                    SignalName += " Y";
                if(UserSettings.EnabledAxes[graph_num] == 2)
                    SignalName += " Z";

            }else if(UserSettings.EnabledSignal[graph_num] == 5){// pletismografo
                FSR_Y = FULL_SCALE_PPG_16BIT;
                FSR_Y_MAX = FULL_SCALE_PPG_16BIT;
                Ymin = 0;
                Ymax = FSR_Y;
                MeasureUnit = "au"; //arbitrary unit
                graph_gain = 1;

                if(UserSettings.EnabledAxes[graph_num] == 0)
                    SignalName += " PPG_RED";
                if(UserSettings.EnabledAxes[graph_num] == 1)
                    SignalName += " PPG_IR";
            }
        }
        Ymax_ever = Ymax*2;
        Ymin_ever = -Ymax_ever;
    }

    //==========================================================================
    private void ResetGraph(){
        //==========================================================================
        ECG0_array = new float[Frame_Width*2];
        ThePath = new Path();
        ThePath.moveTo(PlotBox_TopLeftCorner_X, (PlotBox_TopLeftCorner_Y+PlotBox_Height/2));  //punto di inizio del plot
        ECG0_array_write_idx = 0;
        ECG0_array_read_idx = 0;
        ECG0_data_avail = 0;
        ECG0_data_cnt = 0;
        x_in_start_box = 0;
        GraphRedraw();
    }

    float touch_x=0;
    float touch_y=0;
    @Override
    //==========================================================================
    public boolean onTouchEvent(MotionEvent event) {
        //==========================================================================
        int action = MotionEventCompat.getActionMasked(event);
        // Let the ScaleGestureDetector inspect all events.
        //mScaleDetector.onTouchEvent(event);

        try {

            switch (action) {
                case (MotionEvent.ACTION_DOWN):    // dito appoggiato
                    //==========================================================================
                   /* pointerIndex = MotionEventCompat.getActionIndex(event);
                    x = MotionEventCompat.getX(event, pointerIndex);
                    y = MotionEventCompat.getY(event, pointerIndex);
                    // Remember where we started (for dragging)
                    LastTouchX = x;
                    LastTouchY = y;
                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = MotionEventCompat.getPointerId(event, 0);



                    if((x < Line1_X + Line_SensibleArea) &&(x > Line1_X - Line_SensibleArea)) {   //selezionata la linea 1
                        Line1selected = true;
                    }else if((x < Line2_X + Line_SensibleArea) &&(x > Line2_X - Line_SensibleArea)){//selezionata la linea 2
                        Line2selected = true;
                    }*/



                    return true;
                case (MotionEvent.ACTION_MOVE):        // tra action_down e action_up
                    //==========================================================================
                    /*pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);

                    x = MotionEventCompat.getX(event, pointerIndex);
                    y = MotionEventCompat.getY(event, pointerIndex);

                    // Calculate the distance moved
                    final float dx = x - LastTouchX;
                    final float dy = y - LastTouchY;
                    PosX += dx;
                    PosY += dy;



                    if(Line1selected){
                        if((Line1_X + dx) <= (FrameWidth * 90 / 100) - 20) {// controllo contro uscita linea da box da destra...
                            if(Line1_X + dx >= 10)                       // ... e da sinistra
                                Line1_X += dx;
                        }

                        if((Line1_X) > (Line2_X - 5))   // controllo contro l'inversione dell due linee
                            Line2_X = Line1_X + 5;

                    }else if(Line2selected){
                        if((Line2_X + dx) <= (FrameWidth * 90 / 100) - 10) {   // controllo contro uscita linea da box da destra...
                            if(Line2_X + dx >= 20)                         // ... e da sinistra
                                Line2_X += dx;
                        }

                        if((Line2_X) < (Line1_X + 5))   // controllo contro l'inversione dell due linee
                            Line1_X = Line2_X - 5;

                    }
                    invalidate();



                    // Remember this touch position for the next move event
                    LastTouchX = x;
                    LastTouchY = y;*/

                    return true;
                case (MotionEvent.ACTION_UP):      // dito alzato
                    //==========================================================================
                    /*pointerIndex = MotionEventCompat.getActionIndex(event);
                    int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

                    if(Line1selected)
                        Line1selected = false;
                    if(Line2selected)
                        Line2selected = false;

                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        LastTouchX = MotionEventCompat.getX(event, newPointerIndex);
                        LastTouchY = MotionEventCompat.getY(event, newPointerIndex);
                        mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
*/

                        return true;
                    //}
                default:
                    return super.onTouchEvent(event);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return  true;
    }

    //==========================================================================
    public void StartPlot(){
        //==========================================================================
        // fa partire il runnable
        if(!isPlotting) {
            myPlotThread = new Thread(this);
            myPlotThread.start();

            isPlotting = true;
        }
    }

    //==========================================================================
    public void StopPlot(){
        //==========================================================================
        if(isPlotting) {
            // fa fermare il runnable
            myPlotThread.interrupt();
            isPlotting = false;
            ResetGraph();
        }
    }


    private Canvas myCanvas = null;
    @Override
    //==========================================================================
    //==========================================================================
    //==========================================================================
    public void onDraw(Canvas canvas) {
        //==========================================================================
        //==========================================================================
        //==========================================================================
        super.onDraw(canvas);
        myCanvas = canvas;

        if(checkSignalCommand){
            CheckSignal(GraphNum);
            checkSignalCommand = false;
        }

        DrawScales(canvas);

        if (isPlotting) {
            try {
                // informazione sullo zoom attuale
                canvas.drawText(("" + ActualZoom + "x"), PlotBox_TopLeftCorner_X - 70, PlotBox_TopLeftCorner_Y + PlotBox_Height / 3, TextPaint);

                canvas.drawCircle(ECG0_array[ECG0_array_write_idx - 2], ECG0_array[ECG0_array_write_idx - 1], 5, PlotPaint); // segna il punto in cui si disegna
                canvas.drawPath(ThePath, PlotPaint);

                ECG0_array_read_idx += ECG0_data_avail;
                ECG0_data_avail = 0;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //==========================================================================
    public void DrawScales(Canvas Canv){
        //==========================================================================
        // box che contiene il grafico
        Canv.drawRect(PlotBox_TopLeftCorner_X, PlotBox_TopLeftCorner_Y, PlotBox_TopLeftCorner_X + PlotBox_Width, PlotBox_TopLeftCorner_Y + PlotBox_Height, GraphPaint);

        if (GraphNum == 3){ // disegno scritte dei tempi solo su ultimo grafico in basso
            Canv.drawLine((PlotBox_TopLeftCorner_X), (PlotBox_TopLeftCorner_Y + PlotBox_Height + 10), (PlotBox_TopLeftCorner_X + (PlotBox_Width / 2) - 30), (PlotBox_TopLeftCorner_Y + PlotBox_Height + 10), GraphPaint );
            Canv.drawLine((PlotBox_TopLeftCorner_X + (PlotBox_Width / 2) + 30), (PlotBox_TopLeftCorner_Y + PlotBox_Height + 10), (PlotBox_TopLeftCorner_X + PlotBox_Width), (PlotBox_TopLeftCorner_Y + PlotBox_Height + 10), GraphPaint );

            Canv.drawText("10 s", (PlotBox_TopLeftCorner_X + (PlotBox_Width / 2)-17), (PlotBox_TopLeftCorner_Y + PlotBox_Height + 15), TextPaint);
        }

        //scala sulla y
        Canv.drawLine(PlotBox_TopLeftCorner_X - 5, PlotBox_TopLeftCorner_Y + PlotBox_Height, PlotBox_TopLeftCorner_X, PlotBox_TopLeftCorner_Y + PlotBox_Height, TextPaint);       // riga start
        Canv.drawLine(PlotBox_TopLeftCorner_X - 5, PlotBox_TopLeftCorner_Y, PlotBox_TopLeftCorner_X, PlotBox_TopLeftCorner_Y, TextPaint);       // riga fsr
        // y massima e minima
        Canv.drawText((Ymax + "\n" + MeasureUnit), PlotBox_TopLeftCorner_X, PlotBox_TopLeftCorner_Y+17, TextPaint);
        Canv.drawText((Ymin + "\n" + MeasureUnit), PlotBox_TopLeftCorner_X, PlotBox_TopLeftCorner_Y+PlotBox_Height-5, TextPaint);
    }

    private long[] sample_to_plot = new long[2];
    @Override
    //==========================================================================
    public void run() {
    //==========================================================================
        int data = 0;
        while(isPlotting) {
            while(DecodePlot_SharedMem.getAvailable()>0){

                DecodePlot_SharedMem.get(sample_to_plot);
                //todo rimettere se si toglie mod 17 feb 2016:
                PutData(sample_to_plot);

                //todo mod 17 feb 2016
                /*new_x = RemapData_X(sample_to_plot[0]);
                if(new_x>old_x){
                    PutData(sample_to_plot);
                    old_x = new_x;
                }*/
                // fine mod

            }
            try {
                if (ECG0_data_avail >= SIZE_POINTS_TO_PLOT) {
                    this.postInvalidate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //==========================================================================
    public void PutData(long[] data) {
        //==========================================================================


        //todo mod 17 feb 2016
        ConvertArrayToPath(data[0], (int)data[1], 0, 0);

        //todo rimettere se si toglie mod 17 feb 2016:
        /*
        ThePath.lineTo(RemapData_X(data[0]), RemapData_Y((int) data[1]));

        ECG0_array[ECG0_array_write_idx++] = RemapData_X(data[0]);       // valore X
        ECG0_array[ECG0_array_write_idx++] = RemapData_Y((int)data[1]);//provay++);      // valore y

        ECG0_data_avail+=2;

        if(ECG0_array_write_idx >= ECG0_array.length){
            ECG0_array_write_idx = 0;
        }*/
    }


    float new_y = 0;
    float old_x = 0;
    float new_x = 0;
    float max_y = 0;
    float min_y = 0;
    // per la decimazione dei punti da disegnare
    //==========================================================================
    private void ConvertArrayToPath(long time, int data, float x_offset, int y_offset){
        //==========================================================================
        int i;

        max_y = new_y;
        min_y = new_y;


            new_x = RemapData_X(time) + x_offset;//x offset è in unità display
            new_y = RemapData_Y(data + y_offset);//y offset è in unità originarie

            // se siamo nella x successiva stampa i massimi  e minimi calcolati al giro prima
            if(new_x > old_x){
                ThePath.lineTo(old_x, min_y);
                ThePath.lineTo(old_x, max_y);

                ECG0_array[ECG0_array_write_idx++] = old_x;       // valore X
                ECG0_array[ECG0_array_write_idx++] = new_y;       // valore y

                ECG0_data_avail+=2;

                if(ECG0_array_write_idx >= ECG0_array.length){
                    ECG0_array_write_idx = 0;
                }

                old_x = new_x;
                /*if(old_x > PlotBox_TopLeftCorner_X+PlotBox_Width)
                    x_in_start_box = time;  // torno a disegnare dall'inizio*/

                max_y = new_y;
                min_y = new_y;
            }

            if (new_y > max_y)
                max_y = new_y;
            else if (new_y < min_y)
                min_y = new_y;

    }

    //==========================================================================
    public void GraphRedraw(){
        //==========================================================================
        checkSignalCommand = true;
        this.postInvalidate();


    }


    // 1=zoom in , -1= zoom out
    //==========================================================================
    public void SetZoomingScale(int zoomValue){
        //==========================================================================
            if(zoomValue==1) {//zoom in : aumento risoluzione
            FSR_Y /= 2;
            ActualZoom *= 2;    //x visualizzazione
            Ymax /= 2;
            Ymin /= 2;
        }
        else if((zoomValue == -1) && (FSR_Y*2 <= FSR_Y_MAX)) {
            FSR_Y *= 2;
            ActualZoom /= 2;
            Ymax *= 2;
            Ymin *= 2;
        }
    }

    private int Y_offset=0;
    // 1 = disegno sale , -1= disegno scende
    //==========================================================================
    public void SetOffset(int offsetValue){
        //==========================================================================
        if((offsetValue==1)) {// SHIFT UP
            if((Ymax + FSR_Y / 10) < Ymax_ever) {
                Y_bias += FSR_Y / 10;
                Ymax += FSR_Y / 10;
                Ymin += FSR_Y / 10;
            }
        }

        else if(offsetValue == -1) {    //SHIFT DOWN
            if((Ymin - FSR_Y / 10) > Ymin_ever) {
                Y_bias -= FSR_Y / 10;
                Ymax -= FSR_Y / 10;
                Ymin -= FSR_Y / 10;
            }
        }
    }

    private int Y_bias = 0;
    //==========================================================================
    public void AutoBias(){
        //==========================================================================
        Y_bias = FSR_Y/2 - Ymean;    // tolgo distanza tra y centro e y media

        Ymax += (Ymean - (FSR_Y/2));
        Ymin += (Ymean - (FSR_Y/2));
    }

    //==========================================================================
    public float RemapData_Y(int y_in_value){
        //==========================================================================
       //media un pò farlocca
        y_in_value *= graph_gain;
        Ymean = (Ymean + (y_in_value-Ymin))/2;

        //funzionante senza offset automatico
        float y_new = (PlotBox_TopLeftCorner_Y + PlotBox_Height) - ((y_in_value - Ymin) * PlotBox_Height / FSR_Y) ;

        if(y_in_value > Ymax || y_in_value < Ymin) {  // se il segnale va sopra o sotto i limiti minimi e massimi del grafico cambia colore
            PlotPaint.setColor(Color.GRAY);
        }
        else{
            if(isRecordingPhase)
                set_PlotPaint_RecColor();
            else if(isAcquisitionPhase)
                set_PlotPaint_AcqColor();
        }

        return y_new;
    }

    //==========================================================================
    public long RemapData_X(long x_in_value){
        //==========================================================================
        long x_new=0;
        if(x_in_value - x_in_start_box > FULL_SCALE_TIME){ // se il nuovo campione è distante più di FULL_SCALE ms dal primo valore del plot, allora ricomincia
            x_in_start_box = x_in_value;
            //todo mod 17 feb 2016
            old_x = PlotBox_TopLeftCorner_X;

            ThePath = new Path();
            ThePath.moveTo(PlotBox_TopLeftCorner_X, (PlotBox_TopLeftCorner_Y+PlotBox_Height/2));

            Ymean = 0;// resetto
        }

        x_new = (PlotBox_TopLeftCorner_X) + ((x_in_value - x_in_start_box)*PlotBox_Width/FULL_SCALE_TIME);
        return x_new;

    }

    //==========================================================================
    private int ReverseRemapData_Y(int display_y) {
        //==========================================================================
        int y;
        y = ((PlotBox_TopLeftCorner_Y + PlotBox_Height) * FSR_Y / PlotBox_Height) + Y_bias;
        return y;
    }
    //==========================================================================
    public void putNewSettings(Settings newsettings){
        //==========================================================================
        UserSettings = newsettings;
    }


    // vecchio con graphview
    /*//==========================================================================
    public void SetStyle(LineGraphSeries series){
        //==========================================================================
        // non usare così com'è, è solo un acopia di un esempio
        // styling series
        series.setTitle("TITOLO");
        series.setColor(Color.GREEN);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(8);

        //// custom paint to make a dotted line
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));
        series.setCustomPaint(paint);

    }*/


    //==========================================================================
    public void set_PlotPaint_RecColor(){
        //==========================================================================
        PlotPaint.setColor(Color.BLUE);
        isRecordingPhase = true;
        isAcquisitionPhase = false;
    }
    //==========================================================================
    public void set_PlotPaint_AcqColor(){
        //==========================================================================
        PlotPaint.setColor(Color.LTGRAY);
        isRecordingPhase = false;
        isAcquisitionPhase = true;
    }
    private int count = 0 ;
    Matrix translateMatrix;
    //==========================================================================
    private void UpdateMean(long y_new){
        //==========================================================================
        translateMatrix = new Matrix();


        Ymean = (Ymean + (int)y_new) / 2;
        count++;
        if(count >= 400) {    // 400 campioni = 2 secondi di registrazione
            if(Ymean>(FSR_Y/2 + FSR_Y/5) || Ymean<(FSR_Y/2 - FSR_Y/5) ) {
                translateMatrix.setTranslate(0, (-(Ymean - FSR_Y / 2)));
                Ymean = FSR_Y / 2;
            }
            count = 0;
        }

    }

    public String getSignalName() {return SignalName;}

}
