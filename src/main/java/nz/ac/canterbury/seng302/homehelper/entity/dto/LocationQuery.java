package nz.ac.canterbury.seng302.homehelper.entity.dto;

import java.util.List;

public class LocationQuery {

    private String query;
    private final String viewbox;
    private List<List<String>> suggestions;

    public LocationQuery(String query, String viewbox, List<List<String>> suggestions) {
        this.query = query;
        this.viewbox = viewbox;
        this.suggestions = suggestions;
    }

    public String getViewbox() {
        return viewbox;
    }

    public List<List<String>> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<List<String>> suggestions) {
        this.suggestions = suggestions;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
