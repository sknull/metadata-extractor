package com.drew.tools.filehandler.markdown;

import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.ExifThumbnailDirectory;

import java.io.File;

class Row {
    final File file;
    final Metadata metadata;
    @NotNull
    final String relativePath;
    @Nullable
    String manufacturer;
    @Nullable
    String model;
    @Nullable
    String exifVersion;
    @Nullable
    String thumbnail;
    @Nullable
    String makernote;

    Row(@NotNull File file, @NotNull Metadata metadata, @NotNull String relativePath) {
        this.file = file;
        this.metadata = metadata;
        this.relativePath = relativePath;

        ExifIFD0Directory ifd0Dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        ExifSubIFDDirectory subIfdDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        ExifThumbnailDirectory thumbDir = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
        if (ifd0Dir != null) {
            manufacturer = ifd0Dir.getDescription(ExifIFD0Directory.TAG_MAKE);
            model = ifd0Dir.getDescription(ExifIFD0Directory.TAG_MODEL);
        }
        boolean hasMakernoteData = false;
        if (subIfdDir != null) {
            exifVersion = subIfdDir.getDescription(ExifSubIFDDirectory.TAG_EXIF_VERSION);
            hasMakernoteData = subIfdDir.containsTag(ExifSubIFDDirectory.TAG_MAKERNOTE);
        }
        if (thumbDir != null) {
            Integer width = thumbDir.getInteger(ExifThumbnailDirectory.TAG_IMAGE_WIDTH);
            Integer height = thumbDir.getInteger(ExifThumbnailDirectory.TAG_IMAGE_HEIGHT);
            thumbnail = width != null && height != null
                ? String.format("Yes (%s x %s)", width, height)
                : "Yes";
        }
        for (Directory directory : metadata.getDirectories()) {
            if (directory.getClass().getName().contains("Makernote")) {
                makernote = directory.getName().replace("Makernote", "").trim();
                break;
            }
        }
        if (makernote == null) {
            makernote = hasMakernoteData ? "(Unknown)" : "N/A";
        }
    }
}
