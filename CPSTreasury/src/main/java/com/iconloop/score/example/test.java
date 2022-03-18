package com.iconloop.score.example;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

public class test {
    public static void main(String[] args) throws Exception {
        // Create raw data.
        String prefix = "proposal" + "|" + "10" + "|" + "proposal_key";
        byte[] prefix_byte = prefix.getBytes();
        System.out.println(prefix_byte);
    }
}
