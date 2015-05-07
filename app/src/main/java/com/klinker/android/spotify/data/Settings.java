/*
 * Copyright (C) 2015 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.spotify.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.klinker.android.spotify.R;

/**
 * Helper class for accessing all settings information
 */
public class Settings {

    private static final String TAG = "Settings";
    private static volatile Settings settings;

    private static final boolean DEFAULT_SHUFFLE = true;
    private static final boolean DEFAULT_REPEAT = true;

    /**
     * Get a singleton settings object, shared between all classes
     */
    public static synchronized Settings get(Context context) {
        if (settings == null) {
            settings = new Settings(context);
        }

        return settings;
    }

    private Context context;
    public OAuthAccount spotifyAccount;
    public boolean shuffle;
    public boolean repeat;

    private Settings() {
    }

    private Settings(final Context context) {
        init(context);
    }

    protected void init(Context context) {
        this.context = context;
        spotifyAccount = new SpotifyAccount(context);

        SharedPreferences sharedPrefs = getSharedPrefs();

        shuffle = sharedPrefs.getBoolean(context.getString(R.string.pref_shuffle), DEFAULT_SHUFFLE);
        repeat = sharedPrefs.getBoolean(context.getString(R.string.pref_repeat), DEFAULT_REPEAT);
    }

    protected SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Reinitialize the settings object
     */
    public void forceUpdate() {
        init(context);
    }

    /**
     * Set a new boolean value to shared prefs
     */
    protected void setValue(String key, boolean value, boolean forceUpdate) {
        getSharedPrefs().edit()
                .putBoolean(key, value)
                .commit();
        if (forceUpdate) {
            forceUpdate();
        }
    }

    /**
     * Set a new int value to shared prefs
     */
    protected void setValue(String key, int value, boolean forceUpdate) {
        getSharedPrefs().edit()
                .putInt(key, value)
                .commit();
        if (forceUpdate) {
            forceUpdate();
        }
    }

    /**
     * Set a new string value to shared prefs
     */
    protected void setValue(String key, String value, boolean forceUpdate) {
        getSharedPrefs().edit()
                .putString(key, value)
                .commit();
        if (forceUpdate) {
            forceUpdate();
        }
    }

    /**
     * Set a new long value to shared prefs
     */
    protected void setValue(String key, long value, boolean forceUpdate) {
        getSharedPrefs().edit()
                .putLong(key, value)
                .commit();
        if (forceUpdate) {
            forceUpdate();
        }
    }

    /**
     * Set a new boolean value to shared prefs
     */
    public void setValue(String key, boolean value) {
        setValue(key, value, true);
    }

    /**
     * Set a new int value to shared prefs
     */
    public void setValue(String key, int value) {
        setValue(key, value, true);
    }

    /**
     * Set a new string value to shared prefs
     */
    public void setValue(String key, String value) {
        setValue(key, value, true);
    }

    /**
     * Set a new long value to shared prefs
     */
    public void setValue(String key, long value) {
        setValue(key, value, true);
    }

    /**
     * Remove a value from shared prefs
     */
    public void removeValue(String key) {
        getSharedPrefs().edit().remove(key).commit();
    }

    public long setExpirationTimeFromNow(String key, long expirationDate) {
        expirationDate = getExpirationDate(expirationDate);
        setValue(key, expirationDate);
        return expirationDate;
    }

    public long getExpirationDate(long secondsFromNow) {
        return secondsFromNow * 1000 + getNow();
    }

    protected long getNow() {
        return System.currentTimeMillis();
    }

}
