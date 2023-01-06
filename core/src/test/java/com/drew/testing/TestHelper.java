/*
 * Copyright 2002-2019 Drew Noakes and contributors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
package com.drew.testing;

import com.drew.lang.StreamReader;
import com.drew.lang.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Drew Noakes https://drewnoakes.com
 */
public class TestHelper
{
    public static byte[] skipBytes(byte[] input, int countToSkip)
    {
        if (input.length - countToSkip < 0) {
            throw new IllegalArgumentException("Attempting to skip more bytes than exist in the array.");
        }

        byte[] output = new byte[input.length - countToSkip];
        System.arraycopy(input, countToSkip, output, 0, input.length - countToSkip);
        return output;
    }

    public static File getResourceFile(String resource) {
        final URL url = ClassLoader.getSystemResource(resource);
        File file = null;
        if (url != null) {
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Could not create file for resource '" + resource + "'", e);
            }
        }
        return file;
    }

    public static StreamReader getStreamReader(String resource) {
        final StreamReader streamReader;
        try {
            streamReader = new StreamReader(ClassLoader.getSystemResourceAsStream(resource));
        } catch (Exception e) {
            throw new IllegalStateException("Could not create streamreader for resource '" + resource + "'", e);
        }
        return streamReader;
    }

    /**
     * Reads the contents of a {@link File} into a <code>byte[]</code>. This relies upon <code>File.length()</code>
     * returning the correct value, which may not be the case when using a network file system. However this method is
     * intended for unit test support, in which case the files should be on the local volume.
     */
    @NotNull
    public static byte[] readBytes(@NotNull String resource) throws IOException
    {
        File file = getResourceFile(resource);
        int length = (int)file.length();
        // should only be zero if loading from a network or similar
        assert (length != 0);
        byte[] bytes = new byte[length];

        int totalBytesRead = 0;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            while (totalBytesRead != length) {
                int bytesRead = inputStream.read(bytes, totalBytesRead, length - totalBytesRead);
                if (bytesRead == -1) {
                    break;
                }
                totalBytesRead += bytesRead;
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return bytes;
    }
}
