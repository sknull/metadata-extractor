package com.drew.metadata.imaging.eps;

import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.TagDescriptor;

/**
 * @author Payton Garland
 */
public class EpsDescriptor extends TagDescriptor<EpsDirectory>
{

    public EpsDescriptor(@NotNull EpsDirectory directory)
    {
        super(directory);
    }

    @Override
    public String getDescription(int tagType)
    {
        switch (tagType) {
            case EpsDirectory.TAG_IMAGE_WIDTH:
            case EpsDirectory.TAG_IMAGE_HEIGHT:
                return getPixelDescription(tagType);
            case EpsDirectory.TAG_TIFF_PREVIEW_SIZE:
            case EpsDirectory.TAG_TIFF_PREVIEW_OFFSET:
                return getByteSizeDescription(tagType);
            case EpsDirectory.TAG_COLOR_TYPE:
                return getColorTypeDescription();
            default:
                return _directory.getString(tagType);
        }
    }

    public String getPixelDescription(int tagType)
    {
        return _directory.getString(tagType) + " pixels";
    }

    public String getByteSizeDescription(int tagType)
    {
        return _directory.getString(tagType) + " bytes";
    }

    @Nullable
    public String getColorTypeDescription()
    {
        return getIndexedDescription(EpsDirectory.TAG_COLOR_TYPE, 1,
            "Grayscale", "Lab", "RGB", "CMYK");
    }
}
