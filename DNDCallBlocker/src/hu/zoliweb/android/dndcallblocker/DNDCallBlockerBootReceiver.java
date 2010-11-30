package hu.zoliweb.android.dndcallblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DNDCallBlockerBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DNDCallBlockerNotifier notifier = new DNDCallBlockerNotifier(context);
		notifier.updateNotification();
	}

}
