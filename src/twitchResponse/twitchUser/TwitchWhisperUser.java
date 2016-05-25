package twitchResponse.twitchUser;

import twitchResponse.TwitchTags;

public class TwitchWhisperUser extends TwitchUser {

    public final int messageID;
    public final String threadID;

    public TwitchWhisperUser(TwitchTags tags, String senderNick) {
        super(tags, senderNick);
        messageID = tags.getIntDataByName("message-id");
        threadID = tags.getDataByName("thread-id");
    }

}
