package org.graylog.integrations.ipfix;

import java.util.stream.Stream;

/**
 * Hold the Structured Data Types Semantics
 * 0x00	noneOf	The "noneOf" structured data type semantic specifies that none of the elements are actual properties of the Data Record.	[RFC6313]
 * 0x01	exactlyOneOf	The "exactlyOneOf" structured data type semantic specifies that only a single element from the structured data is an actual property of the Data Record. This is equivalent to a logical XOR operation.	[RFC6313]
 * 0x02	oneOrMoreOf	The "oneOrMoreOf" structured data type semantic specifies that one or more elements from the list in the structured data are actual properties of the Data Record. This is equivalent to a logical OR operation.	[RFC6313]
 * 0x03	allOf	The "allOf" structured data type semantic specifies that all of the list elements from the structured data are actual properties of the Data Record.	[RFC6313]
 * 0x04	ordered	The "ordered" structured data type semantic specifies that elements from the list in the structured data are ordered.	[RFC6313]
 * 0x05-0xFE		unassigned
 * 0xFF	undefined	The "undefined" structured data type semantic specifies that the semantic of the list elements is not specified and that, if a semantic exists, then it is up to the Collecting Process to draw its own conclusions. The "undefined" structured data type semantic is the default structured data type semantic.	[RFC6313]
 */
public enum StructuredDataTypesSemantics {

    NONE_OF((short) 0x00),
    EXACTLY_ONE_OF((short) 0x01),
    ONE_OR_MORE_OF((short) 0x02),
    ALL_OF((short) 0x03),
    ORDERED((short) 0x04),
    // all values between are unassigned
    UNDEFINED((short) 0xFF);

    short value;

    StructuredDataTypesSemantics(final short value) {
        this.value = value;
    }

    /**
     * parse a byte value and returns the correct enum or throw IllegalArgumentException if an unassigned value is used
     * @param input the byte value from the semantics field
     * @return the enum representing the semantic
     */
    static StructuredDataTypesSemantics parse(final short input) {
        return Stream.of(StructuredDataTypesSemantics.values())
                .filter(v -> v.value == input)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("illegal semantics value"));
    }

}
