package com.softbankrobotics.pepperapptemplate.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.softbankrobotics.pepperapptemplate.MainActivity;
import com.softbankrobotics.pepperapptemplate.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class MuseFragment extends Fragment {
    private static final String TAG = "MSI_MuseFragment";
    private MainActivity ma;
    private WebSocket webSocket;
    private Button btnStart;
    private Button btnPause;
    private Button btnEnd;
    private LineChart chart;
    private SSLContext sslContext = null;
    private TextView TextView_delta;
    private TextView TextView_theta;
    private TextView TextView_alpha;
    private TextView TextView_beta;
    private TextView TextView_gamma;
    private Switch deltaSwitch;
    private Switch thetaSwitch;
    private Switch alphaSwitch;
    private Switch betaSwitch;
    private Switch gammaSwitch;
    private boolean plotData = true;
    private LineDataSet deltaSet;
    private LineDataSet thetaSet;
    private LineDataSet alphaSet;
    private LineDataSet betaSet;
    private LineDataSet gammaSet;
    private static final int deltaIndex = 0;
    private static final int thetaIndex = 1;
    private static final int alphaIndex = 2;
    private static final int betaIndex = 3;
    private static final int gammaIndex = 4;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        //Inicializacion del fragmento
        Log.d(TAG, "onCreateView start");
        int fragmentId = R.layout.fragment_muse;
        this.ma = (MainActivity) getActivity();
        if (ma != null) {
            Integer themeId = ma.getThemeId();
            if (themeId != null) {
                final Context contextThemeWrapper = new ContextThemeWrapper(ma, themeId);
                LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
                return localInflater.inflate(fragmentId, container, false);
            } else {
                return inflater.inflate(fragmentId, container, false);
            }
        } else {
            Log.e(TAG, "could not get mainActivity, can't create fragment");
            return null;
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onViewCreated start");

        //Indicamos que debe confiar en las conexiones
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        // Inicializar el contexto SSL con el TrustManager personalizado
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try {
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        btnStart = view.findViewById(R.id.btnStart);
        btnPause = view.findViewById(R.id.btnPause);
        btnEnd = view.findViewById(R.id.btnEnd);
        TextView_delta = view.findViewById(R.id.textView_delta);
        TextView_theta = view.findViewById(R.id.textView_theta);
        TextView_alpha = view.findViewById(R.id.textView_alpha);
        TextView_beta = view.findViewById(R.id.textView_beta);
        TextView_gamma = view.findViewById(R.id.textView_gamma);
        deltaSwitch = view.findViewById(R.id.deltaSwitch);
        thetaSwitch = view.findViewById(R.id.thetaSwitch);
        alphaSwitch = view.findViewById(R.id.alphaSwitch);
        betaSwitch = view.findViewById(R.id.betaSwitch);
        gammaSwitch = view.findViewById(R.id.gammaSwitch);
        chart = view.findViewById(R.id.chart1);

        //Inicializacion de los switches
        deltaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    deltaSet.setVisible(true);
                } else {
                    deltaSet.setVisible(false);
                }
            }
        });

        thetaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    thetaSet.setVisible(true);
                } else {
                    thetaSet.setVisible(false);
                }
            }
        });

        alphaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    alphaSet.setVisible(true);
                } else {
                    alphaSet.setVisible(false);
                }
            }
        });

        betaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    betaSet.setVisible(true);
                } else {
                    betaSet.setVisible(false);
                }
            }
        });

        gammaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    gammaSet.setVisible(true);
                } else {
                    gammaSet.setVisible(false);
                }
            }
        });

        init(); //Se inicializa el grafico
        EEGController(); //Se inicia la conexion con la Muse

        Log.d(TAG, "onViewCreated end");

    }

    public void EEGController() {

        //IMPORTANTE cambiar la URL y la ID dependiendo de la sesion de Naxon Explorer
        String url = "wss://naxonlabs.com/api/v1/action_cable?token=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjozNDEsImV4cCI6MTczMzIzNzk0Nn0.m4__MGLqG063qplD_cJONRYz5_ks1jFEnXIdYuUu5ms";
        int session_id = 2092;
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), new MyTrustManager())
                .build();
        Log.d(TAG, "Request");
        Request request = new Request.Builder().url(url).build(); //Solicitud conexion WebSockets

        //Manejador de la conexion
        WebSocketListener listener = new WebSocketListener() {
            //Cuando se abre la conexion envia el mensaje de suscripcion al servidor
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "Conexión WebSocket abierta");
                webSocket.send("{\"command\":\"subscribe\",\"identifier\":\"{\\\"channel\\\":\\\"RecordingsChannel\\\",\\\"room\\\":\\\"RecordingsRoom\\\",\\\"session_id\\\":\\\"" + session_id + "\\\"}\"}");
            }

            //Gestiona los mensajes recibidos
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                //Log.d(TAG, "Mensaje recibido: " + text);

                try {
                    JSONObject jsonObject = new JSONObject(text);

                    JSONObject message = jsonObject.getJSONObject("message");
                    JSONObject data = message.getJSONObject("message").getJSONObject("data");

                    JSONObject tp9 = data.getJSONObject("TP9");
                    JSONObject af7 = data.getJSONObject("AF7");
                    JSONObject af8 = data.getJSONObject("AF8");
                    JSONObject tp10 = data.getJSONObject("TP10");

                    double delta_tp9 = tp9.getDouble("delta");
                    double theta_tp9 = tp9.getDouble("theta");
                    double alpha_tp9 = tp9.getDouble("alpha");
                    double beta_tp9 = tp9.getDouble("beta");
                    double gamma_tp9 = tp9.getDouble("gamma");

                    double delta_af7 = af7.getDouble("delta");
                    double theta_af7 = af7.getDouble("theta");
                    double alpha_af7 = af7.getDouble("alpha");
                    double beta_af7 = af7.getDouble("beta");
                    double gamma_af7 = af7.getDouble("gamma");

                    double delta_af8 = af8.getDouble("delta");
                    double theta_af8 = af8.getDouble("theta");
                    double alpha_af8 = af8.getDouble("alpha");
                    double beta_af8 = af8.getDouble("beta");
                    double gamma_af8 = af8.getDouble("gamma");

                    double delta_tp10 = tp10.getDouble("delta");
                    double theta_tp10 = tp10.getDouble("theta");
                    double alpha_tp10 = tp10.getDouble("alpha");
                    double beta_tp10 = tp10.getDouble("beta");
                    double gamma_tp10 = tp10.getDouble("gamma");

                    double delta = (delta_af7 + delta_af8 + delta_tp9 + delta_tp10) / 4;
                    double theta = (theta_af7 + theta_af8 + theta_tp9 + theta_tp10) / 4;
                    double alpha = (alpha_af7 + alpha_af8 + alpha_tp9 + alpha_tp10) / 4;
                    double beta = (beta_af7 + beta_af8 + beta_tp9 + beta_tp10) / 4;
                    double gamma = (gamma_af7 + gamma_af8 + gamma_tp9 + gamma_tp10 ) / 4;

                    ma.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView_delta.setText("DELTA: " + String.format("%.3f",delta) + "µV");
                            TextView_theta.setText("THETA: " + String.format("%.3f",theta) + "µV");
                            TextView_alpha.setText("ALPHA: " + String.format("%.3f",alpha) + "µV");
                            TextView_beta.setText("BETA: " + String.format("%.3f",beta) + "µV");
                            TextView_gamma.setText("GAMMA: " + String.format("%.3f",gamma) + "µV");
                        }
                    });

                    //Se mandan a graficar los datos obtenidos
                    if(plotData){
                        addEntry(delta, theta, alpha, beta, gamma);
                        plotData = false;
                    }

