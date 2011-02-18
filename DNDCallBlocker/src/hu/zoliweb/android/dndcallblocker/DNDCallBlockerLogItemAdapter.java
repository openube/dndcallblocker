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

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DNDCallBlockerLogItemAdapter extends
		ArrayAdapter<DNDCallBlockerLogItem> {
	Context context;
	int resource;

	public DNDCallBlockerLogItemAdapter(Context _context, int _resource,
			List<DNDCallBlockerLogItem> _items) {
		super(_context, _resource, _items);
		context = _context;
		resource = _resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout todoView;

		DNDCallBlockerLogItem item = getItem(position);

		String phoneNrString = item.getPhoneNr();
		if (phoneNrString.equals("")) {
			phoneNrString = context.getString(R.string.text_unknownnr);
		}
		Date createdDate = item.getCreated();
		String dateString = DateFormat.getDateTimeInstance()
				.format(createdDate);

		if (convertView == null) {
			todoView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					inflater);
			vi.inflate(resource, todoView, true);
		} else {
			todoView = (LinearLayout) convertView;
		}

		TextView dateView = (TextView) todoView.findViewById(R.id.rowDate);
		TextView phoneNrView = (TextView) todoView.findViewById(R.id.row);

		dateView.setText(dateString);
		phoneNrView.setText(phoneNrString);

		return todoView;
	}
}
