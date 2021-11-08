package com.anderson.keephome;

import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class setPreferencesFragment extends PreferenceFragmentCompat {

	@Override
	public void onCreatePreferences (Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);

		findPreference("wifipassword").setSummaryProvider(preference -> {
				EditTextPreference textPreference = (EditTextPreference) preference;
				if (textPreference.getText() != null) {
					if (textPreference.getText().length() >= 8) {
						return "Password is at least 8 characters";
					} else {
						return "Password should have at least 8 characters";
					}
				}
				return "Not set";
			});
	}
}
