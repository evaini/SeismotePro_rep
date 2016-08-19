package diana.com.seismotepro;

/**
 * Created by Diana Scurati on 18/11/2015.
 */
public class MagicCommProtocol {

    static final byte SIZE_PACKET_HEADER = 8; // number of bytes
    static final byte PROTOCOL_VERSION = (byte)0x30; // first byte of the packet header


    //==========================================================================
    //==========================================================================
    //==========================================================================
    //'constanti che identificano i segnali--lato visualizzatore
    //==========================================================================
    //==========================================================================
    //==========================================================================
    static String SignalList[] = { "ECG", "Resp", "Acc X", "Acc Y", "Acc Z",
                                    "Mote 1: Acc X", "Mote 1: Acc Y", "Mote 1: Acc Z",
                                    "Mote 2: Acc X"};


    public static final int ECG0  = 1;
    public static final int RESP0 = 2;
    public static final int MAGIC_DIG_ACC_X = 3;
    public static final int MAGIC_DIG_ACC_Y = 4;
    public static final int MAGIC_DIG_ACC_Z = 5;










    // MOTE 1
    public static final int MOTE1_ACC1_X = 40;
    public static final int MOTE1_ACC1_Y = 41;
    public static final int MOTE1_ACC1_Z = 42;
    public static final int MOTE1_ACC2_X = 43;
    public static final int MOTE1_ACC2_Y = 44;
    public static final int MOTE1_ACC2_Z = 45;
    public static final int MOTE1_ACC3_X = 46;
    public static final int MOTE1_ACC3_Y = 47;
    public static final int MOTE1_ACC3_Z = 48;
    public static final int MOTE1_ACC4_X = 49;
    public static final int MOTE1_ACC4_Y = 50;
    public static final int MOTE1_ACC4_Z = 51;
    public static final int MOTE1_ACC_MEAN_X = 52;
    public static final int MOTE1_ACC_MEAN_Y = 53;
    public static final int MOTE1_ACC_MEAN_Z = 54;
    public static final int MOTE1_PLETHY_RED = 55;
    public static final int MOTE1_PLETHY_INFRARED = 56;
    //MOTE2
    public static final int MOTE2_ACC1_X = 50;
    public static final int MOTE2_ACC1_Y = 51;
    public static final int MOTE2_ACC1_Z = 52;
    public static final int MOTE2_ACC2_X = 53;
    public static final int MOTE2_ACC2_Y = 54;
    public static final int MOTE2_ACC2_Z = 55;
    public static final int MOTE2_ACC3_X = 56;
    public static final int MOTE2_ACC3_Y = 57;
    public static final int MOTE2_ACC3_Z = 58;
    public static final int MOTE2_ACC4_X = 59;
    public static final int MOTE2_ACC4_Y = 60;
    public static final int MOTE2_ACC4_Z = 61;
    public static final int MOTE2_ACC_MEAN_X = 62;
    public static final int MOTE2_ACC_MEAN_Y = 63;
    public static final int MOTE2_ACC_MEAN_Z = 64;
    public static final int MOTE2_PLETHY_RED = 65;
    public static final int MOTE2_PLETHY_INFRARED = 66;
    //MOTE3
    public static final int MOTE3_ACC1_X = 70;
    public static final int MOTE3_ACC1_Y = 71;
    public static final int MOTE3_ACC1_Z = 72;
    public static final int MOTE3_ACC2_X = 73;
    public static final int MOTE3_ACC2_Y = 74;
    public static final int MOTE3_ACC2_Z = 75;
    public static final int MOTE3_ACC3_X = 76;
    public static final int MOTE3_ACC3_Y = 77;
    public static final int MOTE3_ACC3_Z = 78;
    public static final int MOTE3_ACC4_X = 79;
    public static final int MOTE3_ACC4_Y = 80;
    public static final int MOTE3_ACC4_Z = 81;
    public static final int MOTE3_ACC_MEAN_X = 82;
    public static final int MOTE3_ACC_MEAN_Y = 83;
    public static final int MOTE3_ACC_MEAN_Z = 84;
    public static final int MOTE3_PLETHY_RED = 85;
    public static final int MOTE3_PLETHY_INFRARED = 86;
    //MOTE4
    public static final int MOTE4_ACC1_X = 90;
    public static final int MOTE4_ACC1_Y = 91;
    public static final int MOTE4_ACC1_Z = 92;
    public static final int MOTE4_ACC2_X = 93;
    public static final int MOTE4_ACC2_Y = 94;
    public static final int MOTE4_ACC2_Z = 95;
    public static final int MOTE4_ACC3_X = 96;
    public static final int MOTE4_ACC3_Y = 97;
    public static final int MOTE4_ACC3_Z = 98;
    public static final int MOTE4_ACC4_X = 49;
    public static final int MOTE4_ACC4_Y = 100;
    public static final int MOTE4_ACC4_Z = 101;
    public static final int MOTE4_ACC_MEAN_X = 102;
    public static final int MOTE4_ACC_MEAN_Y = 103;
    public static final int MOTE4_ACC_MEAN_Z = 104;
    public static final int MOTE4_PLETHY_RED = 105;
    public static final int MOTE4_PLETHY_INFRARED = 106;
    //MOTE5
    public static final int MOTE5_ACC1_X = 110;
    public static final int MOTE5_ACC1_Y = 111;
    public static final int MOTE5_ACC1_Z = 112;
    public static final int MOTE5_ACC2_X = 113;
    public static final int MOTE5_ACC2_Y = 114;
    public static final int MOTE5_ACC2_Z = 115;
    public static final int MOTE5_ACC3_X = 116;
    public static final int MOTE5_ACC3_Y = 117;
    public static final int MOTE5_ACC3_Z = 118;
    public static final int MOTE5_ACC4_X = 119;
    public static final int MOTE5_ACC4_Y = 120;
    public static final int MOTE5_ACC4_Z = 121;
    public static final int MOTE5_ACC_MEAN_X = 122;
    public static final int MOTE5_ACC_MEAN_Y = 123;
    public static final int MOTE5_ACC_MEAN_Z = 124;
    public static final int MOTE5_PLETHY_RED = 125;
    public static final int MOTE5_PLETHY_INFRARED = 126;

