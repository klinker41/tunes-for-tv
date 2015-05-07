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

import com.klinker.android.spotify.AbstractSpotifyHelper;
import com.klinker.android.spotify.data.Settings;
import com.klinker.android.spotify.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class SpotifyOAuthFragmentTest extends AbstractSpotifyHelper {

    private SpotifyOAuthFragment fragment;

    @Before
    public void setUp() {
        fragment = new SpotifyOAuthFragment();
        TestUtil.startFragment(fragment);
    }

    @Test
    public void test_getRedirectUrl() {
        assertEquals("https://localhost", fragment.getRedirectUri());
    }

    @Test
    public void test_getLoginUrl() {
        assertEquals("https://accounts.spotify.com/authorize", fragment.getLoginUrl());
    }

    @Test
    public void test_getScope() {
        String perm1 = "playlist-read-private";
        String perm2 = "streaming";
        String perm3 = "user-read-private";

        String[] permissions = fragment.getScope().split(" ");

        assertEquals(3, permissions.length);
        assertEquals(perm1, permissions[0]);
        assertEquals(perm2, permissions[1]);
        assertEquals(perm3, permissions[2]);
    }

    @Test
    public void test_getTokenUrl() {
        assertEquals("https://accounts.spotify.com/api/token", fragment.getTokenUrl());
    }

    @Test
    public void test_getClientId() {
        assertNotNull(fragment.getClientId());
    }

    @Test
    public void test_getClientSecret() {
        assertNotNull(fragment.getClientSecret());
    }

    @Test
    public void test_getGrantType() {
        assertEquals("authorization_code", fragment.getGrantType());
    }

    @Test
    public void test_saveAuthToken() {
        fragment = Mockito.spy(fragment);
        Settings settings = Mockito.spy(Settings.get(fragment.getActivity()));
        doReturn(settings).when(fragment).getSettings();

        fragment.saveAuthToken("auth_token", 1l, "refresh_token", "user_id");

        verify(settings).setValue("spotify_auth_token", "auth_token");
        verify(settings).setValue("spotify_refresh_token", "refresh_token");
        verify(settings).setValue(eq("spotify_expiration_date"), anyLong());
        verify(settings).setValue("spotify_user_id", "user_id");
    }

}