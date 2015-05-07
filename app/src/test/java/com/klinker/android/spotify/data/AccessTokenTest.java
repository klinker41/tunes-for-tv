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

import com.klinker.android.spotify.AbstractSpotifyHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class AccessTokenTest extends AbstractSpotifyHelper {

    private AccessToken accessToken;

    @Mock
    private DefaultHttpClient client;

    @Mock
    private HttpResponse response;

    @Mock
    private HttpEntity entity;

    @Mock
    private InputStream stream;

    @Mock
    private InputStreamReader streamReader;

    @Mock
    private BufferedReader reader;

    @Before
    public void setUp() throws Exception {
        accessToken = Mockito.spy(new AccessToken());

        doReturn(client).when(accessToken).getHttpClient();
        doReturn(response).when(accessToken).executeRequest(any(HttpPost.class));
        doReturn(entity).when(response).getEntity();
        doReturn(stream).when(entity).getContent();
        doReturn(streamReader).when(accessToken).getInputStreamReader(stream);
        doReturn(reader).when(accessToken).getBufferedReader(streamReader);
        doReturn(null).when(reader).readLine();
        doReturn(new JSONObject("{\n" +
                "   \"access_token\": \"test_auth_token\",\n" +
                "   \"token_type\": \"Bearer\",\n" +
                "   \"expires_in\": 3600,\n" +
                "   \"refresh_token\": \"test_refresh_token\"\n" +
                "}")).when(accessToken).createJSON("");
    }

    public JSONObject invokeGetToken() {
        return accessToken.getToken("test_address", "test code", "test client id", "test client secret", "client url", "test grant type");
    }

    @Test
    public void test_getAuthToken() throws Exception {
        assertEquals("test_auth_token", invokeGetToken().getString("access_token"));
    }

    @Test
    public void test_getExpiresIn() throws Exception {
        assertEquals(3600, invokeGetToken().getLong("expires_in"));
    }

    @Test
    public void test_getRefreshToken() throws Exception {
        assertEquals("test_refresh_token", invokeGetToken().getString("refresh_token"));
    }

}