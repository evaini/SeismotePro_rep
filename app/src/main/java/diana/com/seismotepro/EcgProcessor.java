package diana.com.seismotepro;

/**
 * Created by Diana Scurati on 23/12/2015.
 */
// copiata così com'è dal documento fornito da ema


//==========================================================================
public class EcgProcessor {
    //==========================================================================
    public peak_finder myPeak;

    buf1 buf_calc_MA = new buf1();
    buf2 buf_filtered_sig = new buf2();
    buf3 buf_derivata = new buf3();

    private boolean buf1ful;
    private boolean buf2ful;
    private boolean learning_in_progress;
    private boolean firstPeak;
    public boolean refrON;
    private boolean MAXON;

    int refr;
    double thresh;
    long countSample;
    int bufMA_len;
    int learning_samples;
    int nsample_deriv;
    int refrTime;
    double OLDMaxValue;
    public long OLDMaxPosition;
    int MaxWindow;
    int MaxWindow_time;
    double Deriv_value;
    double Var_Deriv;
    double Sd_Deriv;
    double MaxValDeriv;
    public long MaxPosition;
    public int RRInt;
    public int HR;
    double KThresh;

    // buffer e contatore per rilevare il picco massimo
    // che pare essere 6 campioni prima del dato attuale
    //int[] mybuffer = new int[6];
    double[] mybuffer = new double[6];
    int mycounter;
    public boolean newRRpresent = false;
    public double newRRValue = 0.0;
    public double newRRTime = 0;

    double signal_freq_sampling;  //EV
    //==========================================================================
    public EcgProcessor(double frequenza_campionamento)    {
        //==========================================================================
        signal_freq_sampling = frequenza_campionamento;
        Init();
    }
    //mod diana
    //==========================================================================
    public EcgProcessor(int frequenza_campionamento, int acquisiz_num_campioni)    {
        //==========================================================================
        // acquisiz_num_campioni = numero camp del buffer di dati ecg
        signal_freq_sampling = (double)frequenza_campionamento;
        Init();
        myPeak = new peak_finder(acquisiz_num_campioni, frequenza_campionamento);
    }





    //==========================================================================
    public class buf1 {
        public double[] dati = new double[101];
        public int pointer;
        public double somma;
    }
    //==========================================================================
    public class buf2    {
        public double[] dati = new double[201];
        public int pointer;
    };
    //==========================================================================
    public class buf3    {
        public double[] dati = new double[10001];
        public int pointer;
        public double somma;
        public double somma2;
    };



    //==========================================================================
    public class peak_finder{
            //==========================================================================

        public int conta_picchi;            //contatore del numero di picchi
        int dim_peak_pointer;               // dimensioni dell'array sottostante
        public int[] peak_pointer;          // array dei puntatori alla matrice dati sul picco = new int[dim_peak_pointer];

        //==========================================================================
        public peak_finder(int length,int frequency){
            //==========================================================================
            conta_picchi = 0;
            dim_peak_pointer = (int)((length / frequency) * 3);     // ipotesi di avere max circa 3 battiti al secondo
            peak_pointer = new int[dim_peak_pointer];
            for (int conta = 0; conta < dim_peak_pointer; conta++)
                peak_pointer[conta] = 0; //inizializzazione array
        }
    }




    /// Inizializza le strutture dati dell'oggetto
    //==========================================================================
    public void Init(){
    //==========================================================================
        countSample = 0;
        buf1ful = false;
        buf2ful = false;
        learning_in_progress = false;
        firstPeak = true;
        buf_calc_MA.pointer = 0;
        buf_filtered_sig.pointer = 0;
        buf_derivata.pointer = 0;
        buf_calc_MA.somma = 0;
        buf_derivata.somma = 0;
        buf_derivata.somma2 = 0;
        int i;

        //'Clear buffers
        for (i = 0; i < 10; i++)
            buf_calc_MA.dati[i] = 0;


        for (i = 0; i < 20; i++)
            buf_filtered_sig.dati[i] = 0;


        for (i = 0; i < 1000; i++)
            buf_derivata.dati[i] = 0;

        //'Settaggio iniziale parametri?
        bufMA_len = 4;
        //'nsample_deriv                  def=3

        //learning_samples = 400;// 400;      //2 secondi

        learning_samples = (int)(2 * signal_freq_sampling);  // EV

        nsample_deriv = 3;          //la derivata e' (s(n)-s(n-3))/3

        KThresh = 2.0;

        refr = (int)(0.300 * signal_freq_sampling); //perchè è di 300ms
        MaxWindow = (int)(0.100 * signal_freq_sampling); //finestra dopo il passaggio sopra soglia in cui cercare il picco R (100 ms)

    }//fine init

