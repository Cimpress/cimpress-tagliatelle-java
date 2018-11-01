package io.cimpress.tagliatelle.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.cimpress.tagliatelle.LowLevelClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

@JsonInclude(JsonInclude.Include.NON_NULL)
class ConfigElement {
    public Boolean testPropertyBoolean;
    public Long testPropertyInt;
    public String testPropertyString;
}

class Config {
    public List<ConfigElement> someProperty;
}

/**
 * Unit test for simple Client.
 */
public class TagRequestTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089); // No-args constructor defaults to port 8080

    private LowLevelClient lowLevelClient;

    @Test
    public void settingValueAsObjectStoresJson() throws IOException {

        TagRequest tagRequest = new TagRequest();
        tagRequest.setValueAsObject(new Object() {
            public List someProperty = Arrays.asList(
                    new Object() {
                        public Boolean testPropertyBoolean = true;
                    },
                    new Object() {
                        public int testPropertyInt = 123;
                    },
                    new Object() {
                        public String testPropertyString = "dummy value";
                    }
            );
        });
        HashMap map = (HashMap) tagRequest.getValueAsObject();
        List list = (List) map.get("someProperty");
        assertEquals(3, list.size());
        assertEquals(true, ((HashMap) list.get(0)).get("testPropertyBoolean"));
        assertEquals(123, ((HashMap) list.get(1)).get("testPropertyInt"));
        assertEquals("dummy value",((HashMap) list.get(2)).get("testPropertyString"));

        String value = tagRequest.getValueAsString();
        assertEquals("{\"someProperty\":[{\"testPropertyBoolean\":true},{\"testPropertyInt\":123},{\"testPropertyString\":\"dummy value\"}]}", value);
    }

    @Test
    public void settingValueAsObjectStoresJsonGeneric() throws IOException {
        ConfigElement c1 = new ConfigElement();
        c1.testPropertyBoolean = true;
        ConfigElement c2 = new ConfigElement();
        c2.testPropertyInt = 123L;
        ConfigElement c3 = new ConfigElement();
        c3.testPropertyString = "dummy value";

        Config c = new Config();
        c.someProperty = Arrays.asList(c1, c2, c3);

        TagRequest tagRequest = new TagRequest();
        tagRequest.setValueAsObject(c);

        HashMap map = (HashMap) tagRequest.getValueAsObject();
        List list = (List) map.get("someProperty");
        assertEquals(3, list.size());
        assertEquals(true, ((HashMap) list.get(0)).get("testPropertyBoolean"));
        assertEquals(123, ((HashMap) list.get(1)).get("testPropertyInt"));
        assertEquals("dummy value",((HashMap) list.get(2)).get("testPropertyString"));

        Config cT = tagRequest.getValueAsObject(Config.class);

        assertEquals(true, cT.someProperty.get(0).testPropertyBoolean);
        assertEquals(   (Long) 123L, cT.someProperty.get(1).testPropertyInt);
        assertEquals("dummy value", cT.someProperty.get(2).testPropertyString);
        String value = tagRequest.getValueAsString();
        assertEquals("{\"someProperty\":[{\"testPropertyBoolean\":true},{\"testPropertyInt\":123},{\"testPropertyString\":\"dummy value\"}]}", value);
    }

}
