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

package com.drew.tools;

import com.drew.filetypes.imaging.ImageMetadataReader;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;
import com.drew.tools.filehandler.BasicFileHandler;
import com.drew.tools.filehandler.FileHandler;
import com.drew.tools.filehandler.TextFileOutputHandler;
import com.drew.tools.filehandler.UnknownTagHandler;
import com.drew.tools.filehandler.markdown.MarkdownTableOutputHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Drew Noakes https://drewnoakes.com
 */
public class ProcessAllImagesInFolderUtility
{
    public static void main(String[] args) throws IOException
    {
        List<String> directories = new ArrayList<String>();

        FileHandler handler = null;
        PrintStream log = System.out;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--text")) {
                // If "--text" is specified, write the discovered metadata into a sub-folder relative to the image
                handler = new TextFileOutputHandler();
            } else if (arg.equalsIgnoreCase("--markdown")) {
                // If "--markdown" is specified, write a summary table in markdown format to standard out
                handler = new MarkdownTableOutputHandler();
            } else if (arg.equalsIgnoreCase("--unknown")) {
                // If "--unknown" is specified, write CSV tallying unknown tag counts
                handler = new UnknownTagHandler();
            } else if (arg.equalsIgnoreCase("--log-file")) {
                if (i == args.length - 1) {
                    printUsage();
                    System.exit(1);
                }
                log = new PrintStream(new FileOutputStream(args[++i], false), true);
            } else {
                // Treat this argument as a directory
                directories.add(arg);
            }
        }

        if (directories.isEmpty()) {
            System.err.println("Expects one or more directories as arguments.");
            printUsage();
            System.exit(1);
        }

        if (handler == null) {
            handler = new BasicFileHandler();
        }

        long start = System.nanoTime();

        for (String directory : directories) {
            processDirectory(new File(directory), handler, "", log);
        }

        handler.onScanCompleted(log);

        System.out.println(String.format("Completed in %d ms", (System.nanoTime() - start) / 1000000));

        if (log != System.out) {
            log.close();
        }
    }

    private static void printUsage()
    {
        System.out.println("Usage:");
        System.out.println();
        System.out.println("  java com.drew.tools.ProcessAllImagesInFolderUtility [--text|--markdown|--unknown] [--log-file <file-name>]");
    }

    private static void processDirectory(@NotNull File path, @NotNull FileHandler handler, @NotNull String relativePath, PrintStream log)
    {
        handler.onStartingDirectory(path);

        String[] pathItems = path.list();

        if (pathItems == null) {
            return;
        }

        // Order alphabetically so that output is stable across invocations
        Arrays.sort(pathItems);

        for (String pathItem : pathItems) {
            File file = new File(path, pathItem);

            if (file.isDirectory()) {
                processDirectory(file, handler, relativePath.length() == 0 ? pathItem : relativePath + "/" + pathItem, log);
            } else if (handler.shouldProcess(file)) {

                handler.onBeforeExtraction(file, log, relativePath);

                // Read metadata
                final Metadata metadata;
                try {
                    metadata = ImageMetadataReader.readMetadata(file);
                } catch (Throwable t) {
                    handler.onExtractionError(file, t, log);
                    continue;
                }

                handler.onExtractionSuccess(file, metadata, relativePath, log);
            }
        }
    }
}
