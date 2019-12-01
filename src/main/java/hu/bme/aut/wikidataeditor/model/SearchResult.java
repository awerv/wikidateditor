package hu.bme.aut.wikidataeditor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchResult {
    private String label;
    private String entityId;
    private String url;
    private String description;
}