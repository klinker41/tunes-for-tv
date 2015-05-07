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

package com.klinker.android.spotify.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.klinker.android.spotify.R;
import com.klinker.android.spotify.data.Song;
import com.klinker.android.spotify.util.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;

/**
 * Presenter for displaying playlists and songs to a user. This is based off of a RecyclerView
 */
public class CardPresenter extends Presenter {

    private static final String TAG = "CardPresenter";

    private static Context mContext;
    private static int CARD_WIDTH = 250;
    private static int CARD_HEIGHT = 250;

    /**
     * ViewHolder to hold all data that we wish to present
     */
    static class ViewHolder extends Presenter.ViewHolder {
        @Getter @Setter private Song song;
        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;
        private PicassoImageCardViewTarget mImageCardViewTarget;

        /**
         * Constructs new ViewHolder based off of view
         */
        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
            mImageCardViewTarget = new PicassoImageCardViewTarget(mCardView);
            mDefaultCardImage = new ColorDrawable(mContext.getResources().getColor(R.color.fastlane_background));
        }

        /**
         * Use picasso to update the card icon with album artwork
         */
        protected void updateCardViewImage(URI uri) {
            Picasso.with(mContext)
                    .load(uri.toString())
                    .resize(Utils.dpToPx(CARD_WIDTH, mContext), Utils.dpToPx(CARD_HEIGHT, mContext))
                    .error(mDefaultCardImage)
                    .into(mImageCardViewTarget);
        }
    }

    /**
     * Create a new ViewHolder to display data
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        mContext = parent.getContext();

        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
        return new ViewHolder(cardView);
    }

    /**
     * Bind all data to view holder that we wish to display to user
     */
    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        Song song = (Song) item;
        ((ViewHolder) viewHolder).setSong(song);

        if (song.getCardImageUrl() != null) {
            ((ViewHolder) viewHolder).mCardView.setTitleText(song.getTitle());
            ((ViewHolder) viewHolder).mCardView.setContentText(song.getArtist());
            ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            ((ViewHolder) viewHolder).mImageCardViewTarget.mImageCardView.getMainImageView().setImageDrawable(null);
            ((ViewHolder) viewHolder).updateCardViewImage(song.getCardImageURI());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        // TO DO
    }

    public static class PicassoImageCardViewTarget implements Target {
        private ImageCardView mImageCardView;

        public PicassoImageCardViewTarget(ImageCardView imageCardView) {
            mImageCardView = imageCardView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
            mImageCardView.setMainImage(bitmapDrawable);
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            mImageCardView.setMainImage(drawable);
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            // Do nothing, default_background manager has its own transitions
        }
    }

}
