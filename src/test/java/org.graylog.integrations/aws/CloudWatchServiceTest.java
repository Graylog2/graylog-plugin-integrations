package org.graylog.integrations.aws;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CloudWatchServiceTest {

    private CloudWatchService cloudWatchService;

    @Before
    public void setUp() {

        cloudWatchService = new CloudWatchService();
    }

    @Test
    public void testLogGroupNames() {

        List<String> logGroups = cloudWatchService.fakeLogGroups();

        boolean foundLogGroupName = false;
        for (String logGroup : logGroups) {
            if (logGroup.equals("test-group1")) {
                foundLogGroupName = true;
            }
        }
        assertTrue(foundLogGroupName);
        assertEquals(2, logGroups.size());
    }
}