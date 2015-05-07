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
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Data object for holding simple songs to be played
 */
public class Song implements Serializable {

    @Getter @Setter private String id;
    @Getter @Setter private String title;
    @Getter @Setter private String album;
    @Getter @Setter private String backgroundImageUrl;
    @Getter @Setter private String cardImageUrl;
    @Getter @Setter private String artist;
    @Getter @Setter private String type;

    /**
     * Get a URI for the background image to use with picasso
     */
    public URI getBackgroundImageURI() {
        try {
            Log.d("BACK SONG: ", getBackgroundImageUrl());
            return new URI(getBackgroundImageUrl());
        } catch (URISyntaxException e) {
            Log.d("URI exception: ", getBackgroundImageUrl());
            return null;
        } catch (NullPointerException e) {
            Log.e("Song", "Background image not available", e);
            return null;
        }
    }

    /**
     * Get the card image URI
     */
    public URI getCardImageURI() {
        try {
            return new URI(getCardImageUrl());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", backgroundImageUrl='" + getBackgroundImageUrl() + '\'' +
                ", backgroundImageURI='" + getBackgroundImageURI().toString() + '\'' +
                ", cardImageUrl='" + cardImageUrl + '\'' +
                '}';
    }

}
