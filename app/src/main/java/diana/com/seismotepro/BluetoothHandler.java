package diana.com.seismotepro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

//import app.akexorcist.bluetotohspp.library.BluetoothSPP;
//import app.akexorcist.bluetotohspp.library.BluetoothState;


/**
 * Created by Diana Scurati on 03/11/2015.
 */
//==========================================================================
public class BluetoothHandler extends Thread {
    //==========================================================================
    //Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    private Set<BluetoothDevice> BTpairedDevices = null;

    private OutputStream streamOut;
    private InputStream streamIn;

    private byte received8bit = 0;
    private boolean isBT_Enabled = false;
    private boolean isBT_Connected = false;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // NON MODIFICARE!
    private BluetoothSocket BTsocket;

    private Context local_context;

    private BluetoothDevice FoundDevices[];

    /*private DataOutputStream PipeOutput_store;
    private DataOutputStream PipeOutput_decode;*/

    private final static byte MAGIC_START_COMMAND = (byte)0xc4;
    private final static byte MAGIC_STOP_COMMAND = (byte)0xc5;

   // private Activity1.DataExchangeControl Pipe_BTStore_control;
    //private Activity1.DataExchangeControl Pipe_BTPlot_control;

   /* private Activity1.ByteDataExchange Store_SharedMem;
    private Activity1.ByteDataExchange Decode_SharedMem;*/
    private ByteDataExchange Store_SharedMem;
    private ByteDataExchange Decode_SharedMem;

    private Thread btThread;
    private boolean ThreadRunning = false;
    private boolean isRecording = false;


    // costruttore
    //==========================================================================
    public BluetoothHandler(Context c, ByteDataExchange prova_exch, ByteDataExchange prova_exch2){//Activity1.DataExchangeControl prova_exch, Activity1.DataExchangeControl prova_exch2){
        //==========================================================================
        local_context = c;
        Store_SharedMem = prova_exch;
        Decode_SharedMem = prova_exch2;
    }
    //==========================================================================
    public BluetoothHandler(Context c, OutputStream os_save, OutputStream os_decode, ByteDataExchange prova_exch, ByteDataExchange prova_exch2){//Activity1.DataExchangeControl prova_exch, Activity1.DataExchangeControl prova_exch2){
        //==========================================================================
        local_context = c;
       /* PipeOutput_store = new DataOutputStream(os_save);
        PipeOutput_decode = new DataOutputStream(os_decode);*/
        /*Pipe_BTStore_control = prova_exch;
        Pipe_BTPlot_control = prova_exch2;*/

        Store_SharedMem = prova_exch;
        Decode_SharedMem = prova_exch2;
    }

