package com.test;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileRead {
    public static void main(String[] args) {
        try (FileReader fileReader = new FileReader("E:/data.txt")) {
            char arr[] = new char[1024];
            int len;
            while ((len = fileReader.read(arr)) != -1) {
                System.out.println(new String(arr,0 ,len));
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);;
        } catch (IOException e) {
            log.error(e.getMessage(), e);;
        }

    }
}
