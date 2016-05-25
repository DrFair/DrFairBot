package twitchBot;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import twitchResponse.*;
import twitchResponse.twitchUser.TwitchChatUser;
import twitchResponse.twitchUser.TwitchWhisperUser;

import java.io.*;
import java.util.Scanner;

public class TwitchBot {

    public static final String TWITCH_USERNAME = "DrFairBot";
    // Oauth is the path at which the file with the oauth key is in.
    public static final String TWITCH_OAUTH_PATH = "oauth.txt";
    // OAuth token is generated at http://www.twitchapps.com/tmi

    // This info is found at http://help.twitch.tv/customer/portal/articles/1302780-twitch-irc
    public static final String TWITCH_HOSTNAME = "irc.chat.twitch.tv";
    public static final int TWITCH_PORT = 6667;

    public static final String TWITCH_WHISPER_IP = "199.9.253.119"; // Not in use

    public final String channel;
    private String OAuth;
    private TwitchPircBot pircBot;

    private TwitchBotForm form;
    private Thread scannerThread;

    public TwitchBot(String twitchChannel, boolean startForm) {
        this.channel = "#" + twitchChannel;
        if (startForm) form = new TwitchBotForm(this);
        pircBot = new TwitchPircBot(this);
        startScanner();
    }

    public void submitCommand(String command) {
        if (command.equals("exit")) {
            disconnect();
            return;
        } else if (command.startsWith("/r ")) {
            rawLine(command.substring(3));
            return;
        } else if (command.startsWith("/w ")) {
            String username = command.split(" ")[1];
            whisper(username, command.substring(3 + username.length() + 1));
            return;
        } else if (command.startsWith("/")) {
            command(command.substring(1));
            return;
        }
        chat(command);
    }

    public void log(String message) {
        System.out.println(message);
        if (form != null) form.writeConsoleLine(message);
    }

    public void chat(String message) {
        log("(" + channel + ") " + TWITCH_USERNAME + ": " + message);
        pircBot.sendRawLine("PRIVMSG " + channel + " :" + message);
    }

    public void command(String command) {
        log("(" + channel + ") issued command: /" + command);
        pircBot.sendRawLine("PRIVMSG " + channel + " :." + command);
    }

    public void whisper(String user, String message) {
        log("(whisper) " + TWITCH_USERNAME + " > " + user + ": " + message);
        pircBot.sendRawLine("PRIVMSG #jtv :.w " + user + " " + message);
    }

    public void rawLine(String line) {
        pircBot.sendRawLine(line);
    }

    public void onChannelNotice(String channel, TwitchTags tags, String notice) {
        log("(" + channel + ") " + notice);
    }

    public void onChannelNotify(String channel, String message) {
        log("(" + channel + ") " + message);
    }

    public void onChannelMessage(String channel, TwitchChatUser user, String message) {
        String prefix = "";
        if (user.mod) prefix += "MOD ";
        else prefix += "    ";
        if (user.turbo) prefix += "TURBO ";
        else prefix += "      ";
        if (user.subscriber) prefix += "SUB ";
        else prefix += "    ";
        log("(" + channel + ") " + prefix + user.displayName + ": " + message);
    }

    public void onChannelRoomState(String channel, TwitchRoomState roomState) {
        log("(" + channel + ") ROOMSTATE: lang=" + roomState.broadcasterLang + ", submode=" + roomState.subMode + ", r9k=" + roomState.r9k + ", emotemode=" + roomState.emoteOnly + ", slow=" + roomState.slow);
    }

    public void onWhisper(TwitchWhisperUser user, String message) {
        log("(whisper) " + user.displayName + " > " + TWITCH_USERNAME + ": " + message);
    }

    public void onServerResponse(int code, String response) {
        log(code + " - " + response);
    }

    public void onAcknowledge(String message) {
    }

