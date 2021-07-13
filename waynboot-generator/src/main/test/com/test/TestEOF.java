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
            log.error(e.getMessage(), e);;
        } catch (IOException e) {
            log.error(e.getMessage(), e);;
        }
        System.out.println(1000== 1000);
        Integer i1 = 1000;
        Integer i2 = 1000;
        System.out.println(i1== i2);

    }
}
