package io.cimpress.tagliatelle.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class TagRequest {

    private String key;

    private String value;

    private String resourceUri;

    public TagRequest() {
        this.mapper = new ObjectMapper();
    }

    @JsonIgnore
    private final ObjectMapper mapper;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("value")
    public void setValueAsString(String value) {
        this.value = value;
    }

    @JsonIgnore
    public void setValueAsObject(Object value) throws JsonProcessingException {
        this.value = mapper.writeValueAsString(value);
    }

    @JsonProperty("value")
    public String getValueAsString() {
        return value;
    }

    @JsonIgnore
    public Object getValueAsObject() throws IOException {
        return mapper.readValue(value, Object.class);
    }

    @JsonIgnore
    public <T> T getValueAsObject(Class<T> classType) throws IOException {
        return mapper.readValue(value, classType);
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }
}
