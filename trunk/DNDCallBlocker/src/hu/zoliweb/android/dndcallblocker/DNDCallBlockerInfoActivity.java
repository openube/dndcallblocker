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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DNDCallBlockerInfoActivity extends ListActivity {

	private Context myContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		myContext = this;

		String[] menu_items = getResources().getStringArray(
				R.array.infomenu_items);
		setListAdapter(new ArrayAdapter<String>(myContext,
				R.layout.info_list_item, menu_items));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					// prepare the alert box
					AlertDialog.Builder aboutAlertbox = new AlertDialog.Builder(
							myContext);
					// set the icon to display
					aboutAlertbox.setIcon(R.drawable.icon);
					// set the title to display
					aboutAlertbox.setTitle(R.string.title_about);
					// set the message to display
					aboutAlertbox.setMessage(R.string.message_about);
					// add a neutral button to the alert box
					aboutAlertbox.setNeutralButton(R.string.message_ok, null);
					// show it
					aboutAlertbox.show();
					break;
				case 1:
					// prepare the alert box
					AlertDialog.Builder licenseAlertbox = new AlertDialog.Builder(
							myContext);
					// set the icon to display
					licenseAlertbox.setIcon(R.drawable.icon);
					// set the title to display
					licenseAlertbox.setTitle(R.string.title_license);
					// set the message to display
					licenseAlertbox.setMessage(R.string.message_license);
					// add a neutral button to the alert box
					licenseAlertbox.setNeutralButton(R.string.message_ok, null);
					// show it
					licenseAlertbox.show();
					break;
				case 2:
					// construct an alert dialog
					AlertDialog.Builder alert = new AlertDialog.Builder(
							myContext);

					// set icon, title and message
					alert.setIcon(R.drawable.icon);
					alert.setTitle(R.string.app_name);
					alert.setMessage(R.string.donate_message);

					// configure two buttons
					alert.setPositiveButton(R.string.message_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// when OK pressed, go to paypal donations
									// web page
									startActivity(new Intent(
											Intent.ACTION_VIEW,
											Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=JW4NJ9FTLEE2N")));
								}
							});

					alert.setNegativeButton(R.string.message_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Canceled. Do nothing.
								}
							});

					// show the dialog window
					alert.show();
					break;
				default:
					break;
				}
			}
		});
	}
}
