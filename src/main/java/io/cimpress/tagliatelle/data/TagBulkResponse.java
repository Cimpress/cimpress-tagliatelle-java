package io.cimpress.tagliatelle.data;

import java.util.List;

public class TagBulkResponse {

    public int count;
    public int total;
    public String offset;
    public List<TagResponse> results;

}
