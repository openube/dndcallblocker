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

import java.lang.reflect.Method;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

public class DNDCallBlockerIntentService extends IntentService {
	private static final String DNDTAG = "DNDCallBlocker";
	private SharedPreferences _prefs;
	private String _phoneNr;

	DNDCallBlockerDBAdapter logDBAdapter;

	public DNDCallBlockerIntentService() {
		super("DNDCallBlockerIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getBaseContext();

		_prefs = PreferenceManager.getDefaultSharedPreferences(context);

		Log.d(DNDTAG, "SRV: Got control.");

		_phoneNr = intent.getStringExtra("phone_nr");

		// Make sure the phone is still ringing
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
			Log.d(DNDTAG, "SRV: Not ringing anymore.");
			return;
		}

		// Block the call
		try {
			handlePhoneCall(context);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(DNDTAG, "Error trying to reject using telephony service.");
		}

		// save event in call filter log
		logDBAdapter = new DNDCallBlockerDBAdapter(context);
		logDBAdapter.open();
		if (_phoneNr != null) {
			logDBAdapter.insertToLog(_phoneNr.trim());
		} else {
			logDBAdapter.insertToLog("");
		}
		logDBAdapter.close();

		// delete last call from phone history
		Boolean delete_history = _prefs.getBoolean("delete_history", true);
		if (delete_history) {
			Log.d(DNDTAG, "Trying to delete from history...");
			try {
				/*
				int loopCnt = 0;
				while (!deleteLastCall(context) && loopCnt < 16) {
					loopCnt++;
					Thread.sleep(125);
				}
				Log.d(DNDTAG, "History delete loop is over. cnt=" + loopCnt);
				*/
				Thread.sleep(2000);
				deleteLastCall(context);
				Log.d(DNDTAG, "History delete is over.");
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(DNDTAG,
						"There was some problem with deleteLastCall procedure.");
			}
		}

		return;
	}

	@SuppressWarnings("rawtypes")
	private void handlePhoneCall(Context context) throws Exception {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
		// call handling preference
		String handle_call = _prefs.getString("handle_call", "silence");

		Log.d(DNDTAG, "SRV: pref=".concat(handle_call));

		// Silence the ringer first
		AudioManager am = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		// save original audio state
		int old_mode = am.getRingerMode();
		// set to silent
		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		
		// programmatically call blocking only works up to Froyo 
		if ( android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO)
		{
			// Set up communication with the telephony service (thanks to Tedd's
			// Droid Tools!)
			Class c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			ITelephony telephonyService;
			telephonyService = (ITelephony) m.invoke(tm);
	
			if (handle_call.equals("block")) {
				Log.d(DNDTAG, "SRV: Block");
				// just block the call
				telephonyService.endCall();
			}
	
			// FIXME: sometimes just answer, but never hang up
			// not used handling mode
			if (handle_call.equals("answer_then_block")) {
				// pick up the phone
				telephonyService.answerRingingCall();
				// maybe some wait will help
				Thread.sleep(250);
				// then end the call
				telephonyService.endCall();
			}
		}

		// if handling mode was silence, we have to wait the phone to stop
		// ringing
		// if handling mode was block, TelephonyManager.CALL_STATE_RINGING will
		// be false, ringer mode will be restored immediately
		while (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
			Thread.sleep(200);
		}

		// restore saved audio state
		am.setRingerMode(old_mode);

		return;
	}

	private boolean deleteLastCall(Context context) {
		boolean isDeleted = false;
		// Load the calls Log data via context calls Content resolver
		String extraQuerySelection = "";
		// TODO: improve delete with date selection

		if (_phoneNr != null) {
			extraQuerySelection = " and " + android.provider.CallLog.Calls.NUMBER + " = '"
					+ _phoneNr + "'";
		} else {
			Log.d(DNDTAG, "History delete... phone nr is null");
			extraQuerySelection = " and " + android.provider.CallLog.Calls.NUMBER
					+ " in ( '-1', '-2' )"; }
		 
		android.database.Cursor c = context.getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI, null, 
				android.provider.CallLog.Calls.TYPE + " in (" + android.provider.CallLog.Calls.INCOMING_TYPE + ", " + android.provider.CallLog.Calls.MISSED_TYPE + " )" + extraQuerySelection
				, null,
				android.provider.CallLog.Calls.DATE + " DESC");

		String callRecordRowId = null;

		Cursor cursor = c;
		if (cursor.moveToFirst() && !cursor.isAfterLast()) {
			// Load ID of last call
			callRecordRowId = getLasCallRecordID(cursor);
		}

		if (callRecordRowId != null) {
			int deletedRows = context.getContentResolver().delete(
					android.provider.CallLog.Calls.CONTENT_URI,
					"_ID=" + callRecordRowId, null);
			if (deletedRows > 0)
				isDeleted = true;
			else
				isDeleted = false;
			Log.d(DNDTAG, "Row Deleted status: " + isDeleted);
		}
		return isDeleted;
	}

	private String getLasCallRecordID(Cursor cursor) {
		int idIndex = cursor.getColumnIndex(android.provider.CallLog.Calls._ID);
		return cursor.getString(idIndex);
	}
}
