package net.jonathangiles.tools.sitebuilder.models.input;

import net.jonathangiles.tools.sitebuilder.util.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlFile implements InputFile {
    private final Map<String, List<String>> frontMatter;

    private final String content;

    private HtmlFile(Map<String, List<String>> frontMatter, String content) {
        this.frontMatter = frontMatter;
        this.content = content;
    }

    static HtmlFile fromFile(File htmlFile) {
        // read the contents of the html file into a string, and parse the front matter into a map
        String html = FileUtils.readFile(htmlFile.toPath());
        final Map<String, List<String>> frontMatter = readFrontMatter(html);

        // strip the front matter from just the top of the html content
        html = html.replaceFirst("<!--.*?-->", "");

        return new HtmlFile(frontMatter, html);
    }

    @Override public String getBody() {
        return content;
    }

    @Override
    public List<String> getFrontMatterList(String key) {
        return frontMatter.get(key);
    }

    @Override
    public boolean hasFrontMatter() {
        return !frontMatter.isEmpty();
    }

    // reads the front matter from an HTML comment
    private static Map<String, List<String>> readFrontMatter(final String html) {
        final Map<String, List<String>> frontMatter = new HashMap<>();

        final String[] lines = html.split("\n");
        if (lines.length > 0 && lines[0].startsWith("<!--")) {
            int i = 1;
            while (i < lines.length && !lines[i].startsWith("-->")) {
                final String line = lines[i];
                final String[] split = line.split(":");
                if (split.length == 2) {
                    final String key = split[0].trim();
                    final String value = split[1].trim();
                    frontMatter.put(key, List.of(value));
                }
                i++;
            }
        }

        return frontMatter;
    }
}