    /// Aggiorna l'oggetto, inserendo il nuovo valore del campione di segnale ECG acquisito
    /// Rappresenta il centro dell'algoritmo di calcolo del picco R
    //==========================================================================
    public void AddData(double value )    {
        //==========================================================================
        double old_dbl_value;
        double MAvalue;
        int point_old_value, point_new_value;
        boolean MODtoggle = false;

        try
        {
            mybuffer[mycounter] = value;
            if (++mycounter > 5)
                mycounter = 0;

            //bufMA_len contiene il numero di valori su cui viene calcolata il MA (Moving Average) del segnale originale (lunghezza del filtro e quindi del buffer circolare "buf_calc_MA").
            //learning_samples contiene il numero di samples da cui estrarre i parametri //iniziali dell'algoritmo di riconoscimento (lunghezza del buffer circolare "buf_derivata").
            //nsample_deriv contiene il delta t (in numero di samples) su cui calcolare la derivata (maggiore e' questo numero e piu'filtrata e' la derivata!). Da questo 'parametro dipende la lunghezza del buffer circolare "buf_filtered_signal").

            //nsample_deriv = 3           //la derivata e' (s(n)-s(n-3))/3
            /*inizio analisi o reset per errori di trasmissione
            Qui vengono solo inizializzate le variabili che permettono di svolgere le seguenti funzioni in sequenza:
            1 - riempimento di buf_MA '
            2 - learning
            3 - analisi on line
            Il segnale attualmente utilizzato per identificare il picco R e'il modulo ' della derivata filtrata del segnale filtrato. I parametri e i criteri di ' riconoscimento sono:
            1) il punto di trigger è il passaggio della derivata per una soglia (thresh=2*sd_deriv_mod)
            2) Controllo della refrattarietà (pari al 60% dell'IntRR precedente purche'*/
            //   200ms<refr<600ms
            // Durante l'analisi viene aggiornata dinamicamente la refrattarietà (è funzione dell'IntRR)  la sd_deriv_mod

            countSample = countSample + 1;

            // buf_calc_MA da riempire?
            if (buf1ful == false)
            {
                buf_calc_MA.dati[buf_calc_MA.pointer] = value;
                buf_calc_MA.pointer = buf_calc_MA.pointer + 1;
                if (buf_calc_MA.pointer == bufMA_len)
                {
                    buf_calc_MA.pointer = 0;
                    buf1ful = true;
                    learning_in_progress = true;

                }
                return;
            }

            // Learning?
            if (learning_in_progress == true)
            {

                //Azioni durante il learning:
                // 1 - aggiungi il nuovo valore al buf_calc_MA
                // 2 - calcola il valore filtrato del segnale originale
                // 3 - aggiungi il valore filtrato al buf_filtered_sig
                // 4 - calcola il nuovo valore di derivata
                // 5 - aggiungi il valore filtrato della derivata al buf_derivata
                //' 6 - se sei alla fine del periodo di learning calcola i parametri '

                //Learning action 1 - aggiungi nuovo valore al buffer & aggiorna somma
                //salva il vecchio valore
                old_dbl_value = buf_calc_MA.dati[buf_calc_MA.pointer];
                //scrivi il nuovo valore
                buf_calc_MA.dati[buf_calc_MA.pointer] = value;
                //aggiorna il puntatore
                buf_calc_MA.pointer = buf_calc_MA.pointer + 1;
                if (buf_calc_MA.pointer == bufMA_len)
                    buf_calc_MA.pointer = 0;

                //aggiorna la somma
                buf_calc_MA.somma = buf_calc_MA.somma - old_dbl_value + (double)value;
                //Learning actions 2&3 - calcola il valore filtrato del segnale originale e agg. 'buf_filtered_sig

                MAvalue = buf_calc_MA.somma / bufMA_len;
                buf_filtered_sig.dati[buf_filtered_sig.pointer] = MAvalue;
                buf_filtered_sig.pointer = buf_filtered_sig.pointer + 1;
                if (buf_filtered_sig.pointer == (nsample_deriv + 1))
                {
                    buf2ful = true;
                    buf_filtered_sig.pointer = 0;
                }

                //Learning action 4&5 - calcolo della nuova derivata e agg buffer di learning deriv
                if (buf2ful == false)
                    return;   //ancora non ho valori sufficienti per calcolare la prima derivata

                // se non ritorno allora Ho punti a sufficienza
                point_old_value = buf_filtered_sig.pointer - 1 - nsample_deriv;
                if (point_old_value < 0)
                    point_old_value = nsample_deriv + 1 + point_old_value;

                // ricorda che la derivata e' (s(n)-s(n-3))/3 , dove nsample_deriv==3;

                point_new_value = buf_filtered_sig.pointer - 1;

                if (point_new_value < 0)
                    point_new_value = nsample_deriv + 1 + point_new_value;

                Deriv_value = ((buf_filtered_sig.dati[point_new_value] - buf_filtered_sig.dati[point_old_value]) / nsample_deriv);

                old_dbl_value = buf_derivata.dati[buf_derivata.pointer];
                buf_derivata.dati[buf_derivata.pointer] = Deriv_value;
                buf_derivata.pointer = buf_derivata.pointer + 1;
                buf_derivata.somma = buf_derivata.somma - old_dbl_value + Deriv_value;
                buf_derivata.somma2 = buf_derivata.somma2 - java.lang.Math.pow(old_dbl_value, 2.0) + java.lang.Math.pow(Deriv_value, 2.0);

                // Learning action 6 - se fine del learning, calcolo dei parametri ' (attenzione il buf_learn_orig a questo punto contiene learning_samples dati, ma ' buf_learn_deriv ne contiene (learning-samples - nsamp_deriv)!!!)

                if (buf_derivata.pointer == (learning_samples + 1))
                {
                    buf_derivata.pointer = 0;
                    learning_in_progress = false;

                    //Calcolo della sd del modulo della derivata

                    Var_Deriv = buf_derivata.somma2 / (double)learning_samples -
                            (buf_derivata.somma / java.lang.Math.pow((double) learning_samples, 2.0));
                    if (Var_Deriv > 0)
                        Sd_Deriv = java.lang.Math.sqrt(Var_Deriv);
                    else
                        Sd_Deriv = 0;
                    thresh = KThresh * Sd_Deriv;
                }
                return;

            }

            //Si arriva a questo punto se il buffer dati e' carico e il learning e' terminato '
            //(RUNNING ANALYSIS)
            // 'Le operazioni in questa fase sono:
            //' 1- aggiornamento buf_MA & calcolo del nuovo valore filtrato,
            //'      aggiornamento del buf_filtered_sig e calcolo della nuova derivata
            // 2- aggiorna sd e threshold
            // 3- se e// passato il periodo di refrattarieta', verifica dell'eventuale passaggio
            //    per la soglia
            // 4- eventualmente calcola entro 100 ms (20 campioni) il max assoluto e memorizza il tempo del max
            //' 5- eventualmente calcola RRI & aggiorna refrattarieta' (visualizza & sound) '

            //Runnung action 1 - aggiungi nuovo valore al buffer, aggiorna somma & calcolo derivata
            old_dbl_value = buf_calc_MA.dati[buf_calc_MA.pointer];   //salva il vecchio valore
            buf_calc_MA.dati[buf_calc_MA.pointer] = value;       //scrivi il nuovo valore
            buf_calc_MA.pointer = buf_calc_MA.pointer + 1;       //aggiorna il puntatore
            if (buf_calc_MA.pointer == bufMA_len)
                buf_calc_MA.pointer = 0;

            buf_calc_MA.somma = buf_calc_MA.somma - old_dbl_value + (double)value;   //aggiorna la somma

            MAvalue = buf_calc_MA.somma / bufMA_len;
            buf_filtered_sig.dati[buf_filtered_sig.pointer] = MAvalue;
            buf_filtered_sig.pointer = buf_filtered_sig.pointer + 1;
            if (buf_filtered_sig.pointer == nsample_deriv + 1)
            {
                buf2ful = true;
                buf_filtered_sig.pointer = 0;
            }

            point_old_value = buf_filtered_sig.pointer - 1 - nsample_deriv;
            if (point_old_value < 0)
                point_old_value = nsample_deriv + 1 + point_old_value;

            point_new_value = buf_filtered_sig.pointer - 1;
            if (point_new_value < 0)
                point_new_value = nsample_deriv + 1 + point_new_value;

            Deriv_value = (buf_filtered_sig.dati[point_new_value] -
                    buf_filtered_sig.dati[point_old_value]) / nsample_deriv;

            if (MODtoggle == true)
                Deriv_value = java.lang.Math.abs(Deriv_value);

            old_dbl_value = buf_derivata.dati[buf_derivata.pointer];
            buf_derivata.dati[buf_derivata.pointer] = Deriv_value;
            buf_derivata.pointer = buf_derivata.pointer + 1;
            if (buf_derivata.pointer == learning_samples + 1)
                buf_derivata.pointer = 0;
            buf_derivata.somma = buf_derivata.somma - old_dbl_value + Deriv_value;
            buf_derivata.somma2 = buf_derivata.somma2 - java.lang.Math.pow(old_dbl_value, 2.0) +
                    java.lang.Math.pow(Deriv_value, 2.0);

            // Running action 2 - 4- aggiorna sd e threshold
            Var_Deriv = buf_derivata.somma2 / learning_samples - java.lang.Math.pow((buf_derivata.somma / learning_samples), 2.0);
            if (Var_Deriv > 0)
                Sd_Deriv = java.lang.Math.sqrt(Var_Deriv);
            else
                Sd_Deriv = 0;

            thresh = KThresh * Sd_Deriv;

            //Running actions 3,4,5
            // se siamo nel periododi refrattarietà...
            if (refrON == true)
            {
                //se siamo nel periodo di osservazione per il picco R ...
                if (MAXON == true)
                {
                    if (MaxValDeriv < java.lang.Math.abs(Deriv_value))
                    {
                        MaxValDeriv = java.lang.Math.abs(Deriv_value);
                        MaxPosition = countSample;
                        myPeak.peak_pointer[myPeak.conta_picchi] = (int)MaxPosition;
                                            }
                    MaxWindow_time = MaxWindow_time + 1;
                    // se siamo alla fine del periodo di osservazione per identificazione del picco R ...
                    if (MaxWindow_time == MaxWindow)
                    {
                        MAXON = false;
                        if (firstPeak == true)
                            firstPeak = false;
                        else
                        {
                            //***************** Calcolo dell'HR ecc.
                            RRInt = (int)(MaxPosition - OLDMaxPosition);
                            RRInt = RRInt * 5; //RR in ms

                            if (RRInt > 0)
                                HR = ((int)(60000.0F / (float)RRInt));
                            else
                                HR = 0;

                            //espresso in secondi
                            newRRTime = (double)(MaxPosition - 6) / 200.0F; //sarebbe = maxposition * 5ms / 1000 per i secondi
                            newRRTime = (double)(MaxPosition - 6) / ((1.0 * signal_freq_sampling));

                            //lo trasformo in secondi
                            newRRValue = (double)(RRInt) / 1000.0;

                            newRRpresent = true;


                        }
                        OLDMaxValue = MaxValDeriv;
                        OLDMaxPosition = MaxPosition;
                        MaxWindow_time = 0;
                    }
                }

                // altrimenti ....
                refrTime = refrTime + 1;
                //se siamo alla fine del periodo di refrattarietà ...
                if (refrTime >= refr) {
                    refrON = false;
                    refrTime = 0;
                }
                return;
            }
            //'
            // Si arriva qui se siamo fuori del periodo di refrattarietà. Controllo  l'eventuale passaggio per la soglia.
            if (java.lang.Math.abs(Deriv_value) > thresh) {
                refrON = true;
                MAXON = true;
                MaxValDeriv = java.lang.Math.abs(Deriv_value);
                MaxPosition = countSample;

                myPeak.peak_pointer[myPeak.conta_picchi] = (int)MaxPosition;
                myPeak.conta_picchi = myPeak.conta_picchi + 1;

            }
        }

        catch (Exception ex)        {
            ex.printStackTrace();
        }

    }//fine add data

}
