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

package com.klinker.android.spotify.loader;

import com.klinker.android.spotify.AbstractSpotifyHelper;
import com.klinker.android.spotify.data.SpotifyHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpotifyOAuthTokenRefresherTest extends AbstractSpotifyHelper {

    private SpotifyOAuthTokenRefresher refresher;

    @Before
    public void setUp() {
        refresher = new SpotifyOAuthTokenRefresher();
    }

    @Test
    public void test_getTokenUrl() {
        assertEquals("https://accounts.spotify.com/api/token", refresher.getTokenUrl());
    }

    @Test
    public void test_getClientId() {
        assertEquals(SpotifyHelper.CLIENT_ID, refresher.getClientId());
    }

    @Test
    public void test_getClientSecret() {
        assertEquals(SpotifyHelper.CLIENT_SECRET, refresher.getClientSecret());
    }

}