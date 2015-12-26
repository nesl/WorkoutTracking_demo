package edu.ucla.nesl.android.sixteenhours;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

/**
 * Created by timestring on 12/24/15.
 */
public class PlotPanelManager {
    private static final int HISTORY_SIZE = 500;

    private XYPlot plot;
    private TextView text1, text2;
    private Handler uiHandler;
    private String sensorName;

    private int numSamples = 0;

    private SimpleXYSeries[] series = new SimpleXYSeries[3];

    public PlotPanelManager(XYPlot _plot, TextView _text1, TextView _text2, Handler _uiHandler,
                            String _sensorName) {
        plot = _plot;
        text1 = _text1;
        text2 = _text2;
        uiHandler = _uiHandler;
        sensorName = _sensorName;
    }

    public void setup() {
        // setup the APR History plot:
        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        plot.getGraphWidget().getRangeSubGridLinePaint().setColor(Color.TRANSPARENT);

        series[0] = new SimpleXYSeries("X");
        series[1] = new SimpleXYSeries("Y");
        series[2] = new SimpleXYSeries("Z");
        for (SimpleXYSeries s : series)
            s.useImplicitXVals();

        plot.setRangeBoundaries(-20, 20, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        plot.addSeries(series[0], new LineAndPointFormatter(
                Color.rgb(100, 100, 200), Color.rgb(100, 100, 200), null, null));
        plot.addSeries(series[1], new LineAndPointFormatter(
                Color.rgb(100, 200, 100), Color.rgb(100, 200, 100), null, null));
        plot.addSeries(series[2], new LineAndPointFormatter(
                Color.rgb(200, 100, 100), Color.rgb(200, 100, 100), null, null));
        plot.setDomainStepValue(6);
        plot.setRangeStepValue(5);
        plot.setDomainLabel("Axis");
        plot.getDomainLabelWidget().pack();
        plot.setRangeLabel("Value");
        plot.getRangeLabelWidget().pack();
        plot.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void receiveData(long[] timestamps, float[][] data) {
        double freq = (timestamps.length - 1)
                / ((double)(timestamps[timestamps.length - 1] - timestamps[0]) / 1000.);
        numSamples += timestamps.length;
        String caption1 = String.format("[%s] Last receiveData: %s",
                sensorName, TimeString.currentTimeForDisplay());
        String caption2 = String.format("Freq: %.1fHz, total samples: %d(+%d)",
                freq, numSamples,timestamps.length);
        uiHandler.post(new Drawer(data, caption1, caption2));

        Log.i("receiveData", timestamps[timestamps.length - 1] + " - " + timestamps[0]);
    }

    private class Drawer implements Runnable {
        private float[][] data;
        private String caption1, caption2;

        public Drawer(float[][] _data, String _caption1, String _caption2) {
            data = _data;
            caption1 = _caption1;
            caption2 = _caption2;
        }

        @Override
        public void run() {
            for (int i = 0; i < data.length; i++) {
                if (series[0].size() == HISTORY_SIZE) {
                    for (SimpleXYSeries s : series)
                        s.removeFirst();
                }

                for (int j = 0; j < 3; j++)
                    series[j].addLast(null, data[i][j]);
            }
            text1.setText(caption1);
            text2.setText(caption2);
            plot.redraw();
        }
    };
}
