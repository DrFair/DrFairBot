package twitchResponse;

import java.util.Arrays;

public class TwitchResponse {

    private String originalLine;
    private String[] splitLines;

    private TwitchTags twitchTags;
    private String senderInfo;
    private String senderNick, senderLogin, senderHostName;
    private String responseCode, responseContent;
    private String[] responseContentSplit;
    private boolean isRequest;
    private String requestCode, requestContent;

    public TwitchResponse(String line) {
        this.originalLine = line;
        this.splitLines = line.split(" ");
        formatResponse();
    }

    private void formatResponse() {
        int startIndex = 0;
        // Sorts out twitch tags, at the beginning of every response
        if (originalLine.startsWith("@")) {
            twitchTags = new TwitchTags(splitLines[0]);
            startIndex = 1;
        } else twitchTags = null;

        senderInfo = "";
        senderNick = "";
        senderLogin = "";
        senderHostName = "";
        responseCode = "";
        responseContent = "";
        isRequest = false;
        requestCode = "";

        // Figure out sender info
        if (startIndex < splitLines.length) {
            if (!splitLines[startIndex].startsWith(":")) {
                isRequest = true;
                requestCode = splitLines[startIndex];
                requestContent = originalLine.substring(requestCode.length() + 1);
                startIndex++;
            }
            sortSenderInfo(splitLines[startIndex]);
        }

        // Next is the code/tag
        if (startIndex + 1 < splitLines.length) {
            responseCode = splitLines[startIndex + 1];
        }

        // The rest is the content
        if (startIndex + 2 < splitLines.length) {
            responseContentSplit = new String[splitLines.length - (startIndex + 2)];
            responseContentSplit[0] = splitLines[startIndex + 2];
            responseContent = splitLines[startIndex + 2];
            for (int i = startIndex + 3; i < splitLines.length; i++) {
                responseContent += " " + splitLines[i];
                responseContentSplit[i - startIndex - 2] = splitLines[i];
            }
        }
    }

    private void sortSenderInfo(String info) {
        senderInfo = info;
        int exc = senderInfo.indexOf("!");
        int at = senderInfo.indexOf("@");
        if (exc > 0 && at > 0 && exc < at) {
            senderNick = senderInfo.substring(1, exc);
            senderLogin = senderInfo.substring(exc + 1, at);
            senderHostName = senderInfo.substring(at + 1);
        } else if (at > 0) {
            senderLogin = senderInfo.substring(1, at);
            senderHostName = senderInfo.substring(at + 1);
        } else {
            senderHostName = senderInfo.substring(1);
        }
    }

    public TwitchTags getTags() {
        return twitchTags;
    }

    public TwitchTag getTagByName(String name) {
        if (twitchTags == null) return null;
        return twitchTags.getTagByName(name);
    }

    public String getTagDataByName(String name) {
        if (twitchTags == null) return null;
        return twitchTags.getDataByName(name);
    }

    public String getSenderInfo() {
        return senderInfo;
    }

    public String getSenderNick() {
        return senderNick;
    }

    public String getSenderLogin() {
        return senderLogin;
    }

    public String getSenderHostName() {
        return senderHostName;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public String getResponseContent(int index) {
        return responseContentSplit[index];
    }

    public String getResponseContentAfter(int index) {
        String msg = getResponseContent(index);
        for (int i = index + 1; i < getResponseContentLength(); i++) {
            msg += " " + getResponseContent(i);
        }
        return msg;
    }

    public int getResponseContentLength() {
        return responseContentSplit.length;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public String getRequestContent() {
        return requestContent;
    }

    public String getOriginalLine() {
        return originalLine;
    }

    public String toString() {
        if (isRequest()) return "REQ: " + getRequestCode() + " " + getSenderInfo() + " " + getResponseCode() + " " + getResponseContent();
        return getSenderInfo() + " " + getResponseCode() + " " + getResponseContent();
    }

    public void printResponseContent() {
        System.out.println(Arrays.toString(responseContentSplit));
    }
}
