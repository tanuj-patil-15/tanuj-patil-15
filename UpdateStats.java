import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.LocalDate;

public class UpdateStats {
    public static void main(String[] args) throws Exception {
        String username = "tanujp15";
        
        // 1. Fetch real data from LeetCode
        String jsonData = fetchFromLeetCode(username);
        
        // 2. Parse the numbers
        int easy = extract(jsonData, "Easy");
        int medium = extract(jsonData, "Medium");
        int hard = extract(jsonData, "Hard");
        
        // 3. Build the table
        String statsMarkdown = String.format(
            "| Difficulty | Solved Count |\n" +
            "| :--- | :--- |\n" +
            "| 🟢 **Easy** | %d |\n" +
            "| 🟡 **Medium** | %d |\n" +
            "| 🔴 **Hard** | %d |\n" +
            "| 🔥 **Total** | **%d** |",
            easy, medium, hard, (easy + medium + hard)
        );

        // 4. Update README
        Path path = Paths.get("README.md");
        String content = Files.readString(path);
        String updated = content.replaceAll("(?s).*?", 
            "\n" + statsMarkdown + "\n");
        
        // Update the date too
        updated = updated.replace("{{DATE}}", LocalDate.now().toString());
        
        Files.writeString(path, updated);
        System.out.println("Profile Updated!");
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
