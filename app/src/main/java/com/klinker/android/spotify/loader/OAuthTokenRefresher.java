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

import android.util.Base64;
import android.util.Log;
import com.klinker.android.spotify.data.RefreshToken;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * Helper for refreshing OAuth Tokens
 */
@Getter
public abstract class OAuthTokenRefresher {

    private static final String TAG = "OAuthApiTokenRefresher";

    private HttpClient client;

    /**
     * Construct OAuth refresher
     */
    public OAuthTokenRefresher() {
        this.client = new DefaultHttpClient();
    }

    /**
     * Use a refresh token to get a new refresh token that is not expired
     */
    public RefreshToken useRefreshToken(String refreshToken) {
        String url = buildRefreshUrl();
        JSONObject post = executePostObject(url, getRequestEntity(refreshToken));
        return new RefreshToken(post);
    }

    /**
     * Create a request entity for the oauth request
     */
    protected String getRequestEntity(String refreshToken) {
        return "refresh_token=" + refreshToken + "&grant_type=refresh_token";
    }

    /**
     * Create a refresh url to post to
     */
    protected String buildRefreshUrl() {
        return getTokenUrl();
    }

    /**
     * Execute a post to given url with the entity
     */
    protected JSONObject executePostObject(String url, String entity) {
        HttpPost post = newPostRequest(url, entity);
        InputStream stream = executeRequest(client, post);

        if (stream != null) {
            JSONObject json = parseResponseObject(stream);

            if (json != null) {
                return json;
            } else {
                throw new RuntimeException("Error parsing json response, check logs");
            }
        } else {
            throw new RuntimeException("Error retrieving stream, check logs");
        }
    }

    /**
     * Create a new post request with the url and entity
     */
    protected HttpPost newPostRequest(String url, String entity) {
        HttpPost post = new HttpPost(url);
        post = (HttpPost) addAuthHeader(post);

        if (entity != null) {
            try {
                post.setEntity(new StringEntity(entity));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "error attaching entity to post", e);
            }
        }

        return post;
    }

    /**
     * Add an auth header to request
     */
    protected HttpUriRequest addAuthHeader(HttpUriRequest request) {
        byte[] base64 = Base64.encode((getClientId() + ":" + getClientSecret()).getBytes(), Base64.DEFAULT);
        String authorization = new String(base64).replace("\n", "").replace(" ", "");
        request.addHeader("Authorization", "Basic " + authorization);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded"); // need this or we get xml returned
        return request;
    }

    /**
     * Execute request through the client for the request object
     */
    protected InputStream executeRequest(HttpClient client, HttpUriRequest request) {
        try {
            HttpResponse httpResponse = executeClientRequest(client, request);
            HttpEntity httpEntity = httpResponse.getEntity();
            return httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected HttpResponse executeClientRequest(HttpClient client, HttpUriRequest request) throws IOException {
        return client.execute(request);
    }

    /**
     * Decode the response from the input stream=
     */
    protected JSONObject parseResponseObject(InputStream stream) {
        String jsonString = getJsonString(stream);

        if (jsonString != null) {
            try {
                return new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            throw new RuntimeException("Unable to get json response array");
        }
    }

    /**
     * Get JSON from stream
     */
    protected String getJsonString(InputStream stream) {
        String jsonString;

        try {
            BufferedReader reader = getBufferedReader(getInputStreamReader(stream));
            StringBuilder sb = getStringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            stream.close();
            jsonString = sb.toString();

            Log.v(TAG, jsonString);
            return jsonString;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected InputStreamReader getInputStreamReader(InputStream stream) throws UnsupportedEncodingException {
        return new InputStreamReader(stream);
    }

    protected BufferedReader getBufferedReader(InputStreamReader reader) {
        return new BufferedReader(reader);
    }

    protected StringBuilder getStringBuilder() {
        return new StringBuilder();
    }

    public abstract String getTokenUrl();
    public abstract String getClientId();
    public abstract String getClientSecret();

}
