package twitchBot;

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

    private String OAuth;
    private TwitchPircBot pircBot;

    private TwitchBotForm form;
    private Thread scannerThread;

    public TwitchBot(boolean startForm) {
        if (startForm) form = new TwitchBotForm(this);
        pircBot = new TwitchPircBot(this);
        startScanner();
    }

    public void submitCommand(String command) {
        if (command.equals("exit")) {
            disconnect();
            return;
        } else if (command.startsWith("raw ")) {
            rawLine(command.substring(4));
            return;
        } else if (command.startsWith("w ")) {
            String username = command.split(" ")[1];
            whisper(username, command.substring(3 + username.length() + 1));
            return;
        } else if (command.startsWith("join ")) {
            joinChannel(command.substring(5));
            return;
        } else if (command.startsWith("leave ")) {
            leaveChannel(command.substring(6));
            return;
        } else {
            log("Unknown command: " + command);
        }
    }

    public void chat(String channel, String message) {
        log("(" + channel + ") " + TWITCH_USERNAME + ": " + message);
        if (form != null) form.writeChannelLine(channel, TWITCH_USERNAME + ": " + message);
        pircBot.sendRawLine("PRIVMSG " + channel + " :" + message);
    }

    public void command(String channel, String command) {
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
        if (form != null) form.submitChannelNotice(channel, tags, notice);
    }

    public void onChannelNotify(String channel, String message) {
        log("(" + channel + ") " + message);
        if (form != null) form.submitChannelNotify(channel, message);
    }

    public void onChannelMessage(String channel, TwitchTags tags, TwitchChatUser user, String message) {
        String prefix = "";
        if (user.mod) prefix += "MOD ";
        else prefix += "    ";
        if (user.turbo) prefix += "TURBO ";
        else prefix += "      ";
        if (user.subscriber) prefix += "SUB ";
        else prefix += "    ";
        log("(" + channel + ") " + prefix + user.displayName + ": " + message);
        if (form != null) form.submitChannelMessage(channel, tags, user, message);
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

    public void onConnected() {
        // Join channel
//        joinChannel("drfairtv");
    }

    public void joinChannel(String twitchChannel) {
        String channel = "#" + twitchChannel.toLowerCase();
        log("Joining channel " + channel);
        if (form != null) {
            form.writeChannelLine(channel, "Joining channel " + channel);
            form.setFocusChannel(channel);
        }
        rawLine("JOIN " + channel);
    }

    public void leaveChannel(String twitchChannel) {
        String channel = "#" + twitchChannel.toLowerCase();
        log("Leaving channel " + channel);
        if (form != null) {
            form.writeChannelLine(channel, "Leaving channel " + channel);
            form.closeChannel(channel);
        }
        rawLine("PART " + channel);
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

    public void log(String message) {
        System.out.println(message);
        if (form != null) form.writeConsoleLine(message);
    }
}
