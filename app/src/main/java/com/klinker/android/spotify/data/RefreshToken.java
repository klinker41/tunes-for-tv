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

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

/**
 * Helper class for storing refresh tokens and expiration information
 */
@Getter
public class RefreshToken extends JSONParser {

    private static final String TAG = "RefreshToken";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String EXPIRES_IN = "expires_in";

    private String accessToken;
    private long expiresIn;

    /**
     * Get an access token and expiration date from a JSON object
     */
    public RefreshToken(JSONObject json) {
        this.accessToken = parseString(json, ACCESS_TOKEN);
        this.expiresIn = parseLong(json, EXPIRES_IN);
    }

}
