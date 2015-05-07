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

import com.klinker.android.spotify.AbstractSpotifyHelper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RefreshTokenTest extends AbstractSpotifyHelper {

    private RefreshToken token;

    @Before
    public void setUp() throws Exception {
        JSONObject json = new JSONObject(
                "{\n" +
                        "   \"access_token\": \"NgA6ZcYI...ixn8bUQ\",\n" +
                        "   \"token_type\": \"Bearer\",\n" +
                        "   \"expires_in\": 3600\n" +
                        "}"
        );

        token = new RefreshToken(json);
    }

    @Test
    public void test_create() {
        assertEquals("NgA6ZcYI...ixn8bUQ", token.getAccessToken());
        assertEquals(3600, token.getExpiresIn());
    }

}