    // abilita bt se è spento
    //==========================================================================
    public void StartBT(){
        //==========================================================================
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // TODO Device does not support Bluetooth
            isBT_Enabled = false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            isBT_Enabled = mBluetoothAdapter.enable(); // accende bt
        } else {
            isBT_Enabled = true;
        }
    }
    //==========================================================================
    public void StopBT(){
        //==========================================================================
        try {
            //PipeOutput.write(tuttoilfile, 0, tuttoilfile_index);
            if ( isBT_Connected) {
                StopCommunication(BTsocket);
                BTsocket.close();
                isBT_Connected = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //==========================================================================
    public void SearchBondedBTDevices(ListView List){
        //==========================================================================
        byte index = 0;
        FoundDevices = new BluetoothDevice[10];

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BTpairedDevices = GetBondedDevices();

        java.util.List<String> s = new ArrayList<String>();
        for(BluetoothDevice bt : BTpairedDevices) {
            s.add(bt.getName());        // lista per stampa a video
            FoundDevices[index] = bt;       // array di elementi bt
            index++;
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(local_context, android.R.layout.simple_list_item_1, s);
        List.setAdapter(arrayAdapter); //stampa la lista
    }

    //stessa cosa della precedente ma non stampa la lista
    //==========================================================================
    public void SearchBondedBTDevices( Context cont){
        //==========================================================================
        local_context = cont;
        byte index = 0;
        FoundDevices = new BluetoothDevice[10];

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BTpairedDevices = GetBondedDevices();

        List<String> s = new ArrayList<String>();
        for(BluetoothDevice bt : BTpairedDevices) {
            s.add(bt.getName());        // lista per stampa a video
            FoundDevices[index] = bt;       // array di elementi bt
            index++;
        }
    }

    // restituisce i dispositivi bonded
    //==========================================================================
    public Set<BluetoothDevice> GetBondedDevices(){
        //==========================================================================
        BluetoothAdapter BtAdap = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> BTpaired = BtAdap.getBondedDevices(); //cerca i dispositivi già bonded
        return BTpaired;
    }

    //==========================================================================
    public boolean Connect (int num){
        //==========================================================================
        if(FoundDevices != null)
            return ConnectToBTDevice(FoundDevices[num]);
        else
            return false;

    }

    //==========================================================================
    public boolean ConnectToBTDevice(BluetoothDevice ToBeConnected) {
        //==========================================================================
        if (isBT_Enabled) {
            try {
                if (!isBT_Connected) {
                    mBluetoothAdapter.getRemoteDevice(ToBeConnected.getAddress());
                    mBluetoothAdapter.cancelDiscovery();

                    //ToBeConnected.createBond(); MIN API = 19!!!
                    BluetoothSocket localBluetoothSocket = ToBeConnected.createRfcommSocketToServiceRecord(MY_UUID);
                    //BluetoothSocket localBluetoothSocket = createL2CAPBluetoothSocket(ToBeConnected);

                    BTsocket = localBluetoothSocket;
                    BTsocket.connect();
                    isBT_Connected = true;

                    // todo forse da togliere le prox due righe
                    streamOut = BTsocket.getOutputStream();
                    streamIn = BTsocket.getInputStream();
                }
            }catch(Exception e){
                isBT_Connected = false;
                call_toast("connection error");
                call_toast("Check MagIC ON");
                return false;
            }finally {
                if (isBT_Connected) {
                    StartCommunication(BTsocket);
                }
            }
        }
        return true;
    }


    //==========================================================================
    public void StartCommunication(BluetoothSocket BT_sock) {
        //==========================================================================
        streamIn = new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        streamOut = new OutputStream() {
            @Override
            public void write(int oneByte) throws IOException {

            }
        };
        try {
            streamOut = BT_sock.getOutputStream(); //apre stream in ingresso e in uscita
            streamIn =  BT_sock.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // invio del comando di start
        SendCommand30((byte) 0xc4, (byte) 0, (int) 0);
    }

    //==========================================================================
    public void StopCommunication(BluetoothSocket BT_sock) {
        //==========================================================================
        //BT_spp.stopService();
        // invio del comando di stop
        SendCommand30(MAGIC_STOP_COMMAND, (byte) 0, (int) 0);
    }

    // TODO: di paolo, ancora da modificare
    //==========================================================================
    public void SendCommand30(byte comando, byte arg_char, int arg_int) {
        //==========================================================================
        byte[] buffer_out = new byte[9];
        byte chk;
        int i;

        chk = (byte) 0xff;
        buffer_out[0] = 0x00;
        buffer_out[1] = 0x30;   //ver

        // packet ID
        buffer_out[2] = 0x00;
        buffer_out[3] = 0x01;

        // CMD
        buffer_out[4] = comando;

        buffer_out[5] = arg_char;

        //arg int
        buffer_out[6] = (byte) ((arg_int & 0xff00) >> 8);
        buffer_out[7] = (byte) (arg_int & 0x00ff);

        // calcolo il checksum
        for (i = 1; i < 8; i++)
            chk ^= buffer_out[i];

        // imposto il chesckum
        buffer_out[8] = chk;

        try {
            BTsocket.getOutputStream().write(buffer_out, 0, 9);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // con vecchia gestione delle pipe
 /*   private int local_fifo_flush_idx = 1024;//  watermark
    private byte[] local_fifo = new byte[1024*128];//local_fifo_flush_idx*2];
    private int local_fifo_index=0;
    private int BTBurstSize = 256;

    private byte[] buf_in = new byte[1024];
    private int buf_in_idx =0;
*/
    long BytesReceived=0; // stampati a video dopo acquisizione

    //========================================
    // todo nuovo x versione con synchro
    private byte bt_byte_in=0;
  /*  public Object Pipe_BTDecode_Status = (byte) 0; // 1= decode può leggere, 0 =
    public Object Pipe_BTStore_Status = (byte) 0; // 1= store può leggere, 0 = sto elaborando
    private byte BT_START_SEND = 1;
    private byte BT_WAIT_SEND = 0;*/

    // PARTE PER INVIO A DECODIFICA
    private final static int SIZE_PIPE_DECODE_SEND = 128;
    private int Buffer_ToDecode_write_idx = 0;
    private int Buffer_ToDecode_read_idx = 0;
    private int Buffer_ToDecode_avail = 0;
    private byte[] Buffer_ToDecode = new byte[SIZE_PIPE_DECODE_SEND*4];// 4 volte la dimensione del blocco di dati che invio sulla pipe

    // PARTE PER INVIO A STORE
    private final static int SIZE_PIPE_STORE_SEND = 64*1024;
    private int Buffer_ToStore_write_idx = 0;
    private int Buffer_ToStore_read_idx = 0;
    private int Buffer_ToStore_avail = 0;
    private byte[] Buffer_ToStore = new byte[SIZE_PIPE_STORE_SEND*4];// 4 volte la dimensione del blocco di dati che invio sulla pipe
    //========================================


    @Override
    //==========================================================================
    public void run() {
    //==========================================================================
        while (ThreadRunning) {
            try {
                // todo gestione dati con mem condivisa
                bt_byte_in = (byte) streamIn.read();  // ricevo i dati dalla magic
                BytesReceived++;

                // gestione con synchro per l'invio dati alla decode
                Buffer_ToDecode[Buffer_ToDecode_write_idx] = bt_byte_in;  //accodo a buffer locale
                Buffer_ToDecode_write_idx++;
                Buffer_ToDecode_avail++;
                if(Buffer_ToDecode_write_idx>=Buffer_ToDecode.length){
                    Buffer_ToDecode_write_idx = 0;
                }

                // mando nella shared memory
                if (Buffer_ToDecode_avail >= SIZE_PIPE_DECODE_SEND) {
                    for ( int i=0; i< Buffer_ToDecode_avail; i++){

                        Decode_SharedMem.put(Buffer_ToDecode[Buffer_ToDecode_read_idx]);
                        Buffer_ToDecode_read_idx++;
                        Buffer_ToDecode_avail--;
                        if(Buffer_ToDecode_read_idx>=Buffer_ToDecode.length)
                            Buffer_ToDecode_read_idx = 0;
                    }
                }

                if(isRecording) {
                    // accodo dati al buffer circolare solo se sto registrando i dati in memoria
                    // gestione con synchro per l'invio dati alla store
                    Buffer_ToStore[Buffer_ToStore_write_idx] = bt_byte_in;
                    Buffer_ToStore_write_idx++;
                    Buffer_ToStore_avail++;
                    if (Buffer_ToStore_write_idx >= Buffer_ToStore.length) {
                        Buffer_ToStore_write_idx = 0;
                    }

                    if (Buffer_ToStore_avail >= SIZE_PIPE_STORE_SEND) {
                        for (int i = 0; i < Buffer_ToStore_avail; i++) {
                            Store_SharedMem.put(Buffer_ToStore[Buffer_ToStore_read_idx]);
                            Buffer_ToStore_read_idx++;
                            Buffer_ToStore_avail--;
                            if (Buffer_ToStore_read_idx >= Buffer_ToStore.length)
                                Buffer_ToStore_read_idx = 0;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // todo versione funzionante 2 con scambio dati basato su controllo variabile isConsumerReadyToGetData
            /*try {

                bt_byte_in = (byte) streamIn.read();  // ricevo i dati dalla magic
                BytesReceived++;

                // gestione con synchro per l'invio dati alla decode
                Buffer_ToDecode[Buffer_ToDecode_write_idx++] = bt_byte_in;
                Buffer_ToDecode_avail++;
                if(Buffer_ToDecode_write_idx>=Buffer_ToDecode.length){
                    Buffer_ToDecode_write_idx = 0;
                }

                if (Buffer_ToDecode_avail >= SIZE_PIPE_DECODE_SEND) {
                    boolean res = Pipe_BTPlot_control.get();
                    if(res){

                        PipeOutput_decode.write(Buffer_ToDecode, Buffer_ToDecode_read_idx, SIZE_PIPE_DECODE_SEND);

                        Buffer_ToDecode_avail -= SIZE_PIPE_DECODE_SEND;
                        Buffer_ToDecode_read_idx += SIZE_PIPE_DECODE_SEND;
                        if(Buffer_ToDecode_read_idx>=Buffer_ToDecode.length){
                            Buffer_ToDecode_read_idx=0;
                        }
                    }
                }

                // gestione con synchro per l'invio dati alla store
                Buffer_ToStore[Buffer_ToStore_write_idx++] = bt_byte_in;
                Buffer_ToStore_avail++;
                if(Buffer_ToStore_write_idx>=Buffer_ToStore.length){
                    Buffer_ToStore_write_idx = 0;
                }

                if (Buffer_ToStore_avail >= SIZE_PIPE_STORE_SEND) {
                    boolean res = Pipe_BTStore_control.get();
                    if(res){

                        PipeOutput_store.write(Buffer_ToStore, Buffer_ToStore_read_idx, SIZE_PIPE_STORE_SEND);

                        Buffer_ToStore_avail -= SIZE_PIPE_STORE_SEND;
                        Buffer_ToStore_read_idx += SIZE_PIPE_STORE_SEND;
                        if(Buffer_ToStore_read_idx>=Buffer_ToStore.length){
                            Buffer_ToStore_read_idx=0;
                        }
                    }
                }

                //Thread.sleep(1);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            /*try {
                // todo: versione funzionante 1: questa funziona sul tablet: non perde dati
                buf_in[buf_in_idx++] = (byte) streamIn.read();  // ricevo i dati dalla magic

                BytesReceived++;

                if (buf_in_idx >= 256) {
                    System.arraycopy(buf_in, 0, local_fifo, local_fifo_index, buf_in_idx);
                    local_fifo_index += buf_in_idx;
                    buf_in_idx = 0;

                    if (local_fifo_index >= (local_fifo_flush_idx)) {
                        PipeOutput.write(local_fifo, 0, local_fifo_index);              // invia dati per salvataggio su file
                        PipeOutput_decode.write(local_fifo, 0, local_fifo_index);       // invia i dati alla decode per decodificare i raw
                        local_fifo_index = 0;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }


  /*

    //==========================================================================
    public void SendCommand(byte Cmd) {
    //==========================================================================
        new Thread(new Runnable() {
            public void run() {
                *//*try {
                    // TODO da personalizzare--> c'è la build command packet nella magic_packet
                    BTsocket.getOutputStream().write(0, 0, 9);
                } catch (IOException e) {
                    e.printStackTrace();
                }*//*

            }
        }).start();
    }*/


    //==========================================================================
    public void startThread(){
        //==========================================================================
        if(!ThreadRunning && isBT_Connected) {
            BytesReceived=0;
            Buffer_ToDecode_write_idx = 0;
            Buffer_ToDecode_read_idx = 0;
            Buffer_ToDecode_avail = 0;
            Buffer_ToStore_write_idx = 0;
            Buffer_ToStore_read_idx = 0;
            Buffer_ToStore_avail = 0;

            btThread = new Thread(this);
            btThread.start();
            btThread.setPriority(Thread.MAX_PRIORITY);

            ThreadRunning = true;
        }
    }
    //==========================================================================
    public void stopThread(){
        //==========================================================================
        if(ThreadRunning) {
            // fa fermare il runnable
            btThread.interrupt();
            ThreadRunning = false;
        }
    }

    public void put_isRecording(boolean value){
        isRecording = value;
    }
    public boolean get_isRecording(){return isRecording;}




    //==========================================================================
    private void call_toast(CharSequence text){
        //==========================================================================
        // SETS A KIND OF POP-UP MESSAGE
        Context context = local_context;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
