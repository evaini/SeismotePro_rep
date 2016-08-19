package diana.com.seismotepro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//==========================================================================
public class DialogActivity extends Activity {
    //==========================================================================

    Button PositiveButton;
    Button NegativeButton;
    TextView HintView;

    String hint = "";
    String title = "";
    String Positive_string = "";
    String Negative_string = "";


    @Override
    //==========================================================================
    protected void onCreate(Bundle savedInstanceState) {
        //==========================================================================
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        PositiveButton = (Button)findViewById(R.id.button_ok);
        NegativeButton = (Button)findViewById(R.id.button_cancel);
        HintView = (TextView) findViewById(R.id.textView_dialog);

        Intent intent = getIntent();
        title = (String) intent.getSerializableExtra("title");
        hint = (String) intent.getSerializableExtra("hint");
        Positive_string = (String) intent.getSerializableExtra("pos");
        Negative_string = (String) intent.getSerializableExtra("neg");


        setTitle(title);
        HintView.setText(hint);
        if(!Positive_string.equals(""))
            PositiveButton.setText(Positive_string);
        if(!Negative_string.equals(""))
            NegativeButton.setText(Negative_string);



        this.setFinishOnTouchOutside(false);//evita chiusura attività se si tocca fuori dalla finestra

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dialog, menu);
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
    public void PositiveButtonClicked(View v){
        //==========================================================================
        Intent intent = new Intent();
        intent.putExtra("exit", true);
        setResult(RESULT_OK, intent);
        finish();

    }
    //==========================================================================
    public void NegativeButtonClicked(View v){
        //==========================================================================
        Intent intent = new Intent();
        intent.putExtra("exit", false);
        setResult(RESULT_OK, intent);
        finish();
    }

}
