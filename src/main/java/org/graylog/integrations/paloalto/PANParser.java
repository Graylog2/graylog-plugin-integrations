package org.graylog.integrations.paloalto;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PANParser {

    private static final Logger LOG = LoggerFactory.getLogger(PANParser.class);

    private static final Pattern SYSLOG_PARSER = Pattern.compile("<\\d+>1 (.+?) (.+?)$");

    // TODO TESTS

    @Nullable
    public PaloAltoMessageBase parse(@NotNull String raw) {
        String[] parts = raw.split("- - - -");
        if (parts.length != 2) {
            LOG.warn("Cannot parse malformed PAN message: {}", raw);
            return null;
        }

        String syslogHeader = parts[0].trim();
        String panData = parts[1].trim();

        final Matcher matcher = SYSLOG_PARSER.matcher(syslogHeader);

        if (matcher.find()) {
            DateTime timestamp = DateTime.parse(matcher.group(1));
            String source = matcher.group(2);

            ImmutableList<String> fields = ImmutableList.copyOf(Splitter.on(",").split(panData));
            return PaloAltoMessageBase.create(source, timestamp, panData, fields.get(3), fields);
        } else {
            LOG.warn("Cannot parse malformed PAN message: {}", raw);
            return null;
        }
    }


}
