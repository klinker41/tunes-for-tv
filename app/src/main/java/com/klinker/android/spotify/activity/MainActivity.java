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

package com.klinker.android.spotify.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.klinker.android.spotify.R;
import com.klinker.android.spotify.data.Settings;
import com.klinker.android.spotify.data.SpotifyHelper;

/**
 * Activity for displaying all playlists to the user and allowing them to view and listen to songs in each playlist.
 * Settings can also be viewed from the bottom row
 */
public class MainActivity extends SpotifyAbstractActivity {

    private Settings settings;
    private SpotifyHelper spotifyHelper;
    private ProgressDialog loadingAuthToken;

    /**
     * Set up settings and spotify helper and get the correct/valid auth token
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getSettings();
        spotifyHelper = getSpotifyHelper();

        if (settings.spotifyAccount.getAuthToken() == null) {
            Intent intent = new Intent(this, SpotifyOAuthActivity.class);
            startActivity(intent);
            finish();
        } else {
            if (spotifyHelper.isAuthTokenValid()) {
                setContentView(R.layout.main);
            } else {
                loadingAuthToken = initProgressDialog(this);
                loadingAuthToken.show();
                waitForAuthToken();
            }
        }
    }

    /**
     * Ensure that we are still logged in. We could be logged out at the end of a long music session, since tokens only
     * last for 1 hour.
     */
    @Override
    public void onStart() {
        super.onStart();
        checkLoggedIn(spotifyHelper);
    }

    /**
     * Wait for the auth token to be found by the Spotify helper. Auth tokens expire after an hour or so, so this
     * will almost always need to be called when starting the app
     */
    public void waitForAuthToken() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (spotifyHelper.isAuthTokenValid()) {
                    dismissProgressDialog(loadingAuthToken);
                    setContentView(R.layout.main);
                } else {
                    waitForAuthToken();
                }
            }
        }, 200);
    }

    /**
     * Display a progress dialog to notify user that app is loading, meaning that the auth token is being refreshed
     */
    protected ProgressDialog initProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.loading));
        progressDialog.setIndeterminate(true);
        return progressDialog;
    }

    /**
     * Dismiss the progress dialog after loading is complete, abstracted for testing purposes
     * @param progressDialog the progress dialog to be dismissed
     */
    protected void dismissProgressDialog(ProgressDialog progressDialog) {
        progressDialog.dismiss();
    }

    /**
     * Get the spotify helper for auth purposes
     */
    public SpotifyHelper getSpotifyHelper() {
        return SpotifyHelper.get(this);
    }

}
