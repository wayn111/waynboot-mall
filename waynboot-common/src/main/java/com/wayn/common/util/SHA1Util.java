package com.wayn.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Util {
    public static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(input.getBytes());
        byte[] output = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : output) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
