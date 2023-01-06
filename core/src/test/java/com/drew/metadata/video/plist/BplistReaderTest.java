package com.drew.metadata.video.plist;

import com.drew.testing.TestHelper;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Bob Johnson
 */
public class BplistReaderTest
{
    /**
     * When presented as XML, the RUN_TIME tag will be contained within a <tt>dict</tt> XML element.
     *
     * @throws Exception
     */
    @Test
    public void testRunTimeAsXML() throws Exception
    {
        final byte[] bplist = TestHelper.readBytes("Apple_RunTime.plist");
        final String xml = BplistReader.parse(bplist).toXML();

        assertTrue(xml.contains("<plist version=\"1.0\"><dict>"));
    }
}