    /*// (versione aggiornata al 16/2/2006, con resp=2)
    public static final int ECG0  = 1;
    public static final int RESP0 = 2;
    public static final int ACCX0 = 3;
    public static final int ACCY0 = 4;
    public static final int ACCZ0 = 5;
    public static final int ACCX1 = 6;
    public static final int ACCY1 = 7;
    public static final int ACCZ1 = 8;
    //costante aggiunta per disegnare il grafico del modulo dell'accelerazione
    public static final int MOD_ACC = 6;

    public static final int HR0 = 7;

    // andrebbero distinti i segnali provenienti dagli ADC e quelli dagli altri digitali
    public static final int TEMP0 = 9;
    public static final int ACC_FS_X0 = 10;
    public static final int ACC_FS_Y0 = 11;
    public static final int ACC_FS_Z0 = 12;
    public static final int ACC_FS_X1 = 13;
    public static final int ACC_FS_Y1 = 14;
    public static final int ACC_FS_Z1 = 15;

    public static final int MOD_ACC_FS_0 = 16;
    public static final int MOD_ACC_FS_1 = 17;

    public static final int HF_IMP_SLOW = 18;
    public static final int HF_IMP_FAST = 19;
    public static final int HF_IMP_REF = 20;
    public static final int IMP_REF_SLOW = 21;  //Segnale di riferimento dell'impedenza campionato a bassa frequenza (1 Hz)

    public static final int SLOW_IMP_0 = 22;
    public static final int SLOW_IMP_1 = 23;

    public static final int SPARE7 = 24;

    //2013-1029 le seguenti tre definizioni sono relative al segnale dell'accelerometro interno MMA8451 sulla MiniMagIC
    public static final int ACC_INT16_FS_X0 = 25;
    public static final int ACC_INT16_FS_Y0 = 26;
    public static final int ACC_INT16_FS_Z0 = 27;

    public static final int GYRO_INT16_FS_X = 28;
    public static final int GYRO_INT16_FS_Y = 29;
    public static final int GYRO_INT16_FS_Z = 30;

    // 5 novembre 2015: pacchetti mote: al novembre 2015 sono 17 canali occupati e 2 liberi
    public int Mote_Sig_Type;
    public int Mote_Sig_Frequency;

    //MOTE1
    public static final int MOTE1_ACC1_X = 40;
    public static final int MOTE1_ACC1_Y = 41;
    public static final int MOTE1_ACC1_Z = 42;
    public static final int MOTE1_ACC2_X = 43;
    public static final int MOTE1_ACC2_Y = 44;
    public static final int MOTE1_ACC2_Z = 45;
    public static final int MOTE1_ACC3_X = 46;
    public static final int MOTE1_ACC3_Y = 47;
    public static final int MOTE1_ACC3_Z = 48;
    public static final int MOTE1_ACC4_X = 49;
    public static final int MOTE1_ACC4_Y = 50;
    public static final int MOTE1_ACC4_Z = 51;
    public static final int MOTE1_ACC_MEAN_X = 52;
    public static final int MOTE1_ACC_MEAN_Y = 53;
    public static final int MOTE1_ACC_MEAN_Z = 54;
    public static final int MOTE1_PLETHY_RED = 55;
    public static final int MOTE1_PLETHY_INFRARED = 56;
    //MOTE2
    public static final int MOTE2_ACC1_X = 50;
    public static final int MOTE2_ACC1_Y = 51;
    public static final int MOTE2_ACC1_Z = 52;
    public static final int MOTE2_ACC2_X = 53;
    public static final int MOTE2_ACC2_Y = 54;
    public static final int MOTE2_ACC2_Z = 55;
    public static final int MOTE2_ACC3_X = 56;
    public static final int MOTE2_ACC3_Y = 57;
    public static final int MOTE2_ACC3_Z = 58;
    public static final int MOTE2_ACC4_X = 59;
    public static final int MOTE2_ACC4_Y = 60;
    public static final int MOTE2_ACC4_Z = 61;
    public static final int MOTE2_ACC_MEAN_X = 62;
    public static final int MOTE2_ACC_MEAN_Y = 63;
    public static final int MOTE2_ACC_MEAN_Z = 64;
    public static final int MOTE2_PLETHY_RED = 65;
    public static final int MOTE2_PLETHY_INFRARED = 66;
    //MOTE3
    public static final int MOTE3_ACC1_X = 70;
    public static final int MOTE3_ACC1_Y = 71;
    public static final int MOTE3_ACC1_Z = 72;
    public static final int MOTE3_ACC2_X = 73;
    public static final int MOTE3_ACC2_Y = 74;
    public static final int MOTE3_ACC2_Z = 75;
    public static final int MOTE3_ACC3_X = 76;
    public static final int MOTE3_ACC3_Y = 77;
    public static final int MOTE3_ACC3_Z = 78;
    public static final int MOTE3_ACC4_X = 79;
    public static final int MOTE3_ACC4_Y = 80;
    public static final int MOTE3_ACC4_Z = 81;
    public static final int MOTE3_ACC_MEAN_X = 82;
    public static final int MOTE3_ACC_MEAN_Y = 83;
    public static final int MOTE3_ACC_MEAN_Z = 84;
    public static final int MOTE3_PLETHY_RED = 85;
    public static final int MOTE3_PLETHY_INFRARED = 86;
    //MOTE4
    public static final int MOTE4_ACC1_X = 90;
    public static final int MOTE4_ACC1_Y = 91;
    public static final int MOTE4_ACC1_Z = 92;
    public static final int MOTE4_ACC2_X = 93;
    public static final int MOTE4_ACC2_Y = 94;
    public static final int MOTE4_ACC2_Z = 95;
    public static final int MOTE4_ACC3_X = 96;
    public static final int MOTE4_ACC3_Y = 97;
    public static final int MOTE4_ACC3_Z = 98;
    public static final int MOTE4_ACC4_X = 49;
    public static final int MOTE4_ACC4_Y = 100;
    public static final int MOTE4_ACC4_Z = 101;
    public static final int MOTE4_ACC_MEAN_X = 102;
    public static final int MOTE4_ACC_MEAN_Y = 103;
    public static final int MOTE4_ACC_MEAN_Z = 104;
    public static final int MOTE4_PLETHY_RED = 105;
    public static final int MOTE4_PLETHY_INFRARED = 106;
    //MOTE5
    public static final int MOTE5_ACC1_X = 110;
    public static final int MOTE5_ACC1_Y = 111;
    public static final int MOTE5_ACC1_Z = 112;
    public static final int MOTE5_ACC2_X = 113;
    public static final int MOTE5_ACC2_Y = 114;
    public static final int MOTE5_ACC2_Z = 115;
    public static final int MOTE5_ACC3_X = 116;
    public static final int MOTE5_ACC3_Y = 117;
    public static final int MOTE5_ACC3_Z = 118;
    public static final int MOTE5_ACC4_X = 119;
    public static final int MOTE5_ACC4_Y = 120;
    public static final int MOTE5_ACC4_Z = 121;
    public static final int MOTE5_ACC_MEAN_X = 122;
    public static final int MOTE5_ACC_MEAN_Y = 123;
    public static final int MOTE5_ACC_MEAN_Z = 124;
    public static final int MOTE5_PLETHY_RED = 125;
    public static final int MOTE5_PLETHY_INFRARED = 126;
*/



