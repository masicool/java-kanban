package server;

public enum ContentTypes {
    JSON("application/json");

    private final String contentType;

    ContentTypes(String contentType) {
        this.contentType = contentType;
    }

    public String getValue() {
        return contentType;
    }
}
