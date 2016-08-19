package diana.com.seismotepro;

import android.os.AsyncTask;

/**
 * Created by Diana Scurati on 14/01/2016.
 */
//==========================================================================
public class DataDecoder_Offline extends AsyncTask<Void, Boolean, Boolean> {
    //==========================================================================

    private byte[] CircularBuffer;
    private final static int SIZE_CIRC_BUFF = 1024*64;
    private final static int SIZE_INPUT_FROM_PIPE = 128;
    private int CircularBuffer_WriteIndex = 0;
    private int CircularBuffer_ReadIndex = 0;
    private int BytesAvailable = 0;

    private static final short NO_BYTE_AVAILABLE = 7000; // codice di errore non rappresentabile con un byte
    private static final byte[] INVALID_HEADER = null;// (byte) 0xDD;


    private static final int MAXNUMSIGNALS = 20; //      'numero di segnali complessivi
    private static final int MAXNUMANALOGSIGNALS = 5;//      'numero di segnali analogici
    private static final int MAXNUMACQSTEPS = 12;//     'numero di campioni del segnale a peroido inferiore (ECG)
    //private static final int MAXNUMSAMPLES;



    // ID del pacchetto che segnala il reboot del giroscopio
    public static final int L3GD20_REBOOT = 0x0010;
    public static final int MMA845X_REBOOT = 0x0020;

    // contatore degli eventi di reboot
    int counter_L3GD20_REBOOT = 0;
    int counter_MMA845X_REBOOT = 0;

    public static final int SPARE8 = 31;

    // variabili per gestione pacchetti - header - payload
    private int PacketNumber;
    private int Old_PaketNumber;

    private int PacketType;
    private int ArgInt;
    private int ArgChar;

    private int PayloadType;
    private int PayloadSize = -1;
    private byte[] Payload;

    boolean bClip16bitTo12bit = false;


    /*private ByteDataExchange BTDecode_SharedMem;
    private IntDataExchange DecodePlot_SharedMem1;
    private IntDataExchange DecodePlot_SharedMem2;
    private IntDataExchange DecodePlot_SharedMem3;*/

    private byte[] Input = null;
    private int index_input = 0;
    public int[] OutputSignal1;// i due buffer che conterranno tempo e dati decodificati...
    public long[] OutputTime1;
    private int index1 = 0;         // e l'indice per scorrerli entrambi
    public int[] OutputSignal2;
    public long[] OutputTime2;
    private int index2 = 0;
    public int[] OutputSignal3;
    public long[] OutputTime3;
    private int index3 = 0;

    public long[] MagicEvents;
    public int evnt_nmb = 0;

    public AsyncResponse Decode_delegate;


    // VAR DI CONFIGURAZIONE DI QUALI SEGNALI MANDARE SU QUALI ASSi
/*  0 = non decodificare
    1 = per plot 1
    2 = per plot 2
    3 = per plot 3
 */

    private Settings UserSettings = null;

    private int Mote_FS = 1;       // sampling frequency [Hz]
    private double Mote_TS = 1;       // sampling period [ms]


    @Override
    //==========================================================================
    protected Boolean doInBackground(Void... params) {
        //==========================================================================
        ProcessData();
        return true;
    }

    @Override
    //==========================================================================
    protected void onPostExecute(Boolean finished) {
        //==========================================================================
        Decode_delegate.processFinish(finished);
    }


        //==========================================================================
        public DataDecoder_Offline( Settings us,
                                    byte[] input,       // INGRESSO DATI
                                    AsyncResponse delegate){
            //==========================================================================
            Input = input;
            UserSettings = us;
            Decode_delegate = delegate;
            MagicEvents = new long[50];
        }
    //==========================================================================
    public void put_IO_Buffers_Signal1(int size){
        //==========================================================================
        OutputSignal1 = new int[size];
        OutputTime1 = new long[size];
    }
    //==========================================================================
    public void put_IO_Buffers_Signal2(int size){
        //==========================================================================
        OutputSignal2 = new int[size];
        OutputTime2 = new long[size];
    }
    //==========================================================================
    public void put_IO_Buffers_Signal3(int size){
        //==========================================================================
        OutputSignal3 = new int[size];
        OutputTime3 = new long[size];
    }
    //==========================================================================
    public void clearBuffers(){
        //==========================================================================
        OutputTime1 = null;
        OutputTime2 = null;
        OutputTime3 = null;
        OutputSignal1 = null;
        OutputSignal2 = null;
        OutputSignal3 = null;
        Input = null;
    }


        public int CorruptedPayload = 0;
        public int CorruptedHeaders = 0;

        //==========================================================================
        private void ProcessData(){
            //==========================================================================
            if(Input != null) {
                while (index_input < Input.length) {    // scorro tutto il buffer di input

                    byte[] nextHeader = DecodeHeader();
                    if (nextHeader != INVALID_HEADER) { // se header è corretto

                        PacketNumber = (((int) nextHeader[1]) << 8) + ((int) nextHeader[2]);
                        ArgChar = (int) nextHeader[4];
                        ArgInt = (((int) nextHeader[5]) << 8) + ((int) nextHeader[6]);
                        PacketType = nextHeader[3];

                        boolean isPayloadValid = CheckPayload(PacketType, ArgChar, ArgInt);

                        if (isPayloadValid) {
                            DecodePayload(PacketType, ArgChar, ArgInt);
                        } else {
                            CorruptedPayload++;
                        }
                    } else if(HeaderStartFound){
                        CorruptedHeaders++;

                    }
                }
                int j = CorruptedHeaders + CorruptedPayload;
                j++;
            }
        }

        // trova il prossimo header e verifica il checksum
        byte[] HeaderToCheck;

        boolean HeaderStartFound = false;// indica se ha trovato il byte iniziale dell' header

        static final byte HEADER_ERR_INCOMPLETE = 1;//PACCHETTI INCOMPLETI
        static final byte HEADER_ERR_INVALID = 2;//checksum sbagliato
        static final byte HEADER_NO_ERR = 0;

        byte HeaderDecode_Error = HEADER_NO_ERR;

