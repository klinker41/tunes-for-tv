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

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.webkit.WebView;
import com.klinker.android.spotify.AbstractSpotifyHelper;
import com.klinker.android.spotify.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class BaseOAuthFragmentTest extends AbstractSpotifyHelper {

    private BaseOAuthFragment fragment;

    @Before
    public void setUp() {
        fragment = new SpotifyOAuthFragment();
        TestUtil.startFragment(fragment);

        fragment = Mockito.spy(fragment);
        doReturn("login_url").when(fragment).getLoginUrl();
        doReturn("scope").when(fragment).getScope();
        doReturn("token_url").when(fragment).getTokenUrl();
        doReturn("client_id").when(fragment).getClientId();
        doReturn("client_secret").when(fragment).getClientSecret();
        doReturn("redirect_url").when(fragment).getRedirectUri();
        doReturn("grant_type").when(fragment).getGrantType();
        doNothing().when(fragment).saveAuthToken(anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    public void test_onCreateView() {
        View view = fragment.onCreateView(fragment.getActivity().getLayoutInflater(), null, null);

        assertNotNull(view);
        assertTrue(view instanceof WebView);
    }

    @Test
    public void test_shouldRedirect() {
        assertTrue(fragment.shouldRedirect("redirect_url?code=blahblahblah"));
    }

    @Test
    public void test_shouldNotRedirect() {
        assertFalse(fragment.shouldRedirect("https://accounts.spotify.com/"));
    }

    @Test
    public void test_processPageFinished() {
        String authCode = "blahblahblah";
        BaseOAuthFragment.TokenGet getter = Mockito.mock(BaseOAuthFragment.TokenGet.class);
        doReturn(getter).when(fragment).getTokenAsyncTask(authCode);

        boolean finished = fragment.processPageFinished("https://localhost?code=" + authCode, false);

        assertTrue(finished);
        verify(getter).execute();
    }

    @Test
    public void test_processPageFinished_nothing() {
        boolean finished = fragment.processPageFinished("https://localhost", false);
        assertFalse(finished);
    }

    @Test
    public void test_processPageFinished_alreadyDone() {
        boolean finished = fragment.processPageFinished("https://localhost", true);
        assertTrue(finished);
    }

    @Test
    public void test_processPageFinished_error() {
        boolean finished = fragment.processPageFinished("https://localhost?error=access_denied", false);
        assertTrue(finished);
    }

    @Test
    public void test_onStart() {
        fragment.onStart();

        verify(fragment).superOnStart();
        verify(fragment).startLogin();
    }

    @Test
    public void test_startLogin() {
        fragment.loginWebView = Mockito.spy(fragment.loginWebView);

        fragment.startLogin();

        verify(fragment.loginWebView).loadUrl(anyString());
    }

    @Test
    public void test_buildLoginUrl() {
        assertEquals(
                "login_url?redirect_uri=redirect_url&response_type=code&client_id=client_id&scope=scope",
                fragment.buildLoginUrl()
        );
    }

    @Test
    public void test_finishAuthorization() {
        Activity activity = Mockito.spy(fragment.getActivity());
        doReturn(activity).when(fragment).getActivity();

        fragment.finishAuthorization();

        verify(fragment).startActivity(any(Intent.class));
        verify(activity).finish();
    }

}