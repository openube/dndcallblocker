package hu.zoliweb.android.dndcallblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;

public class DNDCallBlockerReceiver extends BroadcastReceiver {
	private static final String BLACKLIST_PREF = "blacklist";

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

		if (phone_state.equals(TelephonyManager.EXTRA_STATE_RINGING)
				&& prefs.getBoolean("enabled", false)) {
			// block all rule...
			if (!prefs.getBoolean("block_all", false)) {

				// block hidden numbers
				if (!(prefs.getBoolean("block_unknown", false) && number == null)) {

					// block from list
					if (prefs.getBoolean("block_list", false)) {
						String tmp_phones = prefs.getString(BLACKLIST_PREF, "");
						if ((number == null)
								|| (tmp_phones.indexOf(number) == -1)) {
							// unknown number or
							// black list is on, but doesn't contains this
							// number
							return;
						}
					} else {
						// no rule applied to this incoming number
						return;
					}
				}
			}

			// check if any exception apply to this incoming number
			String number_exceptions = prefs.getString("number_exceptions",
					"none");
			if (!number_exceptions.equals("none") && (number != null)) {
				int is_starred = isStarred(context, number);
				if (number_exceptions.equals("contacts") && is_starred >= 0) {
					// number is in contact list
					return;
				} else if (number_exceptions.equals("starred")
						&& is_starred == 1) {
					// number is starred
					return;
				}
			}

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
