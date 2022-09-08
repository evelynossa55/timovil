package com.cm.timovil2.pref;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import androidx.annotation.Nullable;

import com.cm.timovil2.R;

/**
 * CREADO POR JORGE ANDRÉS DAVID CARDONA EL 11/11/19.
 */

public class ScannerPreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_scanner);
    }
}
