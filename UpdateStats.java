import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class UpdateStats {
    public static void main(String[] args) throws Exception {
        String username = "tanujp15";

        // 1. Fetch data including Calendar stats (Active Days & Streak)
        String query = "{\"query\": \"query { matchedUser(username: \\\"" + username + "\\\") { " +
                       "submitStats { acSubmissionNum { difficulty count } } " +
                       "userCalendar { totalActiveDays streak } } }\"}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://leetcode.com/graphql"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();

        String jsonData = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        // 2. Extract stats
        int easy = extract(jsonData, "Easy");
        int medium = extract(jsonData, "Medium");
        int hard = extract(jsonData, "Hard");
        int activeDays = extractInt(jsonData, "totalActiveDays");
        int streak = extractInt(jsonData, "streak");

        // 3. Create the Markdown Table
        String statsMarkdown = String.format(
            "| Stat | Count |\n" +
            "| :--- | :--- |\n" +
            "| 🟢 **Easy** | %d |\n" +
            "| 🟡 **Medium** | %d |\n" +
            "| 🔴 **Hard** | %d |\n" +
            "| 🔥 **Total Solved** | **%d** |\n" +
            "| 📅 **Active Days** | %d |\n" +
            "| ⚡ **Current Streak** | %d |",
            easy, medium, hard, (easy + medium + hard), activeDays, streak
        );

        // 4. Update README.md
        Path path = Paths.get("README.md");
        String content = Files.readString(path);

        String startMarker = "<!-- LEETCODE_STATS_START -->";
        String endMarker = "<!-- LEETCODE_STATS_END -->";

        int startIdx = content.indexOf(startMarker);
        int endIdx = content.indexOf(endMarker);

        if (startIdx != -1 && endIdx != -1) {
            String newContent = content.substring(0, startIdx + startMarker.length())
                              + "\n" + statsMarkdown + "\n"
                              + content.substring(endIdx);

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            newContent = newContent.replaceAll("\\*Last updated:.*\\*", "*Last updated: " + now + "*");

            Files.writeString(path, newContent);
            System.out.println("README.md updated successfully!");
        } else {
            System.out.println("ERROR: Markers not found in README.md. Make sure the file contains:");
            System.out.println("  " + startMarker);
            System.out.println("  " + endMarker);
        }
    }

    static int extract(String json, String difficulty) {
        String pattern = "\"difficulty\":\"" + difficulty + "\",\"count\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return 0;
        int start = idx + pattern.length();
        int end = json.indexOf("}", start);
        return Integer.parseInt(json.substring(start, end).trim());
    }

    static int extractInt(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return 0;
        int start = idx + pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Integer.parseInt(json.substring(start, end).trim());
    }
}
