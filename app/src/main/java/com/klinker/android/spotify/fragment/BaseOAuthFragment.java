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

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.klinker.android.spotify.R;
import com.klinker.android.spotify.activity.MainActivity;
import com.klinker.android.spotify.data.AccessToken;
import com.klinker.android.spotify.data.SpotifyHelper;
import kaaes.spotify.webapi.android.SpotifyApi;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base OAuth fragment that could be used for validating against any OAuth service
 */
public abstract class BaseOAuthFragment extends Fragment {

    private static final String TAG = "BaseOauthFragment";

    public WebView loginWebView;

    /**
     * Craete a webview to display information in
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        loginWebView = new WebView(getActivity());

        try {
            loginWebView.getSettings().setJavaScriptEnabled(true);
        } catch (Exception e) {
        }

        loginWebView.setWebViewClient(getWebViewClient());
        return loginWebView;
    }

    /**
     * Set up the webview client that information should be displayed in
     */
    public WebViewClient getWebViewClient() {
        return new WebViewClient() {
            private boolean authComplete = false;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.v(TAG, "callback url: " + url);
                if (shouldRedirect(url)) {
                    view.stopLoading();
                    authComplete = processPageFinished(url, authComplete);
                }
            }
        };
    }

    protected boolean shouldRedirect(String url) {
        return url.startsWith(getRedirectUri());
    }

    /**
     * Process information encoded in URL once a page has finished loading to see if we are done
     */
    protected boolean processPageFinished(String url, boolean authComplete) {
        if (url.contains("?code=") && !authComplete) {
            Uri uri = Uri.parse(url);
            String authCode = uri.getQueryParameter("code");
            Log.v(TAG, "auth code: " + authCode);
            getTokenAsyncTask(authCode).execute();
            authComplete = true;
        } else if (url.contains("error=access_denied")){
            Log.v(TAG, "ACCESS_DENIED_HERE");
            Toast.makeText(getActivity(), getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getActivity().finish();
                }
            }, 500);
            authComplete = true;
        }

        return authComplete;
    }

    public TokenGet getTokenAsyncTask(String authCode) {
        return new TokenGet(authCode);
    }

    /**
     * Start the login process after fragment is showing on screen
     */
    @Override
    public void onStart() {
        superOnStart();
        startLogin();
    }

    public void superOnStart() {
        super.onStart();
    }

    /**
     * Begin loading the URL for displaying the login screen to user
     */
    public void startLogin() {
        loginWebView.loadUrl(buildLoginUrl());
    }

    /**
     * Create a login url that we need to access to make request to service
     */
    public String buildLoginUrl() {
        return getLoginUrl() + "?redirect_uri=" + getRedirectUri() +
                "&response_type=code&client_id=" + getClientId() + "&scope=" + getScope();
    }

    /**
     * Start the main activity after we've finished authorizing
     */
    public void finishAuthorization() {
        SpotifyHelper.remove();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    /**
     * Async Task for getting a token
     */
    protected class TokenGet extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;
        private String code;
        private String userId;

        public TokenGet(String authCode) {
            code = authCode;
        }

        /**
         * Show a dialog to let user know we are confirming credentials
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage(getString(R.string.authorizing));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Fetch the actual credentials from service
         */
        @Override
        protected JSONObject doInBackground(String... args) {
            AccessToken jParser = new AccessToken();
            JSONObject json = jParser.getToken(getTokenUrl(), code, getClientId(), getClientSecret(), getRedirectUri(), getGrantType());

            try {
                SpotifyApi api = new SpotifyApi();
                api.setAccessToken(json.getString("access_token"));
                userId = api.getService().getMe().id;
                Log.v(TAG, "userId: " + userId);
            } catch (JSONException e) {
                throw new RuntimeException("error getting user id", e);
            }

            return json;
        }

        /**
         * Confirm that our credentials are correct and valid
         */
        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            if (json != null) {
                try {
                    String authToken = json.getString("access_token");
                    String expire = json.getString("expires_in");
                    String refresh = json.getString("refresh_token");
                    Log.v(TAG, "Authorization successful.\nAuth Token: " + authToken + "\nExpire: " + expire + "\nRefresh: " + refresh);
                    saveAuthToken(authToken, Long.parseLong(expire), refresh, userId);
                    finishAuthorization();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        }
    }

    public abstract String getLoginUrl();
    public abstract String getScope();
    public abstract String getTokenUrl();
    public abstract String getClientId();
    public abstract String getClientSecret();
    public abstract String getRedirectUri();
    public abstract String getGrantType();
    public abstract void saveAuthToken(String authToken, long expire, String refreshToken, String userId);

}
