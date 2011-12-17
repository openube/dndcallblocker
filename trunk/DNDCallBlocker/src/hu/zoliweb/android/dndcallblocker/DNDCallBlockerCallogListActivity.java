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

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract.PhoneLookup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class DNDCallBlockerCallogListActivity extends ListActivity {

	private static final String TAG = "DNDCallBlockerCallogListActivity";

	private ArrayList<String> m_phones;
	private ArrayList<Integer> m_types;
	private LayoutInflater m_Inflater;
	private ArrayAdapter<String> m_adapter;

	/**
	 * Called when the activity is first created. Responsible for initializing
	 * the UI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.callog_list);

		m_Inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_phones = new ArrayList<String>();
		m_types = new ArrayList<Integer>();

		// get cursor for call log
		Uri allCalls = Uri.parse("content://call_log/calls");
		Cursor c = managedQuery(allCalls, null, null, null, "date DESC");

		if (c.moveToFirst()) {
			do {
				String num = c
						.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
				if (num.length() > 2) {
					m_phones.add(num);

					switch (Integer.parseInt(c.getString(c
							.getColumnIndex(CallLog.Calls.TYPE)))) {
					case 1:
						m_types.add(R.drawable.sym_call_incoming);
						break;
					case 2:
						m_types.add(R.drawable.sym_call_outgoing);
						break;
					case 3:
						m_types.add(R.drawable.sym_call_missed);
						break;
					default:
						m_types.add(null);
						break;
					}
				}
			} while (c.moveToNext());
			c.close();
		}

		// this adapter makes the call history list visible to user
		m_adapter = new ArrayAdapter<String>(this, R.layout.callog_list_item,
				m_phones) {
			@Override
			public View getView(int position, View reusableView,
					ViewGroup parent) {
				View row;

				if (reusableView == null) {
					row = m_Inflater.inflate(R.layout.callog_list_item, null);
				} else {
					row = reusableView;
				}

				TextView tv1 = (TextView) row.findViewById(android.R.id.text1);
				tv1.setText(getItem(position));

				String contactName = null;
				ContentResolver cr = getContentResolver();
				Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
						Uri.encode(getItem(position)));
				Cursor cur = cr.query(uri,
						new String[] { PhoneLookup.DISPLAY_NAME }, null, null,
						null);
				if (cur.moveToFirst()) {
					String name = cur.getString(cur
							.getColumnIndex(PhoneLookup.DISPLAY_NAME));
					contactName = name;
				} else {
					contactName = "N/A";
				}
				cur.close();
				TextView tv2 = (TextView) row.findViewById(android.R.id.text2);
				tv2.setText(contactName);

				if ( m_types.get(position) != null ) {
					// only if call type is known (m_types.get(position) is not null) 
					ImageView iv1 = (ImageView) row.findViewById(R.id.imageview1);
					iv1.setImageResource(m_types.get(position));
				}

				return row;
			}
		};
		setListAdapter(m_adapter);

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		// to handle taps on list items
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// save selected phone nr then return to parent activity
				setResult(RESULT_OK,
						(new Intent()).setAction(m_phones.get(position)));
				finish();
			}
		});

	}

}
