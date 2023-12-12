package net.jonathangiles.tools.sitebuilder.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SiteContentStatus {

    @JsonProperty("publish")
    PUBLISH,

    @JsonProperty("draft")
    DRAFT;
}
