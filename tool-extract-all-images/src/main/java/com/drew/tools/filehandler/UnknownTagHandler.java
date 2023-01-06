package com.drew.tools.filehandler;

import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of unknown tags.
 */
public class UnknownTagHandler extends FileHandlerBase {
    private HashMap<String, HashMap<Integer, Integer>> _occurrenceCountByTagByDirectory = new HashMap<String, HashMap<Integer, Integer>>();

    @Override
    public void onExtractionSuccess(@NotNull File file, @NotNull Metadata metadata, @NotNull String relativePath, @NotNull PrintStream log) {
        super.onExtractionSuccess(file, metadata, relativePath, log);

        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {

                // Only interested in unknown tags (those without names)
                if (tag.hasTagName()) {
                    continue;
                }

                HashMap<Integer, Integer> occurrenceCountByTag = _occurrenceCountByTagByDirectory.get(directory.getName());
                if (occurrenceCountByTag == null) {
                    occurrenceCountByTag = new HashMap<Integer, Integer>();
                    _occurrenceCountByTagByDirectory.put(directory.getName(), occurrenceCountByTag);
                }

                Integer count = occurrenceCountByTag.get(tag.getTagType());
                if (count == null) {
                    count = 0;
                    occurrenceCountByTag.put(tag.getTagType(), 0);
                }

                occurrenceCountByTag.put(tag.getTagType(), count + 1);
            }
        }
    }

    @Override
    public void onScanCompleted(@NotNull PrintStream log) {
        super.onScanCompleted(log);

        for (Map.Entry<String, HashMap<Integer, Integer>> pair1 : _occurrenceCountByTagByDirectory.entrySet()) {
            String directoryName = pair1.getKey();
            List<Map.Entry<Integer, Integer>> counts = new ArrayList<Map.Entry<Integer, Integer>>(pair1.getValue().entrySet());
            Collections.sort(counts, new Comparator<Map.Entry<Integer, Integer>>() {
                public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            for (Map.Entry<Integer, Integer> pair2 : counts) {
                Integer tagType = pair2.getKey();
                Integer count = pair2.getValue();
                log.format("%s, 0x%04X, %d\n", directoryName, tagType, count);
            }
        }
    }
}
