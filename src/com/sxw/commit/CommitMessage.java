package com.sxw.commit;

import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Damien Arrachequesne <damien.arrachequesne@gmail.com>
 */
public class CommitMessage {
    private static final int MAX_COUNT_SCOPES = 10;

    private static final Pattern COMMIT_FIRST_LINE_FORMAT = Pattern.compile("^([a-z]+)(\\((.+)\\))?: (.+)");
    private static final Pattern COMMIT_SCOPE_FIRST_LINE_FORMAT = Pattern.compile("^[a-z]+\\((.+)\\):.*");
    private static final Pattern COMMIT_CLOSES_FORMAT = Pattern.compile("\\[fixed: (.+)\\]");

    private ChangeType changeType;
    private String changeScope, commitTitle, commitDesc, closedIssues;

    private CommitMessage() {
        this.commitDesc = "";
        this.closedIssues = "";
    }

    public CommitMessage(ChangeType changeType, String changeScope, String commitTitle, String commitDesc, String closedIssues) {
        this.changeType = changeType;
        this.changeScope = changeScope;
        this.commitTitle = commitTitle;
        this.commitDesc = commitDesc;
        this.closedIssues = closedIssues;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(changeType.label());
        if (isNotBlank(changeScope)) {
            builder.append('(').append(changeScope).append(')');
        }
        builder.append(": ").append(commitTitle);

        if (isNotBlank(commitDesc)) {
            builder.append(System.lineSeparator()).append(System.lineSeparator()).append("[").append(commitDesc).append("]");
        }


        if (isNotBlank(closedIssues)) {
            builder.append(System.lineSeparator());
            for (String closedIssue : closedIssues.split(",")) {
                builder.append(System.lineSeparator()).append("[").append("fixed: ").append(formatClosedIssue(closedIssue)).append("]");
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
            commitMessage.commitTitle = matcher.group(4);

            String[] strings = message.split("\n");
            if (strings.length < 2) return commitMessage;

            int pos = 1;
            StringBuilder stringBuilder;

            stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                String lineString = strings[pos];
                if (lineString.startsWith("[fixed: ")) break;
                stringBuilder.append(lineString).append('\n');
            }

            int firstIndex = stringBuilder.indexOf("[");
            if (firstIndex >= 0) {
                int lastIndex = stringBuilder.indexOf("]") - 1;
                commitMessage.commitDesc = stringBuilder.toString().trim().substring(firstIndex, lastIndex);
            }

            stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                String lineString = strings[pos];
                if (lineString.startsWith("[fixed: ")) break;
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

    public static Set<String> getScopes(List<String> logs) {
        Set<String> scopes = new HashSet<>();

        logs.forEach(s -> {
            if (scopes.size() > MAX_COUNT_SCOPES) {
                return;
            }
            Matcher matcher = COMMIT_SCOPE_FIRST_LINE_FORMAT.matcher(s);
            if (matcher.find()) {
                scopes.add(matcher.group(1));
            }
        });

        return scopes;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public String getChangeScope() {
        return changeScope;
    }

    public String getCommitTitle() {
        return commitTitle;
    }

    public String getCommitDesc() {
        return commitDesc;
    }


    public String getClosedIssues() {
        return closedIssues;
    }

}