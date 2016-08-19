package diana.com.seismotepro;

import java.io.Serializable;

/**
 * Created by Diana Scurati on 17/12/2015.
 */
//==========================================================================
//==========================================================================
//==========================================================================
public class ByteDataExchange implements Serializable {
    //==========================================================================
    // synchro + memoria condivisa
    private boolean busy = false;
    private int SIZE_BUFFER;
    private byte[] buffer;
    private int buffer_read_idx = 0;
    private int buffer_write_idx = 0;
    private int buffer_available = 0;   //byte disponibili sul buffer
    public int OverFlow_cnt = 0;
    public int UnderFlow_cnt = 0;

    public ByteDataExchange(int bufsize){ // 1024 per decode, 64k per store
        SIZE_BUFFER = bufsize;
        buffer = new byte[SIZE_BUFFER];
    }

    //==========================================================================
    public synchronized boolean put (byte value) { // aggiunge dato BYTE al buffer
        //==========================================================================
        if(!busy){
            busy = true;
            buffer[buffer_write_idx++] = value;
            buffer_available++;

            if(buffer_available> SIZE_BUFFER) {
                //call_toast("err byte" +"_buf size: " + SIZE_BUFFER);
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
    public synchronized short get(){  // prende un dato BYTE dal buffer
        //==========================================================================
        byte value = 0;
        if(!busy){
            if(buffer_available>0) {
                busy = true;
                value = buffer[buffer_read_idx++];
                if (buffer_read_idx >= SIZE_BUFFER) {
                    buffer_read_idx = 0;
                }

                buffer_available--;
                notify();
                busy = false;
            } else {
                UnderFlow_cnt++;
                return 1000; // non rappresentabile con un byte
            }
        } else{
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return value;
    }
    //==========================================================================
    public synchronized void Reset() {
        //==========================================================================
        buffer_read_idx = 0;
        buffer_write_idx = 0;
        buffer_available = 0;
        buffer = new byte[SIZE_BUFFER];
    }
    //==========================================================================
    public synchronized int getAvailable(){
        //==========================================================================
        return buffer_available;
    }
    public int get_SIZE_BUFFER(){return SIZE_BUFFER;}

}
