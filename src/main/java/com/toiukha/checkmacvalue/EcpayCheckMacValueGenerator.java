package com.toiukha.checkmacvalue;

import java.net.URLEncoder;
import java.util.*;

public class EcpayCheckMacValueGenerator {
    public static String generate(Map<String, String> params, String hashKey, String hashIV) throws Exception {
        // 1. 參數依照字母排序
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);

        // 2. 組合字串
        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(hashKey);
        for (String key : keys) {
            sb.append("&").append(key).append("=").append(params.get(key));
        }
        sb.append("&HashIV=").append(hashIV);

        // 3. URL encode
        String urlEncoded = URLEncoder.encode(sb.toString(), "UTF-8")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\%2A", "*")
                .replaceAll("\\%2D", "-")
                .replaceAll("\\%2E", ".")
                .replaceAll("\\%5F", "_");

        // 4. 轉小寫
        urlEncoded = urlEncoded.toLowerCase();

        // 5. MD5
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] array = md.digest(urlEncoded.getBytes("UTF-8"));
        StringBuilder sb2 = new StringBuilder();
        for (byte b : array) {
            sb2.append(String.format("%02X", b));
        }
        return sb2.toString();
    }
}