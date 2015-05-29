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

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FsFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SpotifyTestRunner extends RobolectricTestRunner {

    public SpotifyTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Properties getConfigProperties() {
        Properties properties = super.getConfigProperties();
        if  (properties == null) {
            properties = new Properties();
        }

        properties.setProperty("emulateSdk", "21");
        return properties;
    }

    @Override
    protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir) {
        File currentDir = new File("");
        final String prefix;

        if (!currentDir.getAbsolutePath().endsWith("app")) {
            prefix = "app/";
        } else {
        	prefix = "";
        }

        manifestFile = new FileFsFile(new File(prefix + "src/main/AndroidManifest.xml"));
        resDir = new FileFsFile(new File(prefix + "src/main/res/"));
        assetsDir = new FileFsFile(new File(prefix + "src/main/assets/"));

        return new AndroidManifest(manifestFile, resDir, assetsDir) {
            @Override
            public List<FsFile> findLibraries() {
                List<FsFile> libraries = new ArrayList<FsFile>();
                libraries.add(new FileFsFile(new File(prefix + "build/intermediates/exploded-aar/com.android.support/leanback-v17/22.2.0")));
                return libraries;
            }
        };
    }

}
