package hu.zoliweb.android.dndcallblocker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.admob.android.ads.AdManager;

public class DNDCallBlockerPreferenceActivity extends PreferenceActivity
		implements OnSharedPreferenceChangeListener {

	private DNDCallBlockerNotifier mNotifier;
	private SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// status bar notification
		mNotifier = new DNDCallBlockerNotifier(this);
		mNotifier.updateNotification();
		// get preferences
		sharedPreferences = getPreferenceManager().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		addPreferencesFromResource(R.xml.preferences);

		// set up adMob test device ID
		AdManager.setTestDevices(new String[] { AdManager.TEST_EMULATOR,
				"7BD938EB53D2B6CA90A6494300F3AA67", });
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.about:
			// prepare the alert box
			AlertDialog.Builder aboutAlertbox = new AlertDialog.Builder(this);
			// set the icon to display
			aboutAlertbox.setIcon(R.drawable.icon);
			// set the title to display
			aboutAlertbox.setTitle(R.string.title_about);
			// set the message to display
			aboutAlertbox.setMessage(R.string.message_about);
			// add a neutral button to the alert box
			aboutAlertbox.setNeutralButton("Ok", null);
			// show it
			aboutAlertbox.show();
			return true;

		case R.id.license:
			// prepare the alert box
			AlertDialog.Builder licenseAlertbox = new AlertDialog.Builder(this);
			// set the icon to display
			licenseAlertbox.setIcon(R.drawable.icon);
			// set the title to display
			licenseAlertbox.setTitle(R.string.title_license);
			// set the message to display
			licenseAlertbox.setMessage(R.string.message_license);
			// add a neutral button to the alert box
			licenseAlertbox.setNeutralButton("Ok", null);
			// show it
			licenseAlertbox.show();
			return true;

		case R.id.donate:
			// construct an alert dialog
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			// set icon, title and message
			alert.setIcon(R.drawable.icon);
			alert.setTitle(R.string.app_name);
			alert.setMessage(R.string.donate_message);

			// configure two buttons
			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// when OK pressed, go to paypal donations web page
							startActivity(new Intent(
									Intent.ACTION_VIEW,
									Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=NF4VH9Y7F4PQS")));
						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Canceled. Do nothing.
						}
					});

			// show the dialog window
			alert.show();

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("enabled")) {
			mNotifier.updateNotification();
		}
	}
}