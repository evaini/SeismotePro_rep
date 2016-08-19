package diana.com.seismotepro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//==========================================================================
public class ActivitySettings extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //==========================================================================

    Spinner spinner1;   // selezione sorgente: mote o magic
    Spinner spinner2;
    Spinner spinner3;
    Spinner spinner4;

    Spinner spinner1_1; // lista sensori
    Spinner spinner1_2; // lista assi per sensore
    Spinner spinner2_1; // lista sensori
    Spinner spinner2_2; // lista assi per sensore
    Spinner spinner3_1; // lista sensori
    Spinner spinner3_2; // lista assi per sensore
    Spinner spinner4_1; // lista sensori
    Spinner spinner4_2; // lista assi per sensore

    TextView text1; // scritta filename
    TextView text2; //scritte graph 1
    TextView text3;
    TextView text4;
    TextView text5;

    EditText filename_edit;
    TextView filename_textview;

    Settings UserSettings;

    private final static String[] Spinner1list = {"Magic", "Mote 1", "Mote 2", "Mote 3", "Mote 4", "Mote 5"};
    private final static String[] Spinner2_magiclist = {"Acc", "ECG", "Resp"};  // menu magic
    private final static String[] Spinner2_motelist = {"Acc1", "Acc2", "Acc3", "Acc4", "Acc_mean", "PPG"};          //menu mote
    private final static String[] Spinner3_acclist = {"X", "Y", "Z"};         // menu acc
    private final static String[] Spinner3_ppglist = {"RED", "IR", "Mean"};           // menu ppg

    private CheckBox CheckBox_Developer;
    private CheckBox CheckBox_TransferMode;

    private String mode = "";
    private boolean isCompleteMode = false; // default: visualizzo solo possibilità per cambiare il nome del file

    private EditText Param1_view;
    private EditText Param2_view;
    private EditText Param3_view;
    private EditText Param4_view;
    private EditText Param5_view;
    private EditText Param6_view;
    private EditText Param7_view;
    private EditText Param8_view;
    private EditText Param9_view;
    private EditText Param10_view;
    private EditText Notes_view;


    @Override
    //==========================================================================
    protected void onCreate(Bundle savedInstanceState) {
        //==========================================================================
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        text2 = (TextView)findViewById(R.id.textView_2);
        text3 = (TextView)findViewById(R.id.textView_3);
        text4 = (TextView)findViewById(R.id.textView_4);
        text5 = (TextView)findViewById(R.id.textView_5);

        Intent intent = getIntent();
        UserSettings = (Settings) intent.getSerializableExtra("user");      // GET INPUT FROM  ACTIVITY 1
        UserSettings.ClearMagicPlot();

        spinner1 = (Spinner) findViewById(R.id.spinner_1);
        spinner2 = (Spinner) findViewById(R.id.spinner_2);
        spinner3 = (Spinner) findViewById(R.id.spinner_3);
        spinner4 = (Spinner) findViewById(R.id.spinner_4);
        filename_edit = (EditText)findViewById(R.id.editText_1);
        filename_edit.setHint(UserSettings.FileName);
        filename_textview = (TextView)findViewById(R.id.textView_1);
        CheckBox_Developer = (CheckBox)findViewById(R.id.checkBox_developer);
        CheckBox_Developer.setChecked(UserSettings.DeveloperMode);

        CheckBox_TransferMode = (CheckBox)findViewById(R.id.checkBox_transfermode);
        CheckBox_TransferMode.setChecked(UserSettings.isTransferModeEnabled);


        ArrayAdapter<String> arrayAdapter_1lev = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Spinner1list);
        arrayAdapter_1lev.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(arrayAdapter_1lev);
        spinner2.setAdapter(arrayAdapter_1lev);
        spinner3.setAdapter(arrayAdapter_1lev);
        spinner4.setAdapter(arrayAdapter_1lev);

        spinner1.setOnItemSelectedListener(this);
        spinner2.setOnItemSelectedListener(this);
        spinner3.setOnItemSelectedListener(this);
        spinner4.setOnItemSelectedListener(this);


        Param1_view = (EditText)findViewById(R.id.editText_infoparam1);
        Param2_view = (EditText)findViewById(R.id.editText_infoparam2);
        Param3_view = (EditText)findViewById(R.id.editText_infoparam3);
        Param4_view = (EditText)findViewById(R.id.editText_infoparam4);
        Param5_view = (EditText)findViewById(R.id.editText_infoparam5);
        Param6_view = (EditText)findViewById(R.id.editText_infoparam6);
        Param7_view = (EditText)findViewById(R.id.editText_infoparam7);
        Param8_view = (EditText)findViewById(R.id.editText_infoparam8);
        Param9_view = (EditText)findViewById(R.id.editText_infoparam9);
        Param10_view = (EditText)findViewById(R.id.editText_infoparam10);
        Notes_view = (EditText)findViewById(R.id.editText_info_note);

        // todo da modificare x versione finale come nei commenti occhio: chiama la onitemselected
        /*spinner1.setSelection(UserSettings.EnabledSource[0]);   //default: ecg
        spinner2.setSelection(UserSettings.EnabledSource[1]);   //default: acc z
        spinner3.setSelection(UserSettings.EnabledSource[2]);   //default: pleth*/
        // -1 = da codice id del segnale a sua posizione nella lista

        spinner1_1 = (Spinner) findViewById(R.id.spinner_1_1);
        spinner1_2 = (Spinner) findViewById(R.id.spinner_1_2);
        spinner2_1 = (Spinner) findViewById(R.id.spinner_2_1);
        spinner2_2 = (Spinner) findViewById(R.id.spinner_2_2);
        spinner3_1 = (Spinner) findViewById(R.id.spinner_3_1);
        spinner3_2 = (Spinner) findViewById(R.id.spinner_3_2);
        spinner4_1 = (Spinner) findViewById(R.id.spinner_4_1);
        spinner4_2 = (Spinner) findViewById(R.id.spinner_4_2);

        mode = (String)intent.getSerializableExtra("mode");
        if (mode.equals("Complete") && !mode.equals("")){
            isCompleteMode = true;
        }else if(mode.equals("changeName") && !mode.equals("")){
            isCompleteMode = false;
        }else if(mode.equals("signalSelection") && !mode.equals("")){
            isCompleteMode = true;
            CheckBox_Developer.setVisibility(View.INVISIBLE);
            CheckBox_TransferMode.setVisibility(View.INVISIBLE);
            filename_edit.setVisibility(View.INVISIBLE);
            filename_textview.setVisibility(View.INVISIBLE);
            text5.setVisibility(View.INVISIBLE);
            spinner4.setVisibility(View.INVISIBLE);
            spinner4_1.setVisibility(View.INVISIBLE);
            spinner4_2.setVisibility(View.INVISIBLE);
            Param1_view.setVisibility(View.INVISIBLE);
            Param2_view.setVisibility(View.INVISIBLE);
            Param3_view.setVisibility(View.INVISIBLE);
            Param4_view.setVisibility(View.INVISIBLE);
            Param5_view.setVisibility(View.INVISIBLE);
            Param6_view.setVisibility(View.INVISIBLE);
            Param7_view.setVisibility(View.INVISIBLE);
            Param8_view.setVisibility(View.INVISIBLE);
            Param9_view.setVisibility(View.INVISIBLE);
            Param10_view.setVisibility(View.INVISIBLE);
            Notes_view.setVisibility(View.INVISIBLE);
        }


        //********************************************************
        // leggi impostazioni attuali e rappresentale sugli spinner
        //********************************************************
        // per segnale 1
        spinner1.setSelection(UserSettings.EnabledSource[0]);
        if(UserSettings.EnabledSource[0] == 0) {      //selezionata magic
            CreateNewSpinner(spinner1_1, Spinner2_magiclist, UserSettings.EnabledSignal[0]);

            if(UserSettings.EnabledSignal[0] == 0){// selezionato l'accelerometro
                CreateNewSpinner(spinner1_2, Spinner3_acclist, UserSettings.EnabledSignal[0]);
            }

        } else if(UserSettings.EnabledSource[0] > 0 || UserSettings.EnabledSource[0] <= 5) { // selezionati i mote
            CreateNewSpinner(spinner1_1, Spinner2_motelist, UserSettings.EnabledSignal[0]);

            if(UserSettings.EnabledSignal[0] >= 0 || UserSettings.EnabledSignal[0] <= 4){// selezionati gli accelerometri del mote
                CreateNewSpinner(spinner1_2, Spinner3_acclist, UserSettings.EnabledAxes[0]);
            }else if(UserSettings.EnabledSignal[0] == 5){// selezionato il ppg
                CreateNewSpinner(spinner1_2, Spinner3_ppglist, UserSettings.EnabledAxes[0]);
            }
        }

        // per segnale 2
        spinner2.setSelection(UserSettings.EnabledSource[1]);
        spinner2_1.setSelection(UserSettings.EnabledSignal[1]);
        if (UserSettings.EnabledAxes[1] != -1)
            spinner2_2.setSelection(UserSettings.EnabledAxes[1]);
        // per segnale 3
        spinner3.setSelection(UserSettings.EnabledSource[2]);
        spinner3_1.setSelection(UserSettings.EnabledSignal[2]);
        if (UserSettings.EnabledAxes[2] != -1)
            spinner3_2.setSelection(UserSettings.EnabledAxes[2]);
        // per segnale 4
        spinner4.setSelection(UserSettings.EnabledSource[3]);
        spinner4_1.setSelection(UserSettings.EnabledSignal[3]);
        if (UserSettings.EnabledAxes[3] != -1)
            spinner4_2.setSelection(UserSettings.EnabledAxes[3]);

        SetSpinnerListeners();


        if(!isCompleteMode){ // visualizzo solo parte per modifica del nome del file
            spinner1.setVisibility(View.INVISIBLE);
            spinner1_1.setVisibility(View.INVISIBLE);
            spinner1_2.setVisibility(View.INVISIBLE);

            spinner2.setVisibility(View.INVISIBLE);
            spinner2_1.setVisibility(View.INVISIBLE);
            spinner2_2.setVisibility(View.INVISIBLE);

            spinner3.setVisibility(View.INVISIBLE);
            spinner3_1.setVisibility(View.INVISIBLE);
            spinner3_2.setVisibility(View.INVISIBLE);

            spinner4.setVisibility(View.INVISIBLE);
            spinner4_1.setVisibility(View.INVISIBLE);
            spinner4_2.setVisibility(View.INVISIBLE);

            CheckBox_Developer.setVisibility(View.INVISIBLE);
            CheckBox_TransferMode.setVisibility(View.INVISIBLE);

            text2.setVisibility(View.INVISIBLE);
            text3.setVisibility(View.INVISIBLE);
            text4.setVisibility(View.INVISIBLE);
            text5.setVisibility(View.INVISIBLE);

            Param1_view.setVisibility(View.INVISIBLE);
            Param2_view.setVisibility(View.INVISIBLE);
            Param3_view.setVisibility(View.INVISIBLE);
            Param4_view.setVisibility(View.INVISIBLE);
            Param5_view.setVisibility(View.INVISIBLE);
            Param6_view.setVisibility(View.INVISIBLE);
            Param7_view.setVisibility(View.INVISIBLE);
            Param8_view.setVisibility(View.INVISIBLE);
            Param9_view.setVisibility(View.INVISIBLE);
            Param10_view.setVisibility(View.INVISIBLE);
            Notes_view.setVisibility(View.INVISIBLE);
        }else {

            if (!UserSettings.PatientParam1.equals("")) {
                Param1_view.setText(UserSettings.PatientParam1);
            }
            if (!UserSettings.PatientParam2.equals("")) {
                Param2_view.setText(UserSettings.PatientParam2);
            }
            if (!UserSettings.PatientParam3.equals("")) {
                Param3_view.setText(UserSettings.PatientParam3);
            }
            if (!UserSettings.PatientParam4.equals("")) {
                Param4_view.setText(UserSettings.PatientParam4);
            }
            if (!UserSettings.PatientParam5.equals("")) {
                Param5_view.setText(UserSettings.PatientParam5);
            }
            if (!UserSettings.PatientParam6.equals("")) {
                Param6_view.setText(UserSettings.PatientParam6);
            }
            if (!UserSettings.PatientParam7.equals("")) {
                Param7_view.setText(UserSettings.PatientParam7);
            }
            if (!UserSettings.PatientParam8.equals("")) {
                Param8_view.setText(UserSettings.PatientParam8);
            }
            if (!UserSettings.PatientParam9.equals("")) {
                Param9_view.setText(UserSettings.PatientParam9);
            }
            if (!UserSettings.PatientParam10.equals("")) {
                Param10_view.setText(UserSettings.PatientParam10);
            }
            if (!UserSettings.PatientNOTES.equals("")) {
                Notes_view.setText(UserSettings.PatientNOTES);
            }
        }



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_settings, menu);
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

    int Graph1_Magic_MoteNum = -1; // 0= magic, 1-5= numero del mote
    int Graph1_SigNum = -1;
    int Graph1_AxesNum = -1;   // valido sia per acc che per ppg
    int Graph2_Magic_MoteNum = -1; // 0= magic, 1-5= numero del mote
    int Graph2_SigNum = -1;
    int Graph2_AxesNum = -1;   // valido sia per acc che per ppg
    int Graph3_Magic_MoteNum = -1; // 0= magic, 1-5= numero del mote
    int Graph3_SigNum = -1;
    int Graph3_AxesNum = -1;   // valido sia per acc che per ppg
    int Graph4_Magic_MoteNum = -1; // 0= magic, 1-5= numero del mote
    int Graph4_SigNum = -1;
    int Graph4_AxesNum = -1;   // valido sia per acc che per ppg

    private boolean Signal1Changed = false;
    private boolean Signal2Changed = false;
    private boolean Signal3Changed = false;
    private boolean Signal4Changed = false;

    //==========================================================================
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        //==========================================================================
        //***********************************************
        // selezionato spinner di primo livello
        if((Spinner)parent == spinner1){
            if(pos == 0) {   // selezionata magic
                if(UserSettings.EnabledSignal[0]<3 && UserSettings.EnabledSignal[0] >= 0) {
                    CreateNewSpinner(spinner1_1, Spinner2_magiclist, UserSettings.EnabledSignal[0]);
                    Graph1_Magic_MoteNum = 0;
                    UserSettings.EnabledSource[0] = Graph1_Magic_MoteNum;
                }else{
                    CreateNewSpinner(spinner1_1, Spinner2_magiclist, 0);
                    Graph1_Magic_MoteNum = 0;
                    UserSettings.EnabledSource[0] = Graph1_Magic_MoteNum;
                }
            }else{   // selezionati i mote
                CreateNewSpinner(spinner1_1,  Spinner2_motelist, UserSettings.EnabledSignal[0]);
                Graph1_Magic_MoteNum = pos;
                UserSettings.EnabledSource[0] =  Graph1_Magic_MoteNum;
            }

        } else if((Spinner)parent == spinner2){
            if(pos == 0) {   // selezionata magic
                if(UserSettings.EnabledSignal[1]<3 && UserSettings.EnabledSignal[1] >= 0) {
                    CreateNewSpinner(spinner2_1, Spinner2_magiclist, UserSettings.EnabledSignal[1]);
                    Graph2_Magic_MoteNum = 0;
                    UserSettings.EnabledSource[1] = Graph2_Magic_MoteNum;
                }else{
                    CreateNewSpinner(spinner2_1, Spinner2_magiclist, 0);
                    Graph2_Magic_MoteNum = 0;
                    UserSettings.EnabledSource[1] = Graph2_Magic_MoteNum;
                }
            }else {   // selezionati i mote
                CreateNewSpinner(spinner2_1, Spinner2_motelist, UserSettings.EnabledSignal[1] );
                Graph2_Magic_MoteNum = pos;
                UserSettings.EnabledSource[1] =  Graph2_Magic_MoteNum;
            }

        } else if((Spinner)parent == spinner3){
            if(pos == 0) {   // selezionata magic
                if(UserSettings.EnabledSignal[2]<3 && UserSettings.EnabledSignal[2] >= 0) {
                CreateNewSpinner(spinner3_1, Spinner2_magiclist, UserSettings.EnabledSignal[2]);
                Graph3_Magic_MoteNum = 0;
                UserSettings.EnabledSource[2] = Graph3_Magic_MoteNum;
                }else{
                    CreateNewSpinner(spinner3_1, Spinner2_magiclist, 0);
                    Graph3_Magic_MoteNum = 0;
                    UserSettings.EnabledSource[2] = Graph3_Magic_MoteNum;
                }
            }else {   // selezionati i mote
                CreateNewSpinner(spinner3_1, Spinner2_motelist, UserSettings.EnabledSignal[2] );
                Graph3_Magic_MoteNum = pos;
                UserSettings.EnabledSource[2] =  Graph3_Magic_MoteNum;
            }
        }else if((Spinner)parent == spinner4){
            if(pos == 0) {   // selezionata magic
                if(UserSettings.EnabledSignal[3]<3 && UserSettings.EnabledSignal[3] >= 0) {
                    CreateNewSpinner(spinner4_1, Spinner2_magiclist, UserSettings.EnabledSignal[3]);
                    Graph4_Magic_MoteNum = 0;
                    UserSettings.EnabledSource[3] = Graph4_Magic_MoteNum;
                }else{
                    CreateNewSpinner(spinner4_1, Spinner2_magiclist, 0);
                    Graph4_Magic_MoteNum = 0;
                    UserSettings.EnabledSource[3] = Graph4_Magic_MoteNum;
                }
            }else {   // selezionati i mote
                CreateNewSpinner(spinner4_1, Spinner2_motelist, UserSettings.EnabledSignal[3] );
                Graph4_Magic_MoteNum = pos;
                UserSettings.EnabledSource[3] =  Graph4_Magic_MoteNum;
            }
        }
        //***********************************************
    // selezionato spinner di secondo livello
        else if((Spinner)parent == spinner1_1) {
            Signal1Changed = true;
            switch (parent.getSelectedItem().toString()) {
                case "Acc":
                    CreateNewSpinner(spinner1_2, Spinner3_acclist, UserSettings.EnabledAxes[0]); //crea menu accelerometro
                    Graph1_SigNum = 0;
                    UserSettings.EnabledSignal[0] =  Graph1_SigNum;
                    break;
                case "Acc1":
                    CreateNewSpinner(spinner1_2, Spinner3_acclist,  UserSettings.EnabledAxes[0]); //crea menu accelerometro
                    Graph1_SigNum = 0;
                    UserSettings.EnabledSignal[0] =  Graph1_SigNum;
                    break;
                case "Acc2":
                    CreateNewSpinner(spinner1_2, Spinner3_acclist,  UserSettings.EnabledAxes[0]); //crea menu accelerometro
                    Graph1_SigNum = 1;
                    UserSettings.EnabledSignal[0] =  Graph1_SigNum;
                    break;
                case "Acc3":
                    CreateNewSpinner(spinner1_2, Spinner3_acclist,  UserSettings.EnabledAxes[0]); //crea menu accelerometro
                    Graph1_SigNum = 2;
                    UserSettings.EnabledSignal[0] =  Graph1_SigNum;
                    break;
                case "Acc4":
                    CreateNewSpinner(spinner1_2, Spinner3_acclist,  UserSettings.EnabledAxes[0]); //crea menu accelerometro
                    Graph1_SigNum = 3;
                    UserSettings.EnabledSignal[0] =  Graph1_SigNum;
                    break;
                case "Acc_mean":
                    CreateNewSpinner(spinner1_2, Spinner3_acclist,  UserSettings.EnabledAxes[0]); //crea menu accelerometro
                    Graph1_SigNum = 4;
                    UserSettings.EnabledSignal[0] =  Graph1_SigNum;
                    break;
                case "ECG":
                    spinner1_2.setAdapter(null);    //tolgo tuitto dallo spinner di terzo livello
                    Graph1_AxesNum = -1;
                    Graph1_SigNum = 1;
                    UserSettings.EnabledSignal[0] =  Graph1_SigNum;
                    break;
                case "Resp":
                    spinner1_2.setAdapter(null);
                    Graph1_AxesNum = -1;
                    Graph1_SigNum = 2;
                    UserSettings.EnabledSignal[0] =  Graph1_SigNum;
                    break;
                case "PPG":
                    Graph1_SigNum = 5;
                    if (UserSettings.EnabledAxes[0] < 2 && UserSettings.EnabledAxes[0] >= 0)
                        CreateNewSpinner(spinner1_2, Spinner3_ppglist,  UserSettings.EnabledAxes[0]); // crea menu ppg
                    else
                        CreateNewSpinner(spinner1_2, Spinner3_ppglist,  0); // crea menu ppg
                    UserSettings.EnabledSignal[0] =  Graph1_SigNum;
                    break;
            }
        }else if((Spinner)parent == spinner2_1){
            Signal2Changed = true;
                switch(parent.getSelectedItem().toString()) {
                    case "Acc":
                        CreateNewSpinner(spinner2_2, Spinner3_acclist,  UserSettings.EnabledAxes[1]); //crea menu accelerometro x magic
                        Graph2_SigNum = 0;
                        UserSettings.EnabledSignal[1] =  Graph2_SigNum;
                        break;
                    case "Acc1":
                        CreateNewSpinner(spinner2_2, Spinner3_acclist, UserSettings.EnabledAxes[1]); //crea menu accelerometro
                        Graph2_SigNum = 0;
                        UserSettings.EnabledSignal[1] =  Graph2_SigNum;
                        break;
                    case "Acc2":
                        CreateNewSpinner(spinner2_2, Spinner3_acclist, UserSettings.EnabledAxes[1]); //crea menu accelerometro
                        Graph2_SigNum = 1;
                        UserSettings.EnabledSignal[1] =  Graph2_SigNum;
                        break;
                    case "Acc3":
                        CreateNewSpinner(spinner2_2, Spinner3_acclist, UserSettings.EnabledAxes[1]); //crea menu accelerometro
                        Graph2_SigNum = 2;
                        UserSettings.EnabledSignal[1] =  Graph2_SigNum;
                        break;
                    case "Acc4":
                        CreateNewSpinner(spinner2_2, Spinner3_acclist, UserSettings.EnabledAxes[1]); //crea menu accelerometro
                        Graph2_SigNum = 3;
                        UserSettings.EnabledSignal[1] =  Graph2_SigNum;
                        break;
                    case "Acc_mean":
                        CreateNewSpinner(spinner2_2, Spinner3_acclist, UserSettings.EnabledAxes[1]); //crea menu accelerometro
                        Graph2_SigNum = 4;
                        UserSettings.EnabledSignal[1] =  Graph2_SigNum;
                        break;
                    case "ECG":
                        spinner2_2.setAdapter(null);
                        UserSettings.EnabledSignal[1] =  Graph2_SigNum;
                        Graph2_AxesNum = -1;
                        Graph2_SigNum = 1;
                        UserSettings.EnabledSignal[1] =  Graph2_SigNum;
                        break;
                    case "Resp":
                        spinner2_2.setAdapter(null);
                        Graph2_AxesNum = -1;
                        Graph2_SigNum = 2;
                        UserSettings.EnabledSignal[1] =  Graph2_SigNum;
                        break;
                    case "PPG":
                        Graph2_SigNum = 5;
                        if( UserSettings.EnabledAxes[1] >= 0 &&  UserSettings.EnabledAxes[1] <2)
                            CreateNewSpinner(spinner2_2, Spinner3_ppglist, UserSettings.EnabledAxes[1]); // crea menu ppg
                        else
                            CreateNewSpinner(spinner2_2, Spinner3_ppglist, 0); // crea menu ppg
                        UserSettings.EnabledSignal[1] =  Graph2_SigNum;
                        break;
                }
        }else if((Spinner)parent == spinner3_1) {
            Signal3Changed = true;
            switch (parent.getSelectedItem().toString()) {
                case "Acc":
                    CreateNewSpinner(spinner3_2, Spinner3_acclist, UserSettings.EnabledAxes[2]); //crea menu accelerometr
                    Graph3_SigNum = 0;
                    UserSettings.EnabledSignal[2] = Graph3_SigNum;
                    break;
                case "Acc1":
                    CreateNewSpinner(spinner3_2, Spinner3_acclist, UserSettings.EnabledAxes[2]); //crea menu accelerometro
                    Graph3_SigNum = 0;
                    UserSettings.EnabledSignal[2] = Graph3_SigNum;
                    break;
                case "Acc2":
                    CreateNewSpinner(spinner3_2, Spinner3_acclist, UserSettings.EnabledAxes[2]); //crea menu accelerometro
                    Graph3_SigNum = 1;
                    UserSettings.EnabledSignal[2] = Graph3_SigNum;
                    break;
                case "Acc3":
                    CreateNewSpinner(spinner3_2, Spinner3_acclist, UserSettings.EnabledAxes[2]); //crea menu accelerometro
                    Graph3_SigNum = 2;
                    UserSettings.EnabledSignal[2] = Graph3_SigNum;
                    break;
                case "Acc4":
                    CreateNewSpinner(spinner3_2, Spinner3_acclist, UserSettings.EnabledAxes[2]); //crea menu accelerometro
                    Graph3_SigNum = 3;
                    UserSettings.EnabledSignal[2] = Graph3_SigNum;
                    break;
                case "Acc_mean":
                    CreateNewSpinner(spinner3_2, Spinner3_acclist, UserSettings.EnabledAxes[2]); //crea menu accelerometro
                    Graph3_SigNum = 4;
                    UserSettings.EnabledSignal[2] = Graph3_SigNum;
                    break;
                case "ECG":
                    spinner3_2.setAdapter(null);
                    Graph3_AxesNum = -1;
                    Graph3_SigNum = 1;
                    UserSettings.EnabledSignal[2] = Graph3_SigNum;
                    break;
                case "Resp":
                    spinner3_2.setAdapter(null);
                    Graph3_AxesNum = -1;
                    Graph3_SigNum = 2;
                    UserSettings.EnabledSignal[2] = Graph3_SigNum;
                    break;
                case "PPG":
                    Graph3_SigNum = 5;
                    if (UserSettings.EnabledAxes[2] >= 0 && UserSettings.EnabledAxes[2] < 2)
                        CreateNewSpinner(spinner3_2, Spinner3_ppglist, UserSettings.EnabledAxes[2]); // crea menu ppg
                    else
                        CreateNewSpinner(spinner3_2, Spinner3_ppglist, 0); // crea menu ppg
                    UserSettings.EnabledSignal[2] = Graph3_SigNum;
                    break;
            }

        }else if((Spinner)parent == spinner4_1) {
            Signal4Changed = true;
            switch (parent.getSelectedItem().toString()) {
                case "Acc":
                    CreateNewSpinner(spinner4_2, Spinner3_acclist, UserSettings.EnabledAxes[3]); //crea menu accelerometr
                    Graph4_SigNum = 0;
                    UserSettings.EnabledSignal[3] =  Graph4_SigNum;
                    break;
                case "Acc1":
                    CreateNewSpinner(spinner4_2, Spinner3_acclist, UserSettings.EnabledAxes[3]); //crea menu accelerometro
                    Graph4_SigNum = 0;
                    UserSettings.EnabledSignal[3] =  Graph4_SigNum;
                    break;
                case "Acc2":
                    CreateNewSpinner(spinner4_2, Spinner3_acclist, UserSettings.EnabledAxes[3]); //crea menu accelerometro
                    Graph4_SigNum = 1;
                    UserSettings.EnabledSignal[3] =  Graph4_SigNum;
                    break;
                case "Acc3":
                    CreateNewSpinner(spinner4_2, Spinner3_acclist, UserSettings.EnabledAxes[3]); //crea menu accelerometro
                    Graph4_SigNum = 2;
                    UserSettings.EnabledSignal[3] =  Graph4_SigNum;
                    break;
                case "Acc4":
                    CreateNewSpinner(spinner4_2, Spinner3_acclist, UserSettings.EnabledAxes[3]); //crea menu accelerometro
                    Graph4_SigNum = 3;
                    UserSettings.EnabledSignal[3] =  Graph4_SigNum;
                    break;
                case "Acc_mean":
                    CreateNewSpinner(spinner4_2, Spinner3_acclist, UserSettings.EnabledAxes[3]); //crea menu accelerometro
                    Graph4_SigNum = 4;
                    UserSettings.EnabledSignal[3] =  Graph4_SigNum;
                    break;
                case "ECG":
                    spinner4_2.setAdapter(null);
                    Graph4_AxesNum = -1;
                    Graph4_SigNum = 1;
                    UserSettings.EnabledSignal[3] =  Graph4_SigNum;
                    break;
                case "Resp":
                    spinner4_2.setAdapter(null);
                    Graph4_AxesNum = -1;
                    Graph4_SigNum = 2;
                    UserSettings.EnabledSignal[3] =  Graph4_SigNum;
                    break;
                case "PPG":
                    Graph4_SigNum = 5;
                    if(UserSettings.EnabledAxes[3] >= 0 && UserSettings.EnabledAxes[3] < 2)
                        CreateNewSpinner(spinner4_2, Spinner3_ppglist, UserSettings.EnabledAxes[3]); // crea menu ppg
                    else
                        CreateNewSpinner(spinner4_2, Spinner3_ppglist, 0); // crea menu ppg
                    UserSettings.EnabledSignal[3] =  Graph4_SigNum;
                    break;
            }
            //***********************************************
            // selezionato spinner di terzo livello
        }else if((Spinner)parent == spinner1_2) {
            switch(parent.getSelectedItem().toString()){
                case "X":
                    Graph1_AxesNum = 0;
                    UserSettings.EnabledAxes[0] = Graph1_AxesNum;
                    break;
                case "Y":
                    Graph1_AxesNum = 1;
                    UserSettings.EnabledAxes[0] = Graph1_AxesNum;
                    break;
                case "Z":
                    Graph1_AxesNum = 2;
                    UserSettings.EnabledAxes[0] = Graph1_AxesNum;
                    break;
                case "IR":
                    Graph1_AxesNum = 1;
                    UserSettings.EnabledAxes[0] = Graph1_AxesNum;
                    break;
                case "RED":
                    Graph1_AxesNum = 0;
                    UserSettings.EnabledAxes[0] = Graph1_AxesNum;
                    break;
            }
        }else if((Spinner)parent == spinner2_2) {
            switch(parent.getSelectedItem().toString()){
                case "X":
                    Graph2_AxesNum = 0;
                    UserSettings.EnabledAxes[1] = Graph2_AxesNum;
                    break;
                case "Y":
                    Graph2_AxesNum = 1;
                    UserSettings.EnabledAxes[1] = Graph2_AxesNum;
                    break;
                case "Z":
                    Graph2_AxesNum = 2;
                    UserSettings.EnabledAxes[1] = Graph2_AxesNum;
                    break;
                case "IR":
                    Graph2_AxesNum = 1;
                    UserSettings.EnabledAxes[1] = Graph2_AxesNum;
                    break;
                case "RED":
                    Graph2_AxesNum = 0;
                    UserSettings.EnabledAxes[1] = Graph2_AxesNum;
                    break;
            }
        }else if((Spinner)parent == spinner3_2) {
            switch(parent.getSelectedItem().toString()){
                case "X":
                    Graph3_AxesNum = 0;
                    UserSettings.EnabledAxes[2] = Graph3_AxesNum;
                    break;
                case "Y":
                    Graph3_AxesNum = 1;
                    UserSettings.EnabledAxes[2] = Graph3_AxesNum;
                    break;
                case "Z":
                    Graph3_AxesNum = 2;
                    UserSettings.EnabledAxes[2] = Graph3_AxesNum;
                    break;
                case "IR":
                    Graph3_AxesNum = 1;
                    UserSettings.EnabledAxes[2] = Graph3_AxesNum;
                    break;
                case "RED":
                    Graph3_AxesNum = 0;
                    UserSettings.EnabledAxes[2] = Graph3_AxesNum;
                    break;
            }
        }else if((Spinner)parent == spinner4_2) {
            switch(parent.getSelectedItem().toString()){
                case "X":
                    Graph4_AxesNum = 0;
                    UserSettings.EnabledAxes[3] = Graph4_AxesNum;
                    break;
                case "Y":
                    Graph4_AxesNum = 1;
                    UserSettings.EnabledAxes[3] = Graph4_AxesNum;
                    break;
                case "Z":
                    Graph4_AxesNum = 2;
                    UserSettings.EnabledAxes[3] = Graph4_AxesNum;
                    break;
                case "IR":
                    Graph4_AxesNum = 1;
                    UserSettings.EnabledAxes[3] = Graph4_AxesNum;
                    break;
                case "RED":
                    Graph4_AxesNum = 0;
                    UserSettings.EnabledAxes[3] = Graph4_AxesNum;
                    break;
            }
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //==========================================================================
    private ArrayAdapter<String> CreateNewSpinner(Spinner spinnerToCreate, String[] printList, int selection){
        //==========================================================================
        //creo nuovo spinner con giusto elenco di voci
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, printList); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToCreate.setAdapter(spinnerArrayAdapter);
        spinnerToCreate.setSelection(selection);
        //spinnerToCreate.setOnItemSelectedListener(this); //spostato per permettere corretta inizializzazione di tutti gli spinner
        return spinnerArrayAdapter;
    }

    //==========================================================================
    private void SetSpinnerListeners() {
        //==========================================================================
        // inizializza tutti i listeners per gli spinner
        spinner1.setOnItemSelectedListener(this);
        spinner1_1.setOnItemSelectedListener(this);
        spinner1_2.setOnItemSelectedListener(this);

        spinner2.setOnItemSelectedListener(this);
        spinner2_1.setOnItemSelectedListener(this);
        spinner2_2.setOnItemSelectedListener(this);

        spinner3.setOnItemSelectedListener(this);
        spinner3_1.setOnItemSelectedListener(this);
        spinner3_2.setOnItemSelectedListener(this);

        spinner4.setOnItemSelectedListener(this);
        spinner4_1.setOnItemSelectedListener(this);
        spinner4_2.setOnItemSelectedListener(this);
    }


    private boolean AllowReturnToActivity = false;//TODO
    //==========================================================================
    public void ReturnToActivity1(View v) {
        //==========================================================================
        SetNewSettings();

        //if(AllowReturnToActivity) {
            Intent intent = new Intent();
            intent.putExtra("new_settings", UserSettings);
            setResult(RESULT_OK, intent);
            finish();
        //}
    }
    //==========================================================================
    private void SetNewSettings(){
        //==========================================================================
        // nome del file
        if(!filename_edit.getText().toString().equals(""))
            UserSettings.FileName += "_" + filename_edit.getText().toString();


        //INDIRIZZAMENTO SEGNALE SU CORRETTO PLOT
        //////////////////////////////////////
        // selezione segnale per il grafico 1
        //////////////////////////////////////
        if(Graph1_Magic_MoteNum == -1){//non selezionato niente, errore

        }else if(Graph1_Magic_MoteNum == 0){ // selezionata magic
            if(Graph1_SigNum == 0 && Graph1_AxesNum!= -1) {//acc
                UserSettings.MyMagic[0].MiniMagic.Acc.axes[Graph1_AxesNum] = 1; //mando questo segnale sul grafico 1
            }else if(Graph1_SigNum == 1 ) {//selezionato ecg
                UserSettings.MyMagic[0].MiniMagic.Ecg0 = 1;
            } else if(Graph1_SigNum == 2){//selezionato resp
                UserSettings.MyMagic[0].MiniMagic.Resp0 = 1;
            }

        }else if((Graph1_Magic_MoteNum != -1) && (Graph1_Magic_MoteNum <= 5) && Graph1_AxesNum!= -1){//selezionati i mote

            if (Graph1_SigNum == 0  ) {  //accelerometro 1
                UserSettings.MyMagic[0].Motes[Graph1_Magic_MoteNum-1].Acc[0].axes[Graph1_AxesNum] = 1;
            }else  if (Graph1_SigNum == 1 ) {   // acc2
                UserSettings.MyMagic[0].Motes[Graph1_Magic_MoteNum-1].Acc[1].axes[Graph1_AxesNum] = 1;
            }else if (Graph1_SigNum == 2  ) {//acc3
                UserSettings.MyMagic[0].Motes[Graph1_Magic_MoteNum-1].Acc[2].axes[Graph1_AxesNum] = 1;
            }else if (Graph1_SigNum == 3  ) {//acc4
                UserSettings.MyMagic[0].Motes[Graph1_Magic_MoteNum-1].Acc[3].axes[Graph1_AxesNum] = 1;
            }else if (Graph1_SigNum == 4  ) {//acc_media
                UserSettings.MyMagic[0].Motes[Graph1_Magic_MoteNum-1].Acc[4].axes[Graph1_AxesNum] = 1;
            }else if (Graph1_SigNum == 5  ) {//ppg
                UserSettings.MyMagic[0].Motes[Graph1_Magic_MoteNum-1].PPG.source[Graph1_AxesNum] = 1;
            }
        }
        //////////////////////////////////////
        // selezione segnale per il grafico 2
        //////////////////////////////////////
        if(Graph2_Magic_MoteNum == -1){//non selezionato niente, errore

        }else if(Graph2_Magic_MoteNum == 0){ // selezionata magic
            if(Graph2_SigNum == 0 && Graph2_AxesNum!= -1) {//acc
                UserSettings.MyMagic[1].MiniMagic.Acc.axes[Graph2_AxesNum] = 2; //mando questo segnale sul grafico 2
            }else if(Graph2_SigNum == 1 ) {//selezionato ecg
                UserSettings.MyMagic[1].MiniMagic.Ecg0 = 2;
            } else if(Graph2_SigNum == 2){//selezionato resp
                UserSettings.MyMagic[1].MiniMagic.Resp0 = 2;
            }

        }else if((Graph2_Magic_MoteNum != -1) && (Graph2_Magic_MoteNum <= 5) && Graph2_AxesNum!= -1){//selezionati i mote

            if (Graph2_SigNum == 0  ) {  //accelerometro 1
                UserSettings.MyMagic[1].Motes[Graph2_Magic_MoteNum-1].Acc[0].axes[Graph2_AxesNum] = 2;
            }else  if (Graph2_SigNum == 1 ) {   // acc2
                UserSettings.MyMagic[1].Motes[Graph2_Magic_MoteNum-1].Acc[1].axes[Graph2_AxesNum] = 2;
            }else if (Graph2_SigNum == 2  ) {//acc3
                UserSettings.MyMagic[1].Motes[Graph2_Magic_MoteNum-1].Acc[2].axes[Graph2_AxesNum] = 2;
            }else if (Graph2_SigNum == 3  ) {//acc4
                UserSettings.MyMagic[1].Motes[Graph2_Magic_MoteNum-1].Acc[3].axes[Graph2_AxesNum] = 2;
            }else if (Graph2_SigNum == 4  ) {//acc_media
                UserSettings.MyMagic[1].Motes[Graph2_Magic_MoteNum-1].Acc[4].axes[Graph2_AxesNum] = 2;
            }else if (Graph2_SigNum == 5  ) {//ppg
                UserSettings.MyMagic[1].Motes[Graph2_Magic_MoteNum-1].PPG.source[Graph2_AxesNum] = 2;
            }
        }

        //////////////////////////////////////
        // selezione segnale per il grafico 3
        //////////////////////////////////////
        if(Graph3_Magic_MoteNum == -1){//non selezionato niente, errore

        }else if(Graph3_Magic_MoteNum == 0){ // selezionata magic
            if(Graph3_SigNum == 0 && Graph3_AxesNum!= -1) {//acc
                UserSettings.MyMagic[2].MiniMagic.Acc.axes[Graph3_AxesNum] = 3; //mando questo segnale sul grafico 1
            }else if(Graph3_SigNum == 1 ) {//selezionato ecg
                UserSettings.MyMagic[2].MiniMagic.Ecg0 = 3;
            } else if(Graph3_SigNum == 2){//selezionato resp
                UserSettings.MyMagic[2].MiniMagic.Resp0 = 3;
            }

        }else if((Graph3_Magic_MoteNum != -1) && (Graph3_Magic_MoteNum <= 5) && Graph3_AxesNum!= -1){//selezionati i mote

            if (Graph3_SigNum == 0  ) {  //accelerometro 1
                UserSettings.MyMagic[2].Motes[Graph3_Magic_MoteNum-1].Acc[0].axes[Graph3_AxesNum] = 3;
            }else  if (Graph3_SigNum == 1 ) {   // acc2
                UserSettings.MyMagic[2].Motes[Graph3_Magic_MoteNum-1].Acc[1].axes[Graph3_AxesNum] = 3;
            }else if (Graph3_SigNum == 2  ) {//acc3
                UserSettings.MyMagic[2].Motes[Graph3_Magic_MoteNum-1].Acc[2].axes[Graph3_AxesNum] = 3;
            }else if (Graph3_SigNum == 3  ) {//acc4
                UserSettings.MyMagic[2].Motes[Graph3_Magic_MoteNum-1].Acc[3].axes[Graph3_AxesNum] = 3;
            }else if (Graph3_SigNum == 4  ) {//acc_media
                UserSettings.MyMagic[2].Motes[Graph3_Magic_MoteNum-1].Acc[4].axes[Graph3_AxesNum] = 3;
            }else if (Graph3_SigNum == 5  ) {//ppg
                UserSettings.MyMagic[2].Motes[Graph3_Magic_MoteNum-1].PPG.source[Graph3_AxesNum] = 3;
            }
        }


        //////////////////////////////////////
        // selezione segnale per il grafico 4
        //////////////////////////////////////
        if(Graph4_Magic_MoteNum == -1){//non selezionato niente, errore

        }else if(Graph4_Magic_MoteNum == 0){ // selezionata magic
            if(Graph4_SigNum == 0) {//acc
                UserSettings.MyMagic[3].MiniMagic.Acc.axes[Graph4_AxesNum] = 4; //mando questo segnale sul grafico 4
            }else if(Graph4_SigNum == 1 ) {//selezionato ecg
                UserSettings.MyMagic[3].MiniMagic.Ecg0 = 4;
            } else if(Graph4_SigNum == 2){//selezionato resp
                UserSettings.MyMagic[3].MiniMagic.Resp0 = 4;
            }

        }else if((Graph4_Magic_MoteNum != -1) && (Graph4_Magic_MoteNum <= 5) && Graph4_AxesNum!= -1){//selezionati i mote

            if (Graph4_SigNum == 0  ) {  //accelerometro 1
                UserSettings.MyMagic[3].Motes[Graph4_Magic_MoteNum-1].Acc[0].axes[Graph4_AxesNum] = 4;
            }else  if (Graph4_SigNum == 1 ) {   // acc2
                UserSettings.MyMagic[3].Motes[Graph4_Magic_MoteNum-1].Acc[1].axes[Graph4_AxesNum] = 4;
            }else if (Graph4_SigNum == 2  ) {//acc3
                UserSettings.MyMagic[3].Motes[Graph4_Magic_MoteNum-1].Acc[2].axes[Graph4_AxesNum] = 4;
            }else if (Graph4_SigNum == 3  ) {//acc4
                UserSettings.MyMagic[3].Motes[Graph4_Magic_MoteNum-1].Acc[3].axes[Graph4_AxesNum] = 4;
            }else if (Graph4_SigNum == 4  ) {//acc_media
                UserSettings.MyMagic[3].Motes[Graph4_Magic_MoteNum-1].Acc[4].axes[Graph4_AxesNum] = 4;
            }else if (Graph4_SigNum == 5  ) {//ppg
                UserSettings.MyMagic[3].Motes[Graph4_Magic_MoteNum-1].PPG.source[Graph4_AxesNum] = 4;
            }
        }

        // parametri del paziente e note
        if(!Param1_view.getText().toString().equals("")){
            UserSettings.PatientParam1 = Param1_view.getText().toString();
        }
        if(!Param2_view.getText().toString().equals("")){
            UserSettings.PatientParam2 = Param2_view.getText().toString();
        }
        if(!Param3_view.getText().toString().equals("")){
            UserSettings.PatientParam3 = Param3_view.getText().toString();
        }
        if(!Param4_view.getText().toString().equals("")){
            UserSettings.PatientParam4 = Param4_view.getText().toString();
        }
        if(!Param5_view.getText().toString().equals("")){
            UserSettings.PatientParam5 = Param5_view.getText().toString();
        }
        if(!Param6_view.getText().toString().equals("")){
            UserSettings.PatientParam6 = Param6_view.getText().toString();
        }
        if(!Param7_view.getText().toString().equals("")){
            UserSettings.PatientParam7 = Param7_view.getText().toString();
        }
        if(!Param8_view.getText().toString().equals("")){
            UserSettings.PatientParam8 = Param8_view.getText().toString();
        }
        if(!Param9_view.getText().toString().equals("")){
            UserSettings.PatientParam9 = Param9_view.getText().toString();
        }
        if(!Param10_view.getText().toString().equals("")){
            UserSettings.PatientParam10 = Param10_view.getText().toString();
        }
        if(!Notes_view.getText().toString().equals("")){
            UserSettings.PatientNOTES = Notes_view.getText().toString();
        }
    }

    //==========================================================================
    public void onCheckboxClicked(View view) {
        //==========================================================================
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkBox_developer:
                if (checked)
                    UserSettings.DeveloperMode = true;
                else
                    UserSettings.DeveloperMode = false;
                break;
            case R.id.checkBox_transfermode:
                if (checked)
                    UserSettings.isTransferModeEnabled = true;
                else
                    UserSettings.isTransferModeEnabled = false;
                break;

        }
    }


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

}
