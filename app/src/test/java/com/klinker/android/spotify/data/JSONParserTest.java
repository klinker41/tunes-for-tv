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
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JSONParserTest extends AbstractSpotifyHelper {

    private JSONParser parser;

    @Before
    public void setUp() {
        parser = new JSONParser();
    }

    @Test
    public void test_parseString() throws Exception {
        JSONObject object = new JSONObject();
        object.put("test", "test object");

        assertEquals("test object", parser.parseString(object, "test"));
    }

    @Test
    public void test_parseString_null() throws Exception {
        JSONObject object = new JSONObject();
        object.put("test", "test object");

        assertEquals("", parser.parseString(object, "not_valid"));
    }

    @Test
    public void test_parseLong() throws Exception {
        JSONObject object = new JSONObject();
        object.put("test", 111l);

        assertEquals(111l, parser.parseLong(object, "test"));
    }

    @Test
    public void test_parseLong_null() throws Exception {
        JSONObject object = new JSONObject();
        object.put("test", 111l);

        assertEquals(0, parser.parseLong(object, "not_valid"));
    }

    @Test
    public void test_parseBoolean() throws Exception {
        JSONObject object = new JSONObject();
        object.put("test", true);

        assertEquals(true, parser.parseBoolean(object, "test"));
    }

    @Test
    public void test_parseBoolean_null() throws Exception {
        JSONObject object = new JSONObject();
        object.put("test", true);

        assertEquals(false, parser.parseBoolean(object, "not_valid"));
    }

    @Test
    public void test_parseJSONObject() throws Exception {
        JSONObject object = new JSONObject();
        JSONObject innerObject = new JSONObject();
        object.put("test", innerObject);

        assertEquals(innerObject, parser.parseJSONObject(object, "test"));
    }

    @Test
    public void test_parseJSONObject_null() throws Exception {
        JSONObject object = new JSONObject();
        JSONObject innerObject = new JSONObject();
        object.put("test", innerObject);

        assertEquals(null, parser.parseJSONObject(object, "not_valid"));
    }

    @Test
    public void test_parseJSONArray() throws Exception {
        JSONObject object = new JSONObject();
        JSONArray innerArray = new JSONArray();
        object.put("test", innerArray);

        assertEquals(innerArray, parser.parseJSONArray(object, "test"));
    }

    @Test
    public void test_parseJSONArray_null() throws Exception {
        JSONObject object = new JSONObject();
        JSONArray innerArray = new JSONArray();
        object.put("test", innerArray);

        assertEquals(null, parser.parseJSONArray(object, "not_valid"));
    }

}