package com.anderson.keephome;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlarmReceiver extends BroadcastReceiver {

	static String POST_KEYS = "POST_KEYS";
	static String POST_PARAMETERS = "POST_PARAMETERS";

	@Override
	public void onReceive (Context context, Intent intent) {
		//android.os.Debug.waitForDebugger();
		final Context finalContext = context;
		Log.i("Background sync", "Task was run");

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = null;
		if (cm != null) {
			activeNetwork = cm.getActiveNetworkInfo();
		}
		if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = null;
			if (pm != null) {
				wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "keephome:background");
				if (wl != null) {
					wl.acquire(5);
				}
			}

			// run code here
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			final boolean doNotifications = sharedPreferences.getBoolean("notifications", true);

			RequestQueue queue = Volley.newRequestQueue(context);

			String keephome_ip = sharedPreferences.getString("keephome_ip", "192.168.4.1");

			StringRequest postRequest = new StringRequest(
					Request.Method.POST,
					"http://" + keephome_ip + "/post",
					new Response.Listener<String>() {
						@Override
						public void onResponse (String response) {
							try {
								JSONObject jsonObject = new JSONObject(response);
								String time = jsonObject.getString("time");
								String additional = jsonObject.getString("additional");
								if (doNotifications) {
									MainActivity.displayNotification(finalContext, MainActivity.SYNC_CHANNEL, "Task was run: " + time, "Additional data: " + additional);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					},
					new Response.ErrorListener() {
						@Override
						public void onErrorResponse (VolleyError error) {
							if (doNotifications)
								MainActivity.displayNotification(finalContext, MainActivity.ERROR_CHANNEL, "Error connecting to KeepHome", error.toString());
						}
					}
			) {
				@Override
				protected Map<String, String> getParams () {
					Map<String, String> parameters = new HashMap<>();
					// things to get
					parameters.put("keepbox", "getdata");
					return parameters;
				}
			};
			queue.add(postRequest);

			if (wl != null) {
				if (wl.isHeld()) {
					wl.release();
				}
			}
		}
	}

	public void setAlarm (Context context, int interval) {
		if (!isAlarmSet(context)) {
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, AlarmReceiver.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			if (alarmManager != null) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.SECOND, 10);
				long runAtTime = calendar.getTimeInMillis();
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, runAtTime, 1000 * 60 * interval, pendingIntent); // Milliseconds * Seconds * Minutes
			}
		}
	}

	public boolean isAlarmSet (Context context) {
		return (PendingIntent.getBroadcast(context, 0,	new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);
	}

	public void cancelAlarm (Context context)
	{
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (alarmManager != null) {
			alarmManager.cancel(sender);
		}
	}
}
