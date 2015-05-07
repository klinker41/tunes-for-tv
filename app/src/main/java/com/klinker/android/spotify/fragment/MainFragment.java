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

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.klinker.android.spotify.*;
import com.klinker.android.spotify.activity.PlayerActivity;
import com.klinker.android.spotify.data.Song;
import com.klinker.android.spotify.loader.OnPlaylistLoaded;
import com.klinker.android.spotify.loader.PicassoBackgroundManagerTarget;
import com.klinker.android.spotify.loader.SongItemLoader;
import com.klinker.android.spotify.presenter.CardPresenter;
import com.klinker.android.spotify.provider.PlaylistProvider;
import com.spotify.sdk.android.player.Player;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import kaaes.spotify.webapi.android.models.Playlist;

import java.net.URI;
import java.util.*;

/**
 * Fragment for displaying playlists and songs to user after logging in
 */
public class MainFragment extends BrowseFragment implements
        LoaderManager.LoaderCallbacks<HashMap<String, List<Song>>>, OnPlaylistLoaded {

    private static final String TAG = "MainFragment";

    /**
     * Specifies how long to wait on an object before updating the background
     */
    private static int BACKGROUND_UPDATE_DELAY = 300;

    /**
     * Specifies how big a grid item's width should be
     */
    private static int GRID_ITEM_WIDTH = 200;

    /**
     * Specifies how big a grid item's height should be
     */
    private static int GRID_ITEM_HEIGHT = 200;

    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private final Handler mHandler = new Handler();
    private URI mBackgroundURI;
    private ProgressDialog loadingDialog;

    /**
     * Load all of our playlist data, setup background and ui elements, initialize even listeners
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadPlaylistData();

        prepareBackgroundManager();
        setupUIElements();
        setupEventListeners();
    }

    /**
     * Prepare the background manager
     */
    protected void prepareBackgroundManager() {
        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    /**
     * Set up what should be displayed on screen
     */
    protected void setupUIElements() {
        setTitle(getString(R.string.app_name));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    /**
     * Initialize loading playlists from PlaylistProvider
     */
    protected void loadPlaylistData() {
        PlaylistProvider.setContext(getActivity());
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Set up what should happen on search selected or item's selected or clicked on
     */
    protected void setupEventListeners() {
//        setOnSearchClickedListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Do nothing on search, haven't yet added this functionality and may not
//            }
//        });

        setOnItemViewSelectedListener(getDefaultItemSelectedListener());
        setOnItemViewClickedListener(getDefaultItemClickedListener());
    }

    /**
     * Create a loader for getting all of our songs to display
     */
    @Override
    public Loader<HashMap<String, List<Song>>> onCreateLoader(int arg0, Bundle arg1) {
        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setIndeterminate(true);
        loadingDialog.setMessage(getString(R.string.loading_playlists));
        loadingDialog.show();
        return new SongItemLoader(getActivity(), this);
    }

    /**
     * Callback for when loading playlists has finished
     */
    @Override
    public void onLoadFinished(Loader<HashMap<String, List<Song>>> arg0,
            HashMap<String, List<Song>> data) {

        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }

        if (data == null) {
            return;
        }

        // get all of the rows from the hashmap of data
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        int i = 0;

        for (Map.Entry<String, List<Song>> entry : data.entrySet()) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            List<Song> list = entry.getValue();

            for (int j = 0; j < list.size(); j++) {
                listRowAdapter.add(list.get(j));
            }
            HeaderItem header = new HeaderItem(i, entry.getKey());
            i++;
            mRowsAdapter.add(new ListRow(header, listRowAdapter));
        }

        // add settings row for last row
        HeaderItem gridHeader = new HeaderItem(i, getResources().getString(R.string.preferences));

        GridItemPresenter gridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.personal_settings));
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(mRowsAdapter);
    }

    /**
     * Reset the loader, ie show no rows anymore
     */
    @Override
    public void onLoaderReset(Loader<HashMap<String, List<Song>>> arg0) {
        mRowsAdapter.clear();
    }

    /**
     * Get functionality for selecting an item
     */
    protected OnItemViewSelectedListener getDefaultItemSelectedListener() {
        return new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder holder1, Object item, RowPresenter.ViewHolder holder2, Row row) {
                if (item instanceof Song && ((Song) item).getBackgroundImageURI() != null) {
                    mBackgroundURI = ((Song) item).getBackgroundImageURI();
                    startBackgroundTimer();
                }
            }
        };
    }

    /**
     * Get functionality for clicking on an item
     */
    protected OnItemViewClickedListener getDefaultItemClickedListener() {
        return new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder holder1, Object item, RowPresenter.ViewHolder holder2, Row row) {
                if (item instanceof Song) {
                    HashMap<String, List<Song>> playlists = PlaylistProvider.buildMedia(getActivity(), MainFragment.this);
                    List<Song> songs = playlists.get(row.getHeaderItem().getName());
                    startSong(songs, (Song) item);
                } else if (item instanceof String) {
                    // TODO preferences
                }
            }
        };
    }

    /**
     * Start playing the first song on the list by starting the PlayerActivity
     */
    protected List<String> startSong(List<Song> songs, Song song) {
        Log.d(TAG, "Item: " + song.getTitle());

        // rotate the list so that the selected song is first. This way, the selected song will be first in the list and
        // it will maintain the order of the rest of the songs after that item. After we hit the last item, it will
        // automatically go back to the first item in the list and keep playing with that one
        int index = songs.indexOf(song);
        Collections.rotate(songs, -1 * index);

        // get the rest of the songs, images, titles, and articles
        ArrayList<String> uris = new ArrayList<String>();
        ArrayList<String> images = new ArrayList<String>();
        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<String> artists = new ArrayList<String>();

        for (Song s : songs) {
            uris.add(s.getId());
            images.add(s.getBackgroundImageUrl());
            titles.add(s.getTitle());
            artists.add(s.getArtist());
        }

        // start the player activity
        Intent intent = new Intent(getActivity(), PlayerActivity.class);
        intent.putExtra(PlayerActivity.ARG_SONG_IDS, uris);
        intent.putExtra(PlayerActivity.ARG_SONG_IMAGES, images);
        intent.putExtra(PlayerActivity.ARG_SONG_TITLES, titles);
        intent.putExtra(PlayerActivity.ARG_SONG_ARTISTS, artists);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        return uris;
    }

    /**
     * Update the background with the background manager
     */
    protected void updateBackground(URI uri) {
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .centerCrop()
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
    }

    /**
     * Start a timer counting down how much time is left before switching to a new background
     */
    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    /**
     * Callback for functionality of what happens once the playlist has finished loading when app is starting
     */
    @Override
    public void onPlaylistLoaded(Playlist playlist, final int currentNumber, final int totalNumber) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            Log.v(TAG, "loaded " + playlist.name);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.setMessage(getString(R.string.loading_playlists_num).replace("%d", (currentNumber + 1) + "").replace("%t", totalNumber + ""));
                }
            });
        }
    }

    /**
     * TimerTask for updating the background after the time has finished counting down
     */
    private class UpdateBackgroundTask extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI);
                    }
                }
            });
        }
    }

    /**
     * Presenter for the settings row at bottom grid
     */
    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

    public ArrayObjectAdapter getAdapter() {
        return mRowsAdapter;
    }

    public void setArrayObjectAdapter(ArrayObjectAdapter adapter) {
        mRowsAdapter = adapter;
    }

}
