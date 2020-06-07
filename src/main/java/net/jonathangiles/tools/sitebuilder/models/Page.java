package net.jonathangiles.tools.sitebuilder.models;

import java.util.HashMap;
import java.util.Map;

public class Page {
    private final String name;
    private final Map<String, String> propertiesMap;

    public Page(String name) {
        this.name = name;
        this.propertiesMap = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Page addProperty(String name, String value) {
        this.propertiesMap.put(name, value);
        return this;
    }

    public Map<String, String> getProperties() {
        return propertiesMap;
    }
}
