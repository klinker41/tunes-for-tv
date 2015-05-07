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

import android.app.Activity;
import android.content.Context;
import com.klinker.android.spotify.AbstractSpotifyHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import static org.mockito.Mockito.verify;

public class SongItemLoaderTest extends AbstractSpotifyHelper {

    private Context context;
    private SongItemLoader loader;

    @Before
    public void setUp() {
        context = Robolectric.buildActivity(Activity.class).create().get();
        loader = Mockito.spy(new SongItemLoader(context, null));
    }

    @Test
    public void test_startLoading() {
        loader.onStartLoading();
        verify(loader).forceLoad();
    }

    @Test
    public void test_stopLoading() {
        loader.onStopLoading();
        verify(loader).cancelLoad();
    }

}