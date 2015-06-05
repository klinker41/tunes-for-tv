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

package com.klinker.android.spotify.util;

import com.klinker.android.spotify.data.Song;
import com.klinker.android.spotify.provider.PlaylistProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import lombok.Getter;

/**
 * Simple wrapper for holding playlist and track information
 */
@Getter
public class PlaylistWrapper {

    private Pager<PlaylistSimple> playlists;
    private Map<String, List<PlaylistTrack>> tracks;

    public PlaylistWrapper(Pager<PlaylistSimple> playlists, Map<String, List<PlaylistTrack>> tracks) {
        this.playlists = playlists;
        this.tracks = tracks;
    }

    public HashMap<String, List<Song>> getProviderInformation() {
        PlaylistProvider provider = new PlaylistProvider();
        HashMap<String, List<Song>> providerInfo = new HashMap<String, List<Song>>();

        for (String name : tracks.keySet()) {
            List<PlaylistTrack> track = tracks.get(name);
            List<Song> songs = new ArrayList<Song>(track.size());

            for (PlaylistTrack t : track) {
                songs.add(provider.buildSong(t.track));
            }

            providerInfo.put(name, songs);
        }

        return providerInfo;
    }

}
