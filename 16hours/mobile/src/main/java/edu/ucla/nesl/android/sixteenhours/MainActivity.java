package edu.ucla.nesl.android.sixteenhours;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class MainActivity extends AppCompatActivity implements ISensorDataReceiver {
    private static final String TAG = "MainActivity";

    private PlotPanelManager[] plotPanels = new PlotPanelManager[2];

    private SimpleXYSeries[] gyroSeries = new SimpleXYSeries[3];

    private Handler handler = new Handler();

    private SensorDataProvider sensorDataProvider = null;

    private TextView clockText;
    private String batteryAuxCaption = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // setup the History plot:
        XYPlot accPlot = (XYPlot) findViewById(R.id.accPlot);
        TextView accText1 = (TextView) findViewById(R.id.accCaption1);
        TextView accText2 = (TextView) findViewById(R.id.accCaption2);
        plotPanels[0] = new PlotPanelManager(accPlot, accText1, accText2, handler, "Accelerometer");

        XYPlot gyroPlot = (XYPlot) findViewById(R.id.gyroPlot);
        TextView gyroText1 = (TextView) findViewById(R.id.gyroCaption1);
        TextView gyroText2 = (TextView) findViewById(R.id.gyroCaption2);
        plotPanels[1] = new PlotPanelManager(gyroPlot, gyroText1, gyroText2, handler, "Gyroscope");

        for (PlotPanelManager panel : plotPanels)
            panel.setup();

        //sensorDataProvider = new LocalSensorDataProvider(this, getApplicationContext());
        sensorDataProvider = new WearSensorDataProvider(this, getApplicationContext());

        clockText = (TextView) findViewById(R.id.time);

        handler.postDelayed(clockUpdateProcedure, 1000);

        // setup google client to receiveData information from wear
        //IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        //MessageReceiver messageReceiver = new MessageReceiver();
        //LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onStop() {
        super.onStop();
        sensorDataProvider.invalidate();
        handler.removeCallbacks(clockUpdateProcedure);
    }

    @Override
    public void receiveData(int type, long[] timestamps, float[][] data) {
        PlotPanelManager panel = plotPanels[type];
        panel.receiveData(timestamps, data);
    }

    @Override
    public void receiveBatteryLevel(int level) {
        batteryAuxCaption = ", watch battery " + level + "%";
    }

    private Runnable clockUpdateProcedure = new Runnable() {

        @Override
        public void run() {
            String str = String.format("Current time: %s%s",
                    TimeString.currentTimeForDisplay(), batteryAuxCaption);
            clockText.setText(str);
            handler.postDelayed(clockUpdateProcedure, 1000);
        }
    };
}
