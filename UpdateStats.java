import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

public class UpdateStats {
    public static void main(String[] args) throws Exception {
        String username = "tanujp15";

        // 1. Fetch solve counts + submission calendar
        String query = "{\"query\": \"query { matchedUser(username: \\\"" + username + "\\\") { " +
                       "submitStats { acSubmissionNum { difficulty count } } " +
                       "userCalendar { submissionCalendar } } }\"}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://leetcode.com/graphql"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();

        String jsonData = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        // 2. Extract solve counts
        int easy   = extract(jsonData, "Easy");
        int medium = extract(jsonData, "Medium");
        int hard   = extract(jsonData, "Hard");

        // 3. Calculate current streak from submissionCalendar
        int streak = calculateCurrentStreak(jsonData);

        // 4. Build the Markdown table
        String statsMarkdown = String.format(
            "| Stat | Count |\n" +
            "| :--- | :--- |\n" +
            "| 🟢 **Easy** | %d |\n" +
            "| 🟡 **Medium** | %d |\n" +
            "| 🔴 **Hard** | %d |\n" +
            "| 🔥 **Total Solved** | **%d** |\n" +
            "| ⚡ **Current Streak** | %d |",
            easy, medium, hard, (easy + medium + hard), streak
        );

        // 5. Update README.md between markers
        Path path = Paths.get("README.md");
        String content = Files.readString(path);

        String startMarker = "<!-- LEETCODE_STATS_START -->";
        String endMarker   = "<!-- LEETCODE_STATS_END -->";

        int startIdx = content.indexOf(startMarker);
        int endIdx   = content.indexOf(endMarker);

        if (startIdx != -1 && endIdx != -1) {
            String newContent = content.substring(0, startIdx + startMarker.length())
                              + "\n" + statsMarkdown + "\n"
                              + content.substring(endIdx);

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            newContent = newContent.replaceAll("\\*Last updated:.*\\*", "*Last updated: " + now + "*");

            Files.writeString(path, newContent);
            System.out.println("README.md updated successfully!");
        } else {
            System.out.println("ERROR: Markers not found in README.md.");
        }
    }

    // Calculate current streak: count consecutive days ending today (or yesterday)
    static int calculateCurrentStreak(String json) {
        // Extract the submissionCalendar JSON string
        String key = "\"submissionCalendar\":\"";
        int idx = json.indexOf(key);
        if (idx == -1) return 0;

        int start = idx + key.length();
        int end = json.indexOf("\"", start);
        String calendarRaw = json.substring(start, end).replace("\\\"", "\"");

        // Parse all active day timestamps into a Set of LocalDates
        Set<LocalDate> activeDays = new HashSet<>();
        int pos = 0;
        while ((pos = calendarRaw.indexOf("\"", pos)) != -1) {
            int colonIdx = calendarRaw.indexOf(":", pos);
            if (colonIdx == -1) break;
            String tsStr = calendarRaw.substring(pos + 1, colonIdx).trim();
            pos = colonIdx + 1;
            try {
                long ts = Long.parseLong(tsStr);
                LocalDate date = LocalDate.ofEpochDay(ts / 86400);
                activeDays.add(date);
            } catch (NumberFormatException e) {
                // skip non-numeric keys
            }
        }

        // Walk backwards from today, counting consecutive active days
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int streak = 0;

        // Allow streak to include today OR just count from yesterday if today has no submission yet
        LocalDate checkFrom = activeDays.contains(today) ? today : today.minusDays(1);

        LocalDate cursor = checkFrom;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        return streak;
    }

    static int extract(String json, String difficulty) {
        String pattern = "\"difficulty\":\"" + difficulty + "\",\"count\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return 0;
        int start = idx + pattern.length();
        int end = json.indexOf("}", start);
        return Integer.parseInt(json.substring(start, end).trim());
    }
}
