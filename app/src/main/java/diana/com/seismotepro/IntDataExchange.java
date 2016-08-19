package diana.com.seismotepro;

import java.io.Serializable;

/**
 * Created by Diana Scurati on 17/12/2015.
 */
//==========================================================================
//==========================================================================
//==========================================================================
public class IntDataExchange implements Serializable {
    //==========================================================================
    // per scambio dati decodificati con timestamp
    // synchro + memoria condivisa
    private boolean busy = false;
    private int SIZE_BUFFER = 1024;
    private int[] buffer = new int[SIZE_BUFFER];
    private int buffer_read_idx = 0;
    private int buffer_write_idx = 0;
    private int buffer_available = 0;
    public int OverFlow_cnt = 0;
    public int UnderFlow_cnt = 0;

    private long[] Time;
    private int[] Data;

    public IntDataExchange(){
        Time = new long[SIZE_BUFFER];
        Data = new int[SIZE_BUFFER];
    }
    public IntDataExchange(int buf_size){
        Time = new long[buf_size];
        Data = new int[buf_size];
        SIZE_BUFFER = buf_size;
    }
    //==========================================================================
    public synchronized boolean put (long timestamp, int value) { // aggiunge dato int al buffer
        //==========================================================================
        if(!busy){
            busy = true;
            Time[buffer_write_idx] = timestamp;
            Data[buffer_write_idx] = value;
            buffer_write_idx++;
            buffer_available++;

            if(buffer_available > SIZE_BUFFER) {
                //call_toast("err_int");
                OverFlow_cnt++;
                buffer_available=0;
                buffer_write_idx=0;
                buffer_read_idx=0;
            }
            if(buffer_write_idx >= SIZE_BUFFER) {
                buffer_write_idx = 0;
            }

            notify();
            busy = false;
        } else{
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    //==========================================================================
    public synchronized void get(long[] dest){  //double[] get(double[] dest){  // prende un dato int dal buffer
        //==========================================================================
        // dest = {TIME, DATA}

        if(!busy){
            if(buffer_available>0) {
                busy = true;
                dest[0] = Time[buffer_read_idx];
                dest[1] = Data[buffer_read_idx];
                buffer_read_idx++;
                if (buffer_read_idx >= SIZE_BUFFER) {
                    buffer_read_idx = 0;
                }

                buffer_available--;
                notify();
                busy = false;
            } else {
                UnderFlow_cnt++;
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else{
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //return sample;
    }

    //==========================================================================
    public synchronized void Reset() {
        //==========================================================================
        buffer_read_idx = 0;
        buffer_write_idx = 0;
        buffer_available = 0;
        Time = new long[SIZE_BUFFER];
        Data = new int[SIZE_BUFFER];
    }
    //==========================================================================
    public synchronized int getAvailable(){
        //==========================================================================
        return buffer_available;
    }
    public int get_SIZE_BUFFER(){return SIZE_BUFFER;}
}
