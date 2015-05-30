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

package com.klinker.android.spotify.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.klinker.android.spotify.R;
import com.klinker.android.spotify.data.Settings;
import com.klinker.android.spotify.data.SpotifyHelper;
import com.klinker.android.spotify.loader.PicassoBackgroundManagerTarget;
import com.klinker.android.spotify.util.FileUtils;
import com.klinker.android.spotify.util.NetworkUtils;
import com.klinker.android.spotify.util.OnAuthTokenRefreshedListener;
import com.klinker.android.spotify.util.SpotifyMediaSessionCallback;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URI;
import java.util.List;

/**
 * Activity for displaying media that is currently playing. On the screen, user's will see a play button, a back button,
 * a forward button and the background will be the album artwork
 */
public class PlayerActivity extends SpotifyAbstractActivity {

    /**
     * TAG for logging
     */
    public static final String TAG = "PlayerActivity";

    /**
     * Broadcast action for toggling play state. ie, if playing, pause, if paused, play
     */
    public static final String ACTION_TOGGLE_PLAY = "com.klinker.android.spotify.TOGGLE_MEDIA";

    /**
     * Broadcast action for skipping to next song
     */
    public static final String ACTION_NEXT = "com.klinker.android.spotify.NEXT";

    /**
     * Broadcast action for going to previous song
     */
    public static final String ACTION_PREVIOUS = "com.klinker.android.spotify.PREVIOUS";

    /**
     * Arg for passing song URIs into player from main activity
     */
    public static final String ARG_SONG_IDS = "ids";

    /**
     * Arg for passing song images into player from main activity so that we don't need to requery them when
     * the song starts playing
     */
    public static final String ARG_SONG_IMAGES = "images";

    /**
     * Arg for passing song titles into the player
     */
    public static final String ARG_SONG_TITLES = "titles";

    /**
     * Arg for passing song artists into the player
     */
    public static final String ARG_SONG_ARTISTS = "artists";

    /**
     * Apply a fix for volume on the spotify player being very, very loud. This fix doesn't seem
     * to do anything on the Nexus Player, but it does change the volume on the Nvidia Shield.
     */
    private static final boolean ADJUST_VOLUME_FIX = false;

    private List<String> songIds;
    private List<String> songImages;
    private List<String> songTitles;
    private List<String> songArtists;

    private boolean isActivityShowing;
    private int currentIndex;

    private Settings settings;
    private SpotifyHelper helper;

    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;

    private ImageButton shuffleButton;
    private ImageButton repeatButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton playButton;
    private TextView titleTextView;
    private TextView artistTextView;
    private ImageView shuffleEnabled;
    private ImageView repeatEnabled;

    private Player player;
    private MediaSession mSession;
    private boolean isPlaying;
    private int startingVolume = 0;

    /**
     * Initialize data, prepare background manager and create a new spotify player with our song ids
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        songIds = getIntent().getStringArrayListExtra(ARG_SONG_IDS);
        songImages = getIntent().getStringArrayListExtra(ARG_SONG_IMAGES);
        songTitles = getIntent().getStringArrayListExtra(ARG_SONG_TITLES);
        songArtists = getIntent().getStringArrayListExtra(ARG_SONG_ARTISTS);

        helper = getSpotifyHelper();
        settings = Settings.get(this);

        adjustMediaVolumeStart();
        prepareBackgroundManager();
        setUpUI();
        createPlayer();
        registerMediaReceiver();
    }

    /**
     * Check here to ensure that the app is still logged in and token hasn't expired yet
     */
    @Override
    public void onStart() {
        super.onStart();
        checkLoggedIn(helper, authTokenRefreshedListener);
    }

    /**
     * Adjust the media volume back to its default that it was when we entered activity
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        adjustMediaVolumeStop();
        unregisterMediaReceiver();
    }

    /**
     * Update the activity showing state to true
     */
    @Override
    public void onResume() {
        super.onResume();
        isActivityShowing = true;

        // check if the playing information is up to date and if not, update it
        String title = songTitles.get(currentIndex);
        if (!titleTextView.getText().toString().equals(title)) {
            titleTextView.setText(title);
            artistTextView.setText(songArtists.get(currentIndex));
            updateBackground(URI.create(songImages.get(currentIndex)));
        }
    }

