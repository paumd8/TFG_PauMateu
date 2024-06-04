package com.softbankrobotics.pepperapptemplate.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.EncodedImageHandle;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.softbankrobotics.pepperapptemplate.MainActivity;
import com.softbankrobotics.pepperapptemplate.R;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class GetFaceFragment extends Fragment {

    private static final String TAG = "MSI_GetFaceFragment";
    private static final String TAG_msg_dep = "msg_dep";
    private MainActivity ma;
    private Future<TimestampedImageHandle> timestampedImageHandleFuture;
    private String sendImageURL = "http://192.168.0.99:8080/sendImage";
    private Bitmap bitmap;
    private ImageButton getPic;
    private GoTo Goto;
    private TextView text_countdown;
    private TextView textView_facialExpression;
    private ImageView faceImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inicializacion del fragment
        Log.d(TAG, "onCreateView");
        int fragmentId = R.layout.fragment_get_face;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated start");

        getPic = view.findViewById(R.id.btn_getPic);

        //Cuando se pulsa el boton, se inicia una cuenta atras
        getPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick");

                text_countdown= ma.findViewById(R.id.text_countdown);
                textView_facialExpression = ma.findViewById(R.id.text_expression);
                faceImage = ma.findViewById(R.id.faceImage);
                faceImage.setImageDrawable(null);
                textView_facialExpression.setText(null);
                CuentaAtras();

            }
        });
        Log.d(TAG, "onViewCreated end");

    }

    private void CuentaAtras() {

        //Metodo cuenta atras 3 segundos
        new CountDownTimer(4000, 1000){

            @Override
            public void onTick(long l) {
                long i = l/1000;
                text_countdown.setText(String.valueOf(i));
                Animation animPulse = AnimationUtils.loadAnimation(ma, R.anim.pulse);
                text_countdown.startAnimation(animPulse);
            }

            //Cuando ha finalizado la cuenta atras, se ejecuta el codigo del metodo onFinish()
            @Override
            public void onFinish() {
                text_countdown.setText(" ");
                textView_facialExpression.setText("Detectando...");

                Future<TakePicture> takePictureFuture = TakePictureBuilder.with(ma.qiContext).buildAsync();

                //Accion para sacar la imagen facial del usuario
                Future<TimestampedImageHandle> timestampedImageHandleFuture = takePictureFuture.andThenCompose(takePicture -> {
                    return takePicture.async().run();
                });

                //Cuando se obtiene la imagen se ejecuta este codigo
                timestampedImageHandleFuture.andThenConsume(timestampedImageHandle -> {

                    Log.i(TAG, "Picture taken");

                    EncodedImageHandle encodedImageHandle = timestampedImageHandle.getImage();

                    EncodedImage encodedImage = encodedImageHandle.getValue();
                    Log.i(TAG, "PICTURE RECEIVED!");

                    //Conversion de la imagen a un array de bytes
                    ByteBuffer buffer = encodedImage.getData();
                    buffer.rewind();
                    final int pictureBufferSize = buffer.remaining();
                    final byte[] pictureArray = new byte[pictureBufferSize];
                    buffer.get(pictureArray);

                    faceImage = ma.findViewById(R.id.faceImage);
                    Log.i(TAG, "PICTURE RECEIVED! (" + pictureBufferSize + " Bytes)");
                    //Se muestra la imagen en la interfaz de usuario
                    bitmap = BitmapFactory.decodeByteArray(pictureArray, 0, pictureBufferSize);
                    ma.runOnUiThread(() -> faceImage.setImageBitmap(bitmap));

                    //Se envia la imagen al modulo de vision
                    uploadImage();

                });
            }
        }.start();

    }

    private void uploadImage(){

        Log.i(TAG, "uploadImage");

        //Parametros a enviar para realizar la conexion
        final String task = "expression"; //4
        final String method = "VGG19"; //8
        final String mode = "text"; //4
        String url = sendImageURL+"?task="+task+"&method="+method+"&mode="+mode;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            //Se trata la respuesta recibida del algoritmo
            @Override
            public void onResponse(String response) {

                Log.i(TAG, "onResponse");

                switch (response.trim()){ //trim() elimina espacios en blanco
                    case "anger":
                        textView_facialExpression.setText("ENFADADO");
                        break;
                    case "disgust":
                        textView_facialExpression.setText("INDIGNADO");
                        break;
                    case "fear":
                        textView_facialExpression.setText("ATERRADO");
                        break;
                    case "happiness":
                        textView_facialExpression.setText("FELIZ");
                        break;
                    case "sadness":
                        textView_facialExpression.setText("TRISTE");
                        break;
                    case "surprise":
                        textView_facialExpression.setText("SORPRENDIDO");
                        break;
                    default:
                        textView_facialExpression.setText("Not detected");

                }

                //Toast.makeText(ma.qiContext, "Se ha detectado correctamente.", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "onErrorResponse: " + error.getMessage());
                textView_facialExpression.setText("No detectado");
                //Toast.makeText(ma.qiContext, "Error en la detección.", Toast.LENGTH_SHORT).show();
            }
        }){

            @Override
            public byte[] getBody() throws AuthFailureError {
                byte [] a = new byte[1];
                a[0] = 0;
                try {
                    a = ImgToByte(bitmap);
                }
                catch (Exception error) {
                    Log.i(TAG, "getBody: " + error.getMessage());
                }
                return a;
            }

            @Override
            public String getBodyContentType() {
                return "image/jpg";
            }
        };
        //Solicitud de conexion con el servidor
        Log.i(TAG, "request");
        RequestQueue request =  Volley.newRequestQueue(this.ma);
        request.add(stringRequest);
    }

    //convertir imagen a string
    private String ImgToString(Bitmap bitmap){
        Log.i(TAG, "ImgToString");
        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,array);
        byte[] imageByte = array.toByteArray();
        String imgString = Base64.encodeToString(imageByte, Base64.DEFAULT);

        return imgString;
    }

    //Convertir imagen a bytes
    private byte[] ImgToByte(Bitmap bitmap){
        Log.i(TAG, "ImgToByte");
        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,array);
        byte[] imageByte = array.toByteArray();
        imageByte = Base64.encode(imageByte, Base64.DEFAULT);
        return imageByte;
    }
}