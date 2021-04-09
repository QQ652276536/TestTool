package com.zistone.testtool.secret_key_download;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;
import com.zistone.testtool.R;

public class SecretKeyActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private static final String TAG = "MainActivity";

    private TabLayout _tabLayout;
    private KeyFragment _keyFragment;
    private DesFragment _desFragment;
    private SM4Fragment _sm4Fragment;

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        switch (tab.getPosition()) {
            case 0: {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.show(_keyFragment).hide(_desFragment).hide(_sm4Fragment).commit();
            }
            break;
            case 1: {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.show(_desFragment).hide(_sm4Fragment).hide(_keyFragment).commit();
            }
            break;
            case 2:
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.show(_sm4Fragment).hide(_keyFragment).hide(_desFragment).commit();
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_key);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        _keyFragment = new KeyFragment();
        _desFragment = new DesFragment();
        _sm4Fragment = new SM4Fragment();
        fragmentTransaction.add(R.id.fl, _keyFragment, "KeyFragment").show(_keyFragment);
        fragmentTransaction.add(R.id.fl, _desFragment, "DesFragment").hide(_desFragment);
        fragmentTransaction.add(R.id.fl, _sm4Fragment, "SM4Fragment").hide(_sm4Fragment);
        fragmentTransaction.commit();
        _tabLayout = findViewById(R.id.tab);
        _tabLayout.addOnTabSelectedListener(this);

    }

}