    //==========================================================================
        private byte[] DecodeHeader(){
            //==========================================================================
            byte[] res = INVALID_HEADER;
            short tmp;
            byte i = 1;
            HeaderToCheck = new byte[8];
            HeaderDecode_Error = HEADER_NO_ERR;//resetto l'errore a zero ( no err)

            HeaderStartFound = false;

            tmp = ReadNextByteFromBuffer();

            if(tmp != NO_BYTE_AVAILABLE){
                if(tmp == MagicCommProtocol.PROTOCOL_VERSION) {
                    HeaderToCheck[0] = (byte) tmp;
                    HeaderStartFound = true;

                    //leggo i successivi 7 byte
                    for (i = 1; i < 8; i++) {
                        tmp = ReadNextByteFromBuffer();
                        if(tmp != NO_BYTE_AVAILABLE)
                            HeaderToCheck[i] = (byte) tmp;
                        else {
                            HeaderDecode_Error = HEADER_ERR_INCOMPLETE;
                            break;
                        }
                    }
                    if((i == 8) && (HeaderDecode_Error != HEADER_ERR_INCOMPLETE)){// ho terminato il ciclo e TUTTO il buffer è stato riempito con dati validi
                        byte checkSum = CheckSum(HeaderToCheck);
                        if (checkSum == HeaderToCheck[7]) {
                            res = HeaderToCheck;                // return tutto l'header se corretto
                        }
                        else{
                            HeaderDecode_Error = HEADER_ERR_INVALID;// header sbagliato
                            //index_input -= 7;
                        }
                    }
                }
            }
            /*if(HeaderDecode_Error == HEADER_ERR_INVALID){//il checksum è sbagliato
                index_input -= 7;
            }else if(HeaderDecode_Error == HEADER_ERR_INCOMPLETE){
                index_input -= i-1;
            }*/
            return res;
        }


        /*//==========================================================================
        private byte[] DecodeHeader(){
            //==========================================================================
            byte[] res;
            boolean HeaderFound = false;
            short tmp;
            HeaderToCheck = new byte[8];
            int ProtocolVersionPosition = 0;

            //while (!HeaderFound){//cerco inizio header
                tmp = ReadNextByteFromBuffer();

                if (tmp != NO_BYTE_AVAILABLE){  // se ho dati
                    HeaderToCheck[0] = (byte)tmp;
                    if(HeaderToCheck[0] == MagicCommProtocol.PROTOCOL_VERSION){
                        HeaderFound = true; //esco dal ciclo
                        ProtocolVersionPosition = index_input; // salvo punto del 0x30 così se non trovo header al prossimo giro parto dalla posiz successiva
                        //ProtocolVersionPosition = CircularBuffer_ReadIndex; // salvo punto del 0x30 così se non trovo header al prossimo giro parto dalla posiz successiva
                    }
                }
                else {
                    HeaderFound = false;
                    //break;
                }
            //}

            if(HeaderFound) {
                for (byte i = 1; i < 8; i++) {                  //leggo i successivi 7 byte
                    tmp = ReadNextByteFromBuffer();         // non controllo se è = NO_BYTE_AVAILABLE perchè processo i dati solo se ne ho disponibili
                    HeaderToCheck[i] = (byte) tmp;
                }

                if (HeaderToCheck.length == 8) {//dimensione corretta dell'header
                    byte checkSum = CheckSum(HeaderToCheck);
                    if (checkSum == (HeaderToCheck[7])) {
                        res = HeaderToCheck;                // return tutto l'header se corretto
                    } else {

                        //CircularBuffer_ReadIndex = ProtocolVersionPosition++;
                        res = INVALID_HEADER;
                    }
                } else {
                    res = INVALID_HEADER;
                }
            }else{
                res = INVALID_HEADER;
            }
            if(res == INVALID_HEADER)// torno indietro a cercare il primo byte dell'header
                index_input -= 7;// faccio puntare all'elemento successivo all'ipotetico inizio di header
                //index_input = ProtocolVersionPosition;

            return res;
        }*/

