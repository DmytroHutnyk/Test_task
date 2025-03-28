package io.dmytrohutnyk;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    List<Document> documents = new ArrayList<>();
    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if(document == null){
            throw new IllegalStateException("Null is not allowed");
        }

        if(document.id == null){
            document.setId(UUID.randomUUID().toString());

        }else {
            documents.removeIf(element -> element.getId().equals(document.getId()));
        }
        documents.add(document);

        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if(request == null){
            return Collections.emptyList();
        }

        List<Document> result;
        result = documents
                .stream()
                .filter(document -> matchesTitlePrefixes(document, request))
                .filter(document -> containsContents(document, request))
                .filter(document -> containsAuthors(document, request))
                .filter(document -> createdFrom(document, request))
                .filter(document -> createdTo(document, request))
                .collect(Collectors.toCollection(ArrayList::new));

        return result;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if(id == null){
            return Optional.empty();
        }

        return documents.stream().filter(element -> element.getId().trim().equals(id.trim())).findFirst();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }

    private boolean matchesTitlePrefixes(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() == null || request.getTitlePrefixes().isEmpty())
            return true;

        return request.getTitlePrefixes()
                .stream()
                .map(String::toLowerCase)
                .anyMatch(prefix -> document.getTitle().toLowerCase().startsWith(prefix));
    }

    private boolean containsContents(Document document, SearchRequest request){
        if (request.getContainsContents() == null || request.getContainsContents().isEmpty())
            return true;

        return request.getContainsContents()
                .stream()
                .map(String::toLowerCase)
                .anyMatch(content -> document.getContent().toLowerCase().contains(content));
    }

    private boolean containsAuthors(Document document, SearchRequest request){
        if (request.getAuthorIds() == null || request.getAuthorIds().isEmpty())
            return true;

        return request.getAuthorIds()
                .stream()
                .anyMatch(id -> document.getAuthor().getId().equals(id));
    }

    private boolean createdFrom (Document document, SearchRequest request){
        if (request.getCreatedFrom() == null)
            return true;

        return document.getCreated().compareTo(request.getCreatedFrom()) > -1;
    }

    private boolean createdTo (Document document, SearchRequest request){
        if (request.getCreatedTo() == null)
            return true;

        return document.getCreated().compareTo(request.getCreatedTo()) < 1;
    }
}