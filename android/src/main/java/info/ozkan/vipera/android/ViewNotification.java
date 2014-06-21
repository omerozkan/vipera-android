package info.ozkan.vipera.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class ViewNotification extends Activity {
    private TextView fieldNameTV;
    private TextView patientNameTV;
    private TextView phoneTV;
    private TextView mobilePhoneTV;
    private TextView fieldValueTV;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notification);

        fieldNameTV = (TextView) findViewById(R.id.vnFieldName);
        patientNameTV = (TextView) findViewById(R.id.vnPatientName);
        phoneTV = (TextView) findViewById(R.id.vnPhone);
        mobilePhoneTV = (TextView) findViewById(R.id.vnMobilePhone);
        fieldValueTV =  (TextView)findViewById(R.id.vnFieldValue);
        button = (Button) findViewById(R.id.closeButton);

        Intent intent = getIntent();
        updateScreen(intent);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void updateScreen(Intent intent) {

        fieldNameTV.setText(intent.getStringExtra("fieldName"));
        patientNameTV.setText(intent.getStringExtra("patientName"));
        fieldValueTV.setText(intent.getStringExtra("fieldValue")
                + " " + intent.getStringExtra("fieldUnit"));
        phoneTV.setText(intent.getStringExtra("phone"));
        mobilePhoneTV.setText(intent.getStringExtra("mobilePhone"));

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateScreen(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_notification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}
