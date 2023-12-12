package net.jonathangiles.tools.sitebuilder.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import net.jonathangiles.tools.sitebuilder.models.input.InputFile;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SiteContent {

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
    private SiteContentStatus status;

    @JsonProperty("Template")
    private String template;

    @JsonIgnore
    private String relativePath;

    @JsonIgnore
    private Path fullOutputPath;

    @JsonIgnore
    private final Map<String, String> properties = new HashMap<>();

    public SiteContent() {    }

    public SiteContent(InputFile inputFile) {
        setContent(inputFile.getBody());
        inputFile.getFrontMatterValue("title").ifPresent(this::setTitle);
        inputFile.getFrontMatterValue("date").ifPresent(d -> setDate(LocalDate.parse(d)));
        inputFile.getFrontMatterValue("slug").ifPresent(this::setSlug);
        inputFile.getFrontMatterValue("template").ifPresent(this::setTemplate);
//        this.status = PostStatus.valueOf(mdFile.getFrontMatterValue("status").toUpperCase());
//        this.categories = mdFile.getFrontMatterValue("categories");
//        this.tags = mdFile.getFrontMatterValue("tags");
    }

//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//        properties.put("id", String.valueOf(id));
//    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        properties.put("title", title);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        properties.put("content", content);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
        properties.put("date", date.toString());
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
        properties.put("slug", slug);
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public SiteContentStatus getStatus() {
        return status;
    }

    public void setStatus(SiteContentStatus status) {
        this.status = status;
        properties.put("status", status.toString());
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
        properties.put("relativePath", relativePath);
    }

    public Path getFullOutputPath() {
        return fullOutputPath;
    }

    public void setFullOutputPath(Path fullOutputPath) {
        this.fullOutputPath = fullOutputPath;
        properties.put("fullOutputPath", fullOutputPath.toString());
    }

    // TODO delete this method entirely!
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Post post = (Post) o;
        return getDate().equals(post.getDate()) &&
                getSlug().equals(post.getSlug());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getSlug());
    }
}
