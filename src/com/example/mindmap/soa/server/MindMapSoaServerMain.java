package com.example.mindmap.soa.server;

import com.example.mindmap.db.JdbcConnectionProvider;
import com.example.mindmap.entities.Category;
import com.example.mindmap.entities.MapElement;
import com.example.mindmap.entities.MindMap;
import com.example.mindmap.entities.User;
import com.example.mindmap.repositories.JdbcCategoryRepository;
import com.example.mindmap.repositories.JdbcMapElementRepository;
import com.example.mindmap.repositories.JdbcMapStrokeRepository;
import com.example.mindmap.repositories.JdbcMindMapRepository;
import com.example.mindmap.repositories.JdbcUserRepository;
import com.example.mindmap.services.AuthService;
import com.example.mindmap.services.MindMapService;
import com.example.mindmap.soa.common.Codecs;
import com.example.mindmap.soa.common.CryptoUtils;
import com.example.mindmap.soa.common.FormCodec;
import com.example.mindmap.soa.common.SoaConfig;
import com.example.mindmap.tools.drawing.DrawingStroke;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

public class MindMapSoaServerMain {

    private static final class Req {
        final Map<String, String> form;
        final Integer userId;
        Req(Map<String, String> form, Integer userId) {
            this.form = form;
            this.userId = userId;
        }
    }

    public static void main(String[] args) throws Exception {

        // --- wiring: JDBC репозиторії + сервіси ---
        JdbcConnectionProvider provider = new JdbcConnectionProvider();

        JdbcUserRepository userRepo = new JdbcUserRepository(provider);
        JdbcMindMapRepository mapRepo = new JdbcMindMapRepository(provider);
        JdbcMapElementRepository elementRepo = new JdbcMapElementRepository(provider);
        JdbcMapStrokeRepository strokeRepo = new JdbcMapStrokeRepository(provider);
        JdbcCategoryRepository categoryRepo = new JdbcCategoryRepository(provider);

        AuthService authService = new AuthService(userRepo);

        MindMapService mindMapService = new MindMapService(mapRepo, elementRepo, strokeRepo, categoryRepo);

        TokenService tokens = new TokenService(60 * 60); // 1 година

        // --- HTTP server ---
        HttpServer server = HttpServer.create(new InetSocketAddress(SoaConfig.PORT), 0);

        // health-check (не шифрований)
        server.createContext("/", ex -> {
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                writePlain(ex, 405, "Method Not Allowed");
                return;
            }
            writePlain(ex, 200, "MindMap SOA server is running on port " + SoaConfig.PORT);
        });

        // -------- AUTH --------

