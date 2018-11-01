package io.cimpress.tagliatelle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import io.cimpress.tagliatelle.data.TagBulkResponse;
import io.cimpress.tagliatelle.data.TagRequest;
import io.cimpress.tagliatelle.data.TagResponse;
import io.cimpress.tagliatelle.exceptions.ForbiddenException;
import io.cimpress.tagliatelle.exceptions.UnauthorizedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/***
 * The higher level request object.
 * Its supplementary role is to capture more advanced use cases around calling the low level api, e.g:
 * - adding tag vs updating tag has in most use cases little significance
 * - if the user doesn't store tag id, removing a tag requires at least two calls, and that can be abstracted away
 * for most users
 */
public class ClientRequest {

    /**
     * Request object holding all properties of the tag
     */
    private final TagRequest tagRequest = new TagRequest();

    /**
     * Low level client abstraction for invoking commands on the API
     */
    private LowLevelClient lowLevelClient;

    ClientRequest(String accessToken) {
        this.lowLevelClient = new LowLevelClient(accessToken);
    }

    ClientRequest(String accessToken, String urlOverride) {
        this(accessToken);
        if (urlOverride != null) {
            this.lowLevelClient = new LowLevelClient(accessToken, urlOverride);
        }
    }

    /**
     * Fluent command for specifying the resource the tag should refer to
     *
     * @param resourceUri Resource url the tag should be attached to.
     * @return
     */
    public ClientRequest withResource(String resourceUri) {
        this.tagRequest.setResourceUri(resourceUri);
        return this;
    }

    /**
     * Fluent command for specifying the key of the tag
     * @param tagKey Key of the tag is a user provided URN that describes the tag. The fact that that the key is a URN,
     *               gives freedom for the user to model hierarchy and other aspects of their tag
     * @return
     */
    public ClientRequest withKey(String tagKey) {
        this.tagRequest.setKey(tagKey);
        return this;
    }

    /**
     * Optionally it is possible to attach opaque metadata to the tag.
     * @param value
     * @return
     */
    public ClientRequest withStringValue(String value) {
        this.tagRequest.setValueAsString(value);
        return this;
    }


    public ClientRequest withObjectValue(Object value) throws JsonProcessingException {
        this.tagRequest.setValueAsObject(value);
        return this;
    }

    private <T> void handleErrorCondition(HttpResponse<T> response) throws UnauthorizedException, ForbiddenException {
        switch(response.getStatus()) {
            case 401: throw new UnauthorizedException("Your request was not properly authenticated");
            case 403: throw new ForbiddenException("Your don't have access to perform the action");
        }
    }

    public void apply() throws Exception {
        handleOperationTag();
    }

    public void remove() throws Exception {
        handleOperationUntag();
    }

    public TagBulkResponse fetch() throws Exception {
        return handleOperationFetch();
    }

    private TagBulkResponse handleOperationFetch() throws ExecutionException, InterruptedException {
        HttpResponse<TagBulkResponse> bulkResponse = lowLevelClient.getTags(this.tagRequest.getKey(), this.tagRequest.getResourceUri()).get();
        return bulkResponse.getBody();
    }

    private void handleOperationUntag() throws InterruptedException, java.util.concurrent.ExecutionException, UnauthorizedException, ForbiddenException {
        HttpResponse<TagBulkResponse> bulkResponse = lowLevelClient.getTags(this.tagRequest.getKey(), this.tagRequest.getResourceUri()).get();
        handleErrorCondition(bulkResponse);
        TagBulkResponse bulkResponseResults = bulkResponse.getBody();
        List<Future> futures = new ArrayList<>();
        for (TagResponse r : bulkResponseResults.results) {
            futures.add(lowLevelClient.deleteTag(r.id));
        }
        for (Future f : futures) {
            f.get();
        }
    }

    private void handleOperationTag() throws Exception {
        HttpResponse<TagResponse> response = lowLevelClient.postTag(this.tagRequest).get();
        handleErrorCondition(response);
        if (response.getStatus() == 409) {
            HttpResponse<TagBulkResponse> bulkResponse = lowLevelClient.getTags(this.tagRequest.getKey(), this.tagRequest.getResourceUri()).get();
            if (bulkResponse.getBody().total != 1) {
                throw new Exception("Unable to update the tag");
            }
            TagResponse existingTag = bulkResponse.getBody().results.get(0);
            lowLevelClient.putTag(existingTag.id, this.tagRequest);
        }
    }
}
