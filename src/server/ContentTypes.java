package server;

public enum ContentTypes {
    JSON ("application/json"),
    PLAIN ("text/plain"),
    HTML ("text?html");

    private final String contentType;
    ContentTypes(String contentType) {
        this.contentType = contentType;
    }

    public String getValue() {
        return contentType;
    }
}
