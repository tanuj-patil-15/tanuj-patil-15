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
        
        String startMarker = "";
        String endMarker = "";
        
        int startIdx = content.indexOf(startMarker);
        int endIdx = content.indexOf(endMarker);
        
        if (startIdx != -1 && endIdx != -1) {
            String newContent = content.substring(0, startIdx + startMarker.length())
                              + "\n" + statsMarkdown + "\n"
                              + content.substring(endIdx);
            
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            newContent = newContent.replaceAll
