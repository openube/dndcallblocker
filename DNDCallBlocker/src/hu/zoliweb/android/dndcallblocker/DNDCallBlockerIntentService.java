package hu.zoliweb.android.dndcallblocker;


import java.lang.reflect.Method;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

public class DNDCallBlockerIntentService extends IntentService {
	private static final String DNDTAG = "DNDCallBlocker";

	public DNDCallBlockerIntentService() {
		super("DNDCallBlockerIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getBaseContext();
		
		Log.d(DNDTAG, "SRV: Got control.");

		// Make sure the phone is still ringing
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
			Log.d(DNDTAG, "SRV: Not ringing anymore.");
			return;
		}

		// Block the call
		try {
			handlePhoneCall(context);
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.d(DNDTAG,"Error trying to reject using telephony service.");
		}

		return;
	}

	@SuppressWarnings("rawtypes")
	private void handlePhoneCall(Context context) throws Exception {
		// Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Class c = Class.forName(tm.getClass().getName());
		Method m = c.getDeclaredMethod("getITelephony");
		m.setAccessible(true);
		ITelephony telephonyService;
		telephonyService = (ITelephony)m.invoke(tm);

		// Load preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		// call handling preference
		String handle_call = prefs.getString("handle_call", "silence");
		
		Log.d(DNDTAG, "SRV: pref=".concat(handle_call));
		
		// Silence the ringer first
		telephonyService.silenceRinger();
		
		if (handle_call.equals("block")) {
			Log.d(DNDTAG, "SRV: Block");
			// just block the call
			telephonyService.endCall();
			return;
		}
		
		// FIXME: sometimes just answer, but never hang up
		// not used handling mode
		if (handle_call.equals("answer_then_block")) {
			// pick up the phone
			telephonyService.answerRingingCall();
			// then end the call
			telephonyService.endCall();
			return;
		}
		// phone is still ringing... but in silence
	}
}
