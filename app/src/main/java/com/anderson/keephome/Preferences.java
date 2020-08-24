package com.anderson.keephome;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Preferences extends AppCompatActivity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings_container, new setPreferencesFragment())
				.commit();

		setContentView(R.layout.activity_preferences);
	}
}
