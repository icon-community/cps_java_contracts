package com.iconloop.score.example;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

public class test {
    public static void main(String[] args) throws Exception {
        // Create raw data.
        int ipfs_key = 0;
        String params = "" + "{\"method\":\"disqualify_project\",\"params\":{\"ipfs_key\":" + "\"" + ipfs_key + "\"" + "}}";
        byte[] data = params.getBytes();

        System.out.println(data);

        String unpacked = new String(data);
        System.out.println(unpacked);

        JsonObject json = Json.parse(unpacked).asObject();
        String method = json.get("method").asString();
        String ipfs_key_ = json.get("params").asObject().get("ipfs_key").asString();
        int ipfs_key__ = Integer.parseInt(ipfs_key_);
        System.out.println(ipfs_key__);
    }
}
