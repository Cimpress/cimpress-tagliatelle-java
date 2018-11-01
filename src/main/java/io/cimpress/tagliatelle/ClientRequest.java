package io.cimpress.tagliatelle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.cimpress.tagliatelle.data.TagBulkResponse;
import io.cimpress.tagliatelle.data.TagRequest;
import io.cimpress.tagliatelle.data.TagResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Hello world!
 */
public class ClientRequest {

    private final String accessToken;

    private final Operation operation;

    private final TagRequest tagRequest = new TagRequest();

    private final ObjectMapper mapper;
    private final LowLevelClient lowLevelClient;

    ClientRequest(String accessToken, Operation operation) {
        this.accessToken = accessToken;
        this.operation = operation;
        this.mapper = new ObjectMapper();
        this.lowLevelClient = new LowLevelClient(accessToken);
    }

    public ClientRequest withResource(String resourceUri) {
        this.tagRequest.resourceUri = resourceUri;
        return this;
    }

    public ClientRequest withKey(String tagKey) {
        this.tagRequest.key = tagKey;
        return this;
    }

    public ClientRequest withValue(String value) {
        this.tagRequest.value = value;
        return this;
    }

    public ClientRequest withValue(Object value) throws JsonProcessingException {
        this.tagRequest.value = mapper.writeValueAsString(value);
        return this;
    }

    public void execute() throws Exception {
        Future<HttpResponse<TagResponse>> d;
        switch (this.operation) {
            case TAG:
                HttpResponse<TagResponse> response = lowLevelClient.postTag(this.tagRequest).get();
                if (response.getStatus() == 409) {
                    HttpResponse<TagBulkResponse> bulkResponse = lowLevelClient.getTags(this.tagRequest.key, this.tagRequest.resourceUri).get();
                    if (bulkResponse.getBody().total != 1) {
                        throw new Exception("Unable to update the tag");
                    }
                    TagResponse existingTag = bulkResponse.getBody().results.get(0);
                    lowLevelClient.putTag(existingTag.id, this.tagRequest);
                }
                break;
            case UNTAG:
                TagBulkResponse bulkResponse = lowLevelClient.getTags(this.tagRequest.key, this.tagRequest.resourceUri).get().getBody();
                List<Future> futures = new ArrayList<>();
                for (TagResponse r : bulkResponse.results) {
                    futures.add(lowLevelClient.deleteTag(r.id));
                }
                for (Future f : futures) {
                    f.get();
                }
                break;
        }
    }
}
