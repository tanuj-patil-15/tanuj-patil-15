import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class UpdateStats {
    public static void main(String[] args) throws Exception {
        String username = "tanujp15";
        
        // Fetch data
        String query = "{\"query\": \"query { matchedUser(username: \\\"" + username + "\\\") { submitStats { acSubmissionNum { difficulty count } } } }\"}";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://leetcode.com/graphql"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();
        String jsonData = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        
        // Parse counts
        int easy = extract(jsonData, "Easy");
        int medium = extract(jsonData, "Medium");
        int hard = extract(jsonData, "Hard");

        // Build stats table
        String statsMarkdown = String.format(
            "| Difficulty | Solved Count |\n" +
            "| :--- | :--- |\n" +
            "| 🟢 **Easy** | %d |\n" +
            "| 🟡 **Medium** | %d |\n" +
            "| 🔴 **Hard** | %d |\n" +
            "| 🔥 **Total** | **%d** |",
            easy, medium, hard, (easy + medium + hard)
        );

        // Read and Update README
        Path path = Paths.get("README.md");
        String content = Files.readString(path);
        
        String startMarker = "";
        String endMarker = "";
        
        int startIdx = content.indexOf(startMarker);
        int endIdx = content.indexOf(endMarker);
        
        if (startIdx != -1 && endIdx != -1) {
            String newContent = content.substring(0, startIdx + startMarker.length())
                              + "\n" + statsMarkdown + "\n"
                              + content.substring(endIdx);
            
            // Update Date
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            newContent = newContent.replace("{{DATE}}", now);
            
            Files.writeString(path, newContent);
            System.out.println("README updated successfully!");
        } else {
            System.out.println("Markers not found in README.md");
        }
    }

    private static int extract(String json, String diff) {
        try {
            String tag = "\"difficulty\":\"" + diff + "\",\"count\":";
            int start = json.indexOf(tag) + tag.length();
            return Integer.parseInt(json.substring(start, json.indexOf("}", start)));
        } catch (Exception e) { return 0; }
    }
}
