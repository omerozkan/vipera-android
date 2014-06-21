package info.ozkan.vipera.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import info.ozkan.vipera.android.register.RegistrationProvider;
import info.ozkan.vipera.android.setting.Setting;


public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Context context;
    private Button button;
    private EditText apiKeyEditText;
    private EditText urlEditText;
    private ProgressBar progressBar;
    private GoogleCloudMessaging gcm;
    private AtomicInteger msgId = new AtomicInteger();
    private SharedPreferences prefs;
    private String registerId;
    private ProgressDialog mProgressDialog;


    public String apiKey;
    private String loginResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = getApplicationContext();

        // Check device for Play Services APK.
        checkPlayServices();
        apiKeyEditText = (EditText) findViewById(R.id.apiKeyEditText);
        urlEditText = (EditText) findViewById(R.id.urlEditText);
        button = (Button) findViewById(R.id.loginButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                apiKey = apiKeyEditText.getText().toString();
                new RegisterInBackground().execute();
            }
        });
    }


    private String registerToGCM() {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    registerId = gcm.register(Setting.SENDER_ID);
                    msg = "Device registered, registration ID=" + registerId;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    loginResult = sendRegistrationIdToBackend(apiKey);
                    Log.i(TAG, loginResult);
                    if(loginSuccess(loginResult)) {
                        storeRegistrationId(context, loginResult);
                    }
                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    return msg;
                }
                return loginResult;
    }

    private boolean loginSuccess(String loginResult) {
        return !loginResult.equals(RegistrationProvider.AUTHORIZATION_FAILED) && !loginResult.equals(RegistrationProvider.CONNECTION_FAILED);
    }

    private String sendRegistrationIdToBackend(String apiKey) {
        RegistrationProvider registrationProvider = new RegistrationProvider();
        registrationProvider.setApiKey(apiKey);
        registrationProvider.setRegisterId(registerId);
        registrationProvider.setUrl(urlEditText.getText().toString());
        return registrationProvider.register();
    }

    private void storeRegistrationId(Context context, String doctorName) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Setting.PROPERTY_REG_ID, registerId);
        editor.putInt(Setting.PROPERTY_APP_VERSION, appVersion);
        editor.putString(Setting.DOCTOR_NAME, doctorName);
        editor.putString(Setting.API_KEY, apiKey);
        editor.putString(Setting.VIPERA_URL, urlEditText.getText().toString());
        editor.commit();
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }

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
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private void checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                startActivity(new Intent(LoginActivity.this, Error.class));
                finish();
            }

        }
        ;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    private class RegisterInBackground extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(LoginActivity.this);
            mProgressDialog.setTitle("Bağlanıyor");
            mProgressDialog.setMessage("Sunucu ile bağlantı kurulmaktadır. Lütfen bekleyiniz!");
            mProgressDialog.setCancelable(false);// We can use for don't use
            // back button
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);// lock
            // screen
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = registerToGCM();
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result == null) {
                return;
            }
            if(result.equals(RegistrationProvider.CONNECTION_FAILED)) {
                Toast.makeText(getApplicationContext(), "Vipera ile bağlantı kurulamadı!" ,Toast.LENGTH_LONG).show();
            } else if(result.equals(RegistrationProvider.AUTHORIZATION_FAILED)) {
                Toast.makeText(getApplicationContext(), "Yetkilendirme hatası! Lütfen api anahtarınızı kontrol edin!", Toast.LENGTH_LONG).show();
            } else {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
            mProgressDialog.dismiss();

        }

    }

}
