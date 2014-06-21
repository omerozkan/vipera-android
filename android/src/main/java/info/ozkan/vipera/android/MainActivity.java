package info.ozkan.vipera.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import info.ozkan.vipera.android.register.RegistrationProvider;
import info.ozkan.vipera.android.setting.Setting;


public class MainActivity extends Activity {
    private TextView doctorTextView;
    private Context context;
    private SharedPreferences prefs;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        doctorTextView = (TextView) findViewById(R.id.textView_doctorName);

        checkLogin();

    }

    private void checkLogin() {
        prefs = getGCMPreferences(context);
        if(prefs.contains(Setting.DOCTOR_NAME) && prefs.contains(Setting.API_KEY)) {
            String doctorName = prefs.getString(Setting.DOCTOR_NAME, "");
            doctorTextView.setText(doctorName);
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(Setting.APP_NAME,
                Context.MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.logout) {
            new UnregisterInBackground().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class UnregisterInBackground extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Bağlanıyor");
            mProgressDialog.setMessage("Vipera üzerindeki cihazınız silinmektedir. Lütfen bekleyiniz!");
            mProgressDialog.setCancelable(false);// We can use for don't use
            // back button
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);// lock
            // screen
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            return unregisterFromVipera();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals(RegistrationProvider.SUCCESS)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.commit();
                mProgressDialog.dismiss();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } else {
                Toast.makeText(getApplicationContext(), "Vipera ile bağlantı kurulamadı!", Toast.LENGTH_LONG).show();
                mProgressDialog.dismiss();
            }
        }
    }

    private String unregisterFromVipera() {
        String apiKey = prefs.getString(Setting.API_KEY, "");
        String registerId = prefs.getString(Setting.PROPERTY_REG_ID, "");
        String url = prefs.getString(Setting.VIPERA_URL, "");
        RegistrationProvider registrationProvider = new RegistrationProvider();
        registrationProvider.setApiKey(apiKey);
        registrationProvider.setRegisterId(registerId);
        registrationProvider.setUrl(url);
        return registrationProvider.unregister();
    }

}
