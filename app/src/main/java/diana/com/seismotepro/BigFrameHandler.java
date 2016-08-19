package diana.com.seismotepro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diana Scurati on 28/12/2015.
 */

// class utility per la gestione delle linee per la selezione delle zone del grafico
//==========================================================================
public class BigFrameHandler extends View {
    //==========================================================================
    private int FrameHeight;
    private int FrameWidth;
    private Canvas TheCanvas = null;
    private Paint GraphPaint;
    private Paint LinePaint;
    private Paint VerticalLinePaint;
    private Paint TextPaint;

    // posizionamento della linea di selezione del grafico
    private int Line1_X = 300;
    private int Line2_X = 600;
    private boolean Line1selected = false;
    private boolean Line2selected = false;
    private int mActivePointerId = -1;
    private int pointerIndex;
    private float LastTouchX;
    private float LastTouchY;
    private float PosX=20;
    private float PosY=20;
    private float x;
    private float y;
    private int Line_SensibleArea = 35;

    private  boolean isClearMode = false;

    private short LastMovedLine = 3;//inizializzo a valore fasullo
    private static final short Line1_id = 1;
    private static final short Line2_id = 2;
    private long Rpeak_time = -1;


    //costruttore
    //==========================================================================
    public BigFrameHandler(Context context, int frame_width, int frame_height ) {
        //==========================================================================
        super(context);
        FrameHeight = frame_height;
        FrameWidth = frame_width;

        GraphPaint = new Paint();
        GraphPaint.setDither(true);
        GraphPaint.setAntiAlias(true);
        GraphPaint.setStyle(Paint.Style.STROKE);
        GraphPaint.setStrokeWidth(1);
        GraphPaint.setColor(Color.BLACK);

        LinePaint = new Paint();
        LinePaint.setColor(Color.DKGRAY);
        LinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        LinePaint.setStrokeWidth(2);

        VerticalLinePaint = new Paint();
        VerticalLinePaint.setColor(Color.GREEN);
        VerticalLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        VerticalLinePaint.setStrokeWidth(3/2);
        VerticalLinePaint.setTextSize(17);

        TextPaint = new Paint();
        TextPaint.setColor(Color.WHITE);
        TextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        TextPaint.setStrokeWidth(2);
        TextPaint.setTextSize(20);


    }
    @Override
    //==========================================================================
    public void onDraw(Canvas canvas) {
        //==========================================================================
        super.onDraw(canvas);
        TheCanvas = canvas;



        if(!isClearMode) {
            DrawLine1(canvas);
            DrawLine2(canvas);

            if(doDrawVerticalLine){
                TheCanvas.drawLine(VerticalLine_X, 0, VerticalLine_X, 0 + FrameHeight - 30, VerticalLinePaint); // il meno 30 serve a non uscire dal grafico in basso, visto che lascia lo spazio ai pallini
            }
            if(doDrawEvents){
                int i;
                for(i=0; i<Events_X.length; i++){
                    TheCanvas.drawLine(Events_X[i], 0, Events_X[i], FrameHeight - 30, VerticalLinePaint);
                }
            }
            if(MarkerPos!=null && !MarkerPos.X_screen.isEmpty()){
                for(int idx = 0; idx < MarkerPos.X_screen.size(); idx++){
                    // disegna la linea verde sul marker
                    TheCanvas.drawLine(MarkerPos.X_screen.get(idx), 0, MarkerPos.X_screen.get(idx), FrameHeight-30, VerticalLinePaint);

                    // scrivo il tempo trascorso dal picco r
                    String timeFromRpeak;
                    if(Rpeak_time > 0){//e cioè se è stato impostato
                        timeFromRpeak = (MarkerPos.TimeFromRpeak.get(idx) - Rpeak_time) + "ms";
                    }else{
                        timeFromRpeak = MarkerPos.TimeFromRpeak.get(idx) + "ms";
                    }


                    canvas.drawText(timeFromRpeak, (MarkerPos.X_screen.get(idx) - 5), (FrameHeight - 10), VerticalLinePaint);

                }

            }
        }else{
            doDrawVerticalLine = false;
            doDrawEvents = false;
            isClearMode = false;
            canvas.drawColor(Color.WHITE);
        }
    }


