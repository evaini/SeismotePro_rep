package diana.com.seismotepro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.Serializable;

public class ActivityTimes extends AppCompatActivity {

    HeartIntervals Times; // riceve l'ingresso dall'attività
    Button BackButton;

    // edittext per la visualizzazione e la modifica dei valori temporali
    EditText Text_1_1, Text_1_2, Text_1_3, Text_1_4;
    EditText Text_2_1, Text_2_2, Text_2_3, Text_2_4;
    EditText Text_3_1, Text_3_2, Text_3_3, Text_3_4;
    EditText Text_4_1, Text_4_2, Text_4_3, Text_4_4;
    EditText Text_5_1, Text_5_2, Text_5_3, Text_5_4;
    EditText Text_6_1, Text_6_2, Text_6_3, Text_6_4;
    EditText Text_7_1, Text_7_2, Text_7_3, Text_7_4;
    EditText Text_8_1, Text_8_2, Text_8_3, Text_8_4;
    EditText Text_9_1, Text_9_2, Text_9_3, Text_9_4;
    EditText Text_10_1, Text_10_2, Text_10_3, Text_10_4;
    EditText Text_11_1, Text_11_2, Text_11_3, Text_11_4;
    EditText Text_12_1, Text_12_2, Text_12_3, Text_12_4;

    @Override
    //==========================================================================
    protected void onCreate(Bundle savedInstanceState) {
        //==========================================================================
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_times);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        BackButton = (Button)findViewById(R.id.button_ok_goback);

        Text_1_1 = (EditText)findViewById(R.id.editText_changeintervals_1_1);
        Text_1_2 = (EditText)findViewById(R.id.editText_changeintervals_1_2);
        Text_1_3 = (EditText)findViewById(R.id.editText_changeintervals_1_3);
        Text_1_4 = (EditText)findViewById(R.id.editText_changeintervals_1_4);

        Text_2_1 = (EditText)findViewById(R.id.editText_changeintervals_2_1);
        Text_2_2 = (EditText)findViewById(R.id.editText_changeintervals_2_2);
        Text_2_3 = (EditText)findViewById(R.id.editText_changeintervals_2_3);
        Text_2_4 = (EditText)findViewById(R.id.editText_changeintervals_2_4);

        Text_3_1 = (EditText)findViewById(R.id.editText_changeintervals_3_1);
        Text_3_2 = (EditText)findViewById(R.id.editText_changeintervals_3_2);
        Text_3_3 = (EditText)findViewById(R.id.editText_changeintervals_3_3);
        Text_3_4 = (EditText)findViewById(R.id.editText_changeintervals_3_4);

        Text_4_1 = (EditText)findViewById(R.id.editText_changeintervals_4_1);
        Text_4_2 = (EditText)findViewById(R.id.editText_changeintervals_4_2);
        Text_4_3 = (EditText)findViewById(R.id.editText_changeintervals_4_3);
        Text_4_4 = (EditText)findViewById(R.id.editText_changeintervals_4_4);

        Text_5_1 = (EditText)findViewById(R.id.editText_changeintervals_5_1);
        Text_5_2 = (EditText)findViewById(R.id.editText_changeintervals_5_2);
        Text_5_3 = (EditText)findViewById(R.id.editText_changeintervals_5_3);
        Text_5_4 = (EditText)findViewById(R.id.editText_changeintervals_5_4);

        Text_6_1 = (EditText)findViewById(R.id.editText_changeintervals_6_1);
        Text_6_2 = (EditText)findViewById(R.id.editText_changeintervals_6_2);
        Text_6_3 = (EditText)findViewById(R.id.editText_changeintervals_6_3);
        Text_6_4 = (EditText)findViewById(R.id.editText_changeintervals_6_4);

        Text_7_1 = (EditText)findViewById(R.id.editText_changeintervals_7_1);
        Text_7_2 = (EditText)findViewById(R.id.editText_changeintervals_7_2);
        Text_7_3 = (EditText)findViewById(R.id.editText_changeintervals_7_3);
        Text_7_4 = (EditText)findViewById(R.id.editText_changeintervals_7_4);

        Text_8_1 = (EditText)findViewById(R.id.editText_changeintervals_8_1);
        Text_8_2 = (EditText)findViewById(R.id.editText_changeintervals_8_2);
        Text_8_3 = (EditText)findViewById(R.id.editText_changeintervals_8_3);
        Text_8_4 = (EditText)findViewById(R.id.editText_changeintervals_8_4);

        Text_9_1 = (EditText)findViewById(R.id.editText_changeintervals_9_1);
        Text_9_2 = (EditText)findViewById(R.id.editText_changeintervals_9_2);
        Text_9_3 = (EditText)findViewById(R.id.editText_changeintervals_9_3);
        Text_9_4 = (EditText)findViewById(R.id.editText_changeintervals_9_4);

