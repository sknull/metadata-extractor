package com.drew.tools.filehandler.markdown;

import com.drew.lang.StringUtil;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;
import com.drew.tools.filehandler.FileHandlerBase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a table describing sample images using Wiki markdown.
 */
public class MarkdownTableOutputHandler extends FileHandlerBase {
    private final Map<String, String> _extensionEquivalence = new HashMap<String, String>();
    private final Map<String, List<Row>> _rowListByExtension = new HashMap<String, List<Row>>();

    public MarkdownTableOutputHandler() {
        _extensionEquivalence.put("jpeg", "jpg");
    }

    @Override
    public void onExtractionSuccess(@NotNull File file, @NotNull Metadata metadata, @NotNull String relativePath, @NotNull PrintStream log) {
        super.onExtractionSuccess(file, metadata, relativePath, log);

        String extension = getExtension(file);

        if (extension == null) {
            return;
        }

        // Sanitise the extension
        extension = extension.toLowerCase();
        if (_extensionEquivalence.containsKey(extension)) {
            extension = _extensionEquivalence.get(extension);
        }

        List<Row> list = _rowListByExtension.get(extension);
        if (list == null) {
            list = new ArrayList<Row>();
            _rowListByExtension.put(extension, list);
        }
        list.add(new Row(file, metadata, relativePath));
    }

    @Override
    public void onScanCompleted(@NotNull PrintStream log) {
        super.onScanCompleted(log);

        OutputStream outputStream = null;
        PrintStream stream = null;
        try {
            outputStream = new FileOutputStream("./wiki/ImageDatabaseSummary.md", false);
            stream = new PrintStream(outputStream, false);
            writeOutput(stream);
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeOutput(@NotNull PrintStream stream) throws IOException {
        Writer writer = new OutputStreamWriter(stream);
        writer.write("# Image Database Summary\n\n");

        for (Map.Entry<String, List<Row>> entry : _rowListByExtension.entrySet()) {
            String extension = entry.getKey();
            writer.write("## " + extension.toUpperCase() + " Files\n\n");

            writer.write("Error|File|Manufacturer|Model|Dir Count|Exif?|Makernote|Thumbnail|All Data\n");
            writer.write("-----|----|------------|-----|---------|-----|---------|---------|--------\n");

            List<Row> rows = entry.getValue();

            // Order by manufacturer, then model
            Collections.sort(rows, new Comparator<Row>() {
                public int compare(Row o1, Row o2) {
                    int c1 = StringUtil.compare(o1.manufacturer, o2.manufacturer);
                    return c1 != 0 ? c1 : StringUtil.compare(o1.model, o2.model);
                }
            });

            for (Row row : rows) {
                writer.write(String.format("[%s]|[%s](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/%s/%s)|%s|%s|%d|%s|%s|%s|[metadata](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/%s/metadata/%s.txt)\n",
                    row.metadata.hasErrors() ? "YES" : "NO",
                    row.file.getName(),
                    row.relativePath,
                    StringUtil.urlEncode(row.file.getName()),
                    row.manufacturer == null ? "" : row.manufacturer,
                    row.model == null ? "" : row.model,
                    row.metadata.getDirectoryCount(),
                    row.exifVersion == null ? "" : row.exifVersion,
                    row.makernote == null ? "" : row.makernote,
                    row.thumbnail == null ? "" : row.thumbnail,
                    row.relativePath,
                    StringUtil.urlEncode(row.file.getName()).toLowerCase()
                    ));
            }

            writer.write('\n');
        }
        writer.flush();
    }
}
