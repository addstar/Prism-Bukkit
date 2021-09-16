package me.botsko.prism;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 2/09/2020.
 */
class Il8NHelperTest {

    @Test
    protected void testGetRawMessage() {
        assertEquals("Prism - originally by Viveleroi; maintained by <author> v<version>",
                Il8nHelper.getRawMessage("about-header"));
    }

}