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

import java.text.SimpleDateFormat;
import java.util.Date;

public class DNDCallBlockerLogItem {
	String phone_nr;
	Date created;

	public String getPhoneNr() {
		return phone_nr;
	}

	public Date getCreated() {
		return created;
	}

	public DNDCallBlockerLogItem(String _phone_nr) {
		this(_phone_nr, new Date(java.lang.System.currentTimeMillis()));
	}

	public DNDCallBlockerLogItem(String _phone_nr, Date _created) {
		phone_nr = _phone_nr;
		created = _created;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
		String dateString = sdf.format(created);
		return "(" + dateString + ") " + phone_nr;
	}
}