/*                    Log.d(TAG, "DELTA: " + delta);
                    Log.d(TAG, "THETA: " + theta);
                    Log.d(TAG, "ALPHA: " + alpha);
                    Log.d(TAG, "BETA: " + beta);
                    Log.d(TAG, "GAMMA: " + gamma);*/



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Conexión WebSocket cerrada: " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "Error en la conexión WebSocket: " + t.getMessage());
                t.printStackTrace();
            }
        };

        //Botones para enviar mensajes al servidor de incio, pausa y finalizacion de la sesion

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webSocket.send("{\"command\":\"message\",\"identifier\":\"{\\\"channel\\\":\\\"RecordingsChannel\\\",\\\"room\\\":\\\"RecordingsRoom\\\",\\\"session_id\\\":\\\""+ session_id +"\\\"}\",\"data\":\"{\\\"change_status\\\":\\\"start_recording\\\",\\\"action\\\":\\\"change_recording_status\\\"}\"}");
                Log.d(TAG, "MENSAJE ENVIADO");
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webSocket.send("{\"command\":\"message\",\"identifier\":\"{\\\"channel\\\":\\\"RecordingsChannel\\\",\\\"room\\\":\\\"RecordingsRoom\\\",\\\"session_id\\\":\\\""+ session_id +"\\\"}\",\"data\":\"{\\\"change_status\\\":\\\"stop_recording\\\",\\\"action\\\":\\\"change_recording_status\\\"}\"}");
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webSocket.send("{\"command\":\"message\",\"identifier\":\"{\\\"channel\\\":\\\"RecordingsChannel\\\",\\\"room\\\":\\\"RecordingsRoom\\\",\\\"session_id\\\":\\\""+ session_id +"\\\"}\",\"data\":\"{\\\"change_status\\\":\\\"end_recording\\\",\\\"action\\\":\\\"change_recording_status\\\"}\"}");
            }
        });

        webSocket = client.newWebSocket(request, listener);

    }

    public void closeConnection() {
        webSocket.close(1000, "User initiated");
    }

    //MPAndroidChart

    //Mediante este metodo se introducen los datos en el grafico
    private void addEntry(double delta, double theta, double alpha, double beta, double gamma) {

        LineData data = chart.getData();

        if (data != null) {

            deltaSet = (LineDataSet) data.getDataSetByIndex(deltaIndex);
            thetaSet = (LineDataSet) data.getDataSetByIndex(thetaIndex);
            alphaSet = (LineDataSet) data.getDataSetByIndex(alphaIndex);
            betaSet = (LineDataSet) data.getDataSetByIndex(betaIndex);
            gammaSet = (LineDataSet) data.getDataSetByIndex(gammaIndex);

            if(deltaSet == null){
                deltaSet = createSet(deltaIndex);
                data.addDataSet(deltaSet);
            }

            if(thetaSet == null){
                thetaSet = createSet(thetaIndex);
                data.addDataSet(thetaSet);
            }

            if(alphaSet == null){
                alphaSet = createSet(alphaIndex);
                data.addDataSet(alphaSet);
            }

            if(betaSet == null){
                betaSet = createSet(betaIndex);
                data.addDataSet(betaSet);
            }

            if(gammaSet == null){
                gammaSet = createSet(gammaIndex);
                data.addDataSet(gammaSet);
            }


            data.addEntry(new Entry(deltaSet.getEntryCount(), (float) delta), 0);
            data.addEntry(new Entry(thetaSet.getEntryCount(), (float) theta), 1);
            data.addEntry(new Entry(alphaSet.getEntryCount(), (float) alpha), 2);
            data.addEntry(new Entry(betaSet.getEntryCount(), (float) beta), 3);
            data.addEntry(new Entry(gammaSet.getEntryCount(), (float) gamma), 4);

            data.notifyDataChanged();

            chart.notifyDataSetChanged();

            chart.setVisibleXRangeMaximum(30); //Numero de puntos que se ven en el eje X del grafico

            chart.moveViewToX(data.getEntryCount());

        }
    }

    //Inicializacion de las lineas del grafico
    private LineDataSet createSet(int index){

        LineDataSet set;

        switch(index){
            case deltaIndex:
                set = new LineDataSet(null, "Delta");
                set.setColor(Color.RED);
                break;
            case thetaIndex:
                set = new LineDataSet(null, "Theta");
                set.setColor(Color.YELLOW);
                break;
            case alphaIndex:
                set = new LineDataSet(null, "Alpha");
                set.setColor(Color.GREEN);
                break;
            case betaIndex:
                set = new LineDataSet(null, "Beta");
                set.setColor(Color.MAGENTA);
                break;
            case gammaIndex:
                set = new LineDataSet(null, "Gamma");
                set.setColor(Color.BLUE);
                break;
            default:
                set = null;
        }

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setLineWidth(2f);
        set.setDrawValues(false);
        set.setDrawCircles(false);

        return set;
    }

    private Thread thread;

    //Este metodo gestiona el tiempo cada cuanto se recogen las muestras y se grafican
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
                        Thread.sleep(10); //muestreo
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    //Inicializacion del grafico
    private void init(){

        chart.getDescription().setEnabled(true);
        chart.getDescription().setText("Real Time Brain Waves");

        chart.setTouchEnabled(false);

        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);

        chart.setPinchZoom(false);

        chart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        chart.setData(data);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.BLACK);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(70f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        feedMultiple(); //Se llama a este manejador para empezar a muestrear datos cada cierto tiempo
    }

}

//Manejador de certificados de conexion
class MyTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        // No hacemos nada aquí
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        // No hacemos nada aquí
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}

