package net.jonathangiles.tools.sitebuilder.models.input;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface InputFile {

    String getBody();

    List<String> getFrontMatterList(String key);

    default Optional<String> getFrontMatterValue(String key) {
        if (!hasFrontMatter(key)) {
            return Optional.empty();
        } else {
            return Optional.of(getFrontMatterList(key).get(0));
        }
    }

    boolean hasFrontMatter();

    default boolean hasFrontMatter(String key) {
        return hasFrontMatter() && getFrontMatterList(key) != null;
    }

    static InputFile fromPath(Path path) {
        return fromFile(path.toFile());
    }

    static InputFile fromFile(File file) {
        if (file.getName().endsWith(".md")) {
            return fromMarkdownFile(file);
        } else if (file.getName().endsWith(".html")) {
            return fromHtmlFile(file);
        } else if (file.getName().endsWith(".xml")) {
            return fromXmlFile(file);
        } else {
            throw new RuntimeException("Unknown file type: " + file.getName());
        }
    }

    static MarkdownFile fromMarkdownFile(File markdownFile) {
        return MarkdownFile.fromFile(markdownFile);
    }

    static HtmlFile fromHtmlFile(File htmlFile) {
        return HtmlFile.fromFile(htmlFile);
    }

    static XmlFile fromXmlFile(File xmlFile) {
        return XmlFile.fromFile(xmlFile);
    }
}
