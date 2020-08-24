package com.anderson.keephome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Autostart extends BroadcastReceiver {
	AlarmReceiver alarmReceiver = new AlarmReceiver();
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			int syncInterval = Integer.parseInt(sharedPreferences.getString("sync_interval", "5"));
			alarmReceiver.setAlarm(context, syncInterval);
		}
	}
}