        // capisce la dimensione del payload e verifica integrità dati sul payload
        //==========================================================================
        private boolean CheckPayload(int PacketType, int arg_char, int arg_int){
            //==========================================================================
            boolean isPayloadOk = false;
            short tmp_short=0;
            int Old_ReadIndex = index_input;
            //int Old_ReadIndex = CircularBuffer_ReadIndex;
            PayloadSize = 0;

            try{
                if (PacketType == MagicCommProtocol.CMD_MOTE_SIG)
                {

                    PayloadType = MagicCommProtocol.CMD_MOTE_SIG;
                    int SIGT = (arg_int & 0xff00)/256;   // primo byte dell'arg_int
                    Mote_FS = (byte)(arg_int & 0x0f);   // ultimo nibble dell'arg_int
                    switch (arg_int & 0x0f){    // frequenza di campionamento
                        case 0x03:
                            Mote_FS = 50;
                            Mote_TS = 20;
                            break;
                        case 0x05:
                            Mote_FS = 100;
                            Mote_TS = 10;
                            break;
                        case 0x07:
                            Mote_FS = 200;      // sampling frequency in hertz
                            Mote_TS = 5;        // sampling period in millisecondi
                            break;
                        case 0x09:
                            Mote_FS = 400;
                            Mote_TS = 2.5;
                            break;
                        case 0xb:
                            Mote_FS = 800;
                            Mote_TS = 1.25;
                            break;
                    }

                    if ( SIGT == MagicCommProtocol.ACC_V1){
                        PayloadSize = 77;
                    }else if (SIGT == MagicCommProtocol.PPG_V1) {
                        PayloadSize = 87;
                    }

                }
                else {
                    switch (PacketType) {
                        // PROTOCOLLO 3.0
                        case MagicCommProtocol.CMD_DATA0:   //0x00

                            //Signals[MagicCommProtocol.TEMP0].present = false;
                            PayloadSize = 37;// 3 * NUMACQSTEPS + 1;
                            PayloadType = MagicCommProtocol.CMD_DATA0;

                            break;

                        case MagicCommProtocol.CMD_DATA0T:  //0x01

                            //Signals[MagicCommProtocol.TEMP0].present = true;
                            PayloadSize = 37;// 3 * NUMACQSTEPS + 1;
                            PayloadType = MagicCommProtocol.CMD_DATA0T;
                            break;


                        case MagicCommProtocol.CMD_XPOD0:   //0x08

                            PayloadType = MagicCommProtocol.CMD_XPOD0;
                            PayloadSize = MagicCommProtocol.PAYLOAD_SIZE_XPOD0;

                            break;

                        case MagicCommProtocol.CMD_DATA1:   //0x02

                            //Signals[MagicCommProtocol.TEMP0].present = false;
                            PayloadSize = MagicCommProtocol.PAYLOAD_SIZE_DATA1;
                            PayloadType = MagicCommProtocol.CMD_DATA1;

                            break;

                        case MagicCommProtocol.CMD_XPOD1:

                            PayloadType = MagicCommProtocol.CMD_XPOD1;
                            PayloadSize = MagicCommProtocol.PAYLOAD_SIZE_XPOD1;//44;

                            break;

                        case MagicCommProtocol.CMD_STATUS0:

                            PayloadType = MagicCommProtocol.CMD_XPOD1;
                            PayloadSize = 0;

                            break;

                        case MagicCommProtocol.CMD_STATUS1:

                            PayloadType = MagicCommProtocol.CMD_STATUS1;
                            PayloadSize = MagicCommProtocol.PAYLOAD_SIZE_STATUS1;// 12;

                            break;

                        case MagicCommProtocol.CMD_SINGLE_SIG_UINT12:

                            PayloadType = MagicCommProtocol.CMD_SINGLE_SIG_UINT12;
                            PayloadSize = arg_int + arg_int / 2 + 12; //numero di campioni + 4 timing + 1 canale + 5 spare + chksum
                            break;

                        case MagicCommProtocol.CMD_SINGLE_SIG_UINT16:
                            PayloadType = MagicCommProtocol.CMD_SINGLE_SIG_UINT16;
                            PayloadSize = 2 * arg_int + 12; //2 * numero di campioni + 4 timing + 2 timer_counter + 1 canale + 3 spare + chksum
                            break;

                        case MagicCommProtocol.CMD_MULTI_SIG_GENERIC:

                            PayloadType = MagicCommProtocol.CMD_MULTI_SIG_GENERIC;
                            PayloadSize = arg_int;
                            break;

                        case MagicCommProtocol.CMD_1L_BUF_STATUS:
                            PayloadType = MagicCommProtocol.CMD_1L_BUF_STATUS;
                            PayloadSize = (4 * arg_char) + 15;

                            break;

                        case MagicCommProtocol.CMD_POWER_DATA:

                            PayloadType = MagicCommProtocol.CMD_POWER_DATA;
                            PayloadSize = MagicCommProtocol.PAYLOAD_SIZE_POWER_DATA;

                            break;

                        case MagicCommProtocol.CMD_POWER_PARAM:

                            PayloadType = MagicCommProtocol.CMD_POWER_PARAM;
                            PayloadSize = MagicCommProtocol.PAYLOAD_SIZE_POWER_PARAM;

                            break;

                        case MagicCommProtocol.CMD_ASYNC_DATA:

                            PayloadType = MagicCommProtocol.CMD_ASYNC_DATA;
                            PayloadSize = MagicCommProtocol.PAYLOAD_SIZE_ASYNC_DATA;

                            break;

                        case MagicCommProtocol.CMD_ACK:

                            PayloadType = MagicCommProtocol.CMD_ACK;
                            PayloadSize = 0;
                            if (arg_char == MagicCommProtocol.CMD_GET_RUNNING_STATUS)
                                // todo rimettere: RunningStatus = arg_int;
                                break;

                        case MagicCommProtocol.CMD_FILE_SLICE:

                            PayloadType = MagicCommProtocol.CMD_FILE_SLICE;
                            PayloadSize = arg_int;
                            int prova = arg_char;

                            break;

                        case MagicCommProtocol.CMD_ERROR:
                            PayloadType = MagicCommProtocol.CMD_ERROR;
                            PayloadSize = 0;
                            break;

                        case MagicCommProtocol.CMD_GET_RUNNING_STATUS:
                            PayloadType = MagicCommProtocol.CMD_GET_RUNNING_STATUS;
                            PayloadSize = 0;

                            break;

                        case MagicCommProtocol.CMD_SINGLE_SIG_GENERIC:
                            PayloadType = MagicCommProtocol.CMD_SINGLE_SIG_GENERIC;
                            PayloadSize = arg_int;
                            break;
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            if(PayloadSize>0)
                Payload = new byte[PayloadSize];

            if (Payload.length > 0) {
                // riempio il buffer con il payload della dimensione giusta
                int i;
                for (i = 0; i < Payload.length; i++) {
                    tmp_short = ReadNextByteFromBuffer();
                    if (tmp_short != NO_BYTE_AVAILABLE) {
                        Payload[i] = (byte) tmp_short;
                    }else{
                        break;
                    }
                }
                // controllo integrità dati payload
                if(i == Payload.length) {// ho riempito tutto l'array del payload
                    byte chk = CheckSum(Payload);
                    if (chk == Payload[Payload.length - 1]) {
                        isPayloadOk = true;
                    } else {
                        isPayloadOk = false;
                        Payload = null;
                        //CircularBuffer_ReadIndex = Old_ReadIndex++;//ripartirò da qui a cercare il prossimo header
                        //index_input = Old_ReadIndex;//ripartirò da qui a cercare il prossimo header
                        index_input -= (PayloadSize - 1);//ripartirò da qui a cercare il prossimo header
                    }
                }else{
                    isPayloadOk = true;// siamo alla fine del file e l'ultimo pacchetto è tronco
                }
            }
            return isPayloadOk;
            // todo controllo sul numero pacchetto
        /*if (PacketNumber > LastHeaderPackerNumber)
        {
            NumHeaderError += PacketNumber - LastHeaderPackerNumber - 1;
        }
        LastHeaderPackerNumber = PacketNumber;*/
        }


        // decodifica i dati nel payload
        //==========================================================================
        private void DecodePayload(int payload_type, int arg_char, int arg_int) {
            //==========================================================================
            short tempshort;
            int tempcounter;
            int tempvalue;
            double dval;
            int pos;// ;//As Integer
            int i, k;
            long temp_timestamp = 0;//int temp_timestamp = 0;//UInt32 temp_timestamp = 0;
            byte PacketCounter;

            try {
                switch (payload_type) {

                    case MagicCommProtocol.CMD_MOTE_SIG:

                        int MoteNumber = arg_char - 2;  // riporto il numero dei mote a un numero utilizzabile nella struttura magic
                    /*
                    * MOTE 1 = 0x02
                    * MOTE 2 = 0x03
                    * MOTE 3 = 0x04
                    * MOTE 4 = 0x05
                    * MOTE 5 = 0x06*/
                        int AccNum = (Payload[1] & 0xf0)/16;    // 1 2 3 4 5
                        AccNum -= 1;        // riporto numero a 0-n per usarlo come indice nella struttura magic
                        int AxesNum = Payload[1] & 0x0f;    // 1 2 3
                        AxesNum -= 1;       // riporto numero a 0-n per usarlo come indice nella struttura magic
                        byte SigType = (byte)((arg_int & 0xff00) / 256); // primo byte del arg int:acc o ppg

                        temp_timestamp =    ((long)ConvertByteToUnsignedInt(Payload[2])) << 24;
                        temp_timestamp +=   ((long)ConvertByteToUnsignedInt(Payload[3])) << 16;
                        temp_timestamp +=   ((long)ConvertByteToUnsignedInt(Payload[4])) << 8;
                        temp_timestamp +=    (long)ConvertByteToUnsignedInt(Payload[5]);
                        //correggo il timestamp per trasformarlo in ms
                        temp_timestamp = temp_timestamp * 5;

                        k = 6;
                        if (SigType == MagicCommProtocol.ACC_V1) {   // accelerometro/i
                            for (i = 0; i < 70; i += 7) {

                                //sample 1
                                tempvalue = (Convert2BytesToSignedInt(Payload[k + i], (byte) (Payload[k + i + 1] & 0b11111100)) ) / 4;
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[0].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[1].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[2].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                temp_timestamp += Mote_TS;

                                //sample 2
                                tempshort = (short) (((short)(Payload[k + i + 1] & 0b00000011 ) * 16384) +  (short)((ConvertByteToUnsignedInt(Payload[k + i + 2])) * 64) + (short)((ConvertByteToUnsignedInt((byte)(Payload[k + i + 3] & 0xf0))) / 4));  //sample 2
                                tempshort /= 4;
                                tempvalue = (int) tempshort;
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[0].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[1].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[2].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                temp_timestamp += Mote_TS;

                                //sample 3
                                tempshort = (short) (((short)((Payload[k + i + 3] & 0x0f) * 4096)) +  (short)(ConvertByteToUnsignedInt(Payload[k + i + 4]) * 16) + (short)(ConvertByteToUnsignedInt((byte)(Payload[k + i + 5] & 0b11000000)))/64);  //sample 3
                                tempshort /= 4;
                                tempvalue = (int) tempshort;
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[0].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[1].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[2].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                temp_timestamp += Mote_TS;

                                //sample 4
                                tempshort = (short) ((short)((Payload[k + i + 5] & 0b00111111 ) * 1024) + (short)(ConvertByteToUnsignedInt(Payload[k + i + 6])*4));  //sample 4
                                tempshort /= 4;
                                tempvalue = (int) tempshort;
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[0].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[1].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[2].Motes[MoteNumber].Acc[AccNum].axes[AxesNum]);
                                temp_timestamp += Mote_TS;

                            }




                        } else if(SigType == MagicCommProtocol.PPG_V1){ // pletismografo
                            byte source = (byte)(Payload[1] & 0x0f); // 1=RED   2=IR    3=MEAN

                            for(i = 0; i<80; i+=2){  // 40 campioni da 2 byte( 1 campione = 2 byte)
                                tempvalue = ( ConvertByteToUnsignedInt(Payload[k + i]) ) * 256 +  ConvertByteToUnsignedInt(Payload[k + i + 1]) ;
                                tempvalue = 65535 - tempvalue;  //inverto il segnale

                                if(AxesNum<2) {
                                    PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[0].Motes[MoteNumber].PPG.source[AxesNum]);
                                    PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[1].Motes[MoteNumber].PPG.source[AxesNum]);
                                    PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[2].Motes[MoteNumber].PPG.source[AxesNum]);
                                }
                                temp_timestamp += Mote_TS;
                            }
                        }


                        break;

                    case MagicCommProtocol.CMD_ASYNC_DATA:

                        temp_timestamp =    ((long)ConvertByteToUnsignedInt(Payload[0])) << 24;
                        temp_timestamp +=   ((long)ConvertByteToUnsignedInt(Payload[1])) << 16;
                        temp_timestamp +=   ((long)ConvertByteToUnsignedInt(Payload[2])) << 8;
                        temp_timestamp +=    (long)ConvertByteToUnsignedInt(Payload[3]);
                        temp_timestamp = temp_timestamp * 5;

                        switch(arg_char){
                            case MagicCommProtocol.ASYNC_SIG_EVENTMARKER0:
                                // todo ASYNC_SIG_EVENTMARKER0
                                // arg int contiene l'info relativa al numero dell'evento rispetto all'acq corrente
                                // temp_timestamp contiene info relativa al tempo dell'evento corrente
                                MagicEvents[evnt_nmb++] = temp_timestamp;
                                break;
                            case MagicCommProtocol.ASYNC_SIG_SINCHRO:
                                break;
                            case MagicCommProtocol.ASYNC_SIG_REF_IMPED:
                                break;
                        }



                        break;

                    case MagicCommProtocol.CMD_DATA0:
                    case MagicCommProtocol.CMD_DATA0T:
    /*
                        pos = 0;

                        //'Ciclo sui segnali analogici
                        for (i = 1; i <= NUMACQSTEPS; i++)
                        {//  i = 1 To MAXNUMACQSTEPS
                            //'ciclo su tutti i segnali analogici (1..8)
                            for (k = 1; k <= MAXNUMANALOGSIGNALS; k++)
                            {// For k = 1 To MAXNUMANALOGSIGNALS
                                //DataArrayCounter[k] = DataArrayCounter[k] + 1;// 'incremento il contatore del segnale
                                if (++DataArrayCounter[k] > MAXNUMSAMPLES)
                                    DataArrayCounter[k] = 0;

                                //'se è il turno del particolare segnale
                                if (AcqPattern[k, i])// && Signal[k].present)
                                {
                                    //'prendo il valore del segnale dal payload
                                    pos = pos + 1;

                                    tempvalue = (int)GetIntByteValue(pos);

                                    Signal[k].AddValue(tempvalue);

                                    //Se è 1'accelerometro sull'asse Z, aggiorno l'accelerometro
                                    //e il dataarray corrispondente

                                    //20080513 - modificato per potere salvare i dati dell'asse z dell'accelerometro in maniera corretta

                                    DataArray[k, DataArrayCounter[k]] = tempvalue;

                                    //aggiorno il dato relativo al calcolo dell'HR
                                    if (k == ECG0)
                                    {
                                        myHRProcessor.AddData(tempvalue);

                                        //se sto preparando il file XML, inserisco i dati anche nel
                                        //XML_Processor
                                        //if (XMLStripFlag)
                                        //{
                                        //    //il segnale va da 0 a 4095 - cioè da 0 a 3,3V
                                        //    //0.0008056640625 = 1,65 / 2048
                                        //    // Nota del 20080129 - abbiamo scoperto che il segnale anzichè 1 mV
                                        //    // ne segna circa 140. pertanto abbiamo inserito un /140 e un * 1000
                                        //    // in modo da visualizzare in mV un segnale che sia di ampiezza plausibile
                                        //    myXML_Processor.AddECGValue((double)(tempvalue - 2048) * 0.00575474330357);

                                        //    //vecchia procedura
                                        //    //myXML_Processor.AddECGValue((double)(tempvalue - 2048) * 0.0008056640625);

                                        //}
                                    }
                                    else if (k == RESP0)
                                    {
                                        //verificare il trucchettino seguente ed eliminare il tutto

                                        dval = (double)(tempvalue - 2048) * 0.0008056640625;
                                        //il segnale va da 0 a 4095 - cioè da 0 a 3,3V
                                        //0.0008056640625 = 1,65 / 2048

                                        //questo è un trucchettino per piallare gli spike sul respiro che si vedono ogni tanto...

                                        myRespQueMean -= myRespQue.Dequeue() * 0.25;
                                        myRespQue.Enqueue(dval);
                                        myRespQueMean += dval * 0.25;

                                        //if (XMLStripFlag)
                                        //{
                                        //    myXML_Processor.AddRespValue(myRespQueMean);
                                        //}
                                    }

                                    //lastValue[k] = DataArray[k, DataArrayCounter[k]];
                                    //Signal[k].oldest_val = DataArray[k, DataArrayCounter[k]];
                                }
                                else
                                {
                                    //' il segnale non è presente a questo passo di campionamento
                                    //' e quindi lo metto uguale al valore al passo precedente.
                                    tempcounter = DataArrayCounter[k] - 1;
                                    if (tempcounter < 0)
                                        tempcounter = MAXNUMSAMPLES;
                                    DataArray[k, DataArrayCounter[k]] = DataArray[k, tempcounter];

                                }

                            } //next k

                        }//     Next i

                        if (ViewStatus == VIEWSTATUS_GRAPH || ViewStatus == VIEWSTATUS_HRV_GRAPH)
                        {
                            for (i = 1; i <= MAXNUMANALOGSIGNALS; i++)
                                if (Signal[i].enabled)
                                    StepsToPlotCounter[i] += NUMACQSTEPS;
                        }
                        else
                        {
                            //ciclo su tutti i dati
                            for (i = 1; i <= MAXNUMANALOGSIGNALS; i++)
                                if (Signal[i].enabled)
                                    PlotArrayCounter[i] = DataArrayCounter[i];
                        }

                        //'registro i dati rilevati negli ultimi MAXNUMACQSTEPS
                        //'se:
                        //' a) siamo in registrazione ed
                        //' b) file di scrittura è aperto
                        //' c) il segnale è abilitato,
                        //'allora salvo i dati
                        if (configuration.SaveData)
                        {
                            if (Status == MagicCommProtocol.RECORDING)
                            {
                                //inserisco un try/catch per verificare che non ci siano problemi
                                //in fase di scrittura

                                try
                                {
                                    for (i = 1; i <= NUMACQSTEPS; i++)
                                    {
                                        for (k = 1; k <= MAXNUMANALOGSIGNALS; k++)
                                        {
                                            if (Signal[k].enabled)
                                            {
                                                tempcounter = DataArrayCounter[k] - NUMACQSTEPS + i;
                                                if (tempcounter < 0)
                                                    tempcounter += MAXNUMSAMPLES;

                                                tempshort = (short)DataArray[k, tempcounter];
                                                //hi_b = (byte) (tempint / 256);
                                                //lo_b =(byte) (tempint % 256);
                                                //bw_acq.Write(hi_b);
                                                //bw_acq.Write(lo_b);

                                                bw_acq.Write(tempshort);
                                                if (++mainWriteCounter > MAX_WRITE_COUNTER)
                                                {
                                                    mainWriteCounter = 0;
                                                    //verificare se siano necessari entrambi
                                                    bw_acq.Flush();
                                                    //fs_acq.Flush();
                                                }
                                            }
                                            //};//Next i
                                        }
                                        //da inserire la scrittura del segnlae relativo alla temperatura
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
*/                        break;


                    case MagicCommProtocol.CMD_DATA1:
                        int a_x, a_y, a_z;
                        int A_X, A_Y, A_Z;
                        //decodifico l'ID di sequenza
                        PacketCounter = Payload[0];

                        //************************************************************
                        //1) decodifico il timestamp
                        temp_timestamp =    ((long)ConvertByteToUnsignedInt(Payload[1])) << 24;
                        temp_timestamp +=   ((long)ConvertByteToUnsignedInt(Payload[2])) << 16;
                        temp_timestamp +=   ((long)ConvertByteToUnsignedInt(Payload[3])) << 8;
                        temp_timestamp +=    (long)ConvertByteToUnsignedInt(Payload[4]);

                        //correggo il timestamp per trasformarlo in ms
                        temp_timestamp = temp_timestamp * 5;

                        //************************************************************
                        //2) DECODIFICA ECG (24 campioni da 1,5 byte = 36 byte)
                        k = 5;
                        for (i = 0; i < 36; i += 3)
                        {
                            tempvalue = ( ConvertByteToUnsignedInt(Payload[k + i]) ) * 16 + ( ConvertByteToUnsignedInt((byte)(Payload[k + i + 1] & 0xf0)) ) / 16;

                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[0].MiniMagic.Ecg0);
                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[1].MiniMagic.Ecg0);
                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[2].MiniMagic.Ecg0);

