package awsim;

public final class AwsPriceCatalogMetadata {
    private final String catalogId;
    private final String source;
    private final String retrievedAt;
    private final String currency;
    private final boolean illustrative;

    public AwsPriceCatalogMetadata(
            String catalogId,
            String source,
            String retrievedAt,
            String currency,
            boolean illustrative) {
        this.catalogId = catalogId;
        this.source = source;
        this.retrievedAt = retrievedAt;
        this.currency = currency;
        this.illustrative = illustrative;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public String getSource() {
        return source;
    }

    public String getRetrievedAt() {
        return retrievedAt;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isIllustrative() {
        return illustrative;
    }

    public static AwsPriceCatalogMetadata unspecified() {
        return new AwsPriceCatalogMetadata(
                "unspecified",
                "unspecified",
                "unspecified",
                "USD",
                true);
    }
}