    //==========================================================================
    //==========================================================================
    //==========================================================================
    // tipi di pacchetto
    //==========================================================================
    //==========================================================================
    //==========================================================================

    public static final int CMD_DATA0 = 0x00;    // payload classico 12+3+4x3 codifica0 (dei dati 
    public static final int CMD_DATA0T = 0x01;    // payload codifica0 + temperatura
    public static final int CMD_DATA1 = 0x02;
    public static final int CMD_XPOD0 = 0x08;    // payload XPod codifica0
    public static final int CMD_XPOD1 = 0x09;    // payload XPod codifica1 con timestamp

    public static final int CMD_GPS0 = 0x0a;

    public static final int CMD_UART0 = 0x10;      //payload per l'invio di tutto ciò che arriva dalla seriale

    public static final int CMD_ASYNC_DATA = 0x22;    // Payload con evento numerico

    //public static final int CMD_UART0 = 0x10;      //payload per l'invio di tutto ciò che arriva dalla seriale

    ////public static final int CMD_GEN_DATA0 = 0x40; //Payload per dati generici
    public static final int CMD_SINGLE_SIG_UINT8 = 0x40;    //Payload contenente dati da un singolo segnale in formato uint 8bit
    public static final int CMD_SINGLE_SIG_UINT12 = 0x41;    //Payload contenente dati da un singolo segnale in formato Uint 12bit
    public static final int CMD_SINGLE_SIG_UINT16 = 0x43;    //Payload contenente dati da un singolo segnale in formato Uint 16bit
    public static final int CMD_SINGLE_SIG_UINT20 = 0x44;    //Payload contenente dati da un singolo segnale in formato Uint 20bit
    public static final int CMD_SINGLE_SIG_UINT32 = 0x46;    //Payload contenente dati da un singolo segnale in formato Uint 32bit

