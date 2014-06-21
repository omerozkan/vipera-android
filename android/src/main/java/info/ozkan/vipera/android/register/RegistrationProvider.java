package info.ozkan.vipera.android.register;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import info.ozkan.vipera.android.json.AndroidRegistrationModel;
import info.ozkan.vipera.android.json.AndroidRegistrationResponseModel;

/**
 * Created by omer on 6/18/14.
 */
public class RegistrationProvider {

    public static final String AUTHORIZATION_FAILED = "AUTHORIZATION_FAILED";
    public static final String CONNECTION_FAILED = "CONNECTION_FAILED";
    private static final String TAG = "RegistrationProvider";
    public static final String SUCCESS = "SUCCESS";
    private String apiKey;
    private String registerId;
    private String url;
    private Gson gson = new Gson();


    public void setUrl(String url) {
        this.url = url;
    }


    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getRegisterId() {
        return registerId;
    }

    public String register() {
            String registerUrl = url + "/rest/android/register";
            InputStream inputStream = null;
            String result = "";
            try {

                HttpClient httpclient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(registerUrl);

                AndroidRegistrationModel registrationModel = new AndroidRegistrationModel();
                registrationModel.setApiKey(apiKey);
                registrationModel.setRegistrationId(registerId);

                String json = gson.toJson(registrationModel);

                StringEntity se = new StringEntity(json);

                httpPost.setEntity(se);

                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                HttpResponse httpResponse = httpclient.execute(httpPost);


                inputStream = httpResponse.getEntity().getContent();

                if(inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                    AndroidRegistrationResponseModel responseModel = gson.fromJson(result, AndroidRegistrationResponseModel.class);
                    Log.i(TAG, "Returned result from server " + result);
                    if(responseModel == null) {
                        Log.i(TAG, "Authorization failed!");
                        result = AUTHORIZATION_FAILED;
                    } else {
                        result = responseModel.getDoctorName();
                        Log.i(TAG, result + " has successfully login to notification service!");
                    }
                }
                else {
                    Log.i(TAG, "Could not connect to server!");
                    result = CONNECTION_FAILED;
                }

            } catch (Exception e) {
                Log.i(TAG, "Could not connect to server!");
                result = CONNECTION_FAILED;
                Log.d("InputStream", e.getLocalizedMessage());
            }
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public String unregister() {
        String registerUrl = url + "/rest/android/unregister";
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(registerUrl);

            AndroidRegistrationModel registrationModel = new AndroidRegistrationModel();
            registrationModel.setApiKey(apiKey);
            registrationModel.setRegistrationId(registerId);

            String json = gson.toJson(registrationModel);

            StringEntity se = new StringEntity(json);

            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpclient.execute(httpPost);


            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null) {
                Log.i(TAG, "Device unregistered from Vipera!");
                result = SUCCESS;
            }
            else {
                Log.i(TAG, "Could not connect to server!");
                result = CONNECTION_FAILED;
            }

        } catch (Exception e) {
            Log.i(TAG, "Could not connect to server!");
            result = CONNECTION_FAILED;
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }
}
