package com.example.meelis.audiohammer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.example.meelis.audiohammer.recording.Client;

import java.io.IOException;

public class Main extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestRecordPermission();
        super.onCreate(savedInstanceState);
        System.out.println("Created");
        final Client client = new Client();
        setContentView(R.layout.activity_record);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        new ClientConnection(client).execute();
        final Button button = (Button) findViewById(R.id.startButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v,"Recording",Snackbar.LENGTH_LONG);
                if (client.isRecording()) {
                    button.setText(R.string.Start);

                    client.setRecording(false);
                    try {
                        client.stopRecording();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    //button.setBackgroundColor(Color.DKGRAY);
                    client.setRecording(true);
                    button.setText(R.string.Stop);
                    try {
                        client.startRecording();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return true;
    }
    private void requestRecordPermission() {
        //check API version, do nothing if API version < 23!
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
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
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public class ClientConnection extends AsyncTask<Void,Void,Void>{
        private final Client client;
        public ClientConnection(Client client) {
            this.client = client;
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                System.out.println("doInBackground");
                client.createConnection();
                client.setUsername("Mell");
                client.sendCommand("login");
                client.sendUsername("Mell","123456");
                client.sendCommand("Recording");
                client.sendCommand("filename");
                client.sendCommand("AndroidTest");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }
}
