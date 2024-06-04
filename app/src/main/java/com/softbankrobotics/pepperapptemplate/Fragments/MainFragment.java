package com.softbankrobotics.pepperapptemplate.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.softbankrobotics.pepperapptemplate.MainActivity;
import com.softbankrobotics.pepperapptemplate.R;
import com.softbankrobotics.pepperapptemplate.Utils.Adapter;


public class MainFragment extends Fragment {

    private static final String TAG = "MSI_MainFragment";
    private MainActivity ma;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Button newUser;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        //Inicializacion del fragmento
        View vista = inflater.inflate(R.layout.fragment_main, container, false);
        Log.d(TAG, "onCreateView");

        int fragmentId = R.layout.fragment_main;
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

        tabLayout = ma.findViewById(R.id.tabLayout);
        viewPager = ma.findViewById(R.id.viewPager);
        newUser = ma.findViewById(R.id.btn_newUser);

        //Inicializaciones para la gestion del estado de las tres pestañas (fragmentos) disponibles en el MainFragment
        tabLayout.setupWithViewPager(viewPager);

        Adapter adapter = new Adapter(ma.getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new GetFaceFragment(), "DETECCIÓN EXPRESIÓN");
        adapter.addFragment(new MuseFragment(), "SEÑALES NEURONALES");
        adapter.addFragment(new AboutFragment(), "ABOUT");
        viewPager.setAdapter(adapter);

        //Boton para buscar nuevo usuario
        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ma.setFragment(new LoadingFragment());
            }
        });

        Log.d(TAG, "onViewCreated end");
    }

}