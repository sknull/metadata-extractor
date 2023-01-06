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
package com.drew.metadata.imaging.bmp;

import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.TagDescriptor;

import java.text.DecimalFormat;

/**
 * @author Drew Noakes https://drewnoakes.com
 * @author Nadahar
 */
@SuppressWarnings("WeakerAccess")
public class BmpHeaderDescriptor extends TagDescriptor<BmpHeaderDirectory>
{
    public BmpHeaderDescriptor(@NotNull BmpHeaderDirectory directory)
    {
        super(directory);
    }

    @Override
    public String getDescription(int tagType)
    {
        switch (tagType) {
            case BmpHeaderDirectory.TAG_BITMAP_TYPE:
                return getBitmapTypeDescription();
            case BmpHeaderDirectory.TAG_COMPRESSION:
                return getCompressionDescription();
            case BmpHeaderDirectory.TAG_RENDERING:
                return getRenderingDescription();
            case BmpHeaderDirectory.TAG_COLOR_ENCODING:
                return getColorEncodingDescription();
            case BmpHeaderDirectory.TAG_RED_MASK:
            case BmpHeaderDirectory.TAG_GREEN_MASK:
            case BmpHeaderDirectory.TAG_BLUE_MASK:
            case BmpHeaderDirectory.TAG_ALPHA_MASK:
                return formatHex(_directory.getLongObject(tagType), 8);
            case BmpHeaderDirectory.TAG_COLOR_SPACE_TYPE:
                return getColorSpaceTypeDescription();
            case BmpHeaderDirectory.TAG_GAMMA_RED:
            case BmpHeaderDirectory.TAG_GAMMA_GREEN:
            case BmpHeaderDirectory.TAG_GAMMA_BLUE:
                return formatFixed1616(_directory.getLongObject(tagType));
            case BmpHeaderDirectory.TAG_INTENT:
                return getRenderingIntentDescription();
            default:
                return super.getDescription(tagType);
        }
    }

    @Nullable
    public String getBitmapTypeDescription()
    {
        BmpHeaderDirectory.BitmapType bitmapType = _directory.getBitmapType();
        return bitmapType == null ? null : bitmapType.toString();
    }

    @Nullable
    public String getCompressionDescription()
    {
        //  0 = None
        //  1 = RLE 8-bit/pixel
        //  2 = RLE 4-bit/pixel
        //  3 = Bit fields (or Huffman 1D if OS22XBITMAPHEADER (size 64))
        //  4 = JPEG (or RLE 24-bit/pixel if OS22XBITMAPHEADER (size 64))
        //  5 = PNG
        // 11 = CMYK
        // 12 = CMYK RLE-8
        // 13 = CMYK RLE-4

        BmpHeaderDirectory.Compression compression = _directory.getCompression();
        if (compression != null) {
            return compression.toString();
        }
        Integer value = _directory.getInteger(BmpHeaderDirectory.TAG_COMPRESSION);
        return value == null ? null : "Illegal value 0x" + Integer.toHexString(value.intValue());
    }

    @Nullable
    public String getRenderingDescription()
    {
        BmpHeaderDirectory.RenderingHalftoningAlgorithm renderingHalftoningAlgorithm = _directory.getRendering();
        return renderingHalftoningAlgorithm == null ? null : renderingHalftoningAlgorithm.toString();
    }

    @Nullable
    public String getColorEncodingDescription()
    {
        BmpHeaderDirectory.ColorEncoding colorEncoding = _directory.getColorEncoding();
        return colorEncoding == null ? null : colorEncoding.toString();
    }

    @Nullable
    public String getColorSpaceTypeDescription()
    {
        BmpHeaderDirectory.ColorSpaceType colorSpaceType = _directory.getColorSpaceType();
        return colorSpaceType == null ? null : colorSpaceType.toString();
    }

    @Nullable
    public String getRenderingIntentDescription()
    {
        BmpHeaderDirectory.RenderingIntent renderingIntent = _directory.getRenderingIntent();
        return renderingIntent == null ? null : renderingIntent.toString();
    }

    @Nullable
    public static String formatHex(@Nullable Integer value, int digits) {
        return value == null ? null : formatHex(value.intValue() & 0xFFFFFFFFL, digits);
    }

    @NotNull
    public static String formatHex(int value, int digits) {
        return formatHex(value & 0xFFFFFFFFL, digits);
    }

    @Nullable
    public static String formatHex(@Nullable Long value, int digits) {
        return value == null ? null : formatHex(value.longValue(), digits);
    }

    @NotNull
    public static String formatHex(long value, int digits) {
        return String.format("0x%0"+ digits + "X", value);
    }

    @Nullable
    public static String formatFixed1616(Integer value) {
        return value == null ? null : formatFixed1616(value.intValue() & 0xFFFFFFFFL);
    }

    @NotNull
    public static String formatFixed1616(int value) {
        return formatFixed1616(value & 0xFFFFFFFFL);
    }

    @Nullable
    public static String formatFixed1616(Long value) {
        return value == null ? null : formatFixed1616(value.longValue());
    }

    @NotNull
    public static String formatFixed1616(long value) {
        Double d = (double) value / 0x10000;
        DecimalFormat format = new DecimalFormat("0.###");
        return format.format(d);
    }
}
