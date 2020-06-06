package net.jonathangiles.tools.sitebuilder.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

public class Post {
    @JsonProperty("ID")
    private int id;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Content")
    @JacksonXmlCData
    private String content;

    @JsonProperty("Date")
    private LocalDate date;

    @JsonProperty("Slug")
    private String slug;

    @JsonProperty("Status")
    private PostStatus status;

    @JsonProperty("Categories")
    private String categories;

    @JsonProperty("Tags")
    private String tags;

    @JsonIgnore
    private Path relativePath;

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getDate() {
        return date;
    }

    public PostStatus getStatus() {
        return status;
    }

    public String getSlug() {
        return slug;
    }

    public int getId() {
        return id;
    }

    public String getCategories() {
        return categories;
    }

    public String getTags() {
        return tags;
    }

    public Path getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(final Path relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public String toString() {
        return "Post{" +
                       "title='" + title + '\'' +
                       '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Post post = (Post) o;
        return date.equals(post.date) &&
                       slug.equals(post.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, slug);
    }
}
