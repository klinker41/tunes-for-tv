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
import com.klinker.android.spotify.data.RefreshToken;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OAuthTokenRefresherTest extends AbstractSpotifyHelper {

    private OAuthTokenRefresher refresher;

    @Mock
    private HttpPost post;

    @Mock
    private InputStream stream;

    @Mock
    private JSONObject jsonObject;

    @Mock
    private HttpResponse response;

    @Mock
    private HttpEntity entity;

    @Mock
    private InputStreamReader streamReader;

    @Mock
    private BufferedReader reader;

    @Before
    public void setUp() {
        refresher = Mockito.spy(new SpotifyOAuthTokenRefresher());

        when(refresher.getClientId()).thenReturn("client_id");
        when(refresher.getClientSecret()).thenReturn("client_secret");
        when(refresher.getTokenUrl()).thenReturn("token_url");
    }

    @Test
    public void test_init() {
        assertNotNull(refresher.getClient());
    }

    @Test
    public void test_useRefreshToken() throws Exception {
        JSONObject token = new JSONObject(
                "{\n" +
                        "   \"access_token\":\"test_token\",\n" +
                        "   \"token_type\":\"Bearer\",\n" +
                        "   \"expires_in\":3600\n" +
                        "}"
        );

        doReturn(token).when(refresher).executePostObject(eq("token_url"), anyString());

        RefreshToken refreshToken = refresher.useRefreshToken("refresh_token");
        assertEquals("test_token", refreshToken.getAccessToken());
        assertEquals(3600l, refreshToken.getExpiresIn());
    }

    @Test
    public void test_getRequestEntity() {
        assertEquals(
                "refresh_token=test_refresh_token&grant_type=refresh_token",
                refresher.getRequestEntity("test_refresh_token")
        );
    }

    @Test
    public void test_buildTokenUrl() {
        assertEquals("token_url", refresher.buildRefreshUrl());
    }

    @Test(expected = RuntimeException.class)
    public void test_executePost_noStream() {
        String url = "test_url";
        String entity = "test_entity";
        HttpClient client = refresher.getClient();

        doReturn(post).when(refresher).newPostRequest(url, entity);
        doReturn(null).when(refresher).executeRequest(client, post);

        refresher.executePostObject(url, entity);
    }

    @Test(expected = RuntimeException.class)
    public void test_executePost_noJson() {
        String url = "test_url";
        String entity = "test_entity";
        HttpClient client = refresher.getClient();

        doReturn(post).when(refresher).newPostRequest(url, entity);
        doReturn(stream).when(refresher).executeRequest(client, post);
        doReturn(null).when(refresher).parseResponseObject(stream);

        refresher.executePostObject(url, entity);
    }

    @Test
    public void test_executePost() {
        String url = "test_url";
        String entity = "test_entity";
        HttpClient client = refresher.getClient();

        doReturn(post).when(refresher).newPostRequest(url, entity);
        doReturn(stream).when(refresher).executeRequest(client, post);
        doReturn(jsonObject).when(refresher).parseResponseObject(stream);

        refresher.executePostObject(url, entity);

        assertEquals(jsonObject, refresher.executePostObject(url, entity));
    }

    @Test
    public void test_newPostRequest() {
        String url = "test_url";
        String entity = "test_entity";

        HttpPost post = refresher.newPostRequest(url, entity);

        assertNotNull(post.getEntity());
    }

    @Test
    public void test_newPostRequest_noEntity() {
        String url = "test_url";
        String entity = null;

        HttpPost post = refresher.newPostRequest(url, entity);

        assertNull(post.getEntity());
    }

    @Test
    public void test_addAuthHeader() {
        HttpUriRequest post = new HttpPost("http://www.google.com");
        post = refresher.addAuthHeader(post);

        assertNotNull(post.getFirstHeader("Authorization"));
        assertEquals("application/x-www-form-urlencoded", post.getLastHeader("Content-Type").getValue());
    }

    @Test
    public void test_executeRequest() throws Exception {
        HttpClient client = refresher.getClient();
        HttpPost post = new HttpPost("http://www.google.com");

        doReturn(response).when(refresher).executeClientRequest(client, post);
        doReturn(entity).when(response).getEntity();
        doReturn(stream).when(entity).getContent();

        assertEquals(stream, refresher.executeRequest(client, post));
    }

    @Test
    public void test_parseResponseObject() {
        String string = "{}";

        doReturn(string).when(refresher).getJsonString(stream);

        assertEquals("{}", refresher.parseResponseObject(stream).toString());
    }

    @Test(expected = RuntimeException.class)
    public void test_parseResponseObject_exception() {
        String string = null;

        doReturn(string).when(refresher).getJsonString(stream);

        refresher.parseResponseObject(stream);
    }

    @Test
    public void test_parseResponseObject_null() {
        String string = "---";

        doReturn(string).when(refresher).getJsonString(stream);

        assertNull(refresher.parseResponseObject(stream));
    }

    @Test
    public void test_getJsonString() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("test");

        doReturn(streamReader).when(refresher).getInputStreamReader(stream);
        doReturn(reader).when(refresher).getBufferedReader(streamReader);
        doReturn(null).when(reader).readLine();
        doReturn(builder).when(refresher).getStringBuilder();

        assertEquals("test", refresher.getJsonString(stream));
    }

}