package io.cimpress.tagliatelle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import io.cimpress.tagliatelle.data.TagBulkResponse;
import io.cimpress.tagliatelle.data.TagRequest;
import io.cimpress.tagliatelle.data.TagResponse;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Hello world!
 */
public class LowLevelClient {
    private final String TAGLIATELLE_URL = "https://tagliatelle.trdlnk.cimpress.io/";

    private final String accessToken;

    private String tagliatelleUrl;

    public LowLevelClient(String accessToken) {
        this.accessToken = accessToken;
        this.tagliatelleUrl = TAGLIATELLE_URL;

        // Only one time
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public LowLevelClient(String accessToken, String urlOverride) {
        this(accessToken);
        this.tagliatelleUrl = urlOverride;
    }


    public Future<HttpResponse<TagBulkResponse>> getTags(String key, String resourceUri) {
        GetRequest request = Unirest.get(tagliatelleUrl + "/v0/tags")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken);

        if (key != null) {
            request.queryString("resourceUri", resourceUri);
        }
        if (resourceUri != null) {
            request.queryString("key", key);
        }

        return request.asObjectAsync(TagBulkResponse.class);
    }

    public Future<HttpResponse<TagResponse>> postTag(TagRequest request) {
        Future<HttpResponse<TagResponse>> response = Unirest.post(tagliatelleUrl + "/v0/tags")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .asObjectAsync(TagResponse.class);
        return response;
    }

    public Future<HttpResponse<TagResponse>> putTag(String id, TagRequest request) {
        return Unirest.put(tagliatelleUrl + "/v0/tags/{id}")
                .routeParam("id", id)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .asObjectAsync(TagResponse.class);
    }

    public Future<HttpResponse<JsonNode>> deleteTag(String id) {
        return Unirest.delete(tagliatelleUrl + "/v0/tags/{id}")
                .routeParam("id", id)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .asJsonAsync();
    }
}
