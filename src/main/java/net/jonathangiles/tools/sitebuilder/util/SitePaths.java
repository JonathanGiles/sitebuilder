package net.jonathangiles.tools.sitebuilder.util;

import net.jonathangiles.tools.sitebuilder.models.Post;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    /**
     * This method will recreate the input directory structure in the output directory. It is designed with the assumption
     * that each post will create a single index.html file, and therefore assumes that the directory structure has one
     * post per directory underneath the 'posts' directory.
     */
    public static Function<PostPathRequest, PostPath> recreateDirStructure() {
        return postPathRequest -> {
            final Path basePath = postPathRequest.baseOutputPath;
            final Post post = postPathRequest.post;

            Path relativePath = Paths.get(createRelativePath(basePath.getParent(), postPathRequest.pathToPostFile).toString(), post.getSlug());
            Path fullOutputPath = Paths.get(relativePath.getParent().toString(), "index.html");

            return new PostPath(post, relativePath.toString(), fullOutputPath);
        };
    }

    /**
     * This approach will ignore the directory structure of the input files, instead creating directories based on the
     * slugs contained within each post. It can optionally create directories for each year.
     */
    public static Function<PostPathRequest, PostPath> createSlugDirStructure(String prefix, boolean createYearDirs) {
        return postPathRequest -> {
            final Path basePath = postPathRequest.baseOutputPath;
            final Post post = postPathRequest.post;

            String relativePath = prefix + (createYearDirs ? post.getDate().getYear() + "/" : "") + post.getSlug();
            Path fullOutputPath = new File(OUTPUT_DIR, relativePath + "/index.html").toPath();

            return new PostPath(post, relativePath, fullOutputPath);
        };
    }

    public static class PostPathRequest {
        private final Post post;
        private final Path baseOutputPath;
        private final Path pathToPostFile;

        public PostPathRequest(Post post, Path pathToPostFile, Path baseOutputPath) {
            this.post = post;
            this.pathToPostFile = pathToPostFile;
            this.baseOutputPath = baseOutputPath;
        }


    }

    public static class PostPath {
        private final Post post;

        private final String relativePath;

        private final Path fullOutputPath;

        public PostPath(Post post, String relativePath, Path fullOutputPath) {
            this.post = post;
            this.relativePath = relativePath;
            this.fullOutputPath = fullOutputPath;
        }

        public Post getPost() {
            return post;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public Path getFullOutputPath() {
            return fullOutputPath;
        }
    }
}
