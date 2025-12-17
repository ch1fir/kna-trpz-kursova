package com.example.mindmap.soa.client;

import com.example.mindmap.soa.common.CryptoUtils;
import com.example.mindmap.soa.common.FormCodec;
import com.example.mindmap.soa.common.SoaConfig;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiClient {

    public Map<String,String> postEncrypted(String path, Map<String,String> formParams) {
        try {
            String url = SoaConfig.BASE_URL + path;

            String plain = FormCodec.encode(formParams);
            String ivB64 = CryptoUtils.randomIvB64();
            String ctB64 = CryptoUtils.encryptToB64(ivB64, plain);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.setRequestProperty("X-IV", ivB64);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(ctB64.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

            String respCtB64 = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String respIvB64 = conn.getHeaderField("X-IV");
            if (respIvB64 == null || respIvB64.isBlank()) {
                throw new RuntimeException("Missing X-IV in response");
            }

            String respPlain = CryptoUtils.decryptFromB64(respIvB64, respCtB64);
            return FormCodec.decode(respPlain);

        } catch (Exception e) {
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        }
    }

    public static Map<String,String> mapOf(String... kv) {
        Map<String,String> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(kv[i], kv[i+1]);
        }
        return m;
    }
}
