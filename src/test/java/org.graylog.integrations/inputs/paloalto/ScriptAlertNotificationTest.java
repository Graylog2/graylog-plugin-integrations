package org.graylog.integrations.inputs.paloalto;

import org.graylog.integrations.notifications.ShellExecutor;
import org.junit.Assert;
import org.junit.Test;


public class ScriptAlertNotificationTest {

    @Test
    public void testCommandExecution() {

        Assert.assertEquals("Hello\n", new ShellExecutor().executeCommand("echo Hello"));
    }
}