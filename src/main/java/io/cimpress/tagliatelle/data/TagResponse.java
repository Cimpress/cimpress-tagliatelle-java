package io.cimpress.tagliatelle.data;

import java.util.HashMap;

public class TagResponse extends TagRequest {

    public String id;

    public String createdAt;

    public String createdBy;

    public String modifiedAt;

    public String modifiedBy;

    public HashMap<String, Object> _links;

}
