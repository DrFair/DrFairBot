package twitchBot;

import twitchResponse.TwitchTags;
import twitchResponse.twitchUser.TwitchChatUser;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class TwitchBotForm extends JFrame {

    private TwitchBot bot;

    private JPanel panel;
    private JTextPane console;
    private JScrollPane consoleScroll;
    private JTextField inputField;
    private JButton submitButton;
    private JList channelList;

    private ArrayList<String> channels;
    private ArrayList<StringBuilder> channelDocs;
    private String focusChannel;

    public TwitchBotForm(TwitchBot bot) {
        super("DrFair Twitch Bot");
        this.bot = bot;
        setContentPane(panel);
        pack();
        setSize(500, 500);
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

        channelList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int index = channelList.getSelectedIndex();
                if (index != -1) {
                    String channel = channels.get(channelList.getSelectedIndex());
//                    System.out.println("Changed channel to " + channel);
                    setFocusChannel(channel);
                }
//                System.out.println("Changed value to " + channelList.getSelectedIndex());
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

    private void setFocusChannel(String channel) {
        if (focusChannel != null && focusChannel.equals(channel)) return;
        openChannel(channel);
        focusChannel = channel;
        int index = channels.indexOf(channel);
        if (index != -1) channelList.setSelectedIndex(index);
        updateConsole(focusChannel);
    }

    private void openChannel(String channel) {
        if (!channels.contains(channel)) {
            int lastSelectedIndex = channelList.getSelectedIndex();
            channels.add(channel);
            channelDocs.add(new StringBuilder("<html>"));
            channelList.setListData(channels.toArray());
            channelList.setSelectedIndex(lastSelectedIndex);
        }
    }

    private void submitInputField() {
        bot.submitCommand(inputField.getText());
        inputField.setText("");
        inputField.requestFocus();
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
            console.setCaretPosition(console.getDocument().getLength());
        }
    }

    private void updateConsole(String channel) {
        openChannel(channel);
        updateConsole(channel, channelDocs.get(channels.indexOf(channel)));
    }

    public void submitChannelMessage(String channel, TwitchChatUser user, String message) {
        writeChannel(channel, user.displayName, user.color);
        writeChannelLine(channel, ": " + message);
    }

    public void submitChannelNotice(String channel, TwitchTags tags, String notice) {
        writeChannelLine(channel, notice);
    }

    public void submitChannelNotify(String channel, String message) {
        writeChannelLine(channel, message);
    }
}
