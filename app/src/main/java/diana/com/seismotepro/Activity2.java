package diana.com.seismotepro;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

// ANALISI DEI DATI
public class Activity2 extends ListActivity {

    TextView FileName_view;
    Button ChooseAnotherFile_button;
    Button ToAnalisys_button;
    ListView FileList_view;

    String SeismotePath;
    String FileToBeAnalyzed;

    List<String> FileList;

    long LastModifiedFile_Time; // time in milliseconds
    int LastModifiedFile_Index;

    @Override
    //==========================================================================
    protected void onCreate(Bundle savedInstanceState) {
        //==========================================================================
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        FileName_view = (TextView)findViewById(R.id.textView_filename_1);
        ChooseAnotherFile_button = (Button)findViewById(R.id.button_choose);
        ToAnalisys_button = (Button)findViewById(R.id.button_gotoanalisys);
        FileList_view = (ListView)findViewById(android.R.id.list);

        ToAnalisys_button.setVisibility(View.INVISIBLE);
        ChooseAnotherFile_button.setVisibility(View.INVISIBLE);

        FileList = new ArrayList<>();

        SeismotePath = getExternalStorageDirectory().getAbsolutePath() + "/Seismote/";

        File Folder = new File(SeismotePath);
        File SeismoteFiles[] = Folder.listFiles();

        // cerco il file più recente
        int i;
        LastModifiedFile_Time = SeismoteFiles[0].lastModified();    // time in milliseconds since January 1st, 1970, midnight
        LastModifiedFile_Index = 0;

        for(i=0; i< SeismoteFiles.length; i++){

            String extension = SeismoteFiles[i].getName().substring((SeismoteFiles[i].getName().length()-3), (SeismoteFiles[i].getName().length()));
            extension = extension.toLowerCase();
            if(extension.equals("raw")) {  // se è un file raw
            FileList.add(SeismoteFiles[i].getName());   // aggiungo solo i file raw alla lista per doopo, se si vuole un file diverso

                if(SeismoteFiles[i].lastModified() > LastModifiedFile_Time && (SeismoteFiles[i].isFile())){    // ho trovato un file più recente
                    LastModifiedFile_Time = SeismoteFiles[i].lastModified();
                    LastModifiedFile_Index = i;
                }
            }
        }
        FileName_view.setText("Current File:\n" + SeismoteFiles[LastModifiedFile_Index].getName());
        FileToBeAnalyzed = SeismoteFiles[LastModifiedFile_Index].getName();

        ChooseAnotherFile(null);    // mostra in automatico la lista dei file nella cartella
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity2, menu);
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
/*
    //==========================================================================
    public void Act2_BackToActivity0(View v){
        //==========================================================================
        // chiude questa attività e torna alla attività 0
        Intent intent = new Intent(Activity2.this, Activity0.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }*/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation/keyboard change
        super.onConfigurationChanged(newConfig);
    }
    //==========================================================================
    public void ChooseAnotherFile(View v){
        //==========================================================================
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, FileList);
        FileList_view.setAdapter(arrayAdapter);
    }
    @Override
    //==========================================================================
    public void onListItemClick(ListView l, View v, int position, long id) {
        //==========================================================================
        // primo elemento: id = 0
        FileName_view.setText(FileList.get(position));
        FileToBeAnalyzed = FileList.get(position);
        GoToAnalisys(null);//torna indietro in automatico
    }
    //==========================================================================
    public void GoToAnalisys(View v){
        //==========================================================================
        // ritorna all'attività di analisi il nome del file da decodificare
        Intent intent = new Intent();
        intent.putExtra("new_filename", FileToBeAnalyzed);
        setResult(RESULT_OK, intent);
        finish();
    }
}






























