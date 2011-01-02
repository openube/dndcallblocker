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

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DNDCallBlockerReceiver extends BroadcastReceiver {
	private static final String BLACKLIST_PREF = "blacklist";
	private static final String DNDTAG = "DNDCallBlocker";
	
	private ArrayList<String> m_startswith;
	private ArrayList<String> m_endswith;
	private ArrayList<String> m_contains;
	private ArrayList<String> m_fullnums;

	@Override
	public void onReceive(Context context, Intent intent) {

		// Load preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		// Check phone state
		String phone_state = intent
				.getStringExtra(TelephonyManager.EXTRA_STATE);
		String number = intent
				.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		Log.d(DNDTAG, "INF: Broadcast received.");

		if (phone_state.equals(TelephonyManager.EXTRA_STATE_RINGING)
				&& prefs.getBoolean("enabled", false)) {
			Log.d(DNDTAG, "INF: Phone ringing, app enabled.");
			// block all rule...
			if (!prefs.getBoolean("block_all", false)) {

				// block hidden numbers
				if (!(prefs.getBoolean("block_unknown", false) && number == null)) {

					// block from list
					if (prefs.getBoolean("block_list", false)) {
						m_contains = new ArrayList<String>();
						m_startswith = new ArrayList<String>();
						m_endswith = new ArrayList<String>();
						m_fullnums = new ArrayList<String>();
						
						String tmp_phones = prefs.getString(BLACKLIST_PREF, "");
						String[] tmp_phonesArr = tmp_phones.split(", ");
						for (String s:tmp_phonesArr) {
							if (s.trim().startsWith("*") && s.trim().endsWith("*")) {
								// send to 'contains array'
								m_contains.add(s.substring(1, s.trim().length()-1));
							} else if (s.trim().startsWith("*")) {
								// send to 'ends with array' 
								m_endswith.add(s.substring(1, s.trim().length()));
							} else if (s.trim().endsWith("*")) {
								// send to 'starts with array'
								m_startswith.add(s.substring(0, s.trim().length()-1));
							} else {
								// full number
								m_fullnums.add(s.trim());
							}
						}
						if ((number == null)
								|| (tmp_phones.indexOf(number) == -1)) {
							// unknown number or
							// black list is on, but doesn't contains this
							// number
							Log.d(DNDTAG, "INF: Unknown or not on black list.");
							return;
						}
						Log.d(DNDTAG, "BLOCK: Number on black list.");
					} else {
						// no rule applied to this incoming number
						Log.d(DNDTAG, "INF: No rule.");
						return;
					}
				}
				Log.d(DNDTAG, "BLOCK: Number unknown, blocked.");
			} else {
				Log.d(DNDTAG, "BLOCK: Block all.");
			}

			// check if any exception apply to this incoming number
			String number_exceptions = prefs.getString("number_exceptions",
					"none");
			if (!number_exceptions.equals("none") && (number != null)) {
				int is_starred = isStarred(context, number);
				if (number_exceptions.equals("contacts") && is_starred >= 0) {
					// number is in contact list
					Log.d(DNDTAG, "EXC: Number on contact list.");
					return;
				} else if (number_exceptions.equals("starred")
						&& is_starred == 1) {
					// number is starred
					Log.d(DNDTAG, "EXC: Number is starred.");
					return;
				}
			}
			
			Log.d(DNDTAG, "INF: Start service.");

			// we have to block this call...
			// Call a service, since this could take a few seconds
			context.startService(new Intent(context,
					DNDCallBlockerIntentService.class));
		}
	}

	// returns -1 if not in contact list, 0 if not starred, 1 if starred
	private int isStarred(Context context, String number) {
		int starred = -1;
		Cursor c = context.getContentResolver().query(
				Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number),
				new String[] { PhoneLookup.STARRED }, null, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				starred = c.getInt(0);
			}
			c.close();
		}
		return starred;
	}
}
