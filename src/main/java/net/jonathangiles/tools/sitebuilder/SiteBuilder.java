package net.jonathangiles.tools.sitebuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jonathangiles.tools.sitebuilder.models.PostStatus;
import net.jonathangiles.tools.sitebuilder.models.Page;
import net.jonathangiles.tools.sitebuilder.models.Post;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Reads in all index.xml files and creates a static index.html file from it and the header and footer files.
 */
public abstract class SiteBuilder {
    private static final String OUTPUT_PATH = "output";
    private static final File OUTPUT_DIR = new File(OUTPUT_PATH);

    private final ClassLoader loader;

    private Map<String, String> templates;

    private SortedSet<Post> allPosts;

    // map of slug -> post
    private Map<String, Post> allPostsMap;

    // A map of page (file) name to Page, for all the other metadata
    private Map<String, Page> allPagesMap;

    protected SiteBuilder() {
        loader = Thread.currentThread().getContextClassLoader();
    }

    public void run() {
        // ------------------------------------------------------------------------
        // Templates
        // ------------------------------------------------------------------------

        // read in the static template files as strings
        templates = new HashMap<>();
        loadTemplates();

        // with all the templates in memory, update any ${include ...} directives now in all
        // template files, so that all templates are complete and do not have any 'include' directives.
        processIncludesDirectives();

        // ------------------------------------------------------------------------
        // Posts
        // ------------------------------------------------------------------------

        allPosts = new TreeSet<>(
                Comparator.comparing(Post::getDate).reversed()
                .thenComparing(Post::getSlug));
        allPostsMap = new HashMap<>();

        // If there are any posts in the <output>/posts directory,
        // process each index.xml file separately and convert it into an index.html file.
        processPosts();

        // ------------------------------------------------------------------------
        // Pages
        // ------------------------------------------------------------------------

        allPagesMap = new HashMap<>();

        // allow sub-types of the SiteBuilder to register custom pages.
        registerPages();

        // iterate over all pages in www/pages before copying them into the appropriate location
        // in the output directory
        processPages();

        // ------------------------------------------------------------------------
        // Static Content
        // ------------------------------------------------------------------------

        // copy all static resources into the appropriate locations under the output dir
        processStaticResources();
    }

    // --------------------------------------------------------------------------
    // Protected API (for subclasses to use)
    // --------------------------------------------------------------------------

    protected void registerPages() {
        // no-op, but available for subclasses to override
    }

    protected void registerPage(final Page page) {
        allPagesMap.put(page.getName(), page);
    }

    protected Set<Post> getAllPosts() {
        return Collections.unmodifiableSortedSet(allPosts);
    }

    protected Map<String, Post> getAllPostsMap() {
        return Collections.unmodifiableMap(allPostsMap);
    }

    // --------------------------------------------------------------------------
    // Post utilities
    // --------------------------------------------------------------------------

