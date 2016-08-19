package diana.com.seismotepro;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Diana Scurati on 08/01/2016.
 */

//==========================================================================
public class myDropbox extends AsyncTask<Void, Long, Boolean> {
    //==========================================================================

    private Context AppContext;
    final static private String APP_KEY = "";
    final static private String APP_SECRET = "";
    private final static String DB_Folder = "Seismote_app";

    private DropboxAPI<AndroidAuthSession> mDBApi;

    private String AccessToken;
    private String FilePath;
    private String FileName;

    private File TheFile;

    //==========================================================================
    public myDropbox(Context cont){
        //==========================================================================
        AppContext = cont;


    }

    //==========================================================================
    public DropboxAPI<AndroidAuthSession> Connect(){
        //==========================================================================
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        return mDBApi;

    }

    //==========================================================================
    public void Send(String file_path, String file_name, String access_token) {
        //==========================================================================
        AccessToken = access_token;
        FilePath = file_path;
        FileName = file_name;
    }

    @Override
    //==========================================================================
    protected void onPreExecute() {
        //==========================================================================
        super.onPreExecute();
    }

    private boolean isThreadWorking = false;
    @Override
    //==========================================================================
    protected Boolean doInBackground(Void... params) {
        //==========================================================================

        isThreadWorking = true;

        TheFile = new File(FilePath + FileName);
        FileInputStream inputStream = null;
        if(isNetworkOnline()== true) {

            try {
                inputStream = new FileInputStream(TheFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            DropboxAPI.Entry response = null;
            try {
                response = mDBApi.putFile(DB_Folder + FileName, inputStream, TheFile.length(), null, null);
            } catch (DropboxException e) {
                e.printStackTrace();
            }
            /*if (response.bytes == TheFile.length())
            {
                return true;
            }
            else{

                return false;
            }*/
            isThreadWorking = false;
            return true;
        } else {
            isThreadWorking = false;
            return false;
        }

    }

    public boolean isThreadWorking(){ return isThreadWorking;}

    //==========================================================================
    private void showToast(String msg) {
        //==========================================================================
        Toast error = Toast.makeText(AppContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
    /*@Override
    //==========================================================================
    protected void onPostExecute(String res)
    //==========================================================================
    {

        if (res== null)
        {
            showToast("success!");
            delegate.processFinish(null);
            return;
        }
        else{
            delegate.processFinish(res);
            return;
        }

    }*/

    //==========================================================================
    public boolean isNetworkOnline() {
        //==========================================================================
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) AppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()== NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()== NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;

    }
}
