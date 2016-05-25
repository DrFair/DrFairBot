package twitchResponse;

import java.util.ArrayList;

public class TwitchTags {

    public final String tagLine;
    private ArrayList<TwitchTag> tags;

    public TwitchTags(String tagsLine) {
        this.tagLine = tagsLine;
        this.tags = new ArrayList<>();
        parseTagsLine(tagsLine);
    }

    private void parseTagsLine(String line) {
        if (line.startsWith("@")) line = line.substring(1); // Removes "@" at the beginning of line
        String[] unparsedTags = line.split(";"); // Every tag is separated by a ";"
        for (String tag : unparsedTags) {
            String[] split = tag.split("="); // Tags separate tag name and data with a "="
            String tagName = split[0];
            String tagData = split.length > 1 ? split[1] : ""; // Sometimes the tag data is empty
            tags.add(new TwitchTag(tagName, tagData));
        }
    }

    public TwitchTag getTagByName(String name) {
        for (TwitchTag tag : tags) {
            if (tag.name.equals(name)) return tag;
        }
        return null;
    }

    public String getDataByName(String name) {
        TwitchTag tag = getTagByName(name);
        if (tag != null) return tag.data;
        return null;
    }

    public boolean getBooleanDataByName(String name) {
        String data = getDataByName(name);
        if (data == null) return false;
        try {
            int dataInt = Integer.parseInt(data);
            return dataInt != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getIntDataByName(String name) {
        String data = getDataByName(name);
        if (data == null) return 0;
        try {
            return Integer.parseInt(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
