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
import com.klinker.android.spotify.AbstractSpotifyHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SettingsTest extends AbstractSpotifyHelper {

    private Activity activity;
    private Settings settings;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).create().get();
        settings = Mockito.spy(Settings.get(activity));
    }

    @Test
    public void test_create() {
        assertNotNull(settings.spotifyAccount);
    }

    @Test
    public void test_forceUpdate() {
        settings.forceUpdate();
        verify(settings, times(1)).init(any(Context.class));
    }

    @Test
    public void test_setBooleanValue() {
        settings.setValue("test", true);
        verify(settings, times(1)).init(any(Context.class));
        assertTrue(settings.getSharedPrefs().getBoolean("test", false));
    }

    @Test
    public void test_setIntValue() {
        settings.setValue("test", 1);
        verify(settings, times(1)).init(any(Context.class));
        assertTrue(settings.getSharedPrefs().getInt("test", 2) == 1);
    }

    @Test
    public void test_setStringValue() {
        settings.setValue("test", "test string");
        verify(settings, times(1)).init(any(Context.class));
        assertTrue(settings.getSharedPrefs().getString("test", "not test string").equals("test string"));
    }

    @Test
    public void test_setLongValue() {
        settings.setValue("test", 111L);
        verify(settings, times(1)).init(any(Context.class));
        assertEquals(111L, settings.getSharedPrefs().getLong("test", -1));
    }

    @Test
    public void test_removeKey() {
        settings.setValue("test", "testvalue");
        assertEquals("testvalue", settings.getSharedPrefs().getString("test", null));
        settings.removeValue("test");
        assertEquals(null, settings.getSharedPrefs().getString("test", null));
    }

    @Test
    public void test_setExpirationDate() {
        long seconds = 1001;
        long time = System.currentTimeMillis();
        when(settings.getNow()).thenReturn(time);

        assertEquals((seconds * 1000) + time, settings.setExpirationTimeFromNow("test", seconds));
    }

}