    /**
     * Update the activity showing state to false
     */
    @Override
    public void onPause() {
        super.onPause();
        isActivityShowing = false;
    }

    /**
     * Spotify plays music very, very loud. Here, we adjust the media volume to 60% of its current value for a more
     * enjoyable experience for all
     */
    private void adjustMediaVolumeStart() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        startingVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.v(TAG, "max volume = " + maxVolume + ", currentVolume = " + startingVolume);
        maxVolume = maxVolume * 7 / 10;
        Log.v(TAG, "new volume = " + maxVolume);

        if (ADJUST_VOLUME_FIX) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
        }
    }

    /**
     * Set the media volume back to what it was before we started playing music
     */
    private void adjustMediaVolumeStop() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (ADJUST_VOLUME_FIX) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, startingVolume, 0);
        }
    }

    /**
     * Create a new spotify player from our helper and start the first song in the list of IDs sent to this activity
     */
    protected void createPlayer() {
        player = helper.getPlayer();

        // create a session so that a now playing card is active on the homescreen
        mSession = new MediaSession(this, TAG);
        mSession.setCallback(new SpotifyMediaSessionCallback(player));
        mSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
        mSession.setSessionActivity(getSessionActivity());

        // add callback information, mostly for logging and updating the background on a song change
        player.addPlayerNotificationCallback(notificationCallback);
        player.addConnectionStateCallback(connectionStateCallback);

        player.play(songIds);
        enablePlaying();

        // add a delay here, if we enable shuffle and repeat right after starting the player, then
        // there is an issue where the player skips to the next song right away.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                player.setShuffle(settings.shuffle);
                player.setRepeat(settings.repeat);
            }
        }, 5000);

        // make sure that we actually sent song images into activity
        if (songImages.size() > 0) {
            try {
                titleTextView.setText(songTitles.get(0));
                artistTextView.setText(songArtists.get(0));
                updateBackground(URI.create(songImages.get(0)));
            } catch (IllegalArgumentException e) {
                updateBackground(null);
            }
        } else {
            updateBackground(null);
        }
    }

    /**
     * Prepare a background manager through Picasso to show for album artwork
     */
    protected void prepareBackgroundManager() {
        BackgroundManager backgroundManager = BackgroundManager.getInstance(this);
        backgroundManager.attach(getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    /**
     * Set up the UI for the play/pause controls on the screen
     */
    protected void setUpUI() {
        setContentView(R.layout.player);

        shuffleButton = (ImageButton) findViewById(R.id.shuffle_button);
        repeatButton = (ImageButton) findViewById(R.id.repeat_button);
        previousButton = (ImageButton) findViewById(R.id.previous_button);
        nextButton = (ImageButton) findViewById(R.id.skip_button);
        playButton = (ImageButton) findViewById(R.id.play_button);
        titleTextView = (TextView) findViewById(R.id.title);
        artistTextView = (TextView) findViewById(R.id.artist);
        shuffleEnabled = (ImageView) findViewById(R.id.shuffle_enabled);
        repeatEnabled = (ImageView) findViewById(R.id.repeat_enabled);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlaying();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSong();
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousSong();
            }
        });

        shuffleEnabled.setColorFilter(getResources().getColor(R.color.fastlane_background));
        repeatEnabled.setColorFilter(getResources().getColor(R.color.fastlane_background));

        if (settings.shuffle) {
            shuffleEnabled.setVisibility(View.VISIBLE);
        } else {
            shuffleEnabled.setVisibility(View.GONE);
        }

        if (settings.repeat) {
            repeatEnabled.setVisibility(View.VISIBLE);
        } else {
            repeatEnabled.setVisibility(View.GONE);
        }

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settings.shuffle) {
                    settings.setValue(getString(R.string.pref_shuffle), false);
                    shuffleEnabled.setVisibility(View.GONE);
                    player.setShuffle(false);
                } else {
                    settings.setValue(getString(R.string.pref_shuffle), true);
                    shuffleEnabled.setVisibility(View.VISIBLE);
                    player.setShuffle(true);
                }
            }
        });

        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settings.repeat) {
                    settings.setValue(getString(R.string.pref_repeat), false);
                    repeatEnabled.setVisibility(View.GONE);
                    player.setRepeat(false);
                } else {
                    settings.setValue(getString(R.string.pref_repeat), true);
                    repeatEnabled.setVisibility(View.VISIBLE);
                    player.setRepeat(true);
                }
            }
        });
    }

    /**
     * Toggle the current play state from playing to paused or the other way around
     */
    private void togglePlaying() {
        if (isPlaying) {
            player.pause();
            disablePlaying();
        } else {
            player.resume();
            enablePlaying();
        }
    }

    /**
     * Go to next song
     */
    private void nextSong() {
        player.skipToNext();
        enablePlaying();
        checkLoggedIn(helper, authTokenRefreshedListener);
    }

    /**
     * Go to previous song
     */
    private void previousSong() {
        player.skipToPrevious();
        enablePlaying();
        checkLoggedIn(helper, authTokenRefreshedListener);
    }

    /**
     * Update all parameters to mark music as playing. This includes the isPlaying boolean and the play button icon
     */
    private void enablePlaying() {
        isPlaying = true;
        playButton.setImageDrawable(getDrawable(R.drawable.btn_pause));

        getAudioFocus();

        if (!mSession.isActive()) {
            mSession.setActive(true);
            mSession.setPlaybackState(getPlaybackState());
        }
    }

    /**
     * Request for our app to be the main audio focus
     */
    private void getAudioFocus() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.e(TAG, "audio focus request denied...");
        } else {
            Log.v(TAG, "audio focus request granted");
        }
    }

    /**
     * Update all parameters to mark music as paused. This includes the isPlaying boolean and the pause button icon
     */
    private void disablePlaying() {
        isPlaying = false;
        playButton.setImageDrawable(getDrawable(R.drawable.btn_play));

        dropAudioFocus();

        if (mSession.isActive()) {
            mSession.setActive(false);
            mSession.setPlaybackState(getPlaybackState());
        }
    }

    /**
     * Drop audio focus so other apps can pick it up
     */
    private void dropAudioFocus() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.abandonAudioFocus(afChangeListener);
    }

    /**
     * Get the current playback state for the now playing card
     */
    private PlaybackState getPlaybackState() {
        long position = PlaybackState.PLAYBACK_POSITION_UNKNOWN;
        PlaybackState.Builder builder = new PlaybackState.Builder().setActions(getAvailableActions());
        builder.setState(isPlaying ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED, position, 1.0f);
        return builder.build();
    }

    /**
     * Get the playback state actions (skip, previous, play, pause, etc)
     */
    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS;

        if (isPlaying) {
            actions |= PlaybackState.ACTION_PAUSE;
        } else {
            actions |= PlaybackState.ACTION_PLAY;
        }

        return actions;
    }

    /**
     * Set metadata for the now playing card to show
     * @param title the currently playing song title
     * @param artist the currently playing artist
     * @param url the currently show background art uri
     */
    private void updateSessionMetadata(final String title, final String artist, final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = NetworkUtils.getBitmapFromURL(url);

                MediaMetadata.Builder builder = new MediaMetadata.Builder();
                builder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title);
                builder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, artist);
                builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bmp);
                builder.putString(MediaMetadata.METADATA_KEY_TITLE, title);
                builder.putString(MediaMetadata.METADATA_KEY_ARTIST, artist);
                mSession.setMetadata(builder.build());
            }
        }).start();
    }

    /**
     * Get the now playing card's session activity to resume to
     */
    private PendingIntent getSessionActivity() {
        Intent intent = new Intent(this, PlayerActivity.class);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Update the background to the next album
     * @param uri the URI of the image to be used for background
     */
    protected void updateBackground(URI uri) {
        try {
            if (isActivityShowing) {
                Picasso.with(this)
                        .load(uri.toString())
                        .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                        .centerCrop()
                        .error(mDefaultBackground)
                        .into(mBackgroundTarget);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error attaching background: ", e);
        }
    }

    /**
     * Register receivers for play, next and previous actions
     */
    private void registerMediaReceiver() {
        registerReceiver(toggleReceiver, new IntentFilter(ACTION_TOGGLE_PLAY));
        registerReceiver(nextReceiver, new IntentFilter(ACTION_NEXT));
        registerReceiver(previousReceiver, new IntentFilter(ACTION_PREVIOUS));
    }

    /**
     * Unregister receivers for play, next and previous actions
     */
    private void unregisterMediaReceiver() {
        unregisterReceiver(toggleReceiver);
        unregisterReceiver(nextReceiver);
        unregisterReceiver(previousReceiver);
    }

    /**
     * Manages changing the background when a song changes and logs playback errors
     */
    private PlayerNotificationCallback notificationCallback = new PlayerNotificationCallback() {
        @Override
        public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
            Log.v(TAG, "playback event: " + eventType);

            if (eventType == EventType.TRACK_CHANGED || eventType == EventType.TRACK_START) {
                String songId = playerState.trackUri;
                currentIndex = songIds.indexOf(songId);

                if (currentIndex != -1) {
                    try {
                        titleTextView.setText(songTitles.get(currentIndex));
                        artistTextView.setText(songArtists.get(currentIndex));
                        URI uri = URI.create(songImages.get(currentIndex));
                        updateSessionMetadata(songTitles.get(currentIndex), songArtists.get(currentIndex), songImages.get(currentIndex));
                        updateBackground(uri);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "error setting background", e);
                        updateBackground(null);
                    }
                } else {
                    Log.e(TAG, "ut oh, error finding song in list");
                    titleTextView.setText(null);
                    artistTextView.setText(null);
                }

                checkLoggedIn(helper, authTokenRefreshedListener);
            } else if (eventType == EventType.LOST_PERMISSION) {
                // this occurs when we start playing music from a different device usually
                disablePlaying();
            }
        }

        @Override
        public void onPlaybackError(ErrorType errorType, String s) {
            Log.e(TAG, "Playback Error! " + s);
        }
    };

    /**
     * Logs all connection information: logging in/out, failure to login, temp errors and other connection messages
     */
    private ConnectionStateCallback connectionStateCallback = new ConnectionStateCallback() {
        @Override
        public void onLoggedIn() {
            Log.v(TAG, "User logged into player");
        }

        @Override
        public void onLoggedOut() {
            Log.v(TAG, "User logged out of player");
        }

        @Override
        public void onLoginFailed(Throwable throwable) {
            Log.e(TAG, "Login failed!", throwable);
        }

        @Override
        public void onTemporaryError() {
            Log.e(TAG, "Temporary Error... hmm.");
        }

        @Override
        public void onConnectionMessage(String s) {
            Log.v(TAG, "New connection message: " + s);
        }
    };

    /**
     * Manages callbacks to audio focus changes
     */
    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                if (player != null) {
                    player.pause();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (player != null) {
                    player.resume();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                am.abandonAudioFocus(afChangeListener);
                if (player != null) {
                    player.pause();
                }
            }
        }
    };

    /**
     * Callback for refreshing auth token. We'll reset the player's login status so that it can keep working correctly
     * after the first token has expired
     */
    private OnAuthTokenRefreshedListener authTokenRefreshedListener = new OnAuthTokenRefreshedListener() {
        @Override
        public void authTokenRefreshed() {
            player.login(settings.spotifyAccount.getAuthToken());
        }
    };

    /**
     * Handle changing the playing status from things like notifications
     */
    private BroadcastReceiver toggleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            togglePlaying();
        }
    };

    /**
     * Handle skipping song from things like notifications
     */
    private BroadcastReceiver nextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            nextSong();
        }
    };

    /**
     * Handle going to previous song from things like notifications
     */
    private BroadcastReceiver previousReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            previousSong();
        }
    };

    /**
     * Get the Spotify helper
     * @return singleton of Spotify Helper
     */
    protected SpotifyHelper getSpotifyHelper() {
        return SpotifyHelper.get(this);
    }

}
