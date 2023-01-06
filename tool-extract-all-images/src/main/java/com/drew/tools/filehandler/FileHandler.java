package com.drew.tools.filehandler;

import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;

import java.io.File;
import java.io.PrintStream;

public interface FileHandler {
    /**
     * Called when the scan is about to start processing files in directory <code>path</code>.
     */
    void onStartingDirectory(@NotNull File directoryPath);

    /**
     * Called to determine whether the implementation should process <code>filePath</code>.
     */
    boolean shouldProcess(@NotNull File file);

    /**
     * Called before extraction is performed on <code>filePath</code>.
     */
    void onBeforeExtraction(@NotNull File file, @NotNull PrintStream log, @NotNull String relativePath);

    /**
     * Called when extraction on <code>filePath</code> completed without an exception.
     */
    void onExtractionSuccess(@NotNull File file, @NotNull Metadata metadata, @NotNull String relativePath, @NotNull PrintStream log);

    /**
     * Called when extraction on <code>filePath</code> resulted in an exception.
     */
    void onExtractionError(@NotNull File file, @NotNull Throwable throwable, @NotNull PrintStream log);

    /**
     * Called when all files have been processed.
     */
    void onScanCompleted(@NotNull PrintStream log);
}
