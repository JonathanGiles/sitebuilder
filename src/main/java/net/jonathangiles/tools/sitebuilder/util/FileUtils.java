package net.jonathangiles.tools.sitebuilder.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static net.jonathangiles.tools.sitebuilder.util.SitePaths.createRelativePath;

public class FileUtils {
    private FileUtils() { }

    public static Path getPath(String path, ClassLoader loader) {
        return new File(loader.getResource(path).getFile()).toPath();
    }

    public static void writeToFile(final Path file, final String content) {
        try {
            Files.write(file, content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(final Path basePath, final Path file) {
        try {
            final Path newPath = createRelativePath(basePath, file);
            System.out.println("Copying static file: " + newPath);
            newPath.toFile().mkdirs();
            Files.copy(file, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(final Path file) {
        final StringBuilder sb = new StringBuilder();
        try {
            Files.lines(file, StandardCharsets.UTF_8)
                    .forEach(line -> sb.append(line).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
