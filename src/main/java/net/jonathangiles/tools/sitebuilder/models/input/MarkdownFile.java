package net.jonathangiles.tools.sitebuilder.models.input;

import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MarkdownFile implements InputFile {
    private static final Parser PARSER = Parser.builder().extensions(List.of(YamlFrontMatterExtension.create())).build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

    private final Map<String, List<String>> frontMatter;

    private final String content;

    private MarkdownFile(Map<String, List<String>> frontMatter, String content) {
        this.frontMatter = frontMatter;
        this.content = content;

        // for markdown pages, we set the template to 'page', if one is not set, so that they look as expected
        if (!frontMatter.containsKey("template")) {
            frontMatter.put("template", List.of("page"));
        }
    }

    static MarkdownFile fromFile(File markdownFile) {
        Node document = null;
        try {
            document = PARSER.parseReader(new FileReader(markdownFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        YamlFrontMatterVisitor frontMatter = new YamlFrontMatterVisitor();
        document.accept(frontMatter);
        return new MarkdownFile(frontMatter.getData(), HTML_RENDERER.render(document));
    }

    @Override public String getBody() {
        return content;
    }

    @Override
    public List<String> getFrontMatterList(String key) {
        return frontMatter.getOrDefault(key, null);
    }

    @Override public boolean hasFrontMatter() {
        return !frontMatter.isEmpty();
    }
}
