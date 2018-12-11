package org.graylog.integrations.notifications;


import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Executes the supplied shell command.
 * Verify that this works in Windows.
 */
public class ShellExecutor {

    public String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

}