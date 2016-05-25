package twitchResponse.twitchUser;

import twitchResponse.TwitchTags;

public class TwitchChatUser extends TwitchUser {

    public final boolean mod, subscriber;
    public final int roomID;

    public TwitchChatUser(TwitchTags tags, String senderNick) {
        super(tags, senderNick);
        mod = tags.getBooleanDataByName("mod");
        subscriber = tags.getBooleanDataByName("subscriber");
        roomID = tags.getIntDataByName("room-id");
    }

}
