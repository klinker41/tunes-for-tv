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
import android.app.ProgressDialog;
import android.content.Intent;
import com.klinker.android.spotify.R;
import com.klinker.android.spotify.data.SpotifyHelper;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.Robolectric;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MainActivityTest extends AbstractActivityHelper {

    @Mock
    private SpotifyHelper spotifyHelper;

    @Mock
    private ProgressDialog progressDialog;

    @Test
    public void test_onCreate_noAuth() {
        doReturn(null).when(account).getAuthToken();

        getActivity().onCreate(null);

        verify(getActivity()).startActivity(any(Intent.class));
        verify(getActivity()).finish();
    }

    @Test
    public void test_onCreate_authValidToken() {
        doReturn("test").when(account).getAuthToken();
        MainActivity activity = (MainActivity) getActivity();
        doReturn(spotifyHelper).when(activity).getSpotifyHelper();
        doReturn(true).when(spotifyHelper).isAuthTokenValid();
        doReturn(progressDialog).when(activity).initProgressDialog(activity);

        activity.onCreate(null);

        verify(activity).setContentView(R.layout.main);
    }

    @Test
    public void test_onCreate_authInvalidToken() {
        doReturn("test").when(account).getAuthToken();
        MainActivity activity = (MainActivity) getActivity();
        doReturn(spotifyHelper).when(activity).getSpotifyHelper();
        doReturn(false).when(spotifyHelper).isAuthTokenValid();
        doReturn(progressDialog).when(activity).initProgressDialog(activity);

        activity.onCreate(null);

        verify(activity).waitForAuthToken();
    }

    @Test
    public void test_initProgressDialog() {
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        ProgressDialog dialog = ((MainActivity) getActivity()).initProgressDialog(activity);

        assertNotNull(dialog);
        assertTrue(dialog.isIndeterminate());
    }

    @Test
    public void test_dismissProgressDialog() {
        ((MainActivity) getActivity()).dismissProgressDialog(progressDialog);

        verify(progressDialog, times(1)).dismiss();
    }

    @Override
    public SpotifyAbstractActivity createActivity() {
        return new MainActivity();
    }

}