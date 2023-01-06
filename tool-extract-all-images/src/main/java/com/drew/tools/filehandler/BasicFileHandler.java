package com.drew.tools.filehandler;

import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.PrintStream;

/**
 * Does nothing with the output except enumerate it in memory and format descriptions. This is useful in order to
 * flush out any potential exceptions raised during the formatting of extracted value descriptions.
 */
public class BasicFileHandler extends FileHandlerBase {
    @Override
    public void onExtractionSuccess(@NotNull File file, @NotNull Metadata metadata, @NotNull String relativePath, @NotNull PrintStream log) {
        super.onExtractionSuccess(file, metadata, relativePath, log);

        // Iterate through all values, calling toString to flush out any formatting exceptions
        for (Directory directory : metadata.getDirectories()) {
            directory.getName();
            for (Tag tag : directory.getTags()) {
                tag.getTagName();
                tag.getDescription();
            }
        }
    }
}
