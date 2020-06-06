package net.jonathangiles.tools.sitebuilder.models;

import java.util.HashMap;
import java.util.Map;

public class Page {
    private final String name;
    private final Map<String, String> valueMap;

    public Page(String name) {
        this.name = name;
        this.valueMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Page addProperty(String name, String value) {
        this.valueMap.put(name, value);
        return this;
    }

    public Map<String, String> getValues() {
        return valueMap;
    }
}
