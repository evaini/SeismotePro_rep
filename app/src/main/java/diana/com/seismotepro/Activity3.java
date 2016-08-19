package diana.com.seismotepro;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import java.io.File;
import java.util.ArrayList;

import static android.os.Environment.getExternalStorageDirectory;

// INVIO DI FILE
public class Activity3 extends Activity {

    String Times_FileName = "";
    String ECG_FileName = "";
    String SEISMO_FileName = "";
    String PLETH_FileName = "";
    private String FolderPath;

    TextView Send1;
    TextView Send2;
    TextView Send3;
    TextView Send4;

    EditText EmailSubject;
    EditText EmailText;

    // per dropbox
    myDropbox DropboxSender;
    DropboxAPI<AndroidAuthSession> mDBApi = null;
    String accessToken;
//    SharedPreferences mySharedPreferences;

    @Override
    //==========================================================================
    protected void onCreate(Bundle savedInstanceState) {
        //==========================================================================
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);

        Intent intent = getIntent();
        Times_FileName = (String)intent.getSerializableExtra("tempi");
        ECG_FileName = (String)intent.getSerializableExtra("ecg");
        SEISMO_FileName = (String)intent.getSerializableExtra("seismo");
        PLETH_FileName = (String)intent.getSerializableExtra("pleth");

        EmailSubject = (EditText)findViewById(R.id.editText_emailsubj);
        EmailText = (EditText)findViewById(R.id.editText_emailtext);

        Send1 = (TextView)findViewById(R.id.textView_invia1);
        Send2 = (TextView)findViewById(R.id.textView_invia2);
        Send3 = (TextView)findViewById(R.id.textView_invia3);
        Send4 = (TextView)findViewById(R.id.textView_invia4);

        Send1.setText(Times_FileName);
        Send2.setText(ECG_FileName);
        Send3.setText(SEISMO_FileName);
        Send4.setText(PLETH_FileName);

        FolderPath = getExternalStorageDirectory().getAbsolutePath() + "/Seismote/";

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


    }
    @Override
    //==========================================================================
    protected void onResume() {
        //==========================================================================
        super.onResume();

        if(mDBApi!=null) {
            // se stiamo ritornando dall'autenticazione dropbox
            if (mDBApi.getSession().authenticationSuccessful()) {
                try {
                    // Required to complete auth, sets the access token on the session
                    mDBApi.getSession().finishAuthentication();

                    accessToken = mDBApi.getSession().getOAuth2AccessToken();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity3, menu);
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
    //==========================================================================
    public void Act3_BackToActivity0(View v){
        //==========================================================================
        // chiude questa attività e torna alla attività 0
        Intent intent = new Intent(Activity3.this, Activity0.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    //==========================================================================
    public void SendEmail(View v) {
        //==========================================================================
        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"seismote@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, EmailSubject.getText());
        i.putExtra(Intent.EXTRA_TEXT, EmailText.getText());

        // allegati
        ArrayList<Uri> uris = new ArrayList<Uri>();

        if(!ECG_FileName.equals("")) {
            File file = new File(FolderPath + ECG_FileName);
            if (file.exists() && file.canRead()) {
                Uri uri = Uri.fromFile(file);
                uris.add(uri);
            }
        }
        if(!Times_FileName.equals("")) {
            File file = new File(FolderPath + Times_FileName);
            if (file.exists() && file.canRead()) {
                Uri uri = Uri.fromFile(file);
                uris.add(uri);
            }
        }
        if(!SEISMO_FileName.equals("")) {
            File file = new File(FolderPath + SEISMO_FileName);
            if (file.exists() && file.canRead()) {
                Uri uri = Uri.fromFile(file);
                uris.add(uri);
            }
        }
        if(!PLETH_FileName.equals("")) {
            File file = new File(FolderPath + PLETH_FileName);
            if (file.exists() && file.canRead()) {
                Uri uri = Uri.fromFile(file);
                uris.add(uri);
            }
        }
        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);


        /*i.putExtra(Intent.EXTRA_STREAM, ECG_FileName);
        i.putExtra(Intent.EXTRA_STREAM, SEISMO_FileName);
        i.putExtra(Intent.EXTRA_STREAM, PLETH_FileName);
        i.putExtra(Intent.EXTRA_STREAM, Times_FileName);*/
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException e) {
            e.printStackTrace();
            call_toast("error send");
           //Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    //==========================================================================
    public void SendDropbox(View v) {
        //==========================================================================
        DropboxSender = new myDropbox(getApplicationContext());
        mDBApi = DropboxSender.Connect();
        mDBApi.getSession().startOAuth2Authentication(Activity3.this);//fa partire autenticazione dropbox
        if(!Times_FileName.equals("")) {
            while(DropboxSender.isThreadWorking()){}
            DropboxSender.Send(FolderPath, Times_FileName, accessToken);
            DropboxSender.execute();
        }
        /*if(!ECG_FileName.equals("")) {
            while(DropboxSender.isThreadWorking()){}
            DropboxSender.Send(FolderPath, ECG_FileName, accessToken);
            DropboxSender.execute();
        }
        if(!SEISMO_FileName.equals("")) {
            while(DropboxSender.isThreadWorking()){}
            DropboxSender.Send(FolderPath, SEISMO_FileName, accessToken);
            DropboxSender.execute();
        }
        if(!PLETH_FileName.equals("")) {
            while(DropboxSender.isThreadWorking()){}
            DropboxSender.Send(FolderPath, PLETH_FileName, accessToken);
            DropboxSender.execute();
        }*/


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // ignore orientation/keyboard change
        super.onConfigurationChanged(newConfig);
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
