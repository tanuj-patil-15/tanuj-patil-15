import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UpdateStats {
    public static void main(String[] args) throws Exception {
        String username = "tanujp15"; // Your LeetCode username
        String jsonData = fetchLeetCodeData(username);
        
        // Simple manual parsing of the JSON response to extract counts
        int easy = extractCount(jsonData, "Easy");
        int medium = extractCount(jsonData, "Medium");
        int hard = extractCount(jsonData, "Hard");
        int total = easy + medium + hard;

        String statsMarkdown = String.format(
            "| Difficulty | Solved Count |\n" +
            "| :--- | :--- |\n" +
            "| 🟢 **Easy** | %d |\n" +
            "| 🟡 **Medium** | %d |\n" +
            "| 🔴 **Hard** | %d |\n" +
            "| 🏆 **Total** | **%d** |", 
            easy, medium, hard, total
        );

        Path path = Paths.get("README.md");
        String content = Files.readString(path);
        
        String startMarker = "";
        String endMarker = "";
        
        String newContent = content.replaceAll(
            startMarker + "(?s:.*)" + endMarker,
            startMarker + "\n" + statsMarkdown + "\n" + endMarker
        );
        
        Files.writeString(path, newContent);
        System.out.println("Successfully updated README with real stats for " + username);
    }

    private static String fetchLeetCodeData(String username) throws Exception {
        String query = "{\"query\": \"query userProblemsSolved($username: String!) { allQuestionsCount { difficulty count } matchedUser(username: $username) { submitStats { acSubmissionNum { difficulty count } } } }\", \"variables\": {\"username\": \"" + username + "\"}}";
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://leetcode.com/graphql"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static int extractCount(String json, String difficulty) {
        try {
            // A simple way to find the count for a specific difficulty without a heavy JSON library
            String searchLabel = "\"difficulty\":\"" + difficulty + "\",\"count\":";
            int startIndex = json.indexOf(searchLabel) + searchLabel.length();
            int endIndex = json.indexOf("}", startIndex);
            return Integer.parseInt(json.substring(startIndex, endIndex).trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