                            temp_timestamp += 5;
                            //----------------------------------------------------------
                            //secondo campione
                            tempvalue = ( ConvertByteToUnsignedInt((byte)(Payload[k + i + 1] & 0x0f)) ) * 256 + ConvertByteToUnsignedInt(Payload[k + i + 2]);

                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[0].MiniMagic.Ecg0);
                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[1].MiniMagic.Ecg0);
                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[2].MiniMagic.Ecg0);

                            temp_timestamp += 5;
                        }
                        //************************************************************
                        //3) DECODIFICA RESPIRO (6 campioni da 1,5 byte = 9 byte)
                        k = 41;
                        for (i = 0; i < 9; i += 3)
                        {
                            tempvalue = ( ConvertByteToUnsignedInt(Payload[k + i]) ) * 16 + ( ConvertByteToUnsignedInt((byte)(Payload[k + i + 1] & 0xf0)) ) / 16;

                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[0].MiniMagic.Resp0);
                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[1].MiniMagic.Resp0);
                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[2].MiniMagic.Resp0);

                            temp_timestamp += 5;

                            tempvalue = ( ConvertByteToUnsignedInt((byte)(Payload[k + i + 1] & 0x0f)) ) * 256 + ConvertByteToUnsignedInt(Payload[k + i + 2]);

                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[0].MiniMagic.Resp0);
                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[1].MiniMagic.Resp0);
                            PutNewSampleToOutput(tempvalue, temp_timestamp, UserSettings.MyMagic[2].MiniMagic.Resp0);

                            temp_timestamp += 5;
                        }

                        //************************************************************
                        //4) decodifico il segnale dell'accelerometro (6 campioni da 1,5 byte = 9 byte x 3 segnali = 27 byte)
                        k = 50;
                        for (i = 0; i < 27; i += 9)
                        {

                        /*a_x = ((int)Payload[k + i]) * 16 + ((int)Payload[k + i + 1]) / 16;
                        a_y = ((int)Payload[k + i + 3]) * 16 + ((int)Payload[k + i + 4]) / 16;
                        a_z = ((int)Payload[k + i + 6]) * 16 + ((int)Payload[k + i + 7]) / 16;

                        //verificare la mappatura delle accelerazioni

                        A_X = 4096 - a_y;
                        A_Y = 4096 - a_x;
                        A_Z = 4096 - a_z;

                        if (i == 0)
                        {
                            Signals[ACCX0].AddValue(temp_timestamp, A_X);
                            Signals[ACCY0].AddValue(temp_timestamp, A_Y);
                            Signals[ACCZ0].AddValue(temp_timestamp, A_Z);

                            //aggiorno l'oggetto accelerometro
                            //myAccelerometer.UpdateRealValues();

                            //aggiorno il segnale
                            // Signals[MOD_ACC].AddValue(temp_timestamp, (int)(1000.0 * Math.Round(myAccelerometer.rho, 4)));
                        }
                        else
                        {
                            Signals[ACCX0].AddValue( A_X);
                            Signals[ACCY0].AddValue( A_Y);
                            Signals[ACCZ0].AddValue( A_Z);

                            //aggiorno l'oggetto accelerometro
                            //myAccelerometer.UpdateRealValues();

                            //aggiorno il segnale
                            //Signal[MOD_ACC].AddValue((int)(1000.0 * Math.Round(myAccelerometer.rho, 4)));
                        }

                        a_x = (int)(Payload[k + i + 1] & 0x0f) * 256 + (int)Payload[k + i + 2];
                        a_y = (int)(Payload[k + i + 4] & 0x0f) * 256 + (int)Payload[k + i + 5];
                        a_z = (int)(Payload[k + i + 7] & 0x0f) * 256 + (int)Payload[k + i + 8];

                        A_X = 4096 - a_y;
                        A_Y = 4096 - a_x;
                        A_Z = 4096 - a_z;

                        Signals[ACCX0].AddValue(A_X);
                        Signals[ACCY0].AddValue(A_Y);
                        Signals[ACCZ0].AddValue(A_Z);

                        //aggiorno l'oggetto accelerometro
                        //myAccelerometer.UpdateRealValues();

                        //aggiorno il segnale
                        // Signal[MOD_ACC].AddValue((int)(1000.0 * Math.Round(myAccelerometer.rho, 4)));
*/
                        }

                        //devo aggiornare il file dei dati, nel caso stia registrando...

                        break;

                    case MagicCommProtocol.CMD_SINGLE_SIG_UINT16:

                        //decodifico l'ID di sequenza

                        temp_timestamp =  (long)ConvertByteToUnsignedInt(Payload[1]) << 24;
                        temp_timestamp += (long)ConvertByteToUnsignedInt(Payload[2]) << 16;
                        temp_timestamp += (long)ConvertByteToUnsignedInt(Payload[3]) << 8;
                        temp_timestamp += (long)ConvertByteToUnsignedInt(Payload[4]);

                        //correggo il timestamp per trasformarlo in ms
                        temp_timestamp = temp_timestamp * 5;

                        int max_count = arg_int * 2;
                        int axes_num = (int)Payload[7];  // NUMERO ASSE 0 = X; 1 = Y; 2 = Z
                        k = 11; //posizione del primo byte contenente dati

                        int value = 0;

                        switch (arg_char) {

                            case MagicCommProtocol.SIGNAL_TYPE_ACCEL:
                                //Ciclo su tutti i dati del pacchetto, a due a due
                                for (i = 0; i < max_count; i += 2) {

                                    if (bClip16bitTo12bit) {//qui non ci entra mai
                                        // il valore è da rappresentare in 12 bit
                                        value =  Convert2BytesToSignedInt(Payload[k + i], Payload[k + i + 1]);
                                        value /= 16;        //>>4 ma mantiene i segno
                                        //value += 2048;      //offset x visualizzazione= 2^nbit/2
                                    } else {
                                        // qui invece il valore è a 14 bit
                                        value =  Convert2BytesToSignedInt(Payload[k + i], Payload[k + i + 1]);
                                        value /= 4;//shift di 2 bit mantenendo il segno
                                        //value += 8192;          //offset x visualizzazione= 2^nbit/2
                                    }

                                    if(UserSettings.MyMagic[0].MiniMagic.Acc.axes[axes_num] == 1) {
                                       // DecodePlot_SharedMem1.put(temp_timestamp, value);   //manda su grafico 1
                                        OutputTime1[index1] = temp_timestamp;
                                        OutputSignal1[index1++] = value;
                                    }
                                    if(UserSettings.MyMagic[1].MiniMagic.Acc.axes[axes_num] == 2) {
                                        //DecodePlot_SharedMem2.put(temp_timestamp, value);   //manda su grafico 2
                                        OutputTime2[index2] = temp_timestamp;
                                        OutputSignal2[index2++] = value;
                                    }
                                    if(UserSettings.MyMagic[2].MiniMagic.Acc.axes[axes_num] == 3) {
                                        //DecodePlot_SharedMem3.put(temp_timestamp, value);   //manda su grafico 1
                                        OutputTime3[index3] = temp_timestamp;
                                        OutputSignal3[index3++] = value;
                                    }

                                    temp_timestamp += 5;    // 5 ms =1/fsample con fsample = 200 Hz

                                }
                                break;
                            case MagicCommProtocol.SIGNAL_TYPE_ANGVEL:

                            /*for (i = 0; i < max_count; i += 2)
                            {
                                int value = 0;
                                k = 12;

                                if (bClip16bitTo12bit)
                                {

                                    //RINO
                                    byte[] B = new byte[2];
                                    B[0] = Payload[k + i + 1];
                                    B[1] = Payload[k + i];
                                    Int16 numero = BitConverter.ToInt16(B, 0);
                                    value = (int)numero / 16;
                                    value += 2048;


                                }
                                //value = 4096 - (int)(((Int16)PayloadByteSeq[k + i] * 256 + (Int16)(PayloadByteSeq[k + i + 1])) >> 4);
                                else
                                {

                                    //RINO
                                    byte[] B = new byte[2];
                                    B[0] = Payload[k + i + 1];
                                    B[1] = Payload[k + i];
                                    Int16 numero = BitConverter.ToInt16(B, 0);
                                    value = numero + 32768;

                                }

                                if (sig_num <= 2)
                                {
                                    if (i == 0)
                                        Signal[GYRO_INT16_FS_X + sig_num].AddValue(temp_timestamp, value);
                                    else
                                        Signal[GYRO_INT16_FS_X + sig_num].AddValue(value);

                                }
                                else
                                {
                                    //MessageBox.Show("gyro digital con  signum = " + sig_num);
                                }

                            }*/

                                break;
                        }
                        //dovremo tradurre il segnale, ma per il momento non faccio nulla

                        break;

                    case MagicCommProtocol.CMD_XPOD0:
                    /*//Per il momento effettuo soltanto l'asssegnamento delle grandezze e non registro i dati

                    myXPod.status = PayloadByteSeq[1];

                    for (k = 2; k < 27; k++)
                    {
                        myXPod.pleth.AddValue((int)PayloadByteSeq[k]);
                    }

                    //bisognerebbe mettere un controllino sull'effettiva bontà del
                    //segnale dal PulsoX
                    myXPod.SpO2.AddValue((int)PayloadByteSeq[27]);

                    myXPod.SpO2_D = (int)PayloadByteSeq[28];
                    myXPod.SpO2_Fast = (int)PayloadByteSeq[29];
                    myXPod.SpO2_BB = (int)PayloadByteSeq[30];

                    myXPod.E_SpO2 = (int)PayloadByteSeq[31];
                    myXPod.E_SpO2_D = (int)PayloadByteSeq[32];

                    myXPod.HR.AddValue((int)PayloadByteSeq[33] + ((int)(PayloadByteSeq[37] & 0xC0) * 2));

                    myXPod.E_HR = (int)PayloadByteSeq[34] + ((int)(PayloadByteSeq[37] & 0x30) * 8);
                    myXPod.HR_D = (int)PayloadByteSeq[35] + ((int)(PayloadByteSeq[37] & 0x0C) * 32);

                    myXPod.E_HR_D = (int)PayloadByteSeq[36] + ((int)(PayloadByteSeq[37] & 0x03) * 128);
*/
                        break;

                    case MagicCommProtocol.CMD_XPOD1:
                        //Per il momento effettuo soltanto l'asssegnamento delle grandezze e non registro i dati

                    /*temp_timestamp = ((UInt32)PayloadByteSeq[2]) << 24;
                    temp_timestamp += ((UInt32)PayloadByteSeq[3]) << 16;
                    temp_timestamp += ((UInt32)PayloadByteSeq[4]) << 8;
                    temp_timestamp += (UInt32)PayloadByteSeq[5];

                    //myXPod.last_TAR_timestamp = myXPod.act_TAR_timestamp;

                    //correggo il timestamp per trasformarlo in ms
                    temp_timestamp = temp_timestamp * 1000 / (UInt32)configuration.SampleRate;

                    //aggiungo
                    temp_timestamp += (UInt32)PayloadByteSeq[6] * 10 / (UInt32)configuration.SampleRate;

                    myXPod.status = PayloadByteSeq[7];

                    for (k = 8; k < 33; k++)
                    {
                        myXPod.pleth.AddValue((int)PayloadByteSeq[k]);
                    }

                    //bisognerebbe mettere un controllino sull'effettiva bontà del
                    //segnale dal PulsoX
                    myXPod.SpO2.AddValue((int)PayloadByteSeq[33]);

                    myXPod.SpO2_D = (int)PayloadByteSeq[34];
                    myXPod.SpO2_Fast = (int)PayloadByteSeq[35];
                    myXPod.SpO2_BB = (int)PayloadByteSeq[36];

                    myXPod.E_SpO2 = (int)PayloadByteSeq[37];
                    myXPod.E_SpO2_D = (int)PayloadByteSeq[38];

                    myXPod.HR.AddValue((int)PayloadByteSeq[39] + ((int)(PayloadByteSeq[43] & 0xC0) * 2));

                    myXPod.E_HR = (int)PayloadByteSeq[40] + ((int)(PayloadByteSeq[43] & 0x30) * 8);
                    myXPod.HR_D = (int)PayloadByteSeq[41] + ((int)(PayloadByteSeq[43] & 0x0C) * 32);

                    myXPod.E_HR_D = (int)PayloadByteSeq[42] + ((int)(PayloadByteSeq[43] & 0x03) * 128);
*/
                        break;

                    case MagicCommProtocol.CMD_SINGLE_SIG_UINT12:

                    /*//decodifico l'ID di sequenza
                    temp_timestamp = ((UInt32)PayloadByteSeq[1]) << 24;
                    temp_timestamp += ((UInt32)PayloadByteSeq[2]) << 16;
                    temp_timestamp += ((UInt32)PayloadByteSeq[3]) << 8;
                    temp_timestamp += (UInt32)PayloadByteSeq[4];

                    //correggo il timestamp per trasformarlo in ms
                    temp_timestamp = temp_timestamp * 5;

                    //aggiungo la correzione (il valore è compreso tra 0 e 100 e rappresenta da 0 a 5 ms)
                    //temp_timestamp += ((UInt32)PayloadByteSeq[5] / 20);
                    int max_count = arg_int + arg_int / 2;

                    int sig_num = (int)PayloadByteSeq[7];

                    if (arg_char == MagicCommProtocol.SIGNAL_TYPE_ACCEL)
                    {
                        //Ciclo su tutti i dati del pacchetto, a due a due

                        for (i = 0; i < max_count; i += 3)
                        {
                            int iA0, iA1;
                            k = 12;

                            iA0 = 4096 - ((UInt16)((int)PayloadByteSeq[k + i] * 16 + (int)(PayloadByteSeq[k + i + 1] & 0xf0) / 16));
                            iA1 = 4096 - ((UInt16)((int)(PayloadByteSeq[k + i + 1] & 0x0f) * 256 + (int)PayloadByteSeq[k + i + 2]));

                            //iA0 = MedFilt3[sig_num].Update(iA0);
                            //iA1 = MedFilt3[sig_num].Update(iA1);

                            if (i == 0)
                                Signal[ACC_FS_X0 + sig_num].AddValue(temp_timestamp, iA0);
                            else
                                Signal[ACC_FS_X0 + sig_num].AddValue(iA0);

                            Signal[ACC_FS_X0 + sig_num].AddValue(iA1);


                            //if (sig_num == 3)
                            //    Console.WriteLine(" ");
                            //if (iA0 == 0 && iA1 == 0)
                            //{
                            //    Console.WriteLine("time =" + temp_timestamp.ToString());
                            //}

                            //int d = iA1 - iA0;
                            //if (d == 0)
                            //{
                            //    Console.WriteLine("ecco");
                            //}
                            //seleziono il segnale

                        }

                    }
                    // dovremo tradurre il segnale, ma per il momento non faccio nulla
*/
                        break;

                    case MagicCommProtocol.CMD_MULTI_SIG_GENERIC:
                    /*int num_bit = 0;
                    int fs = 0;
                    int num_sig = 0;
                    int num_of_samples = 0;

                    //decodifico la freq. campionamento
                    fs = (((int)PayloadByteSeq[2]) * 256) +(int)PayloadByteSeq[3];

                    num_bit = (int)PayloadByteSeq[4];
                    num_sig = (int)PayloadByteSeq[5];
                    num_of_samples = (int)PayloadByteSeq[11];

                    //decodifico il timestamp
                    temp_timestamp = ((UInt32)PayloadByteSeq[6]) << 24;
                    temp_timestamp += ((UInt32)PayloadByteSeq[7]) << 16;
                    temp_timestamp += ((UInt32)PayloadByteSeq[8]) << 8;
                    temp_timestamp += (UInt32)PayloadByteSeq[9];

                    if (arg_char == MagicCommProtocol.SIGNAL_TYPE_SLOW_IMPEDANCE)
                    {
                        k = 17;

                        for (int l = 0; l < num_sig; l++)
                            for (int m = 0; m < num_of_samples; m += 2)
                            {
                                int iA0, iA1;
                                int k1;
                                k1 = k + l * num_of_samples + m;

                                iA0 = ((UInt16)((int)PayloadByteSeq[k1] * 16 + (int)(PayloadByteSeq[k1 + 1] & 0xf0) / 16));
                                iA1 = ((UInt16)((int)(PayloadByteSeq[k1 + 1] & 0x0f) * 256 + (int)PayloadByteSeq[k1 + 2]));

                                if (m == 0)
                                    Signal[SLOW_IMP_0+ l].AddValue(temp_timestamp, iA0);
                                else
                                    Signal[SLOW_IMP_0 + l].AddValue(iA0);

                                Signal[SLOW_IMP_0 + l].AddValue(iA1);
                            }
                    }
                    break;

                case MagicCommProtocol.CMD_STATUS1:


                    DeviceID = (int)PayloadByteSeq[1] * 256 + (int)PayloadByteSeq[2];
                    DeviceFwVer[0] = (int)((PayloadByteSeq[3] & 0xf0) >> 4);
                    DeviceFwVer[1] = (int)(PayloadByteSeq[3] & 0x0f);
                    DeviceFwVer[2] = (int)PayloadByteSeq[4];
                    DeviceV = (int)PayloadByteSeq[11] * 256 + (int)PayloadByteSeq[12];
*/
                        break;



                    default:
                        break;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

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
        private int Convert2BytesToSignedInt(byte MSB, byte LSB){
            //==========================================================================
            // converto lsb a unsigned
            int res = ((int)MSB)*256 + ConvertByteToUnsignedInt(LSB);
            return res;
            // ritorna un int a 4byte con segno
        }

        // aggiunge i dati provenienti dal bt al buffer circolare
        //==========================================================================
        private boolean PutNewSampleToOutput(int NewData, long NewTime, int dest){
            //==========================================================================
            // dest è il numero del buffer di output
            switch(dest){
                case 1:
                    if (index1 < OutputSignal1.length-1) {
                        OutputSignal1[index1] = NewData;
                        OutputTime1[index1++] = NewTime;
                    }else{// todo CircularBuffer pieno: cosa fare???
                        return false;
                    }
                    break;
                case 2:
                    if (index2 < OutputSignal2.length-1) {
                        OutputSignal2[index2] = NewData;
                        OutputTime2[index2++] = NewTime;
                    }else{// todo CircularBuffer pieno: cosa fare???
                        return false;
                    }
                    break;
                case 3:
                    if (index3 < OutputSignal3.length-1) {
                        OutputSignal3[index3] = NewData;
                        OutputTime3[index3++] = NewTime;
                    }else{// todo CircularBuffer pieno: cosa fare???
                        return false;
                    }
                    break;
                default:
                    break;
            }
            return true; // tuttp ok
        }


        //==========================================================================
        private short ReadNextByteFromBuffer(){
            //==========================================================================
            short res = NO_BYTE_AVAILABLE;

            if(index_input < Input.length) {
                res = (byte) Input[index_input++];
            }

            return res;
        }

        //==========================================================================
        private byte CheckSum(byte[] checkThis) {
            //==========================================================================
            byte res = (byte)0xFF;
            for(int i=0; i<(checkThis.length-1); i++) {   // il-1: l'ultimo byte è il check sum della magic!!!!
                res ^= checkThis[i];
            }
            return res;
        }

    // ritornano gli indici per capire il riempimento dei buffers
    public int get_Signal1_Size(){return index1;}
    public int get_Signal2_Size(){return index2;}
    public int get_Signal3_Size(){return index3;}
    public int get_input_index(){return index_input;}

}
