package com.leroymerlin.commit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Damien Arrachequesne <damien.arrachequesne@gmail.com>
 */
class CommitMessage {
    private static final int MAX_LINE_LENGTH = 72; // https://stackoverflow.com/a/2120040/5138796

    public static final Pattern COMMIT_FIRST_LINE_FORMAT = Pattern.compile("^([a-z]+)(\\[(.+)\\])?: (.+)");
    public static final Pattern COMMIT_CLOSES_FORMAT = Pattern.compile("\\[fixed: (.+)\\]");

    private ChangeType changeType;
    private String changeScope, shortDescription, longDescription, closedIssues;

    private CommitMessage() {
        this.longDescription = "";
        this.closedIssues = "";
    }

    public CommitMessage(ChangeType changeType, String changeScope, String shortDescription, String longDescription,
                         String closedIssues) {
        this.changeType = changeType;
        this.changeScope = changeScope;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.closedIssues = closedIssues;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(changeType.label());
        if (isNotBlank(changeScope)) {
            builder
                    .append('[')
                    .append(changeScope)
                    .append(']');
        }
        builder
                .append(": ")
                .append(shortDescription);

        if (isNotBlank(longDescription)) {
            builder
                    .append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("[")
                    .append(WordUtils.wrap(longDescription, MAX_LINE_LENGTH))
                    .append("]");
        }


        if (isNotBlank(closedIssues)) {
            builder.append(System.lineSeparator());
            for (String closedIssue : closedIssues.split(",")) {
                builder
                        .append(System.lineSeparator())
                        .append("[")
                        .append("fixed: ")
                        .append(formatClosedIssue(closedIssue))
                        .append("]");
            }
        }


        return builder.toString();
    }

    private String formatClosedIssue(String closedIssue) {
        String trimmed = closedIssue.trim();
        return (StringUtils.isNumeric(trimmed) ? "#" : "") + trimmed;
    }

    public static CommitMessage parse(String message) {
        CommitMessage commitMessage = new CommitMessage();

        try {
            Matcher matcher = COMMIT_FIRST_LINE_FORMAT.matcher(message);
            if (!matcher.find()) return commitMessage;

            commitMessage.changeType = ChangeType.valueOf(matcher.group(1).toUpperCase());
            commitMessage.changeScope = matcher.group(3);
            commitMessage.shortDescription = matcher.group(4);

            String[] strings = message.split("\n");
            if (strings.length < 2) return commitMessage;

            int pos = 1;
            StringBuilder stringBuilder;

            stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                String lineString = strings[pos];
                if (lineString.startsWith("BREAKING") || lineString.startsWith("[fixed: ") || lineString.equalsIgnoreCase("[skip ci]"))
                    break;
                stringBuilder.append(lineString).append('\n');
            }

            int firstIndex = stringBuilder.indexOf("[");
            int lastIndex = stringBuilder.indexOf("]") - 1;
            commitMessage.longDescription = stringBuilder.toString().trim().substring(firstIndex, lastIndex);

            stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                String lineString = strings[pos];
                if (lineString.startsWith("[fixed: ") || lineString.equalsIgnoreCase("[skip ci]")) break;
                stringBuilder.append(lineString).append('\n');
            }

            matcher = COMMIT_CLOSES_FORMAT.matcher(message);
            stringBuilder = new StringBuilder();
            while (matcher.find()) {
                stringBuilder.append(matcher.group(1)).append(',');
            }
            if (stringBuilder.length() > 0) stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            commitMessage.closedIssues = stringBuilder.toString();

        } catch (RuntimeException e) {
        }

        return commitMessage;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public String getChangeScope() {
        return changeScope;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }


    public String getClosedIssues() {
        return closedIssues;
    }

}