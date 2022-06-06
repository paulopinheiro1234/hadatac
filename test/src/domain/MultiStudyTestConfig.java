package src.domain;

public class MultiStudyTestConfig {

    private String tag;
    private String query;
    private String benchmarkFilePath;
    private String validationMethod;
    private String description;

    public MultiStudyTestConfig() {};

    public MultiStudyTestConfig(String tag, String query, String benchmarkFilePath, String validationMethod, String description) {
        this.tag = tag;
        this.query = query;
        this.benchmarkFilePath = benchmarkFilePath;
        this.validationMethod = validationMethod;
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getBenchmarkFilePath() {
        return benchmarkFilePath;
    }

    public void setBenchmarkFilePath(String benchmarkFilePath) {
        this.benchmarkFilePath = benchmarkFilePath;
    }

    public String getValidationMethod() {
        return validationMethod;
    }

    public void setValidationMethod(String validationMethod) {
        this.validationMethod = validationMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
