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

import android.content.Context;
import android.util.Log;
import com.klinker.android.spotify.R;
import com.klinker.android.spotify.loader.OnPlaylistLoaded;
import com.klinker.android.spotify.loader.SpotifyOAuthTokenRefresher;
import com.klinker.android.spotify.util.PlaylistWrapper;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import lombok.Getter;

/**
 * Helper class for managing Spotify data
 */
@Getter
public class SpotifyHelper {

    public static final String CLIENT_ID = "5241fe8c697545ef8400a3a218028f52";
    public static final String CLIENT_SECRET = "3aa70cb142084f27837be88d7d8608f7";

    public static final String BASE_URL = "https://accounts.spotify.com";
    public static final String TOKEN_URL = BASE_URL + "/api/token";
    public static final String AUTHORIZE_URL = BASE_URL + "/authorize";

    private static final String TAG = "SpotifyApi";
    private static volatile SpotifyHelper spotifyHelper;

    /**
     * Get a singleton helper object, shared between all classes
     */
    public static synchronized SpotifyHelper get(Context context) {
        if (spotifyHelper == null) {
            spotifyHelper = new SpotifyHelper(context);
        }

        return spotifyHelper;
    }

    public static void remove() {
        spotifyHelper = null;
    }

    private Context context;
    private Settings settings;
    private SpotifyApi spotifyApi;
    private boolean authTokenValid;

    private SpotifyHelper(Context context) {
        this.context = context;
        this.settings = Settings.get(context);
        this.spotifyApi = new SpotifyApi();

        initAuthToken();
    }

    /**
     * Initialize Spotify auth tokens
     */
    protected void initAuthToken() {
        spotifyApi.setAccessToken(settings.spotifyAccount.getAuthToken());

        if (needsTokenRefresh()) {
            authTokenValid = false;
            Thread tokenRefresher = getTokenRefresherThread();
            tokenRefresher.setPriority(Thread.MIN_PRIORITY);
            tokenRefresher.start();
        } else {
            authTokenValid = true;
        }
    }

    protected Thread getTokenRefresherThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                refreshToken();
            }
        });
    }

    /**
     * Refresh the auth token when it is expired
     */
    public void refreshToken() {
        SpotifyOAuthTokenRefresher refresher = getTokenRefresher();
        RefreshToken token = refresher.useRefreshToken(settings.spotifyAccount.getRefreshToken());

        settings.setValue(context.getString(R.string.pref_spotify_auth_token_key), token.getAccessToken());
        settings.setExpirationTimeFromNow(context.getString(R.string.pref_spotify_expiration_date_key), token.getExpiresIn());
        authTokenValid = true;

        spotifyApi.setAccessToken(token.getAccessToken());
    }

    /**
     * Get a new token refresher to handle refreshing the auth token
     */
    protected SpotifyOAuthTokenRefresher getTokenRefresher() {
        return new SpotifyOAuthTokenRefresher();
    }

    /**
     * Refresh the auth token now if we are within 5 minutes of it expiring
     */
    protected boolean needsTokenRefresh() {
        long expiration = settings.spotifyAccount.getExpirationDate();
        return expiration - (5*60*1000) <= getCurrentTime() && expiration != -1;
    }

    protected long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private Config getPlayerConfig() {
        return new Config(context, settings.spotifyAccount.getAuthToken(), CLIENT_ID);
    }

    /**
     * Get a new Spotify player to handle all media playback
     */
    public Player getPlayer() {
        return Spotify.getPlayer(getPlayerConfig(), this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                Log.v(TAG, "Spotify player initialized");
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "Error initializing Spotify player", throwable);
            }
        });
    }

    /**
     * Get all playlists, should be called off of UI thread
     */
    public PlaylistWrapper loadPlaylists() {
        return loadPlaylists(null);
    }

    /**
     * Get all playlists, should be called off of UI thread and callback can be used to link back to UI thread and
     * update status
     */
    public PlaylistWrapper loadPlaylists(OnPlaylistLoaded callback) {
        Pager<PlaylistSimple> playlists = spotifyApi.getService().getPlaylists(settings.spotifyAccount.getUserId());
        HashMap<String, List<PlaylistTrack>> tracks = new HashMap<String, List<PlaylistTrack>>();

        // logic for fetching tracks from playlist... only 100 can be fetched at a time, so we need to loop through
        // each playlist and find all tracks in that playlist in increments of 100. By doing all of this right when
        // we login, this could cause performance issues and slow startup times. In the future, it would be better
        // to store this information in a database after the initial login and then just compare the changes to see
        // what needs updated, if anything.
        for (int i = 0; i < playlists.items.size(); i++) {
            PlaylistSimple playlist = playlists.items.get(i);
            Log.v(TAG, "playlist name: " + playlist.name);
            int totalTracks = playlist.tracks.total;
            int offset = 0;

            HashMap<String, Object> options = new HashMap<String, Object>();
            List<PlaylistTrack> playlistTracks = new ArrayList<PlaylistTrack>();

            while (totalTracks > 0) {
                options.put("offset", offset);
                playlistTracks.addAll(spotifyApi.getService().getPlaylistTracks(
                        playlist.owner.id,
                        playlist.id,
                        options
                ).items);

                totalTracks -= 100;
                offset += 100;
            }

            if (callback != null) {
                callback.onPlaylistLoaded(playlist, i, playlists.items.size());
            }

            tracks.put(playlist.name, playlistTracks);
        }

        return new PlaylistWrapper(playlists, tracks);
    }

    protected void setSettings(Settings settings) {
        this.settings = settings;
    }

    protected void setSpotifyApi(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

}
