package com.softbankrobotics.pepperapptemplate;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.human.Human;
import com.softbankrobotics.pepperapptemplate.Fragments.LoadingFragment;


import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {

    private static final String TAG = "MSI_MainActivity";
    private FragmentManager fragmentManager;
    public QiContext qiContext;
    private String currentFragment;
    private android.content.res.Configuration config;
    private Resources res;

    public Human human = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getApplicationContext().getResources();
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY);
        config = res.getConfiguration();
        this.fragmentManager = getSupportFragmentManager();
        QiSDK.register(this, this);
        setContentView(R.layout.activity_main);
        //Log.d(TAG, "test");
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.d(TAG, "onRobotFocusedGained");
        this.qiContext = qiContext;

        //Cuando se obtiene la atencion del robot se activa el fragmento inicial de la aplicacion
        runOnUiThread(() -> setFragment(new LoadingFragment()));

        Log.d(TAG, "onRobotFocusedGained final");

    }

    @Override
    public void onRobotFocusLost() {
        Log.d(TAG, "onRobotFocusLost()");
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "onRobotFocusRefused");
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    public void setQiVariable(String variableName, String value) {
    }

    public QiContext getQiContext() {
        return qiContext;
    }

    public Integer getThemeId() {
        try {
            return getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Fragment getFragment() {
        return fragmentManager.findFragmentByTag("currentFragment");
    }

    //Metodo para gestionar el cambio de fragmentos
    public void setFragment(Fragment fragment) {
        Log.d(TAG, "Start setFragment");
        currentFragment = fragment.getClass().getSimpleName();
        Log.d(TAG, "Transaction for fragment : " + fragment.getClass().getSimpleName());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_fade_in_right, R.anim.exit_fade_out_left,
                R.anim.enter_fade_in_left, R.anim.exit_fade_out_right);
        transaction.replace(R.id.placeholder, fragment, "currentFragment");
        transaction.addToBackStack(null);
        transaction.commit();
        Log.d(TAG, "End setFragment");
    }

}

