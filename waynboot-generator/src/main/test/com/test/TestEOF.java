package com.test;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestEOF {
    public static void main(String[] args) {
        try (DataInputStream in = new DataInputStream(new FileInputStream("E:/data.txt"))) {
            while (in.available() != 0) {
                System.out.write(in.readByte());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
