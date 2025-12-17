package com.example.mindmap.soa.common;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FormCodec {
    private FormCodec() {}

    public static String encode(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) sb.append("&");
            first = false;
            sb.append(urlEnc(e.getKey())).append("=").append(urlEnc(e.getValue() == null ? "" : e.getValue()));
        }
        return sb.toString();
    }

    public static Map<String, String> decode(String form) {
        Map<String, String> out = new LinkedHashMap<>();
        if (form == null || form.isBlank()) return out;

        String[] pairs = form.split("&");
        for (String p : pairs) {
            if (p.isBlank()) continue;
            int idx = p.indexOf('=');
            if (idx < 0) {
                out.put(urlDec(p), "");
            } else {
                String k = urlDec(p.substring(0, idx));
                String v = urlDec(p.substring(idx + 1));
                out.put(k, v);
            }
        }
        return out;
    }

    private static String urlEnc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String urlDec(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}

