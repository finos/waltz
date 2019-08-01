package com.khartec.waltz.common;

import java.security.NoSuchAlgorithmException;

import static java.security.MessageDigest.getInstance;
import static java.util.Base64.getEncoder;

public class DigestUtilities {

    public static String digest(byte[] bytes) throws NoSuchAlgorithmException {
        return getEncoder().encodeToString(getInstance("SHA").digest(bytes));
    }

}
