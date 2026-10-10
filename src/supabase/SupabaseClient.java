package supabase;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Low-level HTTP client interacting with Supabase Auth and PostgREST APIs.
 *
 * Built strictly with Java 11+ java.net.http.HttpClient to avoid large external dependencies.
 * Uses the public publishable/anon key with Supabase RLS. Never exposes the service_role key.
 */
public class SupabaseClient {

    private final HttpClient httpClient;
    private String currentAccessToken;
    private String currentUserId;
    private String currentUserEmail;
    private boolean emailConfirmed;

    public SupabaseClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Registers a new user via Supabase Auth (/auth/v1/signup).
     *
     * @param email              The student's email (@nssce.ac.in).
     * @param password           The student's password.
     * @param fullName           The student's full name.
     * @param registrationNumber The college student registration number.
     * @return Raw JSON response string from Supabase.
     */
    public String signUp(String email, String password, String fullName, String registrationNumber) throws IOException, InterruptedException {
        if (!SupabaseConfig.isValidUrl(SupabaseConfig.getSupabaseUrl())) {
            throw new IOException("Invalid or placeholder Supabase URL: " + SupabaseConfig.getSupabaseUrl()
                    + ". Please configure a valid SUPABASE_URL in supabase.properties.");
        }
        String url = SupabaseConfig.getSupabaseUrl() + "/auth/v1/signup";
        String payload = "{"
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"password\":\"" + escapeJson(password) + "\","
                + "\"data\":{"
                + "\"full_name\":\"" + escapeJson(fullName) + "\","
                + "\"registration_number\":\"" + escapeJson(registrationNumber) + "\","
                + "\"name\":\"" + escapeJson(fullName) + "\","
                + "\"student_id\":\"" + escapeJson(registrationNumber) + "\""
                + "}"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("apikey", SupabaseConfig.getSupabaseAnonKey())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String body = response.body();

            // Detect if Supabase masked duplicate signup (identities is empty array when email confirmation is active)
            if (body.contains("\"identities\":[]") || body.contains("\"identities\": []")) {
                throw new IOException("An account with this email address already exists. Please login instead.");
            }

            this.currentUserId = extractField(body, "id");
            this.currentUserEmail = email;

            String token = extractField(body, "access_token");
            if (token != null && !token.isEmpty()) {
                this.currentAccessToken = token;
            }

            this.emailConfirmed = isFieldPresentAndNotEmpty(body, "confirmed_at")
                    || isFieldPresentAndNotEmpty(body, "email_confirmed_at");

            if (this.currentUserId == null || this.currentUserId.trim().isEmpty()) {
                throw new IOException("Supabase Auth did not return a valid user UUID. Response: " + body);
            }

            return body;
        } else {
            String errorMsg = extractErrorMessage(response.body());
            if (errorMsg.toLowerCase().contains("already registered") || errorMsg.toLowerCase().contains("user_already_exists")) {
                throw new IOException("An account with this email address already exists. Please login instead.");
            } else if (errorMsg.toLowerCase().contains("weak_password") || errorMsg.toLowerCase().contains("at least 6 characters")) {
                throw new IOException("Password must be at least 6 characters long.");
            }
            throw new IOException("Supabase signup failed (HTTP " + response.statusCode() + "): " + errorMsg);
        }
    }

    /**
     * Authenticates a user via Supabase Auth (/auth/v1/token?grant_type=password).
     *
     * @param email    The student's email.
     * @param password The student's password.
     * @return Raw JSON response containing token and user profile.
     */
    public String signInWithPassword(String email, String password) throws IOException, InterruptedException {
        if (!SupabaseConfig.isValidUrl(SupabaseConfig.getSupabaseUrl())) {
            throw new IOException("Invalid or placeholder Supabase URL: " + SupabaseConfig.getSupabaseUrl()
                    + ". Please configure a valid SUPABASE_URL in supabase.properties.");
        }
        String url = SupabaseConfig.getSupabaseUrl() + "/auth/v1/token?grant_type=password";
        String payload = "{"
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"password\":\"" + escapeJson(password) + "\""
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("apikey", SupabaseConfig.getSupabaseAnonKey())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String body = response.body();
            this.currentAccessToken = extractField(body, "access_token");
            this.currentUserId = extractField(body, "id");
            this.currentUserEmail = email;

            this.emailConfirmed = isFieldPresentAndNotEmpty(body, "confirmed_at")
                    || isFieldPresentAndNotEmpty(body, "email_confirmed_at");

            return body;
        } else {
            String errorMsg = extractErrorMessage(response.body());
            if (errorMsg.toLowerCase().contains("email not confirmed")) {
                throw new IOException("Email not confirmed. Please verify your college email before logging in.");
            } else if (errorMsg.toLowerCase().contains("invalid login credentials") || errorMsg.toLowerCase().contains("invalid_grant")) {
                throw new IOException("Invalid login credentials. Please check your email and password.");
            }
            throw new IOException("Login failed: " + errorMsg);
        }
    }

    /**
     * Refreshes or checks current user info to verify if email confirmation was completed.
     */
    public boolean checkEmailVerificationStatus() {
        if (currentAccessToken == null || currentAccessToken.isEmpty()) {
            return false;
        }

        try {
            String url = SupabaseConfig.getSupabaseUrl() + "/auth/v1/user";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("apikey", SupabaseConfig.getSupabaseAnonKey())
                    .header("Authorization", "Bearer " + currentAccessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                this.emailConfirmed = isFieldPresentAndNotEmpty(body, "confirmed_at")
                        || isFieldPresentAndNotEmpty(body, "email_confirmed_at");
                return this.emailConfirmed;
            }
        } catch (Exception e) {
            // Retain status on network exception
        }

        return this.emailConfirmed;
    }

    /**
     * Executes a REST query against a PostgREST table.
     */
    public String executeRest(String path, String method, String jsonBody) throws IOException, InterruptedException {
        if (!SupabaseConfig.isValidUrl(SupabaseConfig.getSupabaseUrl())) {
            throw new IOException("Invalid or placeholder Supabase URL: " + SupabaseConfig.getSupabaseUrl()
                    + ". Please configure a valid SUPABASE_URL in supabase.properties.");
        }
        String url = SupabaseConfig.getSupabaseUrl() + "/rest/v1/" + path;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("apikey", SupabaseConfig.getSupabaseAnonKey())
                .header("Prefer", "return=representation");

        String authHeader = (currentAccessToken != null && !currentAccessToken.isEmpty())
                ? "Bearer " + currentAccessToken
                : "Bearer " + SupabaseConfig.getSupabaseAnonKey();
        builder.header("Authorization", authHeader);

        if ("POST".equalsIgnoreCase(method)) {
            builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : "{}"));
        } else if ("PATCH".equalsIgnoreCase(method)) {
            builder.method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody != null ? jsonBody : "{}"));
        } else if ("DELETE".equalsIgnoreCase(method)) {
            builder.DELETE();
        } else {
            builder.GET();
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else if (response.statusCode() == 409) {
            String err = response.body();
            if (err.contains("registration_number")) {
                throw new IOException("Registration number is already registered.");
            } else if (err.contains("email")) {
                throw new IOException("This college email is already registered.");
            } else if (err.contains("students_pkey")) {
                // Primary key already exists (e.g., from an automated database trigger)
                return response.body();
            }
            throw new IOException("Duplicate record conflict: " + extractErrorMessage(err));
        } else if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new IOException("Access denied by Supabase Row Level Security policy: " + extractErrorMessage(response.body()));
        } else {
            throw new IOException("Supabase REST request failed (" + response.statusCode() + "): " + extractErrorMessage(response.body()));
        }
    }

    public void signOut() {
        this.currentAccessToken = null;
        this.currentUserId = null;
        this.currentUserEmail = null;
        this.emailConfirmed = false;
    }

    // Getters and session helpers
    public boolean isAuthenticated() {
        return currentAccessToken != null && !currentAccessToken.isEmpty();
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public String getCurrentAccessToken() {
        return currentAccessToken;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    // Robust JSON Parsing Helpers (Pure Java standard library)
    public static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String extractField(String json, String key) {
        if (json == null || key == null) return null;
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx != -1) {
            int colonIdx = json.indexOf(":", idx + pattern.length());
            if (colonIdx != -1) {
                int firstQuote = json.indexOf("\"", colonIdx + 1);
                if (firstQuote != -1) {
                    String between = json.substring(colonIdx + 1, firstQuote).trim();
                    if (between.isEmpty()) {
                        int secondQuote = json.indexOf("\"", firstQuote + 1);
                        if (secondQuote != -1) {
                            return json.substring(firstQuote + 1, secondQuote);
                        }
                    }
                }
            }
        }
        return null;
    }

    public static boolean isFieldPresentAndNotEmpty(String json, String key) {
        if (json == null || key == null) return false;
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return false;

        int colonIdx = json.indexOf(":", idx + pattern.length());
        if (colonIdx == -1) return false;

        int valStart = colonIdx + 1;
        while (valStart < json.length() && Character.isWhitespace(json.charAt(valStart))) {
            valStart++;
        }

        if (valStart < json.length() && json.startsWith("null", valStart)) {
            return false;
        }

        return true;
    }

    public static String extractErrorMessage(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "Unknown error occurred.";
        }
        String msg = extractField(body, "error_description");
        if (msg == null) msg = extractField(body, "message");
        if (msg == null) msg = extractField(body, "msg");
        if (msg == null) msg = extractField(body, "details");
        if (msg == null) msg = extractField(body, "error");
        if (msg != null && !msg.trim().isEmpty()) {
            return msg.trim();
        }
        return body.trim();
    }
}