    // 2015 mote
    public static final int CMD_MOTE_SIG = 0x50; // MOTE signal
    public static final int ACC_V1=0x10; // accelerometer version1
    public static final int PPG_V1=0x20; // photoplethysmography version1

    public static final int CMD_SINGLE_SIG_GENERIC = 0x60; //Payload per array di dati omogenei generici con Fs, NUmero di segnali e num bit
    public static final int CMD_MULTI_SIG_GENERIC = 0x61; //Payload per array di dati omogenei generici con Fs, NUmero di segnali e num bit


    public static final int CMD_STATUS0 = 0x80;   // Payload assente (lo stato è contenuto nell'argomento)
    public static final int CMD_STATUS1 = -127;//todo diana: prima era così ma non andava: 0x81;

    public static final int CMD_POWER_DATA = 0x90;   // Payload con i dati rilevati dal fuel gauge DS2782
    public static final int CMD_POWER_PARAM = 0x93;  // Payload con i parametri rilevati dal fuel gauge DS2782
    public static final int CMD_TOGGLE_POWER = 0x95;  //cmd per attivare o disattivare l'alimentazione a determinati circuiti

    public static final int CMD_GET = 0xa0;    // cmd per richiedere dei dati
    public static final int CMD_SET = 0xa2;    // cmd per impostare dei dati

