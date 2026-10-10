package supabase;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Central configuration for Supabase integration.
 *
 * IMPORTANT SECURITY NOTES:
 * 1. Only the public publishable/anon key is configured here.
 * 2. NEVER use or expose the Supabase service_role key in this desktop application.
 * 3. Access control is enforced through Supabase Row Level Security (RLS) policies.
 * 4. Configuration resolution order:
 *    System Properties -> Environment Variables -> supabase.properties file -> Default Placeholders
 */
public class SupabaseConfig {

    // Default placeholders
    private static final String DEFAULT_URL = "https://YOUR_PROJECT_ID.supabase.co";
    private static final String DEFAULT_ANON_KEY = "YOUR_PUBLIC_PUBLISHABLE_OR_ANON_KEY";

    private static String supabaseUrl;
    private static String supabaseAnonKey;

    static {
        loadConfig();
    }

    /**
     * Loads Supabase configuration from System Properties, Environment Variables, or supabase.properties.
     */
    public static void loadConfig() {
        // 1. Resolve from Java System Properties
        String url = System.getProperty("SUPABASE_URL");
        String key = System.getProperty("SUPABASE_ANON_KEY");
        if (key == null) key = System.getProperty("SUPABASE_PUBLISHABLE_KEY");
        if (key == null) key = System.getProperty("SUPABASE_KEY");

        // 2. Resolve from OS Environment Variables
        if (url == null || url.trim().isEmpty()) {
            url = System.getenv("SUPABASE_URL");
        }
        if (key == null || key.trim().isEmpty()) {
            key = System.getenv("SUPABASE_ANON_KEY");
        }
        if (key == null || key.trim().isEmpty()) {
            key = System.getenv("SUPABASE_PUBLISHABLE_KEY");
        }
        if (key == null || key.trim().isEmpty()) {
            key = System.getenv("SUPABASE_KEY");
        }

        // 3. Resolve from supabase.properties file if present in workspace
        if (url == null || key == null) {
            File propFile = new File("supabase.properties");
            if (!propFile.exists()) {
                propFile = new File("data/supabase.properties");
            }
            if (propFile.exists()) {
                try (FileInputStream fis = new FileInputStream(propFile)) {
                    Properties props = new Properties();
                    props.load(fis);
                    if (url == null || url.trim().isEmpty()) {
                        url = props.getProperty("SUPABASE_URL");
                        if (url == null) url = props.getProperty("supabase.url");
                        if (url == null) url = props.getProperty("supabase_url");
                    }
                    if (key == null || key.trim().isEmpty()) {
                        key = props.getProperty("SUPABASE_ANON_KEY");
                        if (key == null) key = props.getProperty("SUPABASE_PUBLISHABLE_KEY");
                        if (key == null) key = props.getProperty("SUPABASE_KEY");
                        if (key == null) key = props.getProperty("supabase.anon.key");
                        if (key == null) key = props.getProperty("supabase_anon_key");
                        if (key == null) key = props.getProperty("supabase_publishable_key");
                    }
                } catch (Exception e) {
                    System.out.println("SupabaseConfig: Note loading supabase.properties: " + e.getMessage());
                }
            }
        }

        supabaseUrl = normalizeUrl(url != null && !url.trim().isEmpty() ? url.trim() : DEFAULT_URL);
        supabaseAnonKey = (key != null && !key.trim().isEmpty()) ? key.trim() : DEFAULT_ANON_KEY;
    }

    /**
     * Normalizes Supabase project URL. If a user pastes the dashboard browser URL,
     * extracts the project ref and constructs the correct API endpoint.
     */
    public static String normalizeUrl(String rawUrl) {
        if (rawUrl == null) return null;
        String url = rawUrl.trim();

        // Convert dashboard link: https://supabase.com/dashboard/project/<project_id>
        if (url.contains("supabase.com/dashboard/project/")) {
            int idx = url.indexOf("supabase.com/dashboard/project/");
            String projId = url.substring(idx + "supabase.com/dashboard/project/".length());
            if (projId.contains("/")) {
                projId = projId.substring(0, projId.indexOf("/"));
            }
            if (projId.contains("?")) {
                projId = projId.substring(0, projId.indexOf("?"));
            }
            url = "https://" + projId.trim() + ".supabase.co";
        }

        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String getSupabaseUrl() {
        return supabaseUrl;
    }

    public static void setSupabaseUrl(String url) {
        supabaseUrl = normalizeUrl(url);
    }

    public static String getSupabaseAnonKey() {
        return supabaseAnonKey;
    }

    public static void setSupabaseAnonKey(String anonKey) {
        supabaseAnonKey = anonKey;
    }

    /**
     * Validates whether a given URL is a valid Supabase project endpoint.
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        String trimmed = url.trim();
        return (trimmed.startsWith("https://") || trimmed.startsWith("http://"))
                && (trimmed.contains(".supabase.co") || trimmed.contains("supabase.com/dashboard/project/"))
                && !trimmed.contains(" ")
                && !trimmed.contains("YOUR_PROJECT_ID")
                && !trimmed.contains("your-project");
    }

    /**
     * Checks if Supabase has been configured with actual live project credentials instead of placeholders.
     */
    public static boolean isConfigured() {
        return isValidUrl(supabaseUrl)
                && supabaseAnonKey != null && !supabaseAnonKey.trim().isEmpty()
                && !supabaseAnonKey.equals(DEFAULT_ANON_KEY)
                && !supabaseAnonKey.contains("placeholder")
                && !supabaseAnonKey.contains("YOUR_PUBLIC");
    }
}
