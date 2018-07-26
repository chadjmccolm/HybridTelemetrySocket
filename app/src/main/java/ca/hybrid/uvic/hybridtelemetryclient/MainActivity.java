package ca.hybrid.uvic.hybridtelemetryclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.Locale;
import java.lang.Math;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity {

    private Socket telemetry_server;

    //place holder spots where incoming data will be stored
    //then used to set various UI elements, or passed to different classes.
    CustomGauge CLT_Gauge;
    CustomGauge AFR_Gauge;
    CustomGauge AMS_Volts_Gauge;
    CustomGauge GLV_Volts_Gauge;
    TextView CLT_Value;
    TextView AFR_Value;
    TextView AMS_Volts_Value;
    TextView GLV_Volts_Value;

    public static final String MY_PREFS_NAME = "user_prefs";
    String server_address = "localhost";
    boolean server_connected = false;

    ColourMap CLT_Map = new ColourMap(new int[]{110, 150, 175, 188}, new String[]{"#EE9D40", "#FBE50B", "#30B32D", "#FF0000"});
    ColourMap AFR_Map = new ColourMap(new int[]{0, 105, 147}, new String[]{"#FBE50B", "#30B32D", "#FF0000"});
    ColourMap AMS_Volts_Map = new ColourMap(new int[]{0, 370, 400, 900, 975}, new String[]{"#FF0000", "#EE9D40", "#30B32D", "#EE9D40", "#FF0000"});
    ColourMap GLV_Volts_Map = new ColourMap(new int[]{0, 100, 143}, new String[]{"#FF0000", "#30B32D", "#FF0000"});

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //basic initialization.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Context context = getApplicationContext();

        //Set paths for incoming data.
        CLT_Gauge = findViewById(R.id.CLT_Gauge);
        AFR_Gauge = findViewById(R.id.AFR_Gauge);
        AMS_Volts_Gauge = findViewById(R.id.AMS_Volts_Gauge);
        GLV_Volts_Gauge = findViewById(R.id.GLV_Volts_Gauge);
        CLT_Value = findViewById(R.id.CLT_Value);
        AFR_Value = findViewById(R.id.AFR_Value);
        AMS_Volts_Value = findViewById(R.id.AMS_Volts_Value);
        GLV_Volts_Value = findViewById(R.id.GLV_Volts_Value);

        //grabs server address from prefs
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString("server", null);

        //if statement displays popup on startup for current server from prefs.
        if (restoredText != null) {
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, "Server: " + restoredText, duration).show();
            server_address = restoredText;

            // connect to server
            try {
                IO.Options options = new IO.Options();
                telemetry_server = IO.socket(server_address);
                Log.d("DEBUG", "Connected to server");
                server_connected = true;
            } catch (URISyntaxException e) {
                Log.d("ERROR", "Could not connect to server");
                Toast.makeText(context, "Could not connect to server", duration).show();
            }

            if(server_connected) {
                telemetry_server.on("tele_data", new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {

                        final JSONObject obj = (JSONObject) args[0];
                        Log.d("DEBUG", "Message Received");

                        String CLT_Incoming;
                        int CLT_Incoming_Int;
                        String CLT_Colour;
                        String AFR_Incoming;
                        int AFR_Incoming_Int;
                        String AFR_Colour;
                        String AMS_Volts_Incoming;
                        int AMS_Volts_Incoming_Int;
                        String AMS_Volts_Colour;
                        String GLV_Volts_Incoming;
                        int GLV_Volts_Incoming_Int;
                        String GLV_Volts_Colour;

                        try {

                            // Get the incoming CLT raw value from hybrid/engine/temperature
                            CLT_Incoming = obj.getString("hybrid/engine/temperature");

                            // Get the integer to send to the gauge and make sure it's within the bounds of the gauge
                            CLT_Incoming_Int = Math.round(Float.valueOf(CLT_Incoming));
                            CLT_Incoming_Int = Math.max(CLT_Incoming_Int, CLT_Gauge.getStartValue());
                            CLT_Incoming_Int = Math.min(CLT_Incoming_Int, CLT_Gauge.getEndValue());
                            CLT_Colour = CLT_Map.getColour(CLT_Incoming_Int);

                            // Format the full float value for the text display
                            CLT_Incoming = String.format(Locale.getDefault(), "%.1f", Float.valueOf(CLT_Incoming)) + "F";

                        } catch (org.json.JSONException e) {

                            // This will trigger if the JSON operation fails, this means it could not pull that data
                            CLT_Incoming = "N/A";
                            CLT_Incoming_Int = CLT_Gauge.getStartValue();
                            CLT_Colour = "#636363";

                        }

                        try {
                            AFR_Incoming = obj.getString("hybrid/engine/AFR");

                            AFR_Incoming_Int = Math.round(Float.valueOf(AFR_Incoming)*10);
                            AFR_Incoming_Int = Math.max(AFR_Incoming_Int, AFR_Gauge.getStartValue());
                            AFR_Incoming_Int = Math.min(AFR_Incoming_Int, AFR_Gauge.getEndValue());
                            AFR_Colour = CLT_Map.getColour(CLT_Incoming_Int);

                            AFR_Incoming = String.format(Locale.getDefault(), "%.1f", Float.valueOf(AFR_Incoming));
                        } catch (org.json.JSONException e) {
                            AFR_Incoming = "N/A";
                            AFR_Incoming_Int = AFR_Gauge.getStartValue();
                            AFR_Colour = "#636363";
                        }

                        try {
                            AMS_Volts_Incoming = obj.getString("hybrid/ams/voltage");

                            AMS_Volts_Incoming_Int = Math.round(Float.valueOf(AMS_Volts_Incoming)*10);
                            AMS_Volts_Incoming_Int = Math.max(AMS_Volts_Incoming_Int, AMS_Volts_Gauge.getStartValue());
                            AMS_Volts_Incoming_Int = Math.min(AMS_Volts_Incoming_Int, AMS_Volts_Gauge.getEndValue());
                            AMS_Volts_Colour = CLT_Map.getColour(CLT_Incoming_Int);

                            AMS_Volts_Incoming = String.format(Locale.getDefault(), "%.1f", Float.valueOf(AMS_Volts_Incoming)) + "V";
                        } catch (org.json.JSONException e) {
                            AMS_Volts_Incoming = "N/A";
                            AMS_Volts_Incoming_Int = AMS_Volts_Gauge.getStartValue();
                            AMS_Volts_Colour = "#636363";
                        }

                        try {
                            GLV_Volts_Incoming = obj.getString("hybrid/dash/GLVoltage");

                            GLV_Volts_Incoming_Int = Math.round(Float.valueOf(GLV_Volts_Incoming)*10);
                            GLV_Volts_Incoming_Int = Math.max(GLV_Volts_Incoming_Int, GLV_Volts_Gauge.getStartValue());
                            GLV_Volts_Incoming_Int = Math.min(GLV_Volts_Incoming_Int, GLV_Volts_Gauge.getEndValue());
                            GLV_Volts_Colour = CLT_Map.getColour(CLT_Incoming_Int);

                            GLV_Volts_Incoming = String.format(Locale.getDefault(), "%.1f", Float.valueOf(GLV_Volts_Incoming)) + "V";
                        } catch (org.json.JSONException e) {
                            GLV_Volts_Incoming = "N/A";
                            GLV_Volts_Incoming_Int = GLV_Volts_Gauge.getStartValue();
                            GLV_Volts_Colour = "#636363";
                        }

                        final String CLT_Incoming_Final = CLT_Incoming;
                        final int CLT_Incoming_Int_Final = CLT_Incoming_Int;
                        final String CLT_Colour_Final = CLT_Colour;
                        final String AFR_Incoming_Final = AFR_Incoming;
                        final int AFR_Incoming_Int_Final = AFR_Incoming_Int;
                        final String AFR_Colour_Final = AFR_Colour;
                        final String AMS_Volts_Incoming_Final = AMS_Volts_Incoming;
                        final int AMS_Volts_Incoming_Int_Final = AMS_Volts_Incoming_Int;
                        final String AMS_Volts_Colour_Final = AMS_Volts_Colour;
                        final String GLV_Volts_Incoming_Final = GLV_Volts_Incoming;
                        final int GLV_Volts_Incoming_Int_Final = GLV_Volts_Incoming_Int;
                        final String GLV_Volts_Colour_Final = GLV_Volts_Colour;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CLT_Value.setText(CLT_Incoming_Final);
                                CLT_Gauge.setValue(CLT_Incoming_Int_Final);
                                CLT_Gauge.setStrokeColor(Color.parseColor(CLT_Colour_Final));
                                AFR_Value.setText(AFR_Incoming_Final);
                                AFR_Gauge.setValue(AFR_Incoming_Int_Final);
                                AFR_Gauge.setStrokeColor(Color.parseColor(AFR_Colour_Final));
                                AMS_Volts_Value.setText(AMS_Volts_Incoming_Final);
                                AMS_Volts_Gauge.setValue(AMS_Volts_Incoming_Int_Final);
                                AMS_Volts_Gauge.setStrokeColor(Color.parseColor(AMS_Volts_Colour_Final));
                                GLV_Volts_Value.setText(GLV_Volts_Incoming_Final);
                                GLV_Volts_Gauge.setValue(GLV_Volts_Incoming_Int_Final);
                                GLV_Volts_Gauge.setStrokeColor(Color.parseColor(GLV_Volts_Colour_Final));
                            }
                        });

                    }
                });
                telemetry_server.connect();
            }

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_danger_zone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.select_server) {
            // get prompts.xml view
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            View promptView = layoutInflater.inflate(R.layout.select_server, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setView(promptView);

            final EditText editText = promptView.findViewById(R.id.server_address);
            // setup a dialog window
            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //creates message saying the server is set to "input"
                            int duration = Toast.LENGTH_SHORT;
                            Context context = getApplicationContext();
                            Toast.makeText(context, "Server set to: " + editText.getText(), duration).show();
                            SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                            //places the new server address in prefs
                            editor.putString("server", editText.getText().toString());
                            editor.apply();

                            // restart the application with the new server
                            Intent i = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);

                        }
                    })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create an alert dialog
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
