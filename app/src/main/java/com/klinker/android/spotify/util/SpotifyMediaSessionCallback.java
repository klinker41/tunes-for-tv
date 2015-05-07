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

import android.media.session.MediaSession;
import com.spotify.sdk.android.player.Player;

/**
 * Handles callbacks on the media session from the now playing card
 */
public class SpotifyMediaSessionCallback extends MediaSession.Callback {

    private Player player;

    /**
     * Create a new callback for the now playing card
     * @param player the Spotify music player we will be making changes to
     */
    public SpotifyMediaSessionCallback(Player player) {
        this.player = player;
    }

    /**
     * Play the music
     */
    @Override
    public void onPlay() {
        super.onPlay();

        if (player != null) {
            player.resume();
        }
    }

    /**
     * Pause the music
     */
    @Override
    public void onPause() {
        super.onPause();

        if (player != null) {
            player.pause();
        }
    }

    /**
     * Skip to next track
     */
    @Override
    public void onSkipToNext() {
        super.onSkipToNext();

        if (player != null) {
            player.skipToNext();
        }
    }

    /**
     * Skip to previous track
     */
    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();

        if (player != null) {
            player.skipToPrevious();
        }
    }

}
