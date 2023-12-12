package net.jonathangiles.tools.sitebuilder;

import net.jonathangiles.tools.sitebuilder.models.*;

import static net.jonathangiles.tools.sitebuilder.util.FileUtils.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.jonathangiles.tools.sitebuilder.models.input.InputFile;
import net.jonathangiles.tools.sitebuilder.util.SitePaths;

/**
 * Reads in all index.xml files and creates a static index.html file from it and the header and footer files.
 */
public abstract class SiteBuilder {

    private enum ContentType { PAGE, POST }

    public static final String OUTPUT_PATH = "target/output";
    public static final File OUTPUT_DIR = new File(OUTPUT_PATH);

    private final ClassLoader loader;

    private Map<String, String> templates = new HashMap<>();

    // map of slug -> content
    private final Map<String, SiteContent> allContentMap = new HashMap<>();

    private final Consumer<SiteContent> postPathFunction = SitePaths.createSlugDirStructure("posts/", true);
    private final Consumer<SiteContent> pagePathFunction = SitePaths.createSlugDirStructure("", false);

    protected SiteBuilder() {
        loader = Thread.currentThread().getContextClassLoader();
    }

    public void init() {
        // ------------------------------------------------------------------------
        // Templates
        // ------------------------------------------------------------------------

        // read in the static template files as strings
        loadTemplates();

        // with all the templates in memory, update any ${include ...} directives now in all
        // template files, so that all templates are complete and do not have any 'include' directives.
        processIncludesDirectives();

        // ------------------------------------------------------------------------
        // Content Discovery
        // ------------------------------------------------------------------------

        registerContent();
    }

    public void run() {
        processContent();

        // copy all static resources into the appropriate locations under the output dir
        processStaticResources();
    }

    private void registerContent() {
        registerContent(getPath("www/pages", loader), ContentType.PAGE);
        registerContent(getPath("www/posts", loader), ContentType.POST);
    }

    private void registerContent(final Path rootPath, final ContentType type) {
        try (Stream<Path> files = Files.walk(rootPath)) {
            // we only process pages that have front matter
            files.filter(Files::isRegularFile)
                .filter(path -> {
                    // we process any file that ends with .xml, .html, or .md, but only if they have
                    // front matter that we can process
                    final String n = path.getFileName().toString();
                    return n.endsWith(".html") || n.endsWith(".md") || n.endsWith(".xml");
                }).map(InputFile::fromPath)
                .filter(InputFile::hasFrontMatter)
                .forEach(inputFile -> {
                    switch (type) {
                        case PAGE:
                            registerContent(new Page(inputFile));
                            break;
                        case POST:
                            registerContent(new Post(inputFile));
                            break;
                    }
                });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void registerContent(final SiteContent content) {
        if (content instanceof Page) {
            pagePathFunction.accept(content);
        } else if (content instanceof Post) {
            postPathFunction.accept(content);
        }

        if (allContentMap.containsKey(content.getSlug())) {
            System.err.println("Duplicate slug found '" + content.getSlug() + "' - aborting");
            System.exit(-1);
        }
        allContentMap.put(content.getSlug(), content);
    }

    public Set<Post> getAllPosts() {
        return allContentMap.values().stream()
                .filter(c -> c instanceof Post)
                .map(c -> (Post)c)
                .sorted(Comparator.comparing(Post::getDate).reversed().thenComparing(Post::getSlug))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // --------------------------------------------------------------------------
    // Page utilities
    // --------------------------------------------------------------------------

    private void processStaticResources() {
        final Path staticPath = getPath("www/static", loader);
        try (Stream<Path> files = Files.walk(staticPath)) {
            files.filter(Files::isRegularFile)
                .forEach(file -> copyFile(staticPath, file));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --------------------------------------------------------------------------
    // Templating
    // --------------------------------------------------------------------------

    private void processContent() {
        allContentMap.values().stream()
            .filter(c -> c.getStatus() != SiteContentStatus.DRAFT)
            .forEach(content -> {
                System.out.println("Processing: " + content.getSlug());
                processContent(content);
            });
    }

    private void processContent(SiteContent siteContent) {
        final String template = siteContent.getTemplate();

        String html = processIncludesDirectives(templates.getOrDefault(template, siteContent.getContent()));

        for (Map.Entry<String, String> property : siteContent.getProperties().entrySet()) {
            html = fillTemplate(html, property.getKey(), property.getValue());
        }

        Path outputPath = siteContent.getFullOutputPath();
        outputPath.getParent().toFile().mkdirs();
        writeToFile(outputPath, html);
    }

    private void loadTemplates() {
        final Path templatesPath = getPath("www/templates", loader);

        try (Stream<Path> files = Files.walk(templatesPath)) {
            files.filter(Files::isRegularFile)
                .forEach(file -> {
                    final String filename = file.getFileName().toString();
                    System.out.println("Reading template: " + filename);
                    templates.put(filename.substring(0, filename.lastIndexOf(".")), readFile(file));
                });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void processIncludesDirectives() {
        templates.replaceAll((k, v) -> processIncludesDirectives(v));
    }

    private String processIncludesDirectives(String html) {
        for (final Map.Entry<String, String> template : templates.entrySet()) {
            html = fillTemplate(html, "include " + template.getKey(), template.getValue());
        }

        return html;
    }

    private static String fillTemplate(final String html, final String field, final String data) {
        return fillTemplate(html, field, data, null);
    }

    private static String fillTemplate(final String html, final String field, String data, final Post post) {
        try {
            // we have to escape some characters in the data
            data = data.replace("$", "\\$");

            return html.replaceAll(Pattern.quote("${" + field + "}"), data);
        } catch (IndexOutOfBoundsException e) {
            if (post == null) {
                throw new RuntimeException(e);
            } else {
                System.err.println("Could not do regex on field '" + field + "' on post '" + post.getTitle() + "' with data '" + data + "'");
            }
            return "";
        }
    }
}
