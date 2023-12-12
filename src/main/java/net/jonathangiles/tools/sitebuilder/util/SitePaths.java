package net.jonathangiles.tools.sitebuilder.util;

import net.jonathangiles.tools.sitebuilder.models.Post;
import net.jonathangiles.tools.sitebuilder.models.SiteContent;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.jonathangiles.tools.sitebuilder.SiteBuilder.OUTPUT_PATH;
import static net.jonathangiles.tools.sitebuilder.SiteBuilder.OUTPUT_DIR;

public class SitePaths {

    private SitePaths() { }

    public static Path createRelativePath(final Path basePath, final Path file) {
        return new File(OUTPUT_DIR, basePath.relativize(file).toString()).toPath();
    }

    // strip out the 'output/' from the path
    public static String createRelativePath(final Path path) {
        String pathStr = path.toString();
        return pathStr.substring(pathStr.indexOf(OUTPUT_PATH+"/") + OUTPUT_PATH.length() + 1);
    }

//    /**
//     * This method will recreate the input directory structure in the output directory. It is designed with the assumption
//     * that each post will create a single index.html file, and therefore assumes that the directory structure has one
//     * post per directory underneath the 'posts' directory.
//     */
//    public static Function<PostPathRequest, PostPath> recreateDirStructure() {
//        return postPathRequest -> {
//            final Path basePath = postPathRequest.baseOutputPath;
//            final Post post = postPathRequest.post;
//
//            Path relativePath = Paths.get(createRelativePath(basePath.getParent(), postPathRequest.pathToPostFile).toString(), post.getSlug());
//            Path fullOutputPath = Paths.get(relativePath.getParent().toString(), "index.html");
//
//            return new PostPath(post, relativePath.toString(), fullOutputPath);
//        };
//    }

    /**
     * This approach will ignore the directory structure of the input files, instead creating directories based on the
     * slugs contained within each post. It can optionally create directories for each year.
     */
    public static Consumer<SiteContent> createSlugDirStructure(String prefix, boolean createYearDirs) {
        return siteContent -> {
            final String slug = siteContent.getSlug();

            String relativePath = prefix + (createYearDirs ? siteContent.getDate().getYear() + "/" : "");

            // if the slug starts with a forward-slash, this is a directive to not create a directory based on the slug,
            // and to simply append .html to the end of the filename. This is useful for things like the index.html file.
            Path fullOutputPath;
            if (slug.startsWith("/")) {
                fullOutputPath = new File(OUTPUT_DIR, relativePath + "/" + slug + ".html").toPath();
            } else {
                relativePath += slug;
                fullOutputPath = new File(OUTPUT_DIR, relativePath + "/index.html").toPath();
            }

            siteContent.setRelativePath(relativePath);
            siteContent.setFullOutputPath(fullOutputPath);
        };
    }
}
