package com.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class BufferedInputFile {
    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new FileReader("E:/data.txt"))) {
            System.out.println(reader.lines().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            log.error(e.getMessage(), e);;
        }
    }

}
