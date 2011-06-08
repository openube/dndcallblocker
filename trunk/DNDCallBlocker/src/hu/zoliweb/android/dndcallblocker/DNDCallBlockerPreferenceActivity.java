/*
 * DND Call Blocker
 * A simple Android application that automatically block unwanted incoming calls.
 * Copyright (c) 2010 Zoltan Meleg, android+dndcb@zoliweb.hu
 * 
 * This file is part of DND Call Blocker.
 * 
 * DND Call Blocker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DND Call Blocker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DND Call Blocker.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package hu.zoliweb.android.dndcallblocker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.widget.Toast;

public class DNDCallBlockerPreferenceActivity extends PreferenceActivity
		implements OnSharedPreferenceChangeListener {

	private DNDCallBlockerNotifier mNotifier;
	private SharedPreferences sharedPreferences;
	private Context myContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myContext = this;

		// status bar notification
		mNotifier = new DNDCallBlockerNotifier(this);
		mNotifier.updateNotification();
		// get preferences
		sharedPreferences = getPreferenceManager().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		addPreferencesFromResource(R.xml.preferences);

		// init summary of listpreferences
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			Preference pref = getPreferenceScreen().getPreference(i);
			if (pref instanceof PreferenceGroup) {
				for (int j = 0; j < ((PreferenceGroup) pref)
						.getPreferenceCount(); j++) {
					initListPrefSummary(((PreferenceGroup) pref)
							.getPreference(j));
				}
			} else {
				initListPrefSummary(pref);
			}
		}

		if ((!sharedPreferences.getBoolean("wasGBnotify", false))
				&& (this.checkCallingOrSelfPermission(Manifest.permission.MODIFY_PHONE_STATE) == PackageManager.PERMISSION_DENIED)) {

			// save updated list to sharedpreferences
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("wasGBnotify", true);
			editor.commit();

			// prepare the alert box
			AlertDialog.Builder aboutAlertbox = new AlertDialog.Builder(
					myContext);
			// set the icon to display
			aboutAlertbox.setIcon(R.drawable.icon);
			// set the title to display
			aboutAlertbox.setTitle(R.string.app_name);
			// set the message to display
			aboutAlertbox.setMessage(R.string.gingerbread_fail);
			// add a neutral button to the alert box
			aboutAlertbox.setNeutralButton(R.string.message_ok, null);
			// show it
			aboutAlertbox.show();
		}
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(R.layout.preferences);
	}

	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("enabled") || key.equals("stat_notify")) {
			mNotifier.updateNotification();
		} else {
			if (key.equals("handle_call")
					&& sharedPreferences.getString(key, "silence").equals(
							"block")
					&& (this.checkCallingOrSelfPermission(Manifest.permission.MODIFY_PHONE_STATE) == PackageManager.PERMISSION_DENIED)) {
				// inform user
				String toast_text = getString(R.string.gingerbread_fail2);
				Toast.makeText(getApplicationContext(), toast_text,
						Toast.LENGTH_LONG).show();
			}
			Preference pref = findPreference(key);
			initListPrefSummary(pref);
		}
	}

	private void initListPrefSummary(Preference pref) {
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}
	}
}