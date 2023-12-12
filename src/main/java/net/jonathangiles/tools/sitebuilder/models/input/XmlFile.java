package net.jonathangiles.tools.sitebuilder.models.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.jonathangiles.tools.sitebuilder.util.FileUtils;

import java.io.File;
import java.util.*;

/**
 * Old blog posts, when I exported them from WordPress, took the form of XML files. They aren't the prettiest, and
 * eventually I would like to convert them to markdown, but for now I have this class to handle them.
 */
public class XmlFile implements InputFile {
    private final Map<String, List<String>> frontMatter;

    private final String body;

    private XmlFile(Map<String, List<String>> frontMatter, String body) {
        this.frontMatter = frontMatter;
        this.body = body;
    }

    static XmlFile fromFile(File xmlFile) {
        // Use Jackson to read all elements of the XML file, under the <post> root element. All values are considered
        // front matter, except for the <content> element, which is the HTML content of the blog post.
        final Map<String, List<String>> frontMatter = new HashMap<>();
        String xmlContent = FileUtils.readFile(xmlFile.toPath());
        XmlMapper mapper = new XmlMapper();
        String body = "";

        try {
            Map map = (LinkedHashMap)mapper.readValue(xmlContent, Object.class);
            for (Object key : map.keySet()) {
                if (key.equals("Content")) {
                    body = (String)map.get(key);
                } else {
                    frontMatter.put((String)key, List.of((String)map.get(key)));
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new XmlFile(frontMatter, body);
    }

    @Override public String getBody() {
        return body;
    }

    @Override
    public List<String> getFrontMatterList(String key) {
        return frontMatter.getOrDefault(key, null);
    }

    @Override public Optional<String> getFrontMatterValue(String key) {
        // annoyingly, the XML front matter keys have their first letter upper-cased, so we need to change the
        // key to match what the files have in them
        key = key.substring(0, 1).toUpperCase() + key.substring(1);
        return InputFile.super.getFrontMatterValue(key);
    }

    @Override
    public boolean hasFrontMatter() {
        return !frontMatter.isEmpty();
    }
}
