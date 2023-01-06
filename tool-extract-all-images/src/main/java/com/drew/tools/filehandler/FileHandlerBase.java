package com.drew.tools.filehandler;

import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class FileHandlerBase implements FileHandler {
    // TODO obtain these from FileType enum directly
    private final Set<String> _supportedExtensions = new HashSet<String>(
        Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "heic", "heif", "ico", "webp", "pcx", "ai", "eps",
            "nef", "crw", "cr2", "orf", "arw", "raf", "srw", "x3f", "rw2", "rwl", "dcr", "pef",
            "tif", "tiff", "psd", "dng",
            "j2c", "jp2", "jpf", "jpm", "mj2",
            "mp3", "wav",
            "3g2", "3gp", "m4v", "mov", "mp4", "m2v", "m2ts", "mts",
            "pbm", "pnm", "pgm", "ppm",
            "avi",
            "fuzzed"));

    private int _processedFileCount = 0;
    private int _exceptionCount = 0;
    private int _errorCount = 0;
    private long _processedByteCount = 0;

    public void onStartingDirectory(@NotNull File directoryPath) {
    }

    public boolean shouldProcess(@NotNull File file) {
        String extension = getExtension(file);
        return extension != null && _supportedExtensions.contains(extension.toLowerCase());
    }

    public void onBeforeExtraction(@NotNull File file, @NotNull PrintStream log, @NotNull String relativePath) {
        _processedFileCount++;
        _processedByteCount += file.length();
    }

    public void onExtractionError(@NotNull File file, @NotNull Throwable throwable, @NotNull PrintStream log) {
        _exceptionCount++;
        log.printf("\t[%s] %s\n", throwable.getClass().getName(), throwable.getMessage());
    }

    public void onExtractionSuccess(@NotNull File file, @NotNull Metadata metadata, @NotNull String relativePath, @NotNull PrintStream log) {
        if (metadata.hasErrors()) {
            log.print(file);
            log.print('\n');
            for (Directory directory : metadata.getDirectories()) {
                if (!directory.hasErrors()) {
                    continue;
                }
                for (String error : directory.getErrors()) {
                    log.printf("\t[%s] %s\n", directory.getName(), error);
                    _errorCount++;
                }
            }
        }
    }

    public void onScanCompleted(@NotNull PrintStream log) {
        if (_processedFileCount > 0) {
            log.print(String.format(
                "Processed %,d files (%,d bytes) with %,d exceptions and %,d file errors\n",
                _processedFileCount, _processedByteCount, _exceptionCount, _errorCount
            ));
        }
    }

    @Nullable
    protected String getExtension(@NotNull File file) {
        String fileName = file.getName();
        int i = fileName.lastIndexOf('.');
        if (i == -1) {
            return null;
        }
        if (i == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(i + 1);
    }
}