    public static final int CMD_BEEP0 = 0xb0;      //Beep
    public static final int CMD_BEEPA = 0xba;

    //public static final int CMD_START_ACQ = 0xc0;    // comando per fare partire l'acquisizione
    //public static final int CMD_STOP_ACQ = 0xc1;    // comando per stoppare l'acquisizione

    //public static final int CMD_GOING_DOWN = 0xcd;    // segnale che indica il prossimo spegnimento del sistema

    public static final int CMD_SEND_DATA = 0xc4;
    public static final int CMD_STOP_SEND = 0xc5;

    // RINO : flag per avviare la misura dell'impedenza agli elettrodi
    public static final int CMD_IMPED_MEASURE = 0xc6;

    public static final int CMD_KEEPALIVE = 0xca;    // keepalive per il mantenimento della comunicazione

    public static final int CMD_TIMED_START_SEND = 0xc7;    //comando per fare partire la comunicazione BT per un det. numero di secondi

    public static final int CMD_TIME0 = 0xd0;   //pacchetto 

    public static final int CMD_DOWNLOAD = 0xd5;   //pacchetto 

    public static final int DNLOAD_LIST = 0;
    public static final int DNLOAD_LAST = 1;
    public static final int DNLOAD_FILENUM = 2;

    public static final int CMD_EOA0 = 0xe0;    //pacchetto di fine acquisizione che identifica il numero di pacchetti per ogni tipo memorizzati

    public static final int CMD_GOTO_CMD_MODE = 0xe5;
    public static final int CMD_GOTO_ACQ_MODE = 0xe6;

    public static final int CMD_SHUTDOWN = 0xe8;
    public static final int CMD_FILE_SLICE = 0xea;

    public static final int CMD_GET_RUNNING_STATUS = 0xec;

    public static final int CMD_SET_DATE = 0xdd;
    public static final int CMD_SET_TIME = 0xde;

    public static final int CMD_SET_EVENT = 0xc8; // Event marker: comando da mandare a magic per farle registrare evento


    public static final int CMD_ACK = 0xf0;    // 
    public static final int CMD_NACK = 0xf1;    // 

    public static final int CMD_PING = 0xf3;    // 
    public static final int CMD_PONG = 0xf4;    // 

    public static final int CMD_ERROR = 0xfe;
    public static final int CMD_DUMMY_PAYLOAD = 0xfa;

    //==========================================================================
    //==========================================================================
    //==========================================================================
    //Dimensione dei payload
    //==========================================================================
    //==========================================================================
    //==========================================================================

    public static final int PAYLOAD_SIZE_DATA0 = 37;
    public static final int PAYLOAD_SIZE_DATA1 = 78;

    public static final int PAYLOAD_SIZE_XPOD0 = 38;
    public static final int PAYLOAD_SIZE_XPOD1 = 44;

    public static final int PAYLOAD_SIZE_STATUS1 = 20;

    public static final int CMD_1L_BUF_STATUS = -123; //todo diana: prima era così ma non andava: 0x85;    //payload contenente i valori medi dei contatori dei buffer di primo livello

    public static final int PAYLOAD_SIZE_POWER_DATA = 28;
    public static final int PAYLOAD_SIZE_POWER_PARAM = 30;

