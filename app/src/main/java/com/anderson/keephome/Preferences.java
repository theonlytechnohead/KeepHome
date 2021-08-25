package com.anderson.keephome;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class Preferences extends AppCompatActivity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings_container, new setPreferencesFragment())
				.commit();

		// Check theme
		int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		switch (currentNightMode) {
			case Configuration.UI_MODE_NIGHT_NO:
				setTheme(R.style.PreferenceTheme);
				Log.d("Theme", "Set theme to: light");
				break;
			case Configuration.UI_MODE_NIGHT_YES:
				setTheme(R.style.PreferenceTheme_Dark);
				Log.d("Theme", "Set theme to: dark");
				break;
		}

		setContentView(R.layout.activity_preferences);
	}


}
