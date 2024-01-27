package bash.reactioner.bilang.data;

public class LanguageData {
    private String displayName;
    private String value;
    private String signature;

    public LanguageData(String displayName, String value, String signature) {
        this.displayName = displayName;
        this.value = value;
        this.signature = signature;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }
}
