package com.khartec.waltz.common;

import sun.misc.BASE64Encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtilities {

    public static String digest(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA");
        byte[] digest = sha.digest(bytes);
        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(digest);
    }

}