    //public static final int PAYLOAD_SIZE_EVENT0 = 20;
    public static final int PAYLOAD_SIZE_ASYNC_DATA = 20;

    public static final int PAYLOAD_SIZE_EOA0 = 34;
    public static final int PAYLOAD_SIZE_TIME0 = 21;

    //Elenco dei tipi di dato per il paccheto PACK_TYPE_ASYNC_DATA
    public static final int ASYNC_SIG_EVENTMARKER0 = 0x00;
    public static final int ASYNC_SIG_REF_IMPED = 0x20;
    public static final int ASYNC_SIG_TEMPERATURE = 0x30;

    public static final int ASYNC_SIG_SINCHRO = 0x01;  // segnale di synchro


    // ID pacchetto inviato in caso di reset di sensore (acc/gyro digitali)
    public static final int DEVICE_FAULT = 0x40;
    public static final int ASYNC_SIG_ECG_IMPED = 0x50;
    //Tipo di segnale
    public static final int SIGNAL_TYPE_ECG = 0x01;
    public static final int SIGNAL_TYPE_RESP = 0x05;
    public static final int SIGNAL_TYPE_EEG = 0x07;
    public static final int SIGNAL_TYPE_EOG = 0x12;
    public static final int SIGNAL_TYPE_EMG = 0x15;
    public static final int SIGNAL_TYPE_GSR = 0x19;
    public static final int SIGNAL_TYPE_TEMPERATURE = 0x1b;
    public static final int SIGNAL_TYPE_PRESSURE = 0x1c;
    public static final int SIGNAL_TYPE_EPG = 0x1e;
    public static final int SIGNAL_TYPE_IMPEDANCE = 0x22;
    public static final int SIGNAL_TYPE_SLOW_IMPEDANCE = 0x23;

    public static final int SIGNAL_TYPE_LENGTH = 0x30;
    public static final int SIGNAL_TYPE_ANGLE = 0x32;
    public static final int SIGNAL_TYPE_SPEED = 0x33;
    public static final int SIGNAL_TYPE_ANGVEL = 0x35;
    public static final int SIGNAL_TYPE_ACCEL = 0x37;

    public static final int SIGNAL_TYPE_SPARE_0 = 0xc0;
    public static final int SIGNAL_TYPE_SPARE_1 = 0xc1;

    public static final int SIGNAL_TYPE_GENERIC_8b = 0xe0;
    public static final int SIGNAL_TYPE_GENERIC_12b = 0xe1;
    public static final int SIGNAL_TYPE_GENERIC_16b = 0xe2;
    public static final int SIGNAL_TYPE_GENERIC_24b = 0xe3;
    public static final int SIGNAL_TYPE_GENERIC_32b = 0xe4;

    /**********************************************/
    /**********************************************/

    public static final int PROBLEMS = 3;
    public static final int INIT = 5;
    public static final int IDLE = 7;
    public static final int OFFLINE = 8;
    public static final int PREVIEW = 9;
    public static final int RECORDING = 11;

    // Costanti per il tipo di acquisizione
    public static final int PROTOCOL_20 = 0x20;
    public static final int PROTOCOL_21 = 0x21;
    public static final int PROTOCOL_22 = 0x22;
    public static final int PROTOCOL_23 = 0x23;
    public static final int PROTOCOL_30 = 0x30;

    //Costanti per MiniMAGIC
    public static final int MINIMAGIC_CARDREADER_POWER = 0x01;
    public static final int MINIMAGIC_uSD_POWER = 0x02;
    public static final int MINIMAGIC_BT_POWER = 0x03;
    public static final int MINIMAGIC_ACC_POWER = 0x04;
    public static final int MINIMAGIC_GYRO_POWER = 0x05;
    public static final int MINIMAGIC_ECG_POWER = 0x06;
    public static final int MINIMAGIC_Z_POWER = 0x07;
    public static final int MINIMAGIC_AUX_POWER = 0x08;


    static final short MAX_SIZE_PACKET = 200;//SIZE_PACKET_HEADER + PAYLOAD_SIZE_DATA1;
}
