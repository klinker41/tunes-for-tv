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

import android.app.Activity;
import android.os.Bundle;
import com.klinker.android.spotify.data.Settings;
import com.klinker.android.spotify.data.SpotifyHelper;
import com.klinker.android.spotify.util.OnAuthTokenRefreshedListener;

/**
 * Abstract activity that can be used for helping with lifecycle in testing process
 */
public class SpotifyAbstractActivity extends Activity {

    public void superOnCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void superOnStart() {
        super.onStart();
    }

    public void superOnResume() {
        super.onResume();
    }

    public void superOnDestroy() {
        super.onDestroy();
    }

    public void superOnStop() {
        super.onStop();
    }

    public void superOnPause() {
        super.onPause();
    }

    public void superSetContentView(int contentView) {
        super.setContentView(contentView);
    }

    @Override
    public void setContentView(int contentView) {
        superSetContentView(contentView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        superOnCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        superOnStart();
    }

    @Override
    public void onResume() {
        superOnResume();
    }

    @Override
    public void onDestroy() {
        superOnDestroy();
    }

    @Override
    public void onStop() {
        superOnStop();
    }

    @Override
    public void onPause() {
        superOnPause();
    }

    public void checkLoggedIn(final SpotifyHelper helper) {
        checkLoggedIn(helper, null);
    }

    public void checkLoggedIn(final SpotifyHelper helper, final OnAuthTokenRefreshedListener listener) {
        if (!helper.isAuthTokenValid()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    helper.refreshToken();

                    if (listener != null) {
                        listener.authTokenRefreshed();
                    }
                }
            }).start();
        }
    }

    public Settings getSettings() {
        return Settings.get(this);
    }

}
