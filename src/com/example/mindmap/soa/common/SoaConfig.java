package com.example.mindmap.soa.common;

public final class SoaConfig {
    private SoaConfig() {}

    public static final int PORT = 8088;
    public static final String BASE_URL = "http://localhost:" + PORT;

    // ВАЖЛИВО: однаковий секрет має бути і на сервері, і на клієнті
    //для реального продукту — роблять інакше (key exchange / TLS).
    public static final String SHARED_SECRET = "mindmap-trpz-shared-secret";
}
