package io.cimpress.tagliatelle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import io.cimpress.tagliatelle.data.TagBulkResponse;
import io.cimpress.tagliatelle.data.TagRequest;
import io.cimpress.tagliatelle.data.TagResponse;
import io.cimpress.tagliatelle.exceptions.ForbiddenException;
import io.cimpress.tagliatelle.exceptions.UnauthorizedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for the main Client class.
 * Covers basing use cases.
 */
public class ClientTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089); // No-args constructor defaults to port 8080

    private Client client;

    @Before
    public void initialize() {
        client = new Client("Zndlcmd2MjN0MjN0NTUzNjVmZmZld3FkMndlZmMzNDUy", "http://localhost:8089");
    }

    @Test(expected = UnauthorizedException.class)
    public void unauthenticatedRequestThrowsException() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        stubFor(post(urlMatching("/v0/tags")).atPriority(5).willReturn(aResponse().withStatus(401).withBody("{}")));
        client.tag().withKey("urn:tagspace:tag").withResource("http://some.resource.url").withStringValue("some value").apply();
    }

    @Test(expected = ForbiddenException.class)
    public void unauthorizedRequestThrowsException() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        stubFor(post(urlMatching("/v0/tags")).atPriority(5).willReturn(aResponse().withStatus(403).withBody("{}")));
        client.tag().withKey("urn:tagspace:tag").withResource("http://some.resource.url").withStringValue("some value").apply();
    }

    @Test
    public void tagPerformsAllTheLowLevelCalls() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        stubFor(post(urlMatching("/v0/tags")).atPriority(5).willReturn(aResponse().withStatus(200).withBody(
                objectMapper.writeValueAsString(new Object() {
                    public String resourceUri = "http://some.resource";
                    public String key = "urn:my-service:tag";
                    public String value = "bla bla bla";
                    public String createdAt = "2018-11-01T09:27:17+00:00";
                    public String createdBy = "auth0|a767ex734cv376vx346xv34";
                    ;
                    public String modifiedAt = "2018-11-01T09:27:18+00:00";
                    public String modifiedBy = "auth0|sdfv454353543534534534";
                    ;
                }))));

        client.tag().withKey("urn:tagspace:tag").withResource("http://some.resource.url").withStringValue("some value").apply();

        verify(postRequestedFor(urlMatching("/v0/tags"))
                .withRequestBody(containing(objectMapper.writeValueAsString(new Object() {
                    public String key = "urn:tagspace:tag";
                    public String resourceUri = "http://some.resource.url";
                    public String value = "some value";
                }))).withHeader("Content-Type", matching("application/json")));
    }

    @Test
    public void untagPerformsAllTheLowLevelCalls() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String getResponseBiody = objectMapper.writeValueAsString(new Object() {
              public int total = 1;
              public List results = Arrays.asList(
                      new Object() {
                          public String resourceUri = "http://some.resource";
                          public String key = "urn:my-service:tag";
                          public String id = "abrakadabra1234";
                      });
          });

        stubFor(get(urlPathEqualTo("/v0/tags"))
                .withQueryParam("key", equalTo("urn:tagspace:tag"))
                .withQueryParam("resourceUri", equalTo("http://some.resource.url"))
                .atPriority(5).willReturn(aResponse().withStatus(200).withBody(getResponseBiody)));

        stubFor(delete(urlMatching("/v0/tags/abrakadabra1234")).atPriority(5).willReturn(aResponse().withStatus(200).withBody("{}")));

        client.tag().withKey("urn:tagspace:tag").withResource("http://some.resource.url").withStringValue("some value").remove();

        verify(getRequestedFor(urlPathEqualTo("/v0/tags"))
                .withQueryParam("key", equalTo("urn:tagspace:tag"))
                .withQueryParam("resourceUri", equalTo("http://some.resource.url")));

        verify(deleteRequestedFor(urlMatching("/v0/tags/abrakadabra1234")));
    }
}
