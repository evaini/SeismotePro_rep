package diana.com.seismotepro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * Created by Diana Scurati on 18/12/2015.
 */
//==========================================================================
public class AnalysisGraph extends View {
    //==========================================================================
    private int FrameWidth = 1;
    private int FrameHeight = 1;

    private int Box_Height = 1;
    private int Box_Width = 1;
    private int Box_TopLeftCorner_X = 1;
    private int Box_TopLeftCorner_Y = 1;

    private Paint GraphPaint;
    private Paint PlotPaint;
    private Paint TrianglePaint;
    private Paint RedLinePaint;
    private Paint MaxMinPaint;

    private Path ThePath = null;
    private Canvas TheCanvas = null;

    private GestureDetectorCompat GestureWatcher;
    private ScaleGestureDetector mScaleDetector;

    private Settings UserSettings;
    private String SignalName;
    private static final int FULL_SCALE_ECG0 = 4096;              // 2^12: segnali a 12 bit
    private static final int FULL_SCALE_RESP0 = 4096;             // 2^12: segnali a 12 bit
    private static final int FULL_SCALE_ACC_14BIT = 16384;        // 2^14: segnali a 14 bit: accelerometro magic
    private static final int FULL_SCALE_ACC_16BIT = 65536;        // 2^16: segnali a 16 bit: accelerometro motes
    private static final int FULL_SCALE_PPG_16BIT = 65536;        // 2^16: segnali a 16 bit
    private int FULL_SCALE_TIME = 30000;             // millisecondi da rappresentare nel grafico: default=60secondi

    private double Acc_gain = 4*6.103515625E-02;  // guadagno per passare da count a unità milli g(todo valore di manu: 6.103515625E-05)
    //private double Ecg_resp_gain = 3300.0/4096.0;  // guadagno per passare da count a unità milli volt
    private double Ecg_resp_gain = 1.0/170.0;  // 170.0 livelli/mV MIO!!!
    private double graph_gain;

    private long x_in_start_box = 0;      // timestamp che corrisponde al primo campione da metere nel plot ogni volta

    private int FSR_Y;  // full scale range sulla y
    private int FSR_Y_MAX;
    private String MeasureUnit;

    private int GraphNum;

    // posizionamento della linea di selezione del grafico
    private int Line1_X = 500;
    private int Line2_X = 600;
    private boolean Line1selected = false;
    private boolean Line2selected = false;

    private int[] Data;
    private long[] Time;

    private  boolean isClearMode = false;

    boolean drawMaxMin = false;

