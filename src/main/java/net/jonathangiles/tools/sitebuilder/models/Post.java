package net.jonathangiles.tools.sitebuilder.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;

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
    private String relativePath;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
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
