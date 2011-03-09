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
import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class DNDCallBlockerLogActivity extends Activity {

	DNDCallBlockerDBAdapter logDBAdapter;
	Cursor logCursor;

	private ArrayList<DNDCallBlockerLogItem> logItems;
	private ListView myListView;
	private DNDCallBlockerLogItemAdapter lia;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.loglist);
		
		myListView = (ListView)findViewById(R.id.myListView);

	    logItems = new ArrayList<DNDCallBlockerLogItem>();
	    int resID = R.layout.loglist_item;
	    lia = new DNDCallBlockerLogItemAdapter(this, resID, logItems);
	    myListView.setAdapter(lia);

		logDBAdapter = new DNDCallBlockerDBAdapter(this);
		logDBAdapter.open();

		populateLog();
	}

	private void populateLog() {
		// Get all the todo list items from the database.
		logCursor = logDBAdapter.getAllLogCursor();
		startManagingCursor(logCursor);
		// Update the array.
		updateArray();
	}

	private void updateArray() {
		logCursor.requery();
		
		logItems.clear();
		
		if (logCursor.moveToFirst())
		    do { 
		      String task = logCursor.getString(logCursor.getColumnIndex(DNDCallBlockerDBAdapter.KEY_PHONENR));
		      long created = logCursor.getLong(logCursor.getColumnIndex(DNDCallBlockerDBAdapter.KEY_CREATION_DATE));

		      DNDCallBlockerLogItem newItem = new DNDCallBlockerLogItem(task, new Date(created));
		      logItems.add(0, newItem);
		    } while(logCursor.moveToNext());
		  
		  lia.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Close the database
		logDBAdapter.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.log_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.log_clear:
	        logDBAdapter.clearAllItems();
	        updateArray();
	        lia.notifyDataSetChanged();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

}
