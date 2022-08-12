package com.sxw.commit.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class GitLogQuery {
    private final File workingDirectory;
    private final String gitCommon;

    public GitLogQuery(File workingDirectory, String gitCommon) {
        this.workingDirectory = workingDirectory;
        this.gitCommon = gitCommon;
    }

    public static class Result {
        static Result ERROR = new Result(-1);

        private final int exitValue;
        private final List<String> logs;

        Result(int exitValue) {
            this(exitValue, emptyList());
        }

        Result(int exitValue, List<String> logs) {
            this.exitValue = exitValue;
            this.logs = logs;
        }

        public boolean isSuccess() {
            return exitValue == 0;
        }

        public List<String> getLogs() {
            return logs;
        }
    }

    public Result execute() {
        try {
            ProcessBuilder processBuilder;
            String osName = System.getProperty("os.name");
            if (osName.contains("Windows")) {
                processBuilder = new ProcessBuilder("cmd", "/C", gitCommon);
            } else {
                processBuilder = new ProcessBuilder("sh", "-c", gitCommon);
            }

            Process process = processBuilder.directory(workingDirectory).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            List<String> output = reader.lines().collect(toList());

            process.waitFor(2, TimeUnit.SECONDS);
            process.destroy();
            process.waitFor();

            return new Result(process.exitValue(), output);
        } catch (Exception e) {
            return Result.ERROR;
        }
    }

}