    // costruttore
    //==========================================================================
    public AnalysisGraph(Context context, int frame_width, int frame_height, int graph_num, Settings user_settings) {
        //==========================================================================
        super(context);

        FrameHeight = frame_height;
        FrameWidth = frame_width;
        GraphNum = graph_num;
        //UserSettings = new Settings();  // prendo i segnali di default in questo modo
        UserSettings = user_settings;  // le impostazioni le mando qui dal main

        CheckSignal(GraphNum);

        Box_Height = FrameHeight;
        Box_Width = FrameWidth * 90 / 100;

        Box_TopLeftCorner_X = 0;
        Box_TopLeftCorner_Y = 0;

        GraphPaint = new Paint();   // x grafico e scritte nomi segnali
        GraphPaint.setDither(true);
        GraphPaint.setAntiAlias(true);
        GraphPaint.setStyle(Paint.Style.STROKE);
        GraphPaint.setStrokeWidth(1);
        GraphPaint.setColor(Color.BLACK);
        GraphPaint.setTextSize(25);

        PlotPaint = new Paint();        // segnale vero e proprio
        PlotPaint.setColor(Color.BLUE);
        PlotPaint.setStyle(Paint.Style.STROKE);
        PlotPaint.setStrokeWidth(2);

        MaxMinPaint = new Paint();        // segnale vero e proprio
        MaxMinPaint.setStrokeWidth(1);
        MaxMinPaint.setTextSize(17);

        RedLinePaint = new Paint(); // linee rosse sui picchi r
        RedLinePaint.setDither(true);
        RedLinePaint.setAntiAlias(true);
        RedLinePaint.setStyle(Paint.Style.STROKE);
        RedLinePaint.setStrokeWidth(1);
        RedLinePaint.setColor(Color.RED);

        TrianglePaint = new Paint();
        TrianglePaint.setStyle(Paint.Style.FILL);
        TrianglePaint.setColor(Color.BLACK);
        TrianglePaint.setTextSize(17);

        /*ThePath = new Path();
        ThePath.moveTo(Box_TopLeftCorner_X, (Box_TopLeftCorner_Y + Box_Height / 2));  //punto di inizio del plot*/

        //mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    private float mScaleFactor = 1.f;
    //==========================================================================
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        //==========================================================================
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 10.0f));
            invalidate();
            return true;
        }
    }

    @Override
    //==========================================================================
    public void onDraw(Canvas canvas) {
        //==========================================================================
        super.onDraw(canvas);
        TheCanvas = canvas;

        if(drawRedLines){
            drawRedLines = false;
            int i;
            for(i=0; i<PeakNumber; i++){
                canvas.drawLine(RemapData_X(Time[PeakPositions[i]]), Box_TopLeftCorner_Y, RemapData_X(Time[PeakPositions[i]]), Box_TopLeftCorner_Y + Box_Height, RedLinePaint);
            }
        }

        if(!isClearMode) {
            DrawAxis(canvas);
            /*canvas.save();  // salva il drawing state(con il restore non mi ricordo della prossima trasformazione e posso applicare tutte le trasf che voglio e n elementi a piacere
            canvas.translate(PosX, PosY);
            canvas.scale(mScaleFactor, mScaleFactor);
            //canvas.drawRect(0, 0, 15, 15, new Paint());
            canvas.restore();*/

            //canvas.drawLine((Line1_X + dx_line1), Box_TopLeftCorner_Y, (Line1_X + dx_line1), (Box_TopLeftCorner_Y + Box_Height), LinePaint);
            //canvas.drawLine((Line2_X + dx_line2), Box_TopLeftCorner_Y, (Line2_X + dx_line2), (Box_TopLeftCorner_Y + Box_Height), LinePaint);
            if(ThePath!=null) {
                canvas.drawPath(ThePath, PlotPaint);

                if(drawMaxMin){
                    String Max = "Max: " + String.format("%.02f", signal_max * graph_gain) + " " + MeasureUnit;
                    canvas.drawText( Max, Box_TopLeftCorner_X, Box_TopLeftCorner_Y+20, MaxMinPaint);

                    String Min = "Min: " + String.format("%.02f", signal_min * graph_gain) + " " + MeasureUnit;
                    canvas.drawText( Min, Box_TopLeftCorner_X, Box_TopLeftCorner_Y+Box_Height-5, MaxMinPaint);
                }
                if(enableViewTimeInterval && !drawMaxMin){ // o così o le scritte si sovrappongono
                    canvas.drawText((Time[0]/1000) +"s", Box_TopLeftCorner_X, Box_TopLeftCorner_Y+Box_Height-5, MaxMinPaint);
                    canvas.drawText((Time[Time.length-1]/1000) +"s", Box_TopLeftCorner_X + Box_Width, Box_TopLeftCorner_Y+Box_Height-5, MaxMinPaint);
                }

                if(doDrawBalloon){
                    // valore sulla linea 1
                    // tre linee che formano il triangolino tipo freccia che indica il punto
                    canvas.drawLine(RemapData_X(Time[x_pos1]) + deltaT,
                                    RemapData_Y(Data[x_pos1] - signal_min),
                                    RemapData_X(Time[x_pos1] - 15) + deltaT,
                                    RemapData_Y(Data[x_pos1]-signal_min)+15,
                                    TrianglePaint);

                    canvas.drawLine(RemapData_X(Time[x_pos1]) + deltaT,
                            RemapData_Y(Data[x_pos1] - signal_min),
                            RemapData_X(Time[x_pos1] - 15) + deltaT,
                            RemapData_Y(Data[x_pos1] - signal_min) - 15,
                            TrianglePaint);

                    canvas.drawLine(RemapData_X(Time[x_pos1] - 15) + deltaT,
                            RemapData_Y(Data[x_pos1] - signal_min) + 15,
                            RemapData_X(Time[x_pos1] - 15) + deltaT,
                            RemapData_Y(Data[x_pos1] - signal_min) - 15,
                            TrianglePaint);
                    //il valore di y del punto indicato
                    float Y_text = RemapData_Y(Data[x_pos1] - signal_min) + 7;

                    if( Y_text > (Box_TopLeftCorner_Y+Box_Height)){    // se la scritta va fuori dal grafico da sotto
                        Y_text -= 15;
                    }else if(Y_text < Box_TopLeftCorner_Y){           // se la scritta va fuori dal grafico da sopra
                        Y_text += 15;
                    }

                    canvas.drawText((String.format("%.02f", (Data[x_pos1] * graph_gain)) + " " + MeasureUnit), RemapData_X(Time[x_pos1] - 15) - 70 + deltaT, Y_text , TrianglePaint);


                    // valore sulla linea 2
                    canvas.drawLine(RemapData_X(Time[x_pos2]) + deltaT,
                                    RemapData_Y(Data[x_pos2] - signal_min),
                                    RemapData_X(Time[x_pos2] + 15) + deltaT,
                                    RemapData_Y(Data[x_pos2]-signal_min)+15,
                                    TrianglePaint);

                    canvas.drawLine(RemapData_X(Time[x_pos2]) + deltaT,
                                    RemapData_Y(Data[x_pos2] - signal_min),
                                    RemapData_X(Time[x_pos2] + 15) + deltaT,
                                    RemapData_Y(Data[x_pos2]-signal_min)-15,
                                    TrianglePaint);

                    canvas.drawLine(RemapData_X(Time[x_pos2]+15) + deltaT,
                                    RemapData_Y(Data[x_pos2]-signal_min)+15,
                                    RemapData_X(Time[x_pos2]+15) + deltaT,
                                    RemapData_Y(Data[x_pos2]-signal_min)-15,
                                    TrianglePaint);

                    Y_text = RemapData_Y(Data[x_pos2]-signal_min)+7;

                    if( Y_text > (Box_TopLeftCorner_Y+Box_Height)){    // se la scritta va fuori dal grafico da sotto
                        Y_text -= 15;
                    }else if(Y_text < Box_TopLeftCorner_Y){           // se la scritta va fuori dal grafico da sopra
                        Y_text += 15;
                    }
                    canvas.drawText( (String.format("%.02f", (Data[x_pos2] * graph_gain)) + " " + MeasureUnit),  RemapData_X(Time[x_pos2]+15) + 5 + deltaT, Y_text, TrianglePaint);
                }
            }
        }else{
            canvas.drawColor(Color.WHITE);
            isClearMode = false;
            drawMaxMin = false;
            enableViewTimeInterval = false;
        }
    }

    private boolean doDrawBalloon = false;
    private int x_pos1;
    private int x_pos2;
    //==========================================================================
    public void PutDataForBalloons(long time1, long time2){//time line 1 time line 2
        //==========================================================================
        if(time1 < Time[0]+ReverseRemapData_X((int)deltaT)){
            x_pos1 = 0;
        }
        if (time2 > Time[Time.length-1]+ReverseRemapData_X((int)deltaT)) {
            x_pos2 = Time.length-1;
        }

        doDrawBalloon = true;
        int i;
        for (i = 0; i < Time.length; i++) {
            if (Math.abs(time1 - (Time[i] + ReverseRemapData_X((int) deltaT))) <= 5) {
                x_pos1 = i;
            }
            if (Math.abs(time2 - (Time[i] + ReverseRemapData_X((int) deltaT))) <= 5) {
                x_pos2 = i;
            }
        }
        GraphRedraw();
    }
    //==========================================================================
    public void ClearBalloons(){
        //==========================================================================
        doDrawBalloon = false;
        GraphRedraw();
    }
    //==========================================================================
    public boolean isBalloonsDrawEnabled(){
        //==========================================================================
        return doDrawBalloon;
    }

    //==========================================================================
    private void DrawAxis(Canvas canvas){
        //==========================================================================
        canvas.drawRect(0, 0, Box_Width, Box_Height, GraphPaint);
        canvas.drawText((""+SignalName),(Box_TopLeftCorner_X + Box_Width - 200), (Box_TopLeftCorner_Y+Box_Height-5), GraphPaint);
    }


    private int mActivePointerId = -1;
    private int pointerIndex;
    float LastTouchX;
    float LastTouchY;
    float PosX=20;
    float PosY=20;
    float x;
    float y;
    float dx=0;
    float dx_line1=0;
    float dx_line2=0;
    float dy=0;

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
                    pointerIndex = MotionEventCompat.getActionIndex(event);
                    x = MotionEventCompat.getX(event, pointerIndex);
                    y = MotionEventCompat.getY(event, pointerIndex);
                    // Remember where we started (for dragging)
                    LastTouchX = x;
                    LastTouchY = y;
                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = MotionEventCompat.getPointerId(event, 0);



                    if((x < Line1_X + dx_line1 + 30) &&(x > Line1_X + dx_line1 - 30)) {   //selezionata la linea 1
                        Line1selected = true;
                    }else if((x < Line2_X + dx_line2 + 30) &&(x > Line2_X + dx_line2 - 30)){//selezionata la linea 2
                        Line2selected = true;
                    }



                    return true;
                case (MotionEvent.ACTION_MOVE):        // tra action_down e action_up
                    //==========================================================================
                    pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);

                    x = MotionEventCompat.getX(event, pointerIndex);
                    y = MotionEventCompat.getY(event, pointerIndex);

                    // Calculate the distance moved
                    final float dx = x - LastTouchX;
                    final float dy = y - LastTouchY;
                    PosX += dx;
                    PosY += dy;



                    if(Line1selected){
                        dx_line1 += dx;
                        //Line1selected = false;
                    }else if(Line2selected){
                        dx_line2 += dx;
                        //Line2selected = false;
                    }
                    invalidate();



                    // Remember this touch position for the next move event
                    LastTouchX = x;
                    LastTouchY = y;

                    return true;
                case (MotionEvent.ACTION_UP):      // dito alzato
                    //==========================================================================
                    pointerIndex = MotionEventCompat.getActionIndex(event);
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


                        return true;
                    }
                default:
                    return super.onTouchEvent(event);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return  true;
    }



    float new_y = 0;
    float old_x = 0;
    float new_x = 0;
    float max_y = 0;
    float min_y = 0;

    //==========================================================================
    private Path ConvertArrayToPath(long[] time, int[] data, float x_offset, int y_offset){
        //==========================================================================
        int i;
        Path myPath = new Path();

        new_y = RemapData_Y(data[0] + y_offset);
        old_x = RemapData_X(time[0]) + x_offset;
        new_x = old_x;
        max_y = new_y;
        min_y = new_y;

        myPath.moveTo((Box_TopLeftCorner_X + x_offset), RemapData_Y(data[0]+y_offset) + 1); // tutti i +1 : per evitare di sovrapporre segnale e assi grafico

        max_y = new_y;
        min_y = new_y;

        for(i=0;((i<time.length-1) && (i<data.length-1)); i++){

            new_x = RemapData_X(time[i]) + x_offset;//x offset è in unità display
            new_y = RemapData_Y(data[i] + y_offset) + 1;//y offset è in unità originarie

            // se siamo nella x successiva stampa i massimi  e minimi calcolati al giro prima
            if(new_x > old_x){
                myPath.lineTo(old_x, min_y);
                myPath.lineTo(old_x, max_y);

                old_x = new_x;

                max_y = new_y;
                min_y = new_y;
            }

            if (new_y > max_y)
                max_y = new_y;
            else if (new_y < min_y)
                min_y = new_y;
        }
        return myPath;
    }


    //     old
    //==========================================================================
    public void PutData(long[] time, int[] data, int FSR_X){
        //==========================================================================
        // mando tutto il buffer da stampare
        Data = data;// copio i dati in locale
        Time = time;
        CheckSignal(GraphNum);  // per restaurare il corretto FSR_Y
        drawMaxMin = false;
        enableViewTimeInterval = true;

        ThePath = new Path();
        if (FSR_X>0){
            FULL_SCALE_TIME = FSR_X;   //numero di milli secondi da rappresentare sul grafico
        }
        x_in_start_box = time[0];

        ThePath = ConvertArrayToPath(time, data, 0, 0);
        GraphRedraw();
    }



    int signal_max = 0;
    int signal_min = 0;
    int signal_mean = 0;
    // usato per il comando zoom signal
    //==========================================================================
    public void PutAndCentreSignal(long[] time, int[] data, int FSR_X){//}, int signal_mean) {
        //==========================================================================
        // mando tutto il buffer da stampare e lo fitta al centro del plot box

        drawMaxMin = false;
        enableViewTimeInterval = true;

        Data = data;// copio i dati in locale
        Time = time;

        int i;
        signal_mean = 0;
        signal_max = data[0];
        signal_min = data[0];
        for (i=0; i < data.length; i++){
            signal_mean += data[i];
        }
        signal_mean /= data.length;

        signal_max = data[0] - signal_mean;
        signal_min = data[0] - signal_mean;

        for (i = 0; i < data.length; i++) {
             data[i] -= signal_mean;
            //cerco massimo e minimo
            if(data[i] > signal_max){
                signal_max = data[i];
            } else if(data[i] < signal_min){
                signal_min = data[i];
            }
        }
        signal_mean /= data.length;

        ThePath = new Path();
        //ThePath.moveTo(Box_TopLeftCorner_X, (Box_TopLeftCorner_Y + Box_Height / 2));  //punto di inizio del plot
        if (FSR_X>0){
            FULL_SCALE_TIME = FSR_X;   //numero di milli secondi da rappresentare sul grafico
        }

        FSR_Y = (signal_max - signal_min) + (int)((10.0/100.0)*(signal_max - signal_min));     // range del segnale  medio più il x % dello stesso per contenere i picchi
        //Y_bias = -(signal_min - 5) - FSR_Y/2;


        x_in_start_box = time[0];

        // per centrare il segnale orizzontalmente
        // se lo uso per il fit data non cambia il risultato perchè deltat=0
        float t_centre = RemapData_X(time[time.length/2]);
        float deltaT = Box_Width/2 - t_centre;

        //ThePath.moveTo((Box_TopLeftCorner_X + deltaT), RemapData_Y(data[0]-signal_mean + FSR_Y/2));

        ThePath = ConvertArrayToPath(time, data, deltaT, (-signal_mean + FSR_Y/2) );
        /*for(i=0;i<time.length-1; i++){
            ThePath.lineTo((RemapData_X(time[i]) + deltaT), RemapData_Y(data[i]-signal_mean + FSR_Y/2));
        }*/
        GraphRedraw();
    }

    //CON ZOOM FUNZIONANTE MA LENTO
    // lo uso per mostrare i segnali medi
    float deltaT;
    //==========================================================================
    public void PutAndCentreMeanSignal(long[] time, int[] data, int FSR_X) {
        //==========================================================================
        // mando tutto il buffer da stampare e lo fitta al centro del plot box

        drawMaxMin = true;
        enableViewTimeInterval = false;

        Data = data;// copio i dati in locale
        Time = time;

        ThePath = new Path();
        if (FSR_X > 0) {
            FULL_SCALE_TIME = FSR_X;   //numero di milli secondi da rappresentare sul grafico
        }
        // media segnale: da spostare a metà dinamica:
        signal_max = data[0];
        signal_min = data[0];
        int i;
        for (i = 0; i < data.length; i++) {
            if (data[i] > signal_max)
                signal_max = data[i];
            if (data[i] < signal_min)
                signal_min = data[i];
        }

        FSR_Y = (signal_max - signal_min) + (int)(0.05 * (signal_max - signal_min));// + (int) ((5.0 / 100.0) * (double)(max - min));     // range del segnale  medio più il x % dello stesso per contenere i picchi

        x_in_start_box = time[0];

        // per centrare il segnale orizzontalmente // se lo uso per il fit data non cambia il risultato perchè deltat = 0
        float t_centre = RemapData_X(time[time.length / 2]);
        deltaT = Box_Width / 2 - t_centre;

        ThePath = ConvertArrayToPath(time, data, deltaT, (-signal_min));
        GraphRedraw();
    }

    //==========================================================================
    public float RemapData_Y(int y_in_value){
        //==========================================================================

        float y_new = (Box_TopLeftCorner_Y + Box_Height) - (y_in_value * (float)Box_Height / (float)FSR_Y) ;
        return y_new;
    }

    //==========================================================================
    public long RemapData_X(long x_in_value){
        //==========================================================================
        long x_new=0;
        /*if(x_in_value - x_in_start_box > FULL_SCALE_TIME){ // se il nuovo campione è distante più di FULL_SCALE ms dal primo valore del plot, allora ricomincia
            x_in_start_box = x_in_value;
            ThePath = new Path();
            ThePath.moveTo(Box_TopLeftCorner_X, (Box_TopLeftCorner_Y+Box_Height/2));

            Ymean = 0;
        }*/

        //da millisec a unità display
        x_new = (Box_TopLeftCorner_X) + ((x_in_value - x_in_start_box)*Box_Width/FULL_SCALE_TIME);
        return x_new;

    }
    //==========================================================================
    private int ReverseRemapData_Y(int display_y) {
        //==========================================================================
        int y;
        y = ((Box_TopLeftCorner_Y + Box_Height) * FSR_Y / Box_Height);//todo da verificare
        return y;
    }
    //==========================================================================
    public long ReverseRemapData_X(int display_x){
        //==========================================================================
        // restituisce tempo in millisecondi
        long x;
        x = ((display_x - Box_TopLeftCorner_X) * FULL_SCALE_TIME / Box_Width) + x_in_start_box;
        return x;
    }




    private boolean drawRedLines = false;
    private int[] PeakPositions;
    private int PeakNumber;
    //==========================================================================
    public void DrawLinesOnPeaks(int[] peak_positions, int peak_num) {
        //==========================================================================
        PeakPositions = peak_positions;
        PeakNumber = peak_num;

        drawRedLines = true;
        GraphRedraw();
    }

    //==========================================================================
    public void GraphRedraw(){
        //==========================================================================
        this.postInvalidate();
    }
    //==========================================================================
    private void CheckSignal(int graph_num){
        //==========================================================================
        // CAPIAMO QUALE SEGNALE DEVE ESSER PLOTTATO
        if(UserSettings.EnabledSource[graph_num] == 0) {
            SignalName = "Magic\n";
            if(UserSettings.EnabledSignal[graph_num] == 0){//accelerometro della magic
                FSR_Y_MAX = (int)(FULL_SCALE_ACC_14BIT * Acc_gain);
                FSR_Y = FSR_Y_MAX;
                MeasureUnit = "mg";
                graph_gain = Acc_gain;

                if(UserSettings.EnabledAxes[graph_num] == 0)
                    SignalName += " Acc X";
                if(UserSettings.EnabledAxes[graph_num] == 1)
                    SignalName += " Acc Y";
                if(UserSettings.EnabledAxes[graph_num] == 2)
                    SignalName += " Acc Z";
            } else if(UserSettings.EnabledSignal[graph_num] == 1) {  // ecg
                FSR_Y_MAX = (int)(FULL_SCALE_ECG0 * Ecg_resp_gain);
                FSR_Y = FSR_Y_MAX;
                graph_gain = Ecg_resp_gain;

                MeasureUnit = "mV";
                SignalName += " ECG";

            } else if(UserSettings.EnabledSignal[graph_num] == 2) {  // resp
                FSR_Y_MAX = (int)(FULL_SCALE_RESP0 * Ecg_resp_gain);
                FSR_Y = FSR_Y_MAX;
                graph_gain = Ecg_resp_gain;

                MeasureUnit = "mV";
                SignalName += " Resp";
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
                FSR_Y_MAX = (int)(FULL_SCALE_ACC_14BIT * Acc_gain);
                FSR_Y = FSR_Y_MAX;
                graph_gain = Acc_gain;

                MeasureUnit = "mg";
                if(UserSettings.EnabledAxes[graph_num] == 0)
                    SignalName += " X";
                if(UserSettings.EnabledAxes[graph_num] == 1)
                    SignalName += " Y";
                if(UserSettings.EnabledAxes[graph_num] == 2)
                    SignalName += " Z";

            }else if(UserSettings.EnabledSignal[graph_num] == 5){// pletismografo
                FSR_Y = FULL_SCALE_PPG_16BIT;
                FSR_Y_MAX = FULL_SCALE_PPG_16BIT;
                MeasureUnit = "au"; //arbitrary unit
                graph_gain = 1;

                if(UserSettings.EnabledAxes[graph_num] == 0)
                    SignalName += " PPG_RED";
                if(UserSettings.EnabledAxes[graph_num] == 1)
                    SignalName += " PPG_IR";
            }
        }
    }
    //==========================================================================
    public void ClearAll(){
        //==========================================================================
        if(TheCanvas!= null){
            Time = null;
            Data = null;
            ThePath = null;
            isClearMode = true;
            TheCanvas.drawColor(Color.WHITE);
            //TheCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            GraphRedraw();
        }
    }
    private long T1 = 0;
    private long T2 = 0;
    private boolean enableViewTimeInterval = false;
    //==========================================================================
    public void printT1T2(long t1, long t2){
        //==========================================================================
        if (GraphNum == 3){//mette scritta del tempo di inizio e fine visualizzazione
            T1 = t1;
            T2 = t2;
            enableViewTimeInterval = true;
        }
    }

    //==========================================================================
    public void putNewSettings(Settings newsettings){
        //==========================================================================
        UserSettings = newsettings;
    }
    /*//==========================================================================
    public void PutDataAndFit(long[] time, int[] data, int FSR_X) {
        //==========================================================================
        // mando tutto il buffer da stampare e lo fitta nello spazio disponibile
        Data = data;// copio i dati in locale
        Time = time;

        ThePath = new Path();
        ThePath.moveTo(Box_TopLeftCorner_X, (Box_TopLeftCorner_Y + Box_Height / 2));  //punto di inizio del plot
        if (FSR_X>0){
            FULL_SCALE_TIME = FSR_X;   //numero di milli secondi da rappresentare sul grafico
        }

        // media segnale: da spostare a metà dinamica:
        int i;
        int min = 0;
        int max = 0;
        int mean = 0;
        for(i=0; i< data.length; i++){
            if(data[i]>max)
                max = data[i];
            if(data[i]<min)
                min = data[i];
            mean +=  data[i];
        }
        mean /= data.length;
        FSR_Y = (max - min) + (int)((30.0/100.0)*(max-min));// + (50/100)*(max-min);     // range del segnale  medio più il x % dello stesso per contenere i picchi
        Y_bias = FSR_Y/2;

        x_in_start_box = time[0];
        for(i=0;i<time.length-1; i++){
            ThePath.lineTo(RemapData_X(time[i]), RemapData_Y(data[i]-mean));
        }
        GraphRedraw();
    }*/
}
