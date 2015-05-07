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

import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.util.Util;

import java.io.*;

public class FileFsFile implements FsFile {

    private File file;


    public FileFsFile(File file) {
        try {
            this.file = file.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists() {
        return this.file.exists();
    }

    public boolean isDirectory() {
        return this.file.isDirectory();
    }

    public boolean isFile() {
        return this.file.isFile();
    }

    public FsFile[] listFiles() {
        return this.asFsFiles(this.file.listFiles());
    }

    public FsFile[] listFiles(final Filter filter) {
        return this.asFsFiles(this.file.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return filter.accept(new FileFsFile(pathname));
            }
        }));
    }

    public String[] listFileNames() {
        File[] files = this.file.listFiles();
        if(files == null) {
            return null;
        } else {
            String[] strings = new String[files.length];

            for(int i = 0; i < files.length; ++i) {
                strings[i] = files[i].getName();
            }

            return strings;
        }
    }

    public FsFile getParent() {
        File parentFile = this.file.getParentFile();
        return parentFile == null?null: Fs.newFile(parentFile);
    }

    public String getName() {
        return this.file.getName();
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file);
    }

    public byte[] getBytes() throws IOException {
        return Util.readBytes(new FileInputStream(this.file));
    }

    public FsFile join(String ... pathParts) {
        File f = this.file;
        String[] arr$ = pathParts;
        int len$ = pathParts.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String pathPart = arr$[i$];
            f = new File(f, pathPart);
        }

        return Fs.newFile(f);
    }

    public File getFile() {
        return this.file;
    }

    public String toString() {
        return this.file.getPath();
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            FileFsFile fsFile = (FileFsFile)o;
            return this.file.equals(fsFile.file);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.file.hashCode();
    }

    private FsFile[] asFsFiles(File[] files) {
        if(files == null) {
            return null;
        } else {
            FsFile[] fsFiles = new FsFile[files.length];

            for(int i = 0; i < files.length; ++i) {
                fsFiles[i] = Fs.newFile(files[i]);
            }

            return fsFiles;
        }
    }

    public String getBaseName() {
        String name = this.getName();
        int dotIndex = name.indexOf(".");
        return dotIndex >= 0?name.substring(0, dotIndex):name;
    }

    public String getPath() {
        return this.file.getPath();
    }
}