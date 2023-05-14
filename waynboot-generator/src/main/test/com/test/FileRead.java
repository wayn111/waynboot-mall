package com.test;


import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;

@Slf4j
public class FileRead {
    public static void main(String[] args) {
        try (FileReader fileReader = new FileReader("E:/data.txt")) {
            char[] arr = new char[1024];
            int len;
            while ((len = fileReader.read(arr)) != -1) {
                System.out.println(new String(arr,0 ,len));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);;
        }

    }
}