    public void disconnect() {
        scannerThread.interrupt();
        log("Disconnecting bot..");
        pircBot.disconnect();
        log("Disposing bot threads..");
        pircBot.dispose();
        if (form != null) {
            form.setVisible(false);
            form.dispose();
        }
        System.exit(0);
    }

    public void startScanner() {
        scannerThread = new Thread("scanner") {
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (isAlive()) {
                    String command = scanner.nextLine();
                    if (command.length() != 0) submitCommand(command);
                }
                scanner.close();
            }
        };
        scannerThread.start();
    }

    public String getOAuth() {
        if (OAuth == null) {
            File file = new File(TWITCH_OAUTH_PATH);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                OAuth = br.readLine();
            } catch (FileNotFoundException e) {
                System.err.println("Could not find OAUTH file located at " + TWITCH_OAUTH_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (OAuth == null || OAuth.length() == 0) throw new NullPointerException("Could not read OAUTH in file located at " + TWITCH_OAUTH_PATH + ". Make sure first line is the OAUTH token.");
        }
        return OAuth;
    }

    private static class TwitchPircBot extends PircBot {

        private final TwitchBot bot;

        public TwitchPircBot(TwitchBot bot) {
            this.bot = bot;
//            setVerbose(true);
            setName(TWITCH_USERNAME);
            try {
                bot.log("Connecting to " + TWITCH_HOSTNAME + ":" + TWITCH_PORT);
                connect(TWITCH_HOSTNAME, TWITCH_PORT, bot.getOAuth());
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
                // Join channel
                bot.log("Joining channel " + bot.channel);
                sendRawLine("JOIN " + bot.channel);
            }
        }

        protected void handleLine(String line) {
            // Sometimes twitch has tags at the beginning of the message. PircBot does not support that.
//            super.handleLine(clearTags(line));
            // Disabled PircBot line handling, made own below.

            // Custom line handling
//            System.out.println("<" + line);
            try {
                TwitchResponse tr = new TwitchResponse(line);
//                System.out.println("<TR<" + tr.toString());
                handleTwitchResponse(tr);
//                tr.printResponseContent();
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
                    if (tr.getResponseCode().equals("CAP")) { // Capabilities
                        if (tr.getResponseContent(1).equals("ACK")) { // CAP acknowledge
                            String msg = tr.getResponseContentAfter(2);
                            bot.onAcknowledge(msg);
                            onAcknowledge(msg);
                        }
                    } else if (tr.getResponseCode().equals("ROOMSTATE")) {
                        String channel = tr.getResponseContent(0);
                        bot.onChannelRoomState(channel, new TwitchRoomState(tr.getTags()));
                    } else if (tr.getResponseCode().equals("NOTICE")) {
                        String channel = tr.getResponseContent(0);
                        String msg = clearColon(tr.getResponseContentAfter(1));
                        bot.onChannelNotice(channel, tr.getTags(), msg);
                    } else if (tr.getResponseCode().equals("WHISPER")) {
                        String msg = clearColon(tr.getResponseContentAfter(1));
                        bot.onWhisper(new TwitchWhisperUser(tr.getTags(), tr.getSenderNick()), msg);
                    } else if (tr.getResponseCode().equals("PRIVMSG")) {
                        String channel = tr.getResponseContent(0);
                        String msg = clearColon(tr.getResponseContentAfter(1));
                        if (tr.getSenderNick().equals("twitchnotify")) {
                            bot.onChannelNotify(channel, msg);
                        } else {
                            bot.onChannelMessage(channel, new TwitchChatUser(tr.getTags(), tr.getSenderNick()), msg);
                        }
                    }
                }
            }
        }

        protected String clearColon(String msg) {
            if (msg.startsWith(":")) return msg.substring(1);
            return msg;
        }

        protected String clearTags(String msg) {
            if (msg.startsWith("@")) return msg.substring(msg.indexOf(" ") + 1);
            return msg;
        }
    }
}