        Text_10_1 = (EditText)findViewById(R.id.editText_changeintervals_10_1);
        Text_10_2 = (EditText)findViewById(R.id.editText_changeintervals_10_2);
        Text_10_3 = (EditText)findViewById(R.id.editText_changeintervals_10_3);
        Text_10_4 = (EditText)findViewById(R.id.editText_changeintervals_10_4);

        Text_11_1 = (EditText)findViewById(R.id.editText_changeintervals_11_1);
        Text_11_2 = (EditText)findViewById(R.id.editText_changeintervals_11_2);
        Text_11_3 = (EditText)findViewById(R.id.editText_changeintervals_11_3);
        Text_11_4 = (EditText)findViewById(R.id.editText_changeintervals_11_4);

        Text_12_1 = (EditText)findViewById(R.id.editText_changeintervals_12_1);
        Text_12_2 = (EditText)findViewById(R.id.editText_changeintervals_12_2);
        Text_12_3 = (EditText)findViewById(R.id.editText_changeintervals_12_3);
        Text_12_4 = (EditText)findViewById(R.id.editText_changeintervals_12_4);


        Intent intent = getIntent();
        Times = (HeartIntervals) intent.getSerializableExtra("times");

        if (Times != null){

            Text_1_1.setText(Times.PEP_InfLim_Yellow + "");
            Text_1_2.setText(Times.PEP_InfLim_Green + "");
            Text_1_3.setText(Times.PEP_SupLim_Green + "");
            Text_1_4.setText(Times.PEP_SupLim_Green + "");

            Text_2_1.setText(Times.ICT_InfLim_Yellow + "");
            Text_2_2.setText(Times.ICT_InfLim_Green + "");
            Text_2_3.setText(Times.ICT_SupLim_Green + "");
            Text_2_4.setText(Times.ICT_SupLim_Green + "");

            Text_3_1.setText(Times.LVET_InfLim_Yellow + "");
            Text_3_2.setText(Times.LVET_InfLim_Green + "");
            Text_3_3.setText(Times.LVET_SupLim_Green + "");
            Text_3_4.setText(Times.LVET_SupLim_Green + "");

            Text_4_1.setText(Times.IRT_InfLim_Yellow + "");
            Text_4_2.setText(Times.IRT_InfLim_Green + "");
            Text_4_3.setText(Times.IRT_SupLim_Green + "");
            Text_4_4.setText(Times.IRT_SupLim_Green + "");

            Text_5_1.setText(Times.PTT_InfLim_Yellow + "");
            Text_5_2.setText(Times.PTT_InfLim_Green + "");
            Text_5_3.setText(Times.PTT_SupLim_Green + "");
            Text_5_4.setText(Times.PTT_SupLim_Green + "");

            Text_6_1.setText(Times.PAT_InfLim_Yellow + "");
            Text_6_2.setText(Times.PAT_InfLim_Green + "");
            Text_6_3.setText(Times.PAT_SupLim_Green + "");
            Text_6_4.setText(Times.PAT_SupLim_Green + "");

            Text_7_1.setText(Times.PR_InfLim_Yellow + "");
            Text_7_2.setText(Times.PR_InfLim_Green + "");
            Text_7_3.setText(Times.PR_SupLim_Green + "");
            Text_7_4.setText(Times.PR_SupLim_Green + "");

            Text_8_1.setText(Times.QT_InfLim_Yellow + "");
            Text_8_2.setText(Times.QT_InfLim_Green + "");
            Text_8_3.setText(Times.QT_SupLim_Green + "");
            Text_8_4.setText(Times.QT_SupLim_Green + "");

            Text_9_1.setText(Times.QRS_InfLim_Yellow + "");
            Text_9_2.setText(Times.QRS_InfLim_Green + "");
            Text_9_3.setText(Times.QRS_SupLim_Green + "");
            Text_9_4.setText(Times.QRS_SupLim_Green + "");

            Text_10_1.setText(Times.STI_InfLim_Yellow + "");
            Text_10_2.setText(Times.STI_InfLim_Green + "");
            Text_10_3.setText(Times.STI_SupLim_Green + "");
            Text_10_4.setText(Times.STI_SupLim_Green + "");

            Text_11_1.setText(Times.TEI_InfLim_Yellow + "");
            Text_11_2.setText(Times.TEI_InfLim_Green + "");
            Text_11_3.setText(Times.TEI_SupLim_Green + "");
            Text_11_4.setText(Times.TEI_SupLim_Green + "");

            Text_12_1.setText(Times.QTC_InfLim_Yellow + "");
            Text_12_2.setText(Times.QTC_InfLim_Green + "");
            Text_12_3.setText(Times.QTC_SupLim_Green + "");
            Text_12_4.setText(Times.QTC_SupLim_Green + "");

        }

    }


    //==========================================================================
    public void ReturnToAnalysis_AndGetNewTimes(View v){
        //==========================================================================
        Times.PEP_InfLim_Yellow = Double.parseDouble(Text_1_1.getText().toString());
        Times.PEP_InfLim_Green = Double.parseDouble(Text_1_2.getText().toString());
        Times.PEP_SupLim_Green = Double.parseDouble(Text_1_3.getText().toString());
        Times.PEP_SupLim_Green = Double.parseDouble(Text_1_4.getText().toString());

        Times.ICT_InfLim_Yellow = Double.parseDouble(Text_2_1.getText().toString());
        Times.ICT_InfLim_Green = Double.parseDouble(Text_2_2.getText().toString());
        Times.ICT_SupLim_Green = Double.parseDouble(Text_2_3.getText().toString());
        Times.ICT_SupLim_Green = Double.parseDouble(Text_2_4.getText().toString());

        Times.LVET_InfLim_Yellow = Double.parseDouble(Text_3_1.getText().toString());
        Times.LVET_InfLim_Green = Double.parseDouble(Text_3_2.getText().toString());
        Times.LVET_SupLim_Green = Double.parseDouble(Text_3_3.getText().toString());
        Times.LVET_SupLim_Green = Double.parseDouble(Text_3_4.getText().toString());

        Times.IRT_InfLim_Yellow = Double.parseDouble(Text_4_1.getText().toString());
        Times.IRT_InfLim_Green = Double.parseDouble(Text_4_2.getText().toString());
        Times.IRT_SupLim_Green = Double.parseDouble(Text_4_3.getText().toString());
        Times.IRT_SupLim_Green = Double.parseDouble(Text_4_4.getText().toString());

        Times.PTT_InfLim_Yellow = Double.parseDouble(Text_5_1.getText().toString());
        Times.PTT_InfLim_Green = Double.parseDouble(Text_5_2.getText().toString());
        Times.PTT_SupLim_Green = Double.parseDouble(Text_5_3.getText().toString());
        Times.PTT_SupLim_Green = Double.parseDouble(Text_5_4.getText().toString());

        Times.PAT_InfLim_Yellow = Double.parseDouble(Text_6_1.getText().toString());
        Times.PAT_InfLim_Green = Double.parseDouble(Text_6_2.getText().toString());
        Times.PAT_SupLim_Green = Double.parseDouble(Text_6_3.getText().toString());
        Times.PAT_SupLim_Green = Double.parseDouble(Text_6_4.getText().toString());

        Times.PR_InfLim_Yellow = Double.parseDouble(Text_7_1.getText().toString());
        Times.PR_InfLim_Green = Double.parseDouble(Text_7_2.getText().toString());
        Times.PR_SupLim_Green = Double.parseDouble(Text_7_3.getText().toString());
        Times.PR_SupLim_Green = Double.parseDouble(Text_7_4.getText().toString());

        Times.QT_InfLim_Yellow = Double.parseDouble(Text_8_1.getText().toString());
        Times.QT_InfLim_Green = Double.parseDouble(Text_8_2.getText().toString());
        Times.QT_SupLim_Green = Double.parseDouble(Text_8_3.getText().toString());
        Times.QT_SupLim_Green = Double.parseDouble(Text_8_4.getText().toString());

        Times.QRS_InfLim_Yellow = Double.parseDouble(Text_9_1.getText().toString());
        Times.QRS_InfLim_Green = Double.parseDouble(Text_9_2.getText().toString());
        Times.QRS_SupLim_Green = Double.parseDouble(Text_9_3.getText().toString());
        Times.QRS_SupLim_Green = Double.parseDouble(Text_9_4.getText().toString());

        Times.STI_InfLim_Yellow = Double.parseDouble(Text_10_1.getText().toString());
        Times.STI_InfLim_Green = Double.parseDouble(Text_10_2.getText().toString());
        Times.STI_SupLim_Green = Double.parseDouble(Text_10_3.getText().toString());
        Times.STI_SupLim_Green = Double.parseDouble(Text_10_4.getText().toString());

        Times.TEI_InfLim_Yellow = Double.parseDouble(Text_11_1.getText().toString());
        Times.TEI_InfLim_Green = Double.parseDouble(Text_11_2.getText().toString());
        Times.TEI_SupLim_Green = Double.parseDouble(Text_11_3.getText().toString());
        Times.TEI_SupLim_Green = Double.parseDouble(Text_11_4.getText().toString());

        Times.QTC_InfLim_Yellow = Double.parseDouble(Text_12_1.getText().toString());
        Times.QTC_InfLim_Green = Double.parseDouble(Text_12_2.getText().toString());
        Times.QTC_SupLim_Green = Double.parseDouble(Text_12_3.getText().toString());
        Times.QTC_SupLim_Green = Double.parseDouble(Text_12_4.getText().toString());

        Times.CreateFileWithTimes();



        Intent intent = new Intent();
        intent.putExtra("modified", (Serializable) Times);
        setResult(RESULT_OK, intent);
        finish();
    }

}
