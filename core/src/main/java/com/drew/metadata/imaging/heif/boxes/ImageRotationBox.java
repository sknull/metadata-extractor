package com.drew.metadata.imaging.heif.boxes;

import com.drew.lang.SequentialReader;
import com.drew.metadata.imaging.heif.HeifDirectory;

import java.io.IOException;

/**
 * ISO/IEC 23008-12:2017 pg.15
 */
public class ImageRotationBox extends Box
{
    int angle;

    public ImageRotationBox(SequentialReader reader, Box box) throws IOException
    {
        super(box);

        // First 6 bits are reserved
        angle = reader.getUInt8() & 0x03;
    }

    public void addMetadata(HeifDirectory directory)
    {
        if (!directory.containsTag(HeifDirectory.TAG_IMAGE_ROTATION)) {
            directory.setInt(HeifDirectory.TAG_IMAGE_ROTATION, angle);
        }
    }
}
