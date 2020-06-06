package net.jonathangiles.tools.sitebuilder.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PostStatus {

    @JsonProperty("publish")
    PUBLISH,

    @JsonProperty("draft")
    DRAFT;
}
