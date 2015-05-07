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

import java.util.HashMap;
import java.util.List;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import com.klinker.android.spotify.provider.PlaylistProvider;
import com.klinker.android.spotify.data.Song;

/**
 * Loads songs and playlist information from PlaylistProvider
 */
public class SongItemLoader extends AsyncTaskLoader<HashMap<String, List<Song>>> {

    private static final String TAG = "SongItemLoader";
    private Context mContext;
    private OnPlaylistLoaded callback;

    public SongItemLoader(Context context, OnPlaylistLoaded callback) {
        super(context);
        mContext = context;
        this.callback = callback;
    }

    @Override
    public HashMap<String, List<Song>> loadInBackground() {
        try {
            return PlaylistProvider.buildMedia(mContext, callback);
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch playlist data", e);
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

}