        server.createContext("/api/auth/login", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { writePlain(ex, 405, "Method Not Allowed"); return; }

            Req r = readReq(ex, tokens, false);
            if (r == null) return;

            String username = r.form.getOrDefault("username", "").trim();
            String password = r.form.getOrDefault("password", "");

            Optional<User> uOpt = authService.login(username, password);
            if (uOpt.isEmpty()) { writeEncryptedError(ex, 401, "Invalid credentials"); return; }

            User u = uOpt.get();
            String token = tokens.issueToken(u.getId());

            Map<String,String> resp = new LinkedHashMap<>();
            resp.put("ok", "1");
            resp.put("userId", String.valueOf(u.getId()));
            resp.put("username", u.getUsername());
            resp.put("token", token);
            writeEncryptedForm(ex, 200, resp);
        });

        server.createContext("/api/auth/register", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { writePlain(ex, 405, "Method Not Allowed"); return; }

            Req r = readReq(ex, tokens, false);
            if (r == null) return;

            String username = r.form.getOrDefault("username", "").trim();
            String password = r.form.getOrDefault("password", "");

            try {
                User created = authService.register(username, password);
                String token = tokens.issueToken(created.getId());

                Map<String,String> resp = new LinkedHashMap<>();
                resp.put("ok", "1");
                resp.put("userId", String.valueOf(created.getId()));
                resp.put("username", created.getUsername());
                resp.put("token", token);
                writeEncryptedForm(ex, 200, resp);
            } catch (Exception e) {
                writeEncryptedError(ex, 400, "Register failed: " + e.getMessage());
            }
        });

        // -------- MAPS --------

        server.createContext("/api/maps/list", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { writePlain(ex, 405, "Method Not Allowed"); return; }

            Req r = readReq(ex, tokens, true);
            if (r == null) return;

            User owner = new User();
            owner.setId(r.userId);

            List<MindMap> maps = mindMapService.getMapsByUser(owner);

            StringBuilder lines = new StringBuilder();
            for (MindMap m : maps) lines.append(Codecs.encodeMap(m)).append("\n");

            Map<String,String> resp = new LinkedHashMap<>();
            resp.put("ok", "1");
            resp.put("data", Base64.getEncoder().encodeToString(lines.toString().getBytes(StandardCharsets.UTF_8)));
            writeEncryptedForm(ex, 200, resp);
        });

        server.createContext("/api/maps/create", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { writePlain(ex, 405, "Method Not Allowed"); return; }

            Req r = readReq(ex, tokens, true);
            if (r == null) return;

            String title = r.form.getOrDefault("title", "").trim();
            if (title.isBlank()) { writeEncryptedError(ex, 400, "Title required"); return; }

            User owner = new User();
            owner.setId(r.userId);

            MindMap created = mindMapService.createMap(title, owner);

            Map<String,String> resp = new LinkedHashMap<>();
            resp.put("ok", "1");
            resp.put("data", Base64.getEncoder().encodeToString(Codecs.encodeMap(created).getBytes(StandardCharsets.UTF_8)));
            writeEncryptedForm(ex, 200, resp);
        });

        // -------- CATEGORIES --------

        server.createContext("/api/categories/list", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { writePlain(ex, 405, "Method Not Allowed"); return; }

            Req r = readReq(ex, tokens, true);
            if (r == null) return;

            User owner = new User();
            owner.setId(r.userId);

            List<Category> categories = mindMapService.getCategoriesByUser(owner);

            StringBuilder lines = new StringBuilder();
            for (Category c : categories) lines.append(Codecs.encodeCategory(c)).append("\n");

            Map<String,String> resp = new LinkedHashMap<>();
            resp.put("ok", "1");
            resp.put("data", Base64.getEncoder().encodeToString(lines.toString().getBytes(StandardCharsets.UTF_8)));
            writeEncryptedForm(ex, 200, resp);
        });

        server.createContext("/api/categories/create", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { writePlain(ex, 405, "Method Not Allowed"); return; }

            Req r = readReq(ex, tokens, true);
            if (r == null) return;

            User owner = new User();
            owner.setId(r.userId);

            String title = r.form.getOrDefault("title", "").trim();
            String color = r.form.getOrDefault("color", "").trim();
            if (title.isBlank()) { writeEncryptedError(ex, 400, "title required"); return; }

            Category created = mindMapService.createCategory(owner, title, color);

            Map<String,String> resp = new LinkedHashMap<>();
            resp.put("ok", "1");
            resp.put("data", Base64.getEncoder().encodeToString(Codecs.encodeCategory(created).getBytes(StandardCharsets.UTF_8)));
            writeEncryptedForm(ex, 200, resp);
        });

        // -------- ELEMENTS --------

        server.createContext("/api/elements/list", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { writePlain(ex, 405, "Method Not Allowed"); return; }

            Req r = readReq(ex, tokens, true);
            if (r == null) return;

            int mapId = Integer.parseInt(r.form.getOrDefault("mapId", "0"));

            MindMap map = new MindMap();
            map.setId(mapId);

            List<MapElement> els = mindMapService.getElementsForMap(map);

            StringBuilder lines = new StringBuilder();
            for (MapElement el : els) lines.append(Codecs.encodeElement(el)).append("\n");

            Map<String,String> resp = new LinkedHashMap<>();
            resp.put("ok", "1");
            resp.put("data", Base64.getEncoder().encodeToString(lines.toString().getBytes(StandardCharsets.UTF_8)));
            writeEncryptedForm(ex, 200, resp);
        });

        server.createContext("/api/elements/update", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { writePlain(ex, 405, "Method Not Allowed"); return; }

            Req r = readReq(ex, tokens, true);
            if (r == null) return;

            String elB64 = r.form.get("el");
            if (elB64 == null || elB64.isBlank()) { writeEncryptedError(ex, 400, "el required"); return; }

            String line = new String(Base64.getDecoder().decode(elB64), StandardCharsets.UTF_8);
            MapElement el = Codecs.decodeElement(line);
            mindMapService.updateElement(el);

            Map<String,String> resp = new LinkedHashMap<>();
            resp.put("ok", "1");
            writeEncryptedForm(ex, 200, resp);
        });

        // -------- STROKES --------

        server.createContext("/api/strokes/list", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { writePlain(ex, 405, "Method Not Allowed"); return; }

            Req r = readReq(ex, tokens, true);
            if (r == null) return;

            int mapId = Integer.parseInt(r.form.getOrDefault("mapId", "0"));

            MindMap map = new MindMap();
            map.setId(mapId);

            List<DrawingStroke> strokes = mindMapService.getStrokesForMap(map);

            StringBuilder lines = new StringBuilder();
            for (DrawingStroke s : strokes) lines.append(Codecs.encodeStroke(s)).append("\n");

            Map<String,String> resp = new LinkedHashMap<>();
            resp.put("ok", "1");
            resp.put("data", Base64.getEncoder().encodeToString(lines.toString().getBytes(StandardCharsets.UTF_8)));
            writeEncryptedForm(ex, 200, resp);
        });

        server.start();
        System.out.println("MindMap SOA server started on " + SoaConfig.BASE_URL);
    }

    // ====== Request reading (ONE TIME) ======

    private static Req readReq(HttpExchange ex, TokenService tokens, boolean requireAuth) throws IOException {
        Map<String,String> form;
        try {
            form = readEncryptedForm(ex);
        } catch (Exception e) {
            writeEncryptedError(ex, 400, "Bad encrypted request: " + e.getMessage());
            return null;
        }

        Integer userId = null;
        if (requireAuth) {
            String token = form.get("token");
            userId = tokens.validate(token);
            if (userId == null) {
                writeEncryptedError(ex, 401, "Invalid/expired token");
                return null;
            }
        }

        return new Req(form, userId);
    }

    private static Map<String,String> readEncryptedForm(HttpExchange ex) throws IOException {
        String ivB64 = ex.getRequestHeaders().getFirst("X-IV");
        if (ivB64 == null || ivB64.isBlank()) throw new RuntimeException("Missing X-IV header");

        String bodyB64 = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String plain = CryptoUtils.decryptFromB64(ivB64, bodyB64);
        return FormCodec.decode(plain);
    }

    private static void writeEncryptedForm(HttpExchange ex, int status, Map<String,String> params) throws IOException {
        String plain = FormCodec.encode(params);
        String ivB64 = CryptoUtils.randomIvB64();
        String ctB64 = CryptoUtils.encryptToB64(ivB64, plain);

        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.getResponseHeaders().set("X-IV", ivB64);

        byte[] bytes = ctB64.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static void writeEncryptedError(HttpExchange ex, int status, String msg) throws IOException {
        Map<String,String> resp = new LinkedHashMap<>();
        resp.put("ok", "0");
        resp.put("message", msg);
        writeEncryptedForm(ex, status, resp);
    }

    private static void writePlain(HttpExchange ex, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}
