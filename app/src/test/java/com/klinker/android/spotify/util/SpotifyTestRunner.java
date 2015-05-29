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
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.*;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SpotifyTestRunner extends RobolectricGradleTestRunner {

    private static final String BUILD_OUTPUT = "build/intermediates";

    public SpotifyTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected Properties getConfigProperties() {
        Properties properties = super.getConfigProperties();
        if  (properties == null) {
            properties = new Properties();
        }

        return properties;
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        if (config.constants() == Void.class) {
            Logger.error("Field 'constants' not specified in @Config annotation");
            Logger.error("This is required when using RobolectricGradleTestRunner!");
            throw new RuntimeException("No 'constants' field in @Config annotation!");
        }

        final String type = getType(config);
        final String flavor = getFlavor(config);
        final String packageName = getPackageName(config);

        final org.robolectric.res.FileFsFile res;
        final org.robolectric.res.FileFsFile assets;
        final org.robolectric.res.FileFsFile manifest;

        if (org.robolectric.res.FileFsFile.from(BUILD_OUTPUT, "res").exists()) {
            res = org.robolectric.res.FileFsFile.from(BUILD_OUTPUT, "res", "merged", flavor, type);
        } else {
            res = org.robolectric.res.FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "res");
        }

        if (org.robolectric.res.FileFsFile.from(BUILD_OUTPUT, "assets").exists()) {
            assets = org.robolectric.res.FileFsFile.from(BUILD_OUTPUT, "assets", flavor, type);
        } else {
            assets = org.robolectric.res.FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "assets");
        }

        if (org.robolectric.res.FileFsFile.from(BUILD_OUTPUT, "manifests").exists()) {
            manifest = org.robolectric.res.FileFsFile.from(BUILD_OUTPUT, "manifests", "full", flavor, type, "AndroidManifest.xml");
        } else {
            manifest = org.robolectric.res.FileFsFile.from(BUILD_OUTPUT, "bundles", flavor, type, "AndroidManifest.xml");
        }

        Logger.debug("Robolectric assets directory: " + assets.getPath());
        Logger.debug("   Robolectric res directory: " + res.getPath());
        Logger.debug("   Robolectric manifest path: " + manifest.getPath());
        Logger.debug("    Robolectric package name: " + packageName);
        return new AndroidManifest(manifest, res, assets, packageName);
    }

    private String getType(Config config) {
        try {
            return ReflectionHelpers.getStaticField(config.constants(), "BUILD_TYPE");
        } catch (Throwable e) {
            return null;
        }
    }

    private String getFlavor(Config config) {
        try {
            return ReflectionHelpers.getStaticField(config.constants(), "FLAVOR");
        } catch (Throwable e) {
            return null;
        }
    }

    private String getPackageName(Config config) {
        try {
            final String packageName = config.packageName();
            if (packageName != null && !packageName.isEmpty()) {
                return packageName;
            } else {
                return ReflectionHelpers.getStaticField(config.constants(), "APPLICATION_ID");
            }
        } catch (Throwable e) {
            return null;
        }
    }

}
