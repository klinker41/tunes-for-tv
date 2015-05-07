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

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class for parsing JSON information
 */
public class JSONParser {

    private static final String TAG = "JSONParser";
    private static final boolean DEBUG = false;

    /**
     * Get a string of a specified name from a JSON object
     */
    public String parseString(JSONObject object, String name) {
        try {
            return object.getString(name);
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, "No string value for " + name, e);
            return "";
        }
    }

    /**
     * Get a long of a specified name from a JSON object
     */
    public long parseLong(JSONObject object, String name) {
        try {
            return object.getLong(name);
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, "No long value for " + name, e);
            return 0;
        }
    }

    /**
     * Get a boolean of a specified name from a JSON object
     */
    public boolean parseBoolean(JSONObject object, String name) {
        try {
            return object.getBoolean(name);
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, "No boolean value for " + name, e);
            return false;
        }
    }

    /**
     * Get a JSON object of a specified name from a JSON object
     */
    public JSONObject parseJSONObject(JSONObject object, String name) {
        try {
            return object.getJSONObject(name);
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, "No json object for " + name, e);
            return null;
        }
    }

    /**
     * Get a JSON array of a specified name from a JSON object
     */
    public JSONArray parseJSONArray(JSONObject object, String name) {
        try {
            return object.getJSONArray(name);
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, "no json array for " + name, e);
            return null;
        }
    }

}
