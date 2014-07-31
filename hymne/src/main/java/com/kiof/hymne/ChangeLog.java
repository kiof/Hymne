/**
 * Copyright (C) 2011, Karsten Priegnitz
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * @author: Karsten Priegnitz
 * @see: http://code.google.com/p/android-change-log/
 */

package com.kiof.hymne;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class ChangeLog {

	private static final String TAG = "ChangeLog";
	private String lastVersion, thisVersion;

	// this is the key for storing the version name in SharedPreferences
	private static final String VERSION_KEY = "PREFS_VERSION_KEY";

	/**
	 * Constructor
	 * 
	 * Retrieves the version names and stores the new version name in
	 * SharedPreferences
	 */
	public ChangeLog(Context context) {
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

		// get version numbers
		this.lastVersion = sp.getString(VERSION_KEY, "");
		Log.d(TAG, "lastVersion: " + lastVersion);
		try {
			this.thisVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			this.thisVersion = "?";
			Log.e(TAG, "could not get version name from manifest!");
			e.printStackTrace();
		}
		Log.d(TAG, "appVersion: " + this.thisVersion);

		// save new version number to preferences
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(VERSION_KEY, this.thisVersion);
		editor.apply();
	}

	/**
	 * @return The version name of the last installation of this app (as
	 *         described in the former manifest). This will be the same as
	 *         returned by <code>getThisVersion()</code> from the second time
	 *         this version of the app is launched (more precisely: the second
	 *         time ChangeLog is instantiated).
	 */
	public String getLastVersion() {
		return this.lastVersion;
	}

	/**
	 * manually set the last version name - for testing purposes only
	 * 
	 * @param lastVersion
	 */
	void setLastVersion(String lastVersion) {
		this.lastVersion = lastVersion;
	}

	/**
	 * @return The version name of this app as described in the manifest.
	 */
	public String getThisVersion() {
		return this.thisVersion;
	}

	/**
	 * @return <code>true</code> if this version of your app is started the
	 *         first time
	 */
	public boolean firstRun() {
		return !this.lastVersion.equals(this.thisVersion);
	}

	/**
	 * @return <code>true</code> if your app is started the first time ever.
	 *         Also <code>true</code> if your app was deinstalled and installed
	 *         again.
	 */
	public boolean firstRunEver() {
		return "".equals(this.lastVersion);
	}

}