    //==========================================================================
    private void DrawLine1(Canvas canvas){
        //==========================================================================
        canvas.drawLine((Line1_X), 0, (Line1_X), (0 + FrameHeight), LinePaint);
        canvas.drawCircle((Line1_X), (FrameHeight - 15), 15, LinePaint);
        canvas.drawText("1", (Line1_X - 5), (FrameHeight - 10), TextPaint);
    }
    //==========================================================================
    private void DrawLine2(Canvas canvas){
        //==========================================================================
        canvas.drawLine((Line2_X), 0, (Line2_X), (0 + FrameHeight), LinePaint);
        canvas.drawCircle((Line2_X), (FrameHeight - 15), 15, LinePaint);
        canvas.drawText("2", (Line2_X - 5), (FrameHeight - 10), TextPaint);
    }
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



                    if((x < Line1_X + Line_SensibleArea) &&(x > Line1_X - Line_SensibleArea)) {   //selezionata la linea 1
                        Line1selected = true;
                    }else if((x < Line2_X + Line_SensibleArea) &&(x > Line2_X - Line_SensibleArea)){//selezionata la linea 2
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
                    LastTouchY = y;

                    return true;
                case (MotionEvent.ACTION_UP):      // dito alzato
                    //==========================================================================
                    pointerIndex = MotionEventCompat.getActionIndex(event);
                    int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

                    if(Line1selected) {
                        Line1selected = false;
                        LastMovedLine = Line1_id;
                    }
                    if(Line2selected) {
                        Line2selected = false;
                        LastMovedLine = Line2_id;
                    }
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
    //==========================================================================
    public void GraphRedraw(){
        //==========================================================================
        this.postInvalidate();
    }

    public boolean doDrawVerticalLine = false;
    private int VerticalLine_X;
    //==========================================================================
    public void DrawVerticalLine(int x_pos){
        //==========================================================================
        // x pos in unità display
        VerticalLine_X = x_pos;
        Rpeak_time = Graph1.ReverseRemapData_X(x_pos);
        doDrawVerticalLine = true;
        GraphRedraw();
    }

    public boolean doDrawEvents = false;// per disegnare linea verde in corrispondenza dell'evento magic
    private int[] Events_X;
    //==========================================================================
    public void DrawVerticalLinesOnEvents(int[] x_pos){
        //==========================================================================
        // x pos in unità display
        Events_X = new int[x_pos.length];
        System.arraycopy(x_pos, 0, Events_X, 0, Events_X.length);
        //Events_X = x_pos;
        doDrawEvents = true;
        GraphRedraw();
    }


    public int get_Line1_X(){return (int)(Line1_X);}
    public int get_Line2_X(){return (int)(Line2_X);}

    //==========================================================================
    public void ClearAll(){
        //==========================================================================
        if(TheCanvas!= null){
            TheCanvas.drawColor(Color.WHITE);
            TheCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            isClearMode = true;
            GraphRedraw();
        }
    }
    //==========================================================================
    public void PutLinesAtBorders(){
        //==========================================================================
        Line1_X = 10;
        Line2_X = (FrameWidth * 90 / 100) - 10;
        GraphRedraw();
    }

    //==========================================================================
    public void PutLinesAt(long x_line1, long x_line2){// x in unità display
        //==========================================================================
        Line1_X = (int)x_line1;
        Line2_X = (int)x_line2;
        GraphRedraw();

    }

    //=============================
    private class MarkerPositions{
        //=============================
        public ArrayList<Integer> X_screen;
        public ArrayList<Long> TimeFromRpeak;

        public MarkerPositions(){
            X_screen = new ArrayList<Integer>();
            TimeFromRpeak = new ArrayList<Long>();// time from r-peak
        }
    }
    MarkerPositions MarkerPos = new MarkerPositions();
    //==========================================================================
    public void AddMarker(){//}, long y_signal){// x in unità display
        //==========================================================================
        int x_marker = 0;
        if (LastMovedLine == Line1_id)
            x_marker = get_Line1_X();//in unità display
        else if(LastMovedLine == Line2_id)
            x_marker = get_Line2_X();

        MarkerPos.X_screen.add(x_marker);
        MarkerPos.TimeFromRpeak.add(Graph1.ReverseRemapData_X(x_marker));


        GraphRedraw();
    }
    //==========================================================================
    public void ClearMarkers(){
        //==========================================================================
        MarkerPos = new MarkerPositions();
        GraphRedraw();
    }

    private AnalysisGraph Graph1 = null;
    // per avere il riferimento al graph 1 per la reverse remap della x per ottenere il tempo del marker da plottare
    public void put_graph1_ref(AnalysisGraph graph1){
        Graph1 = graph1;
    }
}
