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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.klinker.android.spotify.AbstractSpotifyHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

public class SpotifyAccountTest extends AbstractSpotifyHelper {

    private SpotifyAccount spotifyAccount;

    @Mock
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        spotifyAccount = Mockito.spy(new SpotifyAccount(context));

        doReturn(sharedPreferences).when(spotifyAccount).getSharedPrefs(any(Context.class));
        doReturn("test_auth_token").when(sharedPreferences).getString("spotify_auth_token", null);
        doReturn("test_refresh_token").when(sharedPreferences).getString("spotify_refresh_token", null);
        doReturn(1000l).when(sharedPreferences).getLong("spotify_expiration_date", -1);
        doReturn("test_user_id").when(sharedPreferences).getString("spotify_user_id", null);

        spotifyAccount.initValues(context, sharedPreferences);
    }

    @Test
    public void test_getAuthToken() {
        assertEquals("test_auth_token", spotifyAccount.getAuthToken());
    }

    @Test
    public void test_getRefreshToken() {
        assertEquals("test_refresh_token", spotifyAccount.getRefreshToken());
    }

    @Test
    public void test_getExpirationDate() {
        assertEquals(1000, spotifyAccount.getExpirationDate());
    }

    @Test
    public void test_getUserId() {
        assertEquals("test_user_id", spotifyAccount.getUserId());
    }

}