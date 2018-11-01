package io.cimpress.tagliatelle;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.cimpress.tagliatelle.data.TagBulkResponse;
import io.cimpress.tagliatelle.data.TagRequest;
import io.cimpress.tagliatelle.data.TagResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Unit test for simple Client.
 */
public class LowLevelClientTest
{
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089); // No-args constructor defaults to port 8080

    private LowLevelClient lowLevelClient;

    @Before
    public void initialize() {
        lowLevelClient = new LowLevelClient("Zndlcmd2MjN0MjN0NTUzNjVmZmZld3FkMndlZmMzNDUy", "http://localhost:8089");
    }

    @Test
    public void postTagHandlesUnauthorizedGracefully() throws ExecutionException, InterruptedException {
        stubFor(post(urlMatching("/v0/tags")).atPriority(5).willReturn(aResponse().withStatus(401).withBody("{}")));
        TagRequest tag = new TagRequest();
        Future<HttpResponse<TagResponse>> result = lowLevelClient.postTag(tag);
        assertEquals("Expecting unauthorized status",401, result.get().getStatus() );
    }

    @Test
    public void postReturnsTheBodyOfCreatedTag() throws ExecutionException, InterruptedException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        stubFor(post(urlMatching("/v0/tags")).atPriority(5).willReturn(aResponse().withStatus(200).withBody(
                objectMapper.writeValueAsString(new Object() {
                    public String resourceUri = "http://some.resource";
                    public String key = "urn:my-service:tag";
                    public String value = "bla bla bla";
                    public String createdAt = "2018-11-01T09:27:17+00:00";
                    public String createdBy= "auth0|a767ex734cv376vx346xv34";;
                    public String modifiedAt = "2018-11-01T09:27:18+00:00";
                    public String modifiedBy = "auth0|sdfv454353543534534534";;
                }))));
        TagRequest tag = new TagRequest();
        tag.setKey("urn:tagspace:tag");
        tag.setResourceUri("http://some.resource.url");
        tag.setValueAsString("Some value");
        Future<HttpResponse<TagResponse>> response = lowLevelClient.postTag(tag);
        TagResponse result = response.get().getBody();
        assertEquals("http://some.resource", result.getResourceUri());
        assertEquals("urn:my-service:tag", result.getKey());
        assertEquals("bla bla bla", result.getValueAsString());
        assertEquals("2018-11-01T09:27:17+00:00", result.createdAt);
        assertEquals("auth0|a767ex734cv376vx346xv34",result.createdBy);
        assertEquals("2018-11-01T09:27:18+00:00", result.modifiedAt);
        assertEquals("auth0|sdfv454353543534534534", result.modifiedBy);
    }

    @Test
    public void putTagHandlesUnauthorizedGracefully() throws ExecutionException, InterruptedException {
        stubFor(put(urlMatching("/v0/tags/0")).atPriority(5).willReturn(aResponse().withStatus(401).withBody("{}")));
        TagRequest tag = new TagRequest();
        tag.setKey("urn:tagspace:tag");
        tag.setResourceUri("http://some.resource.url");
        tag.setValueAsString("Some value");
        Future<HttpResponse<TagResponse>> result = lowLevelClient.putTag("0", tag);
        assertEquals("Expecting unauthorized status",401, result.get().getStatus() );
    }

    @Test
    public void deleteTagHandlesUnauthorizedGracefully() throws ExecutionException, InterruptedException {
        stubFor(delete(urlMatching("/v0/tags/0")).atPriority(5).willReturn(aResponse().withStatus(401).withBody("{}")));
        Future<HttpResponse<JsonNode>> result = lowLevelClient.deleteTag("0");
        assertEquals("Expecting unauthorized status",401, result.get().getStatus() );
    }

    @Test
    public void getTagsHandlesUnauthorizedGracefully() throws ExecutionException, InterruptedException {
        stubFor(get(urlMatching("/v0/tags?.*")).atPriority(5).willReturn(aResponse().withStatus(401).withBody("{}")));
        Future<HttpResponse<TagBulkResponse>> result = lowLevelClient.getTags("urn:test", "https://resource.url");
        assertEquals("Expecting unauthorized status",401, result.get().getStatus() );
    }
}
