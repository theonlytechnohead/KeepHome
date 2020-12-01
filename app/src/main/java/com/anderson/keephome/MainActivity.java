package com.anderson.keephome;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class MainActivity extends AppCompatActivity {

	public static String ERROR_CHANNEL = "error";
	public static String SYNC_CHANNEL = "sync";
	public static String INFO_CHANNEL = "info";

	public static Map<String, Integer> channel_to_id = new HashMap<>();

	static {
		channel_to_id.put(ERROR_CHANNEL, 0);
		channel_to_id.put(SYNC_CHANNEL, 1);
		channel_to_id.put(INFO_CHANNEL, 2);
	}

	SwipeRefreshLayout swipeRefreshLayout;
	TextView connection_status_text;

	AlarmReceiver alarmReceiver;

	JmDNS jmDNS;

	SharedPreferences sharedPreferences;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		swipeRefreshLayout = findViewById(R.id.swiper);
		swipeRefreshLayout.setOnRefreshListener(this::getData);
		findViewById(R.id.restartButton).setOnClickListener(v -> MainActivity.this.restartKeepHome());
		connection_status_text = findViewById(R.id.connection_status_text);

		Toolbar actionBar = findViewById(R.id.action_bar);
		setSupportActionBar(actionBar);
		actionBar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		int syncInterval = Integer.parseInt(sharedPreferences.getString("sync_interval", "5"));

		alarmReceiver = new AlarmReceiver();
		alarmReceiver.setAlarm(this, syncInterval);

		AsyncTask.execute(() -> {
			try {
				jmDNS = JmDNS.create(GetLocalIP());
				jmDNS.addServiceListener("_http._tcp.local.", new keephomeListener());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void onStart () {
		super.onStart();
		registerPrefListener();
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		if (item.getItemId() == R.id.button_settings) {
			// User chose the "Settings" item, show the app settings UI...
			Intent preferencesIntent = new Intent(getApplicationContext(), Preferences.class);
			startActivity(preferencesIntent);
			return true;
		} else if (item.getItemId() == R.id.menu_refresh) {
			getAllData();
			swipeRefreshLayout.setRefreshing(true);
			return true;
		} else {
			// If we got here, the user's action was not recognized.
			// Invoke the superclass to handle it.
			return super.onOptionsItemSelected(item);
		}
	}

	private InetAddress GetLocalIP () {
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
				NetworkInterface networkInterface = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLinkLocalAddress()) {
						localAddress = inetAddress;
					}
				}
			}
			return localAddress;
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void getAllData () {
		//getWiFimode();
		//getSSID();
		//getPassword();
		//getData();
		HashMap<String, String> params = new HashMap<>();
		params.put("WiFimode", "get");
		params.put("SSID", "get");
		params.put("password", "get");
		params.put("keepbox", "getdata");
		sendPostRequest(params);
	}

	private void getWiFimode () {
		HashMap<String, String> params = new HashMap<>();
		params.put("WiFimode", "get");
		sendPostRequest(params);
	}

	private void getSSID () {
		HashMap<String, String> params = new HashMap<>();
		params.put("SSID", "get");
		sendPostRequest(params);
	}

	private void getPassword () {
		HashMap<String, String> params = new HashMap<>();
		params.put("password", "get");
		sendPostRequest(params);
	}

	private void getData () {
		HashMap<String, String> params = new HashMap<>();
		params.put("keepbox", "getdata");
		sendPostRequest(params);
	}

	private void setWiFimode (int WiFimode) {
		HashMap<String, String> params = new HashMap<>();
		params.put("WiFimode", "set");
		params.put("newWiFimode", String.valueOf(WiFimode));
		sendPostRequest(params);
	}

	private void setSSID (String SSID) {
		HashMap<String, String> params = new HashMap<>();
		params.put("SSID", "set");
		params.put("newWiFiSSID", SSID);
		sendPostRequest(params);
	}

	private void setPassword (String password) {
		HashMap<String, String> params = new HashMap<>();
		params.put("password", "set");
		params.put("newPassword", password);
		sendPostRequest(params);
	}

	private void restartKeepHome () {
		RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
		StringRequest restartRequest = new StringRequest(
				Request.Method.GET,
				"http://" + sharedPreferences.getString("keephome_ip", "192.168.4.1") + "/restart",
				null,
				null
		);
		restartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		queue.add(restartRequest);
	}

	private void sendPostRequest (HashMap<String, String> params) {
		RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
		final TextView text1 = findViewById(R.id.text);
		final TextView text2 = findViewById(R.id.text2);

		final Map<String, String> finalMap = params;

		MainActivity.displayNotification(getApplicationContext(), INFO_CHANNEL, "POST parameters", finalMap.toString());

		StringRequest postRequest = new StringRequest(
				Request.Method.POST,
				"http://" + sharedPreferences.getString("keephome_ip", "192.168.4.1") + "/post",
				response -> {
					swipeRefreshLayout.setRefreshing(false);
					try {
						JSONObject jsonObject = new JSONObject(response);
						final JSONObject finalJSONObject = jsonObject;
						runOnUiThread(() -> {
							try {
								text1.setText(finalJSONObject.getString("time"));
								text2.setText(finalJSONObject.getString("additional"));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						});
						if (jsonObject.has("WiFimode")) {
							String temp = jsonObject.getString("WiFimode");
							unregisterPrefListener();
							if (temp.equals("1")) {
								sharedPreferences.edit().putBoolean("wifi_ap_mode", true).apply();
							} else {
								sharedPreferences.edit().putBoolean("wifi_ap_mode", false).apply();
							}
							registerPrefListener();
						}
						if (jsonObject.has("SSID")) {
							unregisterPrefListener();
							sharedPreferences.edit().putString("wifissid", jsonObject.getString("SSID")).apply();
							registerPrefListener();
						}
						if (jsonObject.has("password")) {
							unregisterPrefListener();
							sharedPreferences.edit().putString("wifipassword", jsonObject.getString("password")).apply();
							registerPrefListener();
						}
					} catch (final JSONException e) {
						e.printStackTrace();
						runOnUiThread(() -> text1.setText(e.toString()));
					}
				},
				error -> runOnUiThread(() -> {
					text1.setText(error.toString());
					swipeRefreshLayout.setRefreshing(false);
				})
		) {
			@Override
			protected Map<String, String> getParams () {
				//Map<String, String> parameters = params;
				// things to get
				//Toast.makeText(getApplicationContext(), finalMap.toString(), Toast.LENGTH_SHORT).show();
				return finalMap;
			}
		};
		postRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		queue.add(postRequest);
	}

	public static void displayNotification (@NonNull Context context, String channelID, String title, String content) {
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(channelID, "KeepHome", importance);
			// Register the channel with the system; you can't change the priority
			// or other notification behaviors after this
			notificationManager.createNotificationChannel(channel);
		}

		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		int ID = channel_to_id.get(channelID);

		NotificationCompat.Builder notification = new NotificationCompat.Builder(context, channelID)
				.setContentTitle("Expand for details...")
				.setContentIntent(pendingIntent)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(content).setBigContentTitle(title))
				.setAutoCancel(true)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setSmallIcon(R.drawable.ic_launcher_foreground);
		notificationManager.notify(ID, notification.build());

	}

	private void updateDNS (ServiceEvent event) {
		runOnUiThread(() -> {
			if (event != null) {
				sharedPreferences.edit().putString("keephome_ip", event.getInfo().getHostAddresses()[0]).apply();
				connection_status_text.setText(R.string.online);
				connection_status_text.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
			} else {
				connection_status_text.setText(R.string.offline);
				connection_status_text.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
			}
		});
	}

	private class keephomeListener implements ServiceListener {

		@Override
		public void serviceAdded(ServiceEvent event) {
			Log.d("mDNS listener", "Service found: " + event.getInfo());
			if (event.getName().equals("KeepHome")) {
				jmDNS.requestServiceInfo(event.getType(), event.getName());
			}
		}

		@Override
		public void serviceRemoved(ServiceEvent event) {
			Log.d("mDNS listener", "Service lost: " + event.getInfo());
		}

		@Override
		public void serviceResolved(ServiceEvent event) {
			Log.d("mDNS listener", "Service rseolved: " + event.getInfo());
			if (event.getName().equals("KeepHome")) {
				updateDNS(event);
			}
		}
	}

	public SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, s) -> {
		if (s.equals("notifications")) {
			Log.i("KeepHome", "Notifications were updated to: " + sharedPreferences.getBoolean(s, true));
			if (sharedPreferences.getBoolean(s, true))
				Toast.makeText(getApplicationContext(), "Notifications enabled", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(getApplicationContext(), "Notifications disabled", Toast.LENGTH_SHORT).show();
		}
		if (s.equals("sync_interval")) {
			int value = Integer.parseInt(sharedPreferences.getString("sync_interval", "5"));
			Log.i("KeepHome", "Sync interval was updated to: " + value);
			AlarmReceiver alarmReceiver = new AlarmReceiver();
			alarmReceiver.cancelAlarm(getApplicationContext());
			alarmReceiver.setAlarm(getApplicationContext(), value);
			Toast.makeText(getApplicationContext(), "Set sync interval to " + value + " minutes", Toast.LENGTH_LONG).show();
		}
		if (s.equals("wifi_ap_mode")) {
			boolean wifimode = sharedPreferences.getBoolean("wifi_ap_mode", true);
			if (wifimode)
				setWiFimode(1);
			else
				setWiFimode(0);
		}
		if (s.equals("wifissid")) {
			setSSID(sharedPreferences.getString("wifissid", "KeepHome"));
		}
		if (s.equals("wifipassword")) {
			String password = sharedPreferences.getString("wifipassword", "");
			if (password.length() >= 8)
				setPassword(password);
			else
				Toast.makeText(getApplicationContext(), "Password must be at least 8 characters!", Toast.LENGTH_LONG).show();
		}
	};

	void registerPrefListener () {
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener);
	}

	void unregisterPrefListener () {
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public void onResume () {
		super.onResume();
		registerPrefListener();
	}

	@Override
	public void onDestroy () {
		super.onDestroy();
		unregisterPrefListener();
	}

}
