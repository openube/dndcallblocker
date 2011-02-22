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
import java.util.ArrayList;

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
	private String _phoneNr;
	
	DNDCallBlockerDBAdapter logDBAdapter;

	public DNDCallBlockerIntentService() {
		super("DNDCallBlockerIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getBaseContext();

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
		if (_phoneNr != null)
		{
			logDBAdapter.insertToLog(_phoneNr.trim());
		} else {
			logDBAdapter.insertToLog("");
		}
        logDBAdapter.close();
		
		// wait 2 sec, then delete last call from phone history
		try {
			Thread.sleep(2000);
			deleteLastCall(context);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(DNDTAG, "There was some problem with deleteLastCall procedure.");
		}

		return;
	}

	@SuppressWarnings("rawtypes")
	private void handlePhoneCall(Context context) throws Exception {
		// Set up communication with the telephony service (thanks to Tedd's
		// Droid Tools!)
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Class c = Class.forName(tm.getClass().getName());
		Method m = c.getDeclaredMethod("getITelephony");
		m.setAccessible(true);
		ITelephony telephonyService;
		telephonyService = (ITelephony) m.invoke(tm);

		// Load preferences
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		// call handling preference
		String handle_call = prefs.getString("handle_call", "silence");

		Log.d(DNDTAG, "SRV: pref=".concat(handle_call));

		// Silence the ringer first
		AudioManager am = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		// save original audio state
		int old_mode = am.getRingerMode();
		//set to silent
		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);

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
		//TODO: add selection to query
		android.database.Cursor c = context.getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI, null, null, null,
				android.provider.CallLog.Calls.DATE + " DESC");
		ArrayList<CallRecord> CallRecordsList = new ArrayList<CallRecord>();
		// Retrieve the column-indices of phoneNumber, date and calltype
		Cursor cursor = c;
		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				// Load attributes of last call
				CallRecord CallRecords = LoadCallRecord(cursor);
				CallRecordsList.add(CallRecords);
				cursor.moveToNext();
			}
		}
		if (CallRecordsList.size() > 0) {
			int deletedRows = context.getContentResolver().delete(
					android.provider.CallLog.Calls.CONTENT_URI,
					"_ID=" + CallRecordsList.get(0).row_id, null);
			if (deletedRows > 0)
				isDeleted = true;
			else
				isDeleted = false;
			Log.d(DNDTAG, "Row Deleted status: " + isDeleted);
		}
		return isDeleted;
	}

	private CallRecord LoadCallRecord(Cursor cursor) {
		CallRecord contactData = new CallRecord();
		//TODO: needs restructuring
		String[] ColumnNames = cursor.getColumnNames();
		for (int intLoop = 0; intLoop < ColumnNames.length; intLoop++) {
			// Load id of Last call
			if (android.provider.CallLog.Calls._ID
					.compareTo(ColumnNames[intLoop]) == 0) {
				contactData.row_id = cursor.getString(intLoop);
			}
		}

		return contactData;
	}

	class CallRecord {
		public String row_id;
	}
}
