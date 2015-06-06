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

package com.klinker.android.spotify.provider;

import android.content.Context;

import android.text.TextUtils;
import com.klinker.android.spotify.data.Song;
import com.klinker.android.spotify.data.SpotifyHelper;
import com.klinker.android.spotify.loader.OnPlaylistLoaded;
import com.klinker.android.spotify.util.PlaylistWrapper;

import kaaes.spotify.webapi.android.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Provides playlist information to Main Fragment
 */
public class PlaylistProvider {

    private static final String TAG = "PlaylistProvider";

    private static HashMap<String, List<Song>> mPlaylistList;
    private static Context mContext;

    /**
     * Set the context for the provider
     */
    public static void setContext(Context context) {
        if (mContext == null)
            mContext = context;
    }

    /**
     * Get the context for the provider
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * Get the already fetched playlist list
     */
    public static HashMap<String, List<Song>> getPlaylistList() {
        return mPlaylistList;
    }

    /**
     * Build the playlist list
     */
    public static HashMap<String, List<Song>> buildMedia(Context context, OnPlaylistLoaded callback) {
        if (null != mPlaylistList) {
            return getPlaylistList();
        }

        SpotifyHelper helper = SpotifyHelper.get(context);
        PlaylistWrapper playlistWrapper = helper.loadPlaylists(callback);

        mPlaylistList = playlistWrapper.getProviderInformation();

        return mPlaylistList;
    }

    /**
     * Build a song from a Spotify track object
     */
    public Song buildSong(Track track) {
        if (TextUtils.isEmpty(track.uri)) {
            return null;
        }

        Song song = new Song();
        song.setId(track.uri);
        song.setTitle(track.name);
        song.setAlbum(track.album.name);

        List<Image> images = track.album.images;
        if (images.size() > 0) {
            song.setBackgroundImageUrl(images.get(0).url);
            song.setCardImageUrl(images.get(0).url);
        }

        List<ArtistSimple> artists = track.artists;
        if (artists.size() > 0) {
            song.setArtist(artists.get(0).name);
            song.setType(artists.get(0).type);
        }

        return song;
    }

}
