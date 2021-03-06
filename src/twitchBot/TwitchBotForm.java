package twitchBot;

import twitchResponse.TwitchResponse;
import twitchResponse.TwitchTags;
import twitchResponse.twitchUser.TwitchChatUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class TwitchBotForm extends JFrame {

    private TwitchBot bot;

    private JPanel panel;
    private JTextPane console;
    private JScrollPane consoleScroll;
    private JTextField inputField;
    private JButton submitButton;
    private JList channelList;
    private JButton leaveChannelButton;
    private JTextField joinChannelField;
    private JButton joinChannelButton;

    private ArrayList<String> channels;
    private ArrayList<StringBuilder> channelDocs;
    private String focusChannel;
    private int focusIndex;

    public TwitchBotForm(TwitchBot bot) {
        super("DrFair Twitch Bot");
        this.bot = bot;
        setContentPane(panel);
        pack();
        setSize(700, 500);
        setVisible(true);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { // Close event listener
                bot.disconnect();
            }
        });

        submitButton.addActionListener(e -> submitInputField()); // Button to send input

        inputField.addKeyListener(new KeyAdapter() { // Add enter key listener to send input
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submitInputField();
                }
            }
        });

        channelList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int index = channelList.getSelectedIndex();
            if (index != -1) {
                String channel = channels.get(channelList.getSelectedIndex());
//                System.out.println("Changed channel to " + channel);
                setFocusChannel(channel);
            }
