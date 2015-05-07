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

import android.os.Bundle;
import com.klinker.android.spotify.AbstractSpotifyHelper;
import com.klinker.android.spotify.data.Settings;
import com.klinker.android.spotify.data.SpotifyAccount;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

public abstract class AbstractActivityHelper extends AbstractSpotifyHelper {

    private SpotifyAbstractActivity activity;

    @Mock
    public Settings settings;

    @Mock
    public SpotifyAccount account;

    @Before
    public void setUp() {
        activity = Mockito.spy(createActivity());

        doReturn(settings).when(activity).getSettings();
        settings.spotifyAccount = account;

        doNothing().when(activity).superOnCreate(any(Bundle.class));
        doNothing().when(activity).superOnStart();
        doNothing().when(activity).superOnResume();
        doNothing().when(activity).superOnDestroy();
        doNothing().when(activity).superOnStop();
        doNothing().when(activity).superOnPause();
        doNothing().when(activity).superSetContentView(anyInt());
    }

    public SpotifyAbstractActivity getActivity() {
        return activity;
    }

    public abstract SpotifyAbstractActivity createActivity();
}