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

package com.klinker.android.spotify.fragment;

import com.klinker.android.spotify.R;
import com.klinker.android.spotify.data.Settings;
import com.klinker.android.spotify.data.SpotifyHelper;

/**
 * Implements BaseOAuthFragment specifically for Spotify
 */
public class SpotifyOAuthFragment extends BaseOAuthFragment {

    private static final String REDIRECT_URL = "https://localhost";

    /**
     * Url that we need to login at for Spotify
     */
    @Override
    public String getLoginUrl() {
        return SpotifyHelper.AUTHORIZE_URL;
    }

    /**
     * Get the scope of data we will be reading from Spotify's services
     */
    @Override
    public String getScope() {
        return "playlist-read-private streaming user-read-private";
    }

    /**
     * Find the URL we need for making a new token request
     */
    @Override
    public String getTokenUrl() {
        return SpotifyHelper.TOKEN_URL;
    }

    /**
     * Make request using Tune's client id
     */
    @Override
    public String getClientId() {
        return SpotifyHelper.CLIENT_ID;
    }

    /**
     * Make request using Tune's client secret
     */
    @Override
    public String getClientSecret() {
        return SpotifyHelper.CLIENT_SECRET;
    }

    /**
     * Specify where to redirect to after auth complete
     */
    @Override
    public String getRedirectUri() {
        return REDIRECT_URL;
    }

    /**
     * Get the oauth grant type we want
     */
    @Override
    public String getGrantType() {
        return "authorization_code";
    }

    /**
     * Save the auth token and other information to the shared prefs so we don't have to login every time
     */
    @Override
    public void saveAuthToken(String authToken, long expire, String refreshToken, String userId) {
        Settings settings = getSettings();
        settings.setValue(getString(R.string.pref_spotify_auth_token_key), authToken);
        settings.setValue(getString(R.string.pref_spotify_refresh_token_key), refreshToken);
        settings.setExpirationTimeFromNow(getString(R.string.pref_spotify_expiration_date_key), expire);
        settings.setValue(getString(R.string.pref_spotify_user_id_key), userId);
    }

    protected Settings getSettings() {
        return Settings.get(getActivity());
    }

}
