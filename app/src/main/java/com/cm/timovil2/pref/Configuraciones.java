package com.cm.timovil2.pref;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;

import com.cm.timovil2.R;

import java.util.List;

public class Configuraciones extends PreferenceActivity{

    private AppCompatDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.preferences_header, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        String printFragment = PrintPreferencesFragment.class.getName();
        String scannerFragment = ScannerPreferencesFragment.class.getName();
        return printFragment.equals(fragmentName) || scannerFragment.equals(fragmentName);
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null)
            mDelegate = AppCompatDelegate.create(this, null);

        return mDelegate;
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }
}
