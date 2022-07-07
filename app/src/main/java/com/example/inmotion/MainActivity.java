package com.example.inmotion;

import static java.lang.Math.abs;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private boolean color = false;

    private LineChart xChart;
    private LineChart yChart;
    private LineChart zChart;

    private Thread thread;
    private boolean plotData = true;

    private TextView viewSensors;
    private TextView viewX;
    private TextView viewY;
    private TextView viewZ;
    private TextView viewSteps;
    private TextView viewStepsDetector;
    private int steps = 0;
    private int totalSteps = 0;
    private int prevTotalSteps = 0;

    private long lastUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //viewSensors = findViewById(R.id.ViewSensors);
        viewX = findViewById(R.id.ViewX);
        viewY = findViewById(R.id.ViewY);
        viewZ = findViewById(R.id.ViewZ);
        viewSteps = findViewById(R.id.ViewSteps);
        viewStepsDetector = findViewById(R.id.ViewStepsDetector);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();

        sensorManager.registerListener((SensorEventListener) this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ALL),
                SensorManager.SENSOR_DELAY_NORMAL);


        this.xChart = (LineChart) findViewById(R.id.chartX);
        configureChart(xChart);

        this.yChart = (LineChart) findViewById(R.id.chartY);
        configureChart(yChart);

        this.zChart = (LineChart) findViewById(R.id.chartZ);
        configureChart(zChart);

        feedMultiple();
    }
    private void configureChart(LineChart chart){

        // enable description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        //xChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.TRANSPARENT);
        // add empty data
        chart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.EMPTY);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.TRANSPARENT);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.TRANSPARENT);
        leftAxis.setAxisMaximum(15f);
        leftAxis.setAxisMinimum(-15f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.setDrawBorders(false);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            totalSteps = (int) event.values[0];
            int currentSteps = totalSteps - prevTotalSteps;
            viewSteps.setText("Steps: " + currentSteps);
        }
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps++;
            viewStepsDetector.setText("Steps: " + steps);
        }


    }

    private void getStepCounter(SensorEvent event) {
        steps++;
        viewSteps.setText("Steps: " + steps);

    }
    private void getStepDetector(SensorEvent event) {
        float steps = event.values[0];
        long actualTime = event.timestamp;

        if(actualTime - lastUpdate > 50000000 //| abs(x - lastX) < 1
        ) {
            //Update step Count
            lastUpdate = actualTime;
            viewStepsDetector.setText("Steps:" + steps);
        }
    }


    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        //float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;
        float lastX = 0;
        if (actualTime - lastUpdate > 500000000 //| abs(x - lastX) < 1
        ){
            //Update x view
            lastX = x;
            lastUpdate = actualTime;
            viewX.setText("X Acceleration: \n" + x);

            //Update y view
            viewY.setText("Y Acceleration: \n" + y);

            //Update z view
            viewZ.setText("Z Acceleration: \n" + z);


            updateGraph(xChart, x);
            updateGraph(yChart, y);
            updateGraph(zChart, z);

        }
    }


    private void updateGraph(LineChart chart, float value) {

        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            /*if ( value >   chart.getAxisLeft().getAxisMaximum()){
                chart.getAxisLeft().setAxisMaximum(value);
            }

            if ( value <   chart.getAxisLeft().getAxisMinimum()){
                chart.getAxisLeft().setAxisMinimum(value);
            }*/

            data.addEntry(new Entry(set.getEntryCount(), value), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(10);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.CYAN);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }
    private void feedMultiple() {

        if (thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors

    }
}