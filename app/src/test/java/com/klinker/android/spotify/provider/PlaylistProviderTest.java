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

import com.klinker.android.spotify.AbstractSpotifyHelper;
import com.klinker.android.spotify.data.Song;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PlaylistProviderTest extends AbstractSpotifyHelper {

    private PlaylistProvider provider;

    @Before
    public void setUp() {
        provider = new PlaylistProvider();
    }

    @Test
    public void test_buildSong() {
        Track track = new Track();
        track.uri = "123";
        track.name = "test track";

        AlbumSimple album = new AlbumSimple();
        album.name = "test album";
        track.album = album;

        List<Image> images = new ArrayList<Image>();
        Image image = new Image();
        image.url = "www.google.com";
        images.add(image);

        album.images = images;

        List<ArtistSimple> artists = new ArrayList<ArtistSimple>();
        ArtistSimple artist = new ArtistSimple();
        artist.name = "test artist";
        artist.type = "test type";
        artists.add(artist);

        track.artists = artists;

        Song song = provider.buildSong(track);

        assertEquals("123", song.getId());
        assertEquals("test track", song.getTitle());
        assertEquals("test album", song.getAlbum());
        assertEquals("www.google.com", song.getBackgroundImageUrl());
        assertEquals("www.google.com", song.getCardImageUrl());
        assertEquals("test artist", song.getArtist());
        assertEquals("test type", song.getType());
    }

    @Test
    public void test_buildSong_blankUri() {
        Track track = new Track();
        track.uri = "";

        Song song = provider.buildSong(track);

        assertNull(song);
    }

    @Test
    public void test_buildSong_nullUri() {
        Track track = new Track();
        track.uri = null;

        Song song = provider.buildSong(track);

        assertNull(song);
    }

}