package org.graylog.integrations.inputs.paloalto;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaloAltoParser {

    private static final Logger LOG = LoggerFactory.getLogger(PaloAltoParser.class);

    private static final Pattern PANORAMA_SYSLOG_PARSER = Pattern.compile("<\\d+>1 (.+?) (.+?)$");
    private static final Pattern STANDARD_SYSLOG_PARSER = Pattern.compile("<\\d+>(.+?) (.+?) (.+?) (.+?)(\\s.*)");
    public static final String PANORAMA_DELIMITER = "- - - -";

    // TODO TESTS

    @Nullable
    public PaloAltoMessageBase parse(@NotNull String raw) {

        /* The message might arrive in one of the following formats:
         *  1) Panorama format: "<14>1 2018-09-19T11:50:33-05:00 Panorama--1 - - - -
         *  2) Syslog:           <14>Aug 22 11:21:04 hq-lx-net-7.dart.org
         *
         *  Note the ' - - - - ' delimiter for panorama.
         */

        if (raw.contains(PANORAMA_DELIMITER)) {
            LOG.trace("Message is in Panorama format.");

            String[] parts = raw.split(PANORAMA_DELIMITER);
            if (parts.length != 2) {
                LOG.warn("Cannot parse malformed PAN message: {}", raw);
                return null;
            }

            String syslogHeader = parts[0].trim();
            String panData = parts[1].trim();

            final Matcher matcher = PANORAMA_SYSLOG_PARSER.matcher(syslogHeader);

            if (matcher.find()) {
                DateTime timestamp = DateTime.parse(matcher.group(1));
                String source = matcher.group(2);

                ImmutableList<String> fields = ImmutableList.copyOf(Splitter.on(",").split(panData));
                return PaloAltoMessageBase.create(source, timestamp, panData, fields.get(3), fields);
            } else {
                LOG.warn("Cannot parse malformed PAN message: {}", raw);
                return null;
            }
        } else {

            LOG.trace("Message is in Syslog format.");
            final Matcher matcher = STANDARD_SYSLOG_PARSER.matcher(raw);

            if (matcher.matches()) {
                // Attempt to parse date in format: Aug 22 11:21:04
                // TODO This needs work.
                DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM d HH:mm:ss YYYY").withLocale(Locale.US);
                DateTime timestamp  = formatter.parseDateTime(matcher.group(1) + " " + matcher.group(2) + " " +  matcher.group(3) + " " + DateTime.now().getYear());
                String source = matcher.group(4);
                String panData = matcher.group(5);

                ImmutableList<String> fields = ImmutableList.copyOf(Splitter.on(",").split(panData));
                return PaloAltoMessageBase.create(source, timestamp, panData, fields.get(3), fields);
            } else {
                LOG.warn("Cannot parse malformed Syslog PAN message: {}", raw);
                return null;
            }
        }
    }
}