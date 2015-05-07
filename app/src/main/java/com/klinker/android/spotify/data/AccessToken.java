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

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpRetryException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data class for storing and retreiving an auth token from spotify after logging in
 */
public class AccessToken {

    private static InputStream inputStream = null;
    private static JSONObject jsonObject = null;
    private static String json = "";

    public AccessToken() {
    }

    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    private DefaultHttpClient httpClient;
    private HttpPost httpPost;

    /**
     * Get a new auth token from spotify by making all of the proper requests
     * @param address the address to direct request to
     * @param token the token from the oauth process
     * @param clientId the client id used to show that Tunes is the accessing app
     * @param clientSecret the client secret used to show that Tunes is the accessing app
     * @param redirectUri where to redirect to once the token has been fetched
     * @param grantType type of request
     * @return a JSON object with all of the token information as specified by Spotify
     */
    public JSONObject getToken(String address, String token, String clientId, String clientSecret, String redirectUri, String grantType) {
        try {
            httpClient = getHttpClient();
            httpPost = new HttpPost(address);
            params.add(new BasicNameValuePair("code", token));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("redirect_uri", redirectUri));
            params.add(new BasicNameValuePair("grant_type", grantType));
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = executeRequest(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = getBufferedReader(getInputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            inputStream.close();
            json = sb.toString();
            Log.e("JSONStr", json);
        } catch (Exception e) {
            e.getMessage();
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        try {
            jsonObject = createJSON(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        return jsonObject;
    }

    /**
     * Get the http client, exposed for testing
     */
    public DefaultHttpClient getHttpClient() {
        return new DefaultHttpClient();
    }

    /**
     * Execute a request on a post object from the http client
     */
    public HttpResponse executeRequest(HttpPost post) throws IOException {
        return httpClient.execute(post);
    }

    /**
     * Create a new InputStreamReader from an InputStream
     */
    public InputStreamReader getInputStreamReader(InputStream stream) throws UnsupportedEncodingException {
        return new InputStreamReader(stream);
    }

    /**
     * Create a new BufferedReader from an InputStreamReader
     */
    public BufferedReader getBufferedReader(InputStreamReader reader) {
        return new BufferedReader(reader);
    }

    /**
     * Parse a JSON object from a string
     */
    public JSONObject createJSON(String string) throws JSONException {
        return new JSONObject(string);
    }

}
