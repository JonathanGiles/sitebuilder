package net.jonathangiles.tools.sitebuilder.models;

import net.jonathangiles.tools.sitebuilder.models.input.InputFile;

public class Post extends SiteContent {
    public Post(InputFile inputFile) {
        super(inputFile);

        // by default we have posts use the post template
        setTemplate("post");
    }
}