//            System.out.println("Changed value to " + channelList.getSelectedIndex());
        });



        leaveChannelButton.addActionListener(e -> {
            if (focusIndex != 0) bot.leaveChannel(focusChannel.substring(1));
            inputField.requestFocus();
        });
        joinChannelButton.addActionListener(e -> {
            if (joinChannelField.getText().length() > 0) {
                bot.joinChannel(joinChannelField.getText());
                joinChannelField.setText("");
                inputField.requestFocus();
            }
        });
        joinChannelField.addKeyListener(new KeyAdapter() { // Add enter key listener to join channel
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (joinChannelField.getText().length() > 0) {
                        bot.joinChannel(joinChannelField.getText());
                        joinChannelField.setText("");
                        inputField.requestFocus();
                    }
                }
            }
        });

        inputField.requestFocus();

        init();
    }

    private void init() {
        channels = new ArrayList<>();
        channelDocs = new ArrayList<>();
        openChannel("IRC-console");
        setFocusChannel("IRC-console");
    }

    public void setFocusChannel(String channel) {
        if (focusChannel != null && focusChannel.equals(channel)) return;
        openChannel(channel);
        focusChannel = channel;
        int index = channels.indexOf(channel);
        if (index != -1) {
            focusIndex = index;
            channelList.setSelectedIndex(index);
        }
        updateConsole(focusChannel);
    }

    public void openChannel(String channel) {
        if (!channels.contains(channel)) {
            int lastSelectedIndex = channelList.getSelectedIndex();
            channels.add(channel);
            channelDocs.add(new StringBuilder("<html>"));
            channelList.setListData(channels.toArray());
            channelList.setSelectedIndex(lastSelectedIndex);
        }
    }

    public void closeChannel(String channel) {
        closeChannel(channels.indexOf(channel));
    }

    public void closeCurrentChannel() {
        closeChannel(focusIndex);
    }

    public void closeChannel(int index) {
        if (index > 0) { // Not console log
            int lastSelectedIndex = channelList.getSelectedIndex();
            channels.remove(index);
            channelDocs.remove(index);
            channelList.setListData(channels.toArray());
            if (lastSelectedIndex == index) lastSelectedIndex--;
            setFocusChannel(channels.get(lastSelectedIndex));
        }
    }

    private void submitInputField() {
        if (inputField.getText().length() == 0) return;
        submitCommand(inputField.getText());
        inputField.setText("");
        inputField.requestFocus();
    }

    private void submitCommand(String command) {
        if (focusIndex == 0) { // In console log
            bot.submitCommand(command);
        } else { // In some channel
            if (command.startsWith("/")) { // Is a command
                bot.command(focusChannel, command.substring(1));
            } else {
                bot.chat(focusChannel, command);
            }
        }
    }

    public void writeConsole(String message) {
        writeChannel("IRC-console", message, Color.BLACK);
    }

    public void writeConsoleLine(String message) {
        writeChannelLine("IRC-console", message);
    }

    public void writeChannelLine(String channel, String message) {
        writeChannel(channel, message, Color.BLACK);
        newLine(channel);
    }

    public void writeChannel(String channel, String message, Color color) {
        String hexColor = String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        writeChannel(channel, message, hexColor);
    }

    public void writeChannel(String channel, String message, String color) {
        openChannel(channel);
        StringBuilder sb = channelDocs.get(channels.indexOf(channel));
        sb.append("<span style=\"color:");
        sb.append(color);
        sb.append("\">");
        sb.append(message);
        sb.append("</span>");
//        channels.put(channel, sb);
        updateConsole(channel, sb);
    }

    public void newLine(String channel) {
        openChannel(channel);
        StringBuilder sb = channelDocs.get(channels.indexOf(channel));
        sb.append("<br>");
//        channels.put(channel, sb);
        updateConsole(channel, sb);
    }

    private void updateConsole(String channel, StringBuilder sb) {
        if (focusChannel.equals(channel)) {
            console.setText(sb.toString());
            scrollDown();
        }
    }

    public void scrollDown() {
//        JScrollBar vScroll = consoleScroll.getVerticalScrollBar();
//        vScroll.setValue(vScroll.getMaximum());
        console.setCaretPosition(console.getDocument().getLength());
    }

    private void updateConsole(String channel) {
        openChannel(channel);
        updateConsole(channel, channelDocs.get(channels.indexOf(channel)));
    }

    public void submitChannelMessage(String channel, TwitchTags tags, TwitchChatUser user, String message) {
        writeChannel(channel, user.displayName, user.color);
        writeChannelLine(channel, ": " + message);
//        writeChannelLine(channel, ": " + insertEmotes(tags.getDataByName("emotes"), message));
    }

    public void submitChannelNotice(String channel, TwitchTags tags, String notice) {
        writeChannelLine(channel, notice);
    }

    public void submitChannelNotify(String channel, String message) {
        writeChannelLine(channel, message);
    }

    private String insertEmotes(String emoteData, String message) {
        if (emoteData.length() == 0) return message; // No emotes in message
        ArrayList<EmoteReplace> replaces = new ArrayList<>();
        String[] difEmotes = emoteData.split("/");
        for (String sEmoteData : difEmotes) {
            String[] dataSplit = sEmoteData.split(":");
            String emoteID = dataSplit[0];
            String[] emotePlaces = dataSplit[1].split(",");
            // Only need 1 place to get code that replaces all
            String placeData = emotePlaces[0];
            String[] splitPlace = placeData.split("-");
            try {
                int start = Integer.parseInt(splitPlace[0]);
                int end = Integer.parseInt(splitPlace[1]);
                replaces.add(new EmoteReplace(message.substring(start, end + 1), emoteID));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String out = " " + message + " ";
        for (EmoteReplace r : replaces) {
            out = r.format(out);
        }
        return out.trim();
    }

    private static class EmoteReplace {
        public final String code, emoteID;

        public EmoteReplace(String code, String emoteID) {
            this.code = code;
            System.out.println("CODE: \"" + code + "\"");
            this.emoteID = emoteID;
        }

        public String format(String originalText) {
            String htmlText = " <img src=\"http://static-cdn.jtvnw.net/emoticons/v1/" + emoteID + "/1.0\"> ";
            return originalText.replaceAll(" " + code + " ", htmlText);
        }
    }
}
