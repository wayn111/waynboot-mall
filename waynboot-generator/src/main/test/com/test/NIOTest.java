package com.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOTest {

    private static String name = "E:/data.txt";
    private static final int BSIZE = 1024;


    public static void main(String[] args) throws IOException {

        // 写入一个文件:
        FileChannel fc = new FileOutputStream(name)
                .getChannel();
        fc.write(ByteBuffer
                .wrap("Some text ".getBytes()));
        // 读取文件e:
        FileChannel fc2 = new FileInputStream(name)
                .getChannel();
        ByteBuffer buff = ByteBuffer.allocate(BSIZE);
        fc2.read(buff);
        buff.flip();
        while (buff.hasRemaining())
            System.out.write(buff.get());
        System.out.println();
        System.out.flush();
    }
}
