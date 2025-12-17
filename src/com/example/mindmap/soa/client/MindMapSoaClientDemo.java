package com.example.mindmap.soa.client;

import com.example.mindmap.entities.MindMap;
import com.example.mindmap.entities.User;
import com.example.mindmap.soa.common.Codecs;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;

public class MindMapSoaClientDemo {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ApiClient api = new ApiClient();

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine();

        // LOGIN
        Map<String,String> loginResp = api.postEncrypted("/api/auth/login",
                ApiClient.mapOf("username", username, "password", password));

        if (!"1".equals(loginResp.get("ok"))) {
            System.out.println("Login failed: " + loginResp.getOrDefault("message", "unknown"));
            return;
        }

        String token = loginResp.get("token");
        int userId = Integer.parseInt(loginResp.get("userId"));
        System.out.println("OK! token=" + token);

        User owner = new User();
        owner.setId(userId);
        owner.setUsername(username);

        // LIST MAPS
        Map<String,String> mapsResp = api.postEncrypted("/api/maps/list",
                ApiClient.mapOf("token", token));

        if (!"1".equals(mapsResp.get("ok"))) {
            System.out.println("Maps list failed: " + mapsResp.getOrDefault("message", "unknown"));
            return;
        }

        String dataB64 = mapsResp.getOrDefault("data", "");
        String lines = new String(Base64.getDecoder().decode(dataB64), StandardCharsets.UTF_8);

        List<MindMap> maps = new ArrayList<>();
        for (String line : lines.split("\n")) {
            line = line.trim();
            if (line.isBlank()) continue;
            maps.add(Codecs.decodeMap(line, owner));
        }

        System.out.println("Your maps (" + maps.size() + "):");
        for (MindMap m : maps) {
            System.out.println(" - [" + m.getId() + "] " + m.getTitle() + (m.isFavorite() ? " ★" : ""));
        }

        // CREATE MAP
        System.out.print("\nCreate new map? (y/n): ");
        String ans = sc.nextLine().trim().toLowerCase(Locale.ROOT);
        if (ans.equals("y")) {
            System.out.print("New map title: ");
            String title = sc.nextLine().trim();

            Map<String,String> createResp = api.postEncrypted("/api/maps/create",
                    ApiClient.mapOf("token", token, "title", title));

            if (!"1".equals(createResp.get("ok"))) {
                System.out.println("Create failed: " + createResp.getOrDefault("message", "unknown"));
                return;
            }

            String createdLine = new String(Base64.getDecoder().decode(createResp.get("data")), StandardCharsets.UTF_8);
            MindMap created = Codecs.decodeMap(createdLine, owner);
            System.out.println("Created map: [" + created.getId() + "] " + created.getTitle());
        }
    }
}
