package org.graylog.integrations.inputs.paloalto;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utils for manually parsing Palo Alto logs from files.
 */
public class PaloAltoUtils {

    private PaloAltoUtils() {
    }

    /**
     * Helper for parsing PAN messages from HEX export. */
    public static void dataParserTest() throws Exception {

        List<String> hexVals = new ArrayList<>();
        String buffer = "";
        for (String textLine : getTextLines()) {
            if (!textLine.equals("")) {
                buffer += textLine;
            } else {
                hexVals.add(buffer);
                buffer = "";
            }
        }

        hexVals = hexVals.stream().map(s -> s.replace(" ", "")).map(h -> {
            byte[] bytes = new byte[0];
            try {
                bytes = Hex.decodeHex(h.toCharArray());
            } catch (DecoderException e) {
                e.printStackTrace();
            }
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "";
        })
                         .filter(s -> s.contains("- - - -"))
                         .filter(s -> s.contains(">1"))
                         .filter(s -> s.contains("<"))
                         .map(s -> s.substring(s.indexOf(">1") - 3, s.length()))
                         .collect(Collectors.toList());


        FileWriter writer = new FileWriter("capture-clean.txt");
        for (String str : hexVals) {
            writer.write(str + "\n");
        }
        writer.close();
    }

    private static List<String> getTextLines() throws Exception {

        String s = new String(Files.readAllBytes(Paths.get("capture")));
        return Arrays.asList(s.replace("\t", "").split("\\n")).stream().map(v -> {
                                                                                String withoutPrefix = v.length() > 7 ? v.substring(7, v.length()) : v;

                                                                                if (withoutPrefix.length() > 32) {
                                                                                    withoutPrefix = withoutPrefix.substring(0, 39);
                                                                                }
                                                                                return withoutPrefix;
                                                                            }
        ).collect(Collectors.toList());
    }
}
