package com.softbankrobotics.pepperapptemplate.Fragments;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.builder.EngageHumanBuilder;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.PathPlanningPolicy;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.EngageHuman;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.EncodedImageHandle;
import com.aldebaran.qi.sdk.object.image.TimestampedImageHandle;
import com.softbankrobotics.pepperapptemplate.MainActivity;
import com.softbankrobotics.pepperapptemplate.R;

import java.nio.ByteBuffer;

public class LoadingFragment extends Fragment {

    private static final String TAG = "MSI_LoadingFragment";
    private MainActivity ma;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        //Inicializacion del fragmento
        int fragmentId = R.layout.fragment_loading;
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
        TextView textLoading = view.findViewById(R.id.loading_text);
        Animation animPulse = AnimationUtils.loadAnimation(ma, R.anim.pulse);
        textLoading.startAnimation(animPulse);

        Log.d(TAG, "onViewCreated inicio");

        try{

            //Accion buscar humano en el entorno
            HumanAwareness humanAwareness = ma.qiContext.getHumanAwareness();
            Log.d(TAG, "onViewCreated1");
            Future<Human> engagedHuman = humanAwareness.async().getEngagedHuman();
            Log.d(TAG, "onViewCreated2");

            //Una vez encontrada la persona
            humanAwareness.addOnEngagedHumanChangedListener(human -> {

                Log.d(TAG, "addOnEngagedHumanChangedListener");

                if(human != null) {
                    Log.d(TAG, "human != null");

                    ma.human = human;
                    Future<EngageHuman> engageHuman = EngageHumanBuilder.with(ma.qiContext).withHuman(human).buildAsync();

                    //Le indicamos que se dirija hacia la persona localizada
                    Frame human_position = human.getHeadFrame();
                    GoTo Goto = GoToBuilder.with(ma.qiContext).withFrame(human_position).withPathPlanningPolicy(PathPlanningPolicy.STRAIGHT_LINES_ONLY).build();
                    //Cuando esta enfrente de la persona
                    Goto.async().run().thenConsume(future -> {
                        Log.d(TAG, "thenConsume");

                        //Se produce el cambio de fragmento para mostrar el MainFragment
                        if(future.isDone()) {

                            Goto.removeAllOnStartedListeners();
                            engageHuman.cancel(true);
                            humanAwareness.removeAllOnEngagedHumanChangedListeners();
                            ma.setFragment(new MainFragment());

                        }
                    });

                }

            });

        } catch (Exception e){

        }

        Log.d(TAG, "onViewCreated final");

    }

}
