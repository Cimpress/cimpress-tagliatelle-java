package io.cimpress.tagliatelle;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        lowLevelClient = new LowLevelClient("", "http://localhost:8089");
    }

    @Test
    public void postTagHandlesUnauthorizedGracefully() throws UnirestException, ExecutionException, InterruptedException {
        stubFor(post(urlMatching("/v0/tags")).atPriority(5).willReturn(aResponse().withStatus(401).withBody("{}")));
        TagRequest tag = new TagRequest();
        tag.key = "urn:tagspace:tag";
        tag.resourceUri = "http://some.resource.url";
        tag.value = "Some value";
        Future<HttpResponse<TagResponse>> result = lowLevelClient.postTag(tag);
        assertEquals("Expecting Unauthorized status",401, result.get().getStatus() );
    }

    @Test
    public void putTagHandlesUnauthorizedGracefully() throws UnirestException, ExecutionException, InterruptedException {
        stubFor(put(urlMatching("/v0/tags/0")).atPriority(5).willReturn(aResponse().withStatus(401).withBody("{}")));
        TagRequest tag = new TagRequest();
        tag.key = "urn:tagspace:tag";
        tag.resourceUri = "http://some.resource.url";
        tag.value = "Some value";
        Future<HttpResponse<TagResponse>> result = lowLevelClient.putTag("0", tag);
        assertEquals("Expecting Unauthorized status",401, result.get().getStatus() );
    }

    @Test
    public void deleteTagHandlesUnauthorizedGracefully() throws UnirestException, ExecutionException, InterruptedException {
        stubFor(delete(urlMatching("/v0/tags/0")).atPriority(5).willReturn(aResponse().withStatus(401).withBody("{}")));
        Future<HttpResponse<JsonNode>> result = lowLevelClient.deleteTag("0");
        assertEquals("Expecting Unauthorized status",401, result.get().getStatus() );
    }

    @Test
    public void getTagsHandlesUnauthorizedGracefully() throws UnirestException, ExecutionException, InterruptedException {
        stubFor(get(urlMatching("/v0/tags?.*")).atPriority(5).willReturn(aResponse().withStatus(401).withBody("{}")));
        Future<HttpResponse<TagBulkResponse>> result = lowLevelClient.getTags("urn:test", "https://resource.url");
        assertEquals("Expecting Unauthorized status",401, result.get().getStatus() );
    }
}
