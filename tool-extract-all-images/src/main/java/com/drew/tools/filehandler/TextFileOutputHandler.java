package com.drew.tools.filehandler;

import com.adobe.internal.xmp.XMPException;
import com.adobe.internal.xmp.XMPIterator;
import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.options.IteratorOptions;
import com.adobe.internal.xmp.properties.XMPPropertyInfo;
import com.drew.filetypes.FileType;
import com.drew.filetypes.FileTypeDetector;
import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.file.FileSystemDirectory;
import com.drew.metadata.imaging.xmp.XmpDirectory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Writes a text file containing the extracted metadata for each input file.
 */
public class TextFileOutputHandler extends FileHandlerBase {
    /**
     * Standardise line ending so that generated files can be more easily diffed.
     */
    private static final String NEW_LINE = "\n";

    @Override
    public void onStartingDirectory(@NotNull File directoryPath) {
        super.onStartingDirectory(directoryPath);

        // Delete any existing 'metadata' folder
        File metadataDirectory = new File(directoryPath + "/metadata/java");
        try {
            System.out.println("Using output directory: " + metadataDirectory.getCanonicalPath());
        } catch (IOException e) {
            // ignore
        }
        if (metadataDirectory.exists()) {
            deleteRecursively(metadataDirectory);
        }
    }

    private static void deleteRecursively(@NotNull File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Must be a directory.");
        }

        if (directory.exists()) {
            String[] list = directory.list();
            if (list != null) {
                for (String item : list) {
                    File file = new File(item);
                    if (file.isDirectory()) {
                        deleteRecursively(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }

        directory.delete();
    }

    @Override
    public void onBeforeExtraction(@NotNull File file, @NotNull PrintStream log, @NotNull String relativePath) {
        super.onBeforeExtraction(file, log, relativePath);
        log.print(file.getAbsoluteFile());
        log.print(NEW_LINE);
    }

    @Override
    public void onExtractionSuccess(@NotNull File file, @NotNull Metadata metadata, @NotNull String relativePath, @NotNull PrintStream log) {
        super.onExtractionSuccess(file, metadata, relativePath, log);

        try {
            PrintWriter writer = null;
            try {
                writer = openWriter(file);

                // Write any errors
                if (metadata.hasErrors()) {
                    for (Directory directory : metadata.getDirectories()) {
                        if (!directory.hasErrors()) {
                            continue;
                        }
                        for (String error : directory.getErrors())
                            writer.format("[ERROR: %s] %s%s", directory.getName(), error, NEW_LINE);
                    }
                    writer.write(NEW_LINE);
                }

                // Write tag values for each directory
                for (Directory directory : metadata.getDirectories()) {
                    String directoryName = directory.getName();
                    // Write the directory's tags
                    for (Tag tag : directory.getTags()) {
                        String tagName = tag.getTagName();
                        String description;
                        try {
                            description = tag.getDescription();
                        } catch (Exception ex) {
                            description = "ERROR: " + ex.getMessage();
                        }
                        if (description == null) {
                            description = "";
                        }
                        // Skip the file write-time as this changes based on the time at which the regression test image repository was cloned
                        if (directory instanceof FileSystemDirectory && tag.getTagType() == FileSystemDirectory.TAG_FILE_MODIFIED_DATE) {
                            description = "<omitted for regression testing as checkout dependent>";
                        }
                        writer.format("[%s - %s] %s = %s%s", directoryName, tag.getTagTypeHex(), tagName, description, NEW_LINE);
                    }
                    if (directory.getTagCount() != 0) {
                        writer.write(NEW_LINE);
                    }
                    // Special handling for XMP directory data
                    if (directory instanceof XmpDirectory) {
                        boolean wrote = false;
                        XmpDirectory xmpDirectory = (XmpDirectory) directory;
                        XMPMeta xmpMeta = xmpDirectory.getXMPMeta();
                        try {
                            IteratorOptions options = new IteratorOptions().setJustLeafnodes(true);
                            XMPIterator iterator = xmpMeta.iterator(options);
                            while (iterator.hasNext()) {
                                XMPPropertyInfo prop = (XMPPropertyInfo) iterator.next();
                                String ns = prop.getNamespace();
                                String path = prop.getPath();
                                String value = prop.getValue();

                                if (path == null) {
                                    continue;
                                }
                                if (ns == null) {
                                    ns = "";
                                }

                                final int MAX_XMP_VALUE_LENGTH = 512;
                                if (value == null) {
                                    value = "";
                                } else if (value.length() > MAX_XMP_VALUE_LENGTH) {
                                    value = String.format("%s <truncated from %d characters>", value.substring(0, MAX_XMP_VALUE_LENGTH), value.length());
                                }

                                writer.format("[XMPMeta - %s] %s = %s%s", ns, path, value, NEW_LINE);
                                wrote = true;
                            }
                        } catch (XMPException e) {
                            e.printStackTrace();
                        }
                        if (wrote) {
                            writer.write(NEW_LINE);
                        }
                    }
                }

                // Write file structure
                writeHierarchyLevel(metadata, writer, null, 0);

                writer.write(NEW_LINE);
            } finally {
                closeWriter(writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeHierarchyLevel(@NotNull Metadata metadata, @NotNull PrintWriter writer, @Nullable Directory parent, int level) {
        final int indent = 4;

        for (Directory child : metadata.getDirectories()) {
            if (parent == null) {
                if (child.getParent() != null) {
                    continue;
                }
            } else if (!parent.equals(child.getParent())) {
                continue;
            }

            for (int i = 0; i < level * indent; i++) {
                writer.write(' ');
            }
            writer.write("- ");
            writer.write(child.getName());
            writer.write(NEW_LINE);
            writeHierarchyLevel(metadata, writer, child, level + 1);
        }
    }

    @Override
    public void onExtractionError(@NotNull File file, @NotNull Throwable throwable, @NotNull PrintStream log) {
        super.onExtractionError(file, throwable, log);

        try {
            PrintWriter writer = null;
            try {
                writer = openWriter(file);
                writer.write("EXCEPTION: " + throwable.getMessage() + NEW_LINE);
                writer.write(NEW_LINE);
            } finally {
                closeWriter(writer);
            }
        } catch (IOException e) {
            log.printf("IO exception writing metadata file: %s%s", e.getMessage(), NEW_LINE);
        }
    }

    @NotNull
    private static PrintWriter openWriter(@NotNull File file) throws IOException {
        // Create the output directory if it doesn't exist
        File metadataDir = new File(String.format("%s/metadata", file.getParent()));
        if (!metadataDir.exists()) {
            metadataDir.mkdir();
        }

        File javaDir = new File(String.format("%s/metadata/java", file.getParent()));
        if (!javaDir.exists()) {
            javaDir.mkdir();
        }

        String outputPath = String.format("%s/metadata/java/%s.txt", file.getParent(), file.getName());
        Writer writer = new OutputStreamWriter(
            new FileOutputStream(outputPath),
            "UTF-8"
        );
        writer.write("FILE: " + file.getName() + NEW_LINE);

        // Detect file type
        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
            FileType fileType = FileTypeDetector.detectFileType(stream);
            writer.write(String.format("TYPE: %s" + NEW_LINE, fileType.toString().toUpperCase()));
            writer.write(NEW_LINE);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return new PrintWriter(writer);
    }

    private static void closeWriter(@Nullable Writer writer) throws IOException {
        if (writer != null) {
            writer.write("Generated using metadata-extractor" + NEW_LINE);
            writer.write("https://drewnoakes.com/code/exif/" + NEW_LINE);
            writer.flush();
            writer.close();
        }
    }
}
