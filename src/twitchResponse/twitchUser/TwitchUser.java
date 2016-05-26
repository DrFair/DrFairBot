package twitchResponse.twitchUser;

import twitchResponse.TwitchTags;

import java.awt.*;

public class TwitchUser {

    public final TwitchTags tags;
    public final String displayName, badges, userType;
    public final boolean hasSetDisplayName;
    public final String color;
    public final int userID;
    public final boolean turbo;

    public TwitchUser(TwitchTags tags, String senderNick) {
        this.tags = tags;
        String displayName = tags.getDataByName("display-name");
        if (displayName == null) throw new NullPointerException("Could not get twitch user from tags: " + tags.tagLine);
        if (displayName.length() == 0) {
            this.displayName = senderNick + "*";
            hasSetDisplayName = false;
        } else {
            this.displayName = displayName;
            hasSetDisplayName = true;
        }
        badges = tags.getDataByName("badges");
//        color = Color.decode(tags.getDataByName("color"));
        color = tags.getDataByName("color");
        turbo = tags.getBooleanDataByName("turbo");
        userID = tags.getIntDataByName("user-id");
        userType = tags.getDataByName("user-type");
    }
}
