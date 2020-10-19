package org.graylog.integrations.notifications.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;


public class SlackMessageTest {

    @Test
    public void test_good_usename() throws IOException {
        SlackMessage message = new SlackMessage("#FF2052", ":turtle:", "https://media.defcon.org/DEF CON 1/DEF CON 1 logo.jpg", "aaa", "#general", false, "this is a happy message", "This is a happy custom message");
        String expected = message.getJsonString();
        List<String> username = getUserNames(expected);
        assertThat(username).isNotEmpty();
        assertThat(username).isNotNull();
    }

    @Test
    public void test_empty_usernames() throws  IOException{
        SlackMessage message = new SlackMessage("#FF2052",":turtle:","https://media.defcon.org/DEF CON 1/DEF CON 1 logo.jpg",null,"aaa",false,"sss","sss");
        String anotherMessage = message.getJsonString();
        List<String> userNames = getUserNames(anotherMessage);
        assertThat(userNames).isEmpty();
        assertThat(userNames).isNotNull();

    }

    private List<String> getUserNames(String expected) throws IOException {
        final byte[] bytes1 = expected.getBytes();
        JsonNode jsonNode1 = new ObjectMapper().readTree(bytes1);
        return jsonNode1.findValuesAsText("username");
    }
}