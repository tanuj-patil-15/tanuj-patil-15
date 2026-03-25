import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class UpdateStats {
    public static void main(String[] args) throws Exception {
        String username = "tanujp15";
        String jsonData = fetchFromLeetCode(username);
        
        int easy = extract(jsonData, "Easy");
        int medium = extract(jsonData, "Medium");
        int hard = extract(jsonData, "Hard");
        int total = easy + medium + hard;

        String statsMarkdown = String.format(
            "| Difficulty | Solved Count |\n" +
            "| :--- | :--- |\n" +
            "| 🟢 **Easy** | %d |\n" +
            "| 🟡 **Medium** | %d |\n" +
            "| 🔴 **Hard** | %d |\n" +
            "| 🔥 **Total** | **%d** |",
            easy, medium, hard, total
        );

        Path path = Paths.get("README.md");
        String content = Files.readString(path);
        
        // Update Stats
        String updated = content.replaceAll("(?s).*?", 
            "\n" + statsMarkdown + "\n");
        
        // Update Date (Fixes the {{DATE}} placeholder)
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        updated = updated.replace("{{DATE}}", now);
        // This handles cases where {{DATE}} was already replaced by a real date
        updated = updated.replaceAll("Last updated: .*", "Last updated: " + now);
        
        Files.writeString(path, updated);
        System.out.println("Success! Updated stats for " + username);
    }

    private static String fetchFromLeetCode(String user) throws Exception {
        String query = "{\"query\": \"query { matchedUser(username: \\\"" + user + "\\\") { submitStats { acSubmissionNum { difficulty count } } } }\"}";
        return HttpClient.newHttpClient().send(
            HttpRequest.newBuilder().uri(URI.create("https://leetcode.com/graphql"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(query)).build(),
            HttpResponse.BodyHandlers.ofString()).body();
    }

    private static int extract(String json, String diff) {
        try {
            String tag = "\"difficulty\":\"" + diff + "\",\"count\":";
            int start = json.indexOf(tag) + tag.length();
            return Integer.parseInt(json.substring(start, json.indexOf("}", start)));
        } catch (Exception e) { return 0; }
    }
}
