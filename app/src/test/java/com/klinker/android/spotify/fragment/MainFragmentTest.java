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
import android.content.Intent;
import android.content.Loader;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.view.View;
import com.klinker.android.spotify.AbstractSpotifyHelper;
import com.klinker.android.spotify.R;
import com.klinker.android.spotify.data.Song;
import com.klinker.android.spotify.provider.PlaylistProvider;
import com.klinker.android.spotify.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MainFragmentTest extends AbstractSpotifyHelper {

    private MainFragment fragment;

    @Before
    public void setUp() {
        fragment = Mockito.spy(new MainFragment());
        doNothing().when(fragment).prepareBackgroundManager();
        TestUtil.startFragment(fragment);
    }

    @Test
    public void test_isAdded() {
        assertTrue(fragment.isAdded());
    }

    @Test
    public void test_onActivityCreated() {
        verify(fragment).loadPlaylistData();
        verify(fragment).prepareBackgroundManager();
        verify(fragment).setupUIElements();
        verify(fragment).setupEventListeners();
    }

//    @Test
//    public void test_setupUIElements() {
//        verify(fragment).setTitle("Tunes for TV");
//        verify(fragment).setHeadersState(MainFragment.HEADERS_ENABLED);
//        verify(fragment).setHeadersTransitionOnBackEnabled(true);
//        verify(fragment).setBrandColor(anyInt());
//        verify(fragment).setSearchAffordanceColor(anyInt());
//    }

    @Test
    public void test_loadPlaylistData() {
        LoaderManager manager = Mockito.mock(LoaderManager.class);
        doReturn(manager).when(fragment).getLoaderManager();

        fragment.loadPlaylistData();

        assertNotNull(PlaylistProvider.getContext());
        verify(manager).initLoader(0, null, fragment);
    }

    @Test
    public void test_setupEventListeners() {
        //verify(fragment).setOnSearchClickedListener(any(View.OnClickListener.class));
        verify(fragment).setOnItemViewSelectedListener(fragment.getDefaultItemSelectedListener());
        verify(fragment).setOnItemViewClickedListener(fragment.getDefaultItemClickedListener());
    }

    @Test
    public void test_onCreateLoader() {
        Loader<HashMap<String, List<Song>>> loader = fragment.onCreateLoader(0, null);
        assertNotNull(loader);
    }

    @Test
    public void test_onLoadFinished() {
        HashMap<String, List<Song>> map = new HashMap<String, List<Song>>();
        List<Song> songs = new ArrayList<Song>();
        songs.add(new Song());
        songs.add(new Song());
        songs.add(new Song());

        map.put("Playlist", songs);

        fragment.onLoadFinished(null, map);
        ArrayObjectAdapter adapter = fragment.getAdapter();

        assertEquals(2, adapter.size());
        verify(fragment).setAdapter(adapter);
    }

    @Test
    public void test_onLoaderReset() {
        fragment.setArrayObjectAdapter(new ArrayObjectAdapter());
        fragment.onLoaderReset(null);
        assertEquals(0, fragment.getAdapter().size());
    }

    @Test
    public void test_startSong() {
        List<Song> songs = new ArrayList<Song>();

        for (int i = 0; i < 10; i++) {
            Song song = new Song();
            song.setId(i + "");
            songs.add(song);
        }

        Song song = songs.get(2);
        List<String> ids = fragment.startSong(songs, song);

        assertEquals(song.getId(), ids.get(0));
        assertNotEquals(song.getId(), ids.get(2));
        assertNotEquals(song.getId(), ids.get(3));

        verify(fragment).startActivity(any(Intent.class));
    }

}