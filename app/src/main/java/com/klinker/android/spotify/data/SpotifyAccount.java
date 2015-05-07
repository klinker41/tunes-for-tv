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
import lombok.Getter;
import lombok.Setter;

/**
 * Helper class for getting spotify account oauth information
 */
@Getter @Setter
public class SpotifyAccount implements OAuthAccount {

    private String authToken;
    private String refreshToken;
    private long expirationDate;
    private String userId;

    public SpotifyAccount(Context context) {
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        initValues(context, sharedPrefs);
    }

    public void initValues(Context context, SharedPreferences sharedPreferences) {
        authToken = sharedPreferences.getString(context.getString(R.string.pref_spotify_auth_token_key), null);
        refreshToken = sharedPreferences.getString(context.getString(R.string.pref_spotify_refresh_token_key), null);
        expirationDate = sharedPreferences.getLong(context.getString(R.string.pref_spotify_expiration_date_key), -1);
        userId = sharedPreferences.getString(context.getString(R.string.pref_spotify_user_id_key), null);
    }

    public SharedPreferences getSharedPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
