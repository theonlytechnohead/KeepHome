package com.anderson.keephome;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class setPreferencesFragment extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences (Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
}
