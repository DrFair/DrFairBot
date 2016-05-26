package twitchBot;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import twitchResponse.TwitchResponse;
import twitchResponse.TwitchRoomState;
import twitchResponse.twitchUser.TwitchChatUser;
import twitchResponse.twitchUser.TwitchWhisperUser;

import java.io.IOException;

public class TwitchPircBot extends PircBot {

    private final TwitchBot bot;

    public TwitchPircBot(TwitchBot bot) {
        this.bot = bot;
        setName(TwitchBot.TWITCH_USERNAME);
        try {
            bot.log("Connecting to " + TwitchBot.TWITCH_HOSTNAME + ":" + TwitchBot.TWITCH_PORT);
            connect(TwitchBot.TWITCH_HOSTNAME, TwitchBot.TWITCH_PORT, bot.getOAuth());
            // Now wait for onConnect() method to be run.
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IrcException e) {
            e.printStackTrace();
        }
    }

    protected void onConnect() {
        // Sends requests to get information about members
        bot.log("Requesting tags capabilities.");
        sendRawLine("CAP REQ :twitch.tv/tags");
        // Wait for it getting acknowledge in tagsAck()
    }

    public void onAcknowledge(String message) {
        if (message.equals(":twitch.tv/tags")) {
            bot.log("Got tags capabilities.");
            // Sends requests to get information about membership
            bot.log("Requesting membership capabilities.");
            sendRawLine("CAP REQ :twitch.tv/membership");
            // Wait for it getting acknowledge
        } else if (message.equals(":twitch.tv/membership")) {
            bot.log("Got membership capabilities.");
            // Sends requests to get information about commands
            bot.log("Requesting commands capabilities.");
            sendRawLine("CAP REQ :twitch.tv/commands");
            // Wait for it getting acknowledge
        } else if (message.equals(":twitch.tv/commands")) {
            bot.log("Got commands capabilities.");
            // Send connected event to bot
            bot.onConnected();
        }
    }

    protected void handleLine(String line) {
        // Sometimes twitch has tags at the beginning of the message. PircBot does not support that.
//        super.handleLine(clearTags(line));
        // Disabled PircBot line handling, made own below.

        // Custom line handling
//        System.out.println("<" + line);
        try {
            TwitchResponse tr = new TwitchResponse(line);
//            System.out.println("<TR<" + tr.toString());
            handleTwitchResponse(tr);
//            tr.printResponseContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleTwitchResponse(TwitchResponse tr) {
        if (tr.isRequest()) {
            if (tr.getRequestCode().equals("PING")) { // Send a ping reply
                sendRawLine("PONG " + tr.getRequestContent());
            }
        } else {
            try { // Check if message is a server response
                int code = Integer.parseInt(tr.getResponseCode());
                bot.onServerResponse(code, tr.getResponseContent());
            } catch (Exception e) {
                // Was not a server response
                switch (tr.getResponseCode()) {
                    case "CAP":  // Capabilities
                        if (tr.getResponseContent(1).equals("ACK")) { // CAP acknowledge
                            String msg = tr.getResponseContentAfter(2);
                            bot.onAcknowledge(msg);
                            onAcknowledge(msg);
                        }
                        break;
                    case "ROOMSTATE": {
                        String channel = tr.getResponseContent(0);
                        bot.onChannelRoomState(channel, new TwitchRoomState(tr.getTags()));
                        break;
                    }
                    case "NOTICE": {
                        String channel = tr.getResponseContent(0);
                        String msg = clearColon(tr.getResponseContentAfter(1));
                        bot.onChannelNotice(channel, tr.getTags(), msg);
                        break;
                    }
                    case "WHISPER": {
                        String msg = clearColon(tr.getResponseContentAfter(1));
                        bot.onWhisper(new TwitchWhisperUser(tr.getTags(), tr.getSenderNick()), msg);
                        break;
                    }
                    case "PRIVMSG": {
                        String channel = tr.getResponseContent(0);
                        String msg = clearColon(tr.getResponseContentAfter(1));
                        if (tr.getSenderNick().equals("twitchnotify")) {
                            bot.onChannelNotify(channel, msg);
                        } else {
                            bot.onChannelMessage(channel, tr.getTags(), new TwitchChatUser(tr.getTags(), tr.getSenderNick()), msg);
                        }
                        break;
                    }
                }
            }
        }
    }

    protected String clearColon(String msg) {
        if (msg.startsWith(":")) return msg.substring(1);
        return msg;
    }
}
