package twitchResponse;

public class TwitchRoomState {

    public final TwitchTags tags;
    public final String broadcasterLang;
    public final boolean emoteOnly, r9k, subMode;
    public final int slow;

    public TwitchRoomState(TwitchTags tags) {
        this.tags = tags;
        broadcasterLang = tags.getDataByName("broadcaster-lang");
        emoteOnly = tags.getBooleanDataByName("emote-only");
        r9k = tags.getBooleanDataByName("r9k");
        subMode = tags.getBooleanDataByName("sub-only");
        slow = tags.getIntDataByName("slow");
    }

}
