package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OSExecute {
    public static void command(String command) throws Exception {
        boolean err = false;
        try {
            Process process = new ProcessBuilder(
                    command.split(" ")).start();
            try (
                    BufferedReader results = new BufferedReader(
                            new InputStreamReader(
                                    process.getInputStream(), "gbK"));
                    BufferedReader errors = new BufferedReader(
                            new InputStreamReader(
                                    process.getErrorStream()))
            ) {
                results.lines()
                        .forEach(System.out::println);
                err = errors.lines()
                        .peek(System.err::println)
                        .count() > 0;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (err)
            throw new Exception(
                    "Errors executing " + command);
    }

    public static void main(String[] args) throws Exception {
        command("java -version");
    }
}
