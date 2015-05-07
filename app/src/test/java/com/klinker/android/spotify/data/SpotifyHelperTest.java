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
import com.klinker.android.spotify.AbstractSpotifyHelper;
import com.klinker.android.spotify.loader.SpotifyOAuthTokenRefresher;
import kaaes.spotify.webapi.android.SpotifyApi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class SpotifyHelperTest extends AbstractSpotifyHelper {

    private SpotifyHelper helper;

    @Mock
    private Thread thread;

    @Mock
    private SpotifyOAuthTokenRefresher tokenRefresher;

    @Mock
    private RefreshToken refreshToken;

    @Mock
    private Settings settings;

    @Mock
    private SpotifyAccount account;

    @Before
    public void setUp() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        helper = Mockito.spy(SpotifyHelper.get(activity));
        helper.setSettings(settings);
        settings.spotifyAccount = account;
        doReturn("test_refresh_token").when(account).getRefreshToken();
        doReturn("test_auth_token").when(account).getAuthToken();
    }

    @Test
    public void test_init() {
        assertNotNull(helper.getSettings());
        assertNotNull(helper.getSpotifyApi());
    }

    @Test
    public void test_initAuthToken_noRefresh() {
        SpotifyApi api = Mockito.spy(helper.getSpotifyApi());
        helper.setSpotifyApi(api);
        doReturn(false).when(helper).needsTokenRefresh();

        helper.initAuthToken();

        verify(api).setAccessToken("test_auth_token");
        assertTrue(helper.isAuthTokenValid());
    }

    @Test
    public void test_initAuthToken() {
        SpotifyApi api = Mockito.spy(helper.getSpotifyApi());
        helper.setSpotifyApi(api);
        doReturn(true).when(helper).needsTokenRefresh();
        doReturn(thread).when(helper).getTokenRefresherThread();

        helper.initAuthToken();

        verify(api).setAccessToken("test_auth_token");
        verify(thread).start();
        assertFalse(helper.isAuthTokenValid());
    }

    @Test
    public void test_getTokenRefresherThread() {
        Thread thread = helper.getTokenRefresherThread();
        thread.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }

                verify(helper).refreshToken();
            }
        }).start();
    }

    @Test
    public void test_refreshToken() {
        doReturn(tokenRefresher).when(helper).getTokenRefresher();
        doReturn(refreshToken).when(tokenRefresher).useRefreshToken("test_refresh_token");
        doReturn("test_access_token").when(refreshToken).getAccessToken();
        doReturn(3600l).when(refreshToken).getExpiresIn();

        helper.refreshToken();

        verify(settings).setValue("spotify_auth_token", "test_access_token");
        verify(settings).setExpirationTimeFromNow("spotify_expiration_date", 3600l);
        assertTrue(helper.isAuthTokenValid());
    }

    @Test
    public void test_needsRefresh_true() {
        doReturn(3000000l).when(helper).getCurrentTime();
        doReturn(3000000l).when(account).getExpirationDate();

        assertTrue(helper.needsTokenRefresh());
    }

    @Test
    public void test_needsRefresh_5minutes() {
        doReturn(3000000l).when(helper).getCurrentTime();
        doReturn(3000000l + 299999l).when(account).getExpirationDate();

        assertTrue(helper.needsTokenRefresh());
    }

    @Test
    public void test_needsRefresh_false() {
        doReturn(3000000l).when(helper).getCurrentTime();
        doReturn(3000000l + 300001l).when(account).getExpirationDate();

        assertFalse(helper.needsTokenRefresh());
    }

    @Test
    public void test_needsRefresh_default() {
        doReturn(1l).when(helper).getCurrentTime();
        doReturn(-1l).when(account).getExpirationDate();

        assertFalse(helper.needsTokenRefresh());
    }

}