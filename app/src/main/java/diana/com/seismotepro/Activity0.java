package diana.com.seismotepro;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Activity0 extends Activity {

    ImageView logo_view;

    Button GoToAcquire_Button;
    Button GoToAnalysis_Button;
    Button GoToFileTransm_Button;

    Settings sett;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_0);

        logo_view = (ImageView) findViewById(R.id.LogoView);
        GoToAcquire_Button = (Button)findViewById(R.id.goto_act1_button);
        GoToAnalysis_Button = (Button)findViewById(R.id.goto_act2_button);  // che in realtà non è più act 2 ma act settings
        GoToFileTransm_Button = (Button)findViewById(R.id.goto_act3_button);




        logo_view.setBackgroundResource(R.drawable.logo);

        sett = new Settings();
        // legge le impostazioni dal file di settings globale della app
        GlobalSettingsFileHandler app_sett = new GlobalSettingsFileHandler(sett);
        sett = app_sett.Read_AppSettingsInfo();
        if(sett == null){
            sett = new Settings();
            app_sett = new GlobalSettingsFileHandler(sett);
            app_sett.Write_AppSettingsInfo();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity0, menu);
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
    public void GoToActivity1(View v){
        //==========================================================================
        Intent intent = new Intent(Activity0.this, Activity1.class);
        //intent.putExtra("user", user_child);
        startActivity(intent);

    }
    //==========================================================================
    public void GoToActivity2(View v){
        //==========================================================================
        Intent intent = new Intent(Activity0.this, ActivityAnalysis.class);
        startActivity(intent);
    }
    //==========================================================================
    public void GoToActivity3(View v){
        //==========================================================================
        if(sett.isTransferModeEnabled) {
            Intent intent = new Intent(Activity0.this, Activity3.class);
            startActivity(intent);
        }else{
            call_toast("File Trasferring not enabled.");
            call_toast("Select another option.");
        }
    }
    //==========================================================================
    public void ExitFromApp(View v){
        //==========================================================================
        // chiude la app
        finish();
        System.exit(0);
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