    private void processPosts() {
        try {
            final Path postsPath = Paths.get(loader.getResource("www/posts").getPath());
            //Files.walk(new File(WORDPRESS_OUTPUT_DIR, "posts").toPath())
            Files.walk(postsPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().contains("index.xml"))
                    .forEach(indexXmlFile -> processPost(postsPath, indexXmlFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processPost(final Path basePath, final Path indexXmlFile) {
        try {
            final ObjectMapper xmlMapper = new XmlMapper()
                 .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                 .registerModule(new JavaTimeModule());
            final Post post = xmlMapper.readValue(indexXmlFile.toFile(), Post.class);

            if (post.getStatus() == PostStatus.DRAFT) {
                return;
            }

            final Path relativePath = createRelativePath(basePath.getParent(), indexXmlFile);
            post.setRelativePath(relativePath);

            System.out.println("Processing post: " + post.getSlug());

            if (allPostsMap.containsKey(post.getSlug())) {
                System.err.println("Duplicate slug found '" + post.getSlug() + "' - aborting");
                System.exit(-1);
            }

            allPosts.add(post);
            allPostsMap.put(post.getSlug(), post);

            String html = templates.get("post");

            for (Map.Entry<String, String> property : getPostProperties(post).entrySet()) {
                html = fillTemplate(html, property.getKey(), property.getValue());
            }

            relativePath.toFile().getParentFile().mkdirs();
            writeToFile(Paths.get(relativePath.getParent().toString(), "index.html"), html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Map<String, String> getPostProperties(Post post) {
        Map<String, String> properties = new HashMap<>();

        properties.put("title", post.getTitle());
        properties.put("content", post.getContent());
        properties.put("date", post.getDate().toString());
        properties.put("path", createRelativePath(post.getRelativePath().getParent()));

        return properties;
    }

    // --------------------------------------------------------------------------
    // Page utilities
    // --------------------------------------------------------------------------

    private void processPages() {
        try {
            final Path pagesPath = Paths.get(loader.getResource("www/pages").getPath());
            Files.walk(pagesPath)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    final Path path = createRelativePath(pagesPath, file);
                    System.out.println("Processing page: " + path);

                    String html = processIncludesDirectives(readFile(file));

                    // check if there is a Page for this page
                    final String filename = path.getFileName().toString();
                    final String pageName = filename.substring(0, filename.lastIndexOf("."));
                    final Page page = allPagesMap.get(pageName);

                    for (final Map.Entry<String, String> entry : getPageProperties(page, path).entrySet()) {
                        html = fillTemplate(html, entry.getKey(), entry.getValue());
                    }

                    path.toFile().getParentFile().mkdirs();
                    writeToFile(path, html);
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Map<String, String> getPageProperties(Page page, Path pagePath) {
        Map<String, String> properties = new HashMap<>();

        if (page != null) {
            properties.putAll(page.getProperties());
        }

        properties.put("path", createRelativePath(pagePath));

        return properties;
    }

    private void processStaticResources() {
        try {
            final Path staticPath = Paths.get(loader.getResource("www/static").getPath());
            Files.walk(staticPath)
                .filter(Files::isRegularFile)
                .forEach(file -> copyFile(staticPath, file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------------------
    // Templating
    // --------------------------------------------------------------------------

    private void loadTemplates() {
        final Path templatesPath = Paths.get(loader.getResource("www/templates").getPath());

        try {
            Files.walk(templatesPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        final String filename = file.getFileName().toString();
                        System.out.println("Reading template: " + filename);
                        templates.put(filename.substring(0, filename.lastIndexOf(".")), readFile(file));
                    });
        } catch (IOException e) {
            e.printStackTrace();
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
                e.printStackTrace();
            } else {
                System.err.println("Could not do regex on field '" + field + "' on post '" + post.getTitle() + "' with data '" + data + "'");
            }
            return "";
        }
    }

    // --------------------------------------------------------------------------
    // File utilities
    // --------------------------------------------------------------------------

    private static void writeToFile(final Path file, final String content) {
        try {
            Files.write(file, content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path createRelativePath(final Path basePath, final Path file) {
        return new File(OUTPUT_DIR, basePath.relativize(file).toString()).toPath();
    }

    // strip out the 'output/' from the path
    private static String createRelativePath(final Path path) {
        String pathStr = path.toString();
        return pathStr.substring(pathStr.indexOf(OUTPUT_PATH+"/") + OUTPUT_PATH.length() + 1);
    }

    private static void copyFile(final Path basePath, final Path file) {
        try {
            final Path newPath = createRelativePath(basePath, file);
            System.out.println("Copying static file: " + newPath);
            newPath.toFile().mkdirs();
            Files.copy(file, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFile(final Path file) {
        final StringBuilder sb = new StringBuilder();
        try {
            Files.lines(file, StandardCharsets.UTF_8)
                    .forEach(line -> sb.append(line).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // --------------------------------------------------------------------------
    // Misc utilities
    // --------------------------------------------------------------------------

    private static String buildRelative(final int count) {
        String s = "";
        for (int i = 0; i < count; i++) {
            s += "../";
        }
        return s;
    }
}
