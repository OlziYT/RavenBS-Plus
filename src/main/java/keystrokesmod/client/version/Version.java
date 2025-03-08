package keystrokesmod.client.version;

public class Version {
    private static final String VERSION = "@VERSION@";
    private static final String CLIENT_NAME = "raven bs++";

    public static String getVersion() {
        return "v" + VERSION;
    }

    public static String getClientName() {
        return CLIENT_NAME;
    }

    public static String getClientNameForDisplay() {
        return CLIENT_NAME;
    }

    public static String getVersionForDisplay() {
        return getVersion();
    }
}
