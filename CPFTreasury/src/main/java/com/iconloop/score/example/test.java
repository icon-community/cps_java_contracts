package com.iconloop.score.example;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

public class test {
    public static void main(String[] args) throws Exception {
        // Create raw data.
        String SYSTEM_ADDRESS = "cx000";
        List<?> path = new ArrayList<>();
        String params = "" + "{\"method\":\"_swap\",\"params\":{\"toToken\":" + "\"" + SYSTEM_ADDRESS.toString() + "\"," + "\"path\":" + "\"" + "[]" + "\""  + "}}";
        byte[] data = params.getBytes();

        System.out.println(data);

        String unpacked = new String(data);
        System.out.println(unpacked);

        JsonObject json = Json.parse(unpacked).asObject();
        String method = json.get("method").asString();
        String params_ = json.get("params").asObject().get("path").asString();
        System.out.println(params_);

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("method", "_swap");
        JsonObject params__ = new JsonObject();
        params__.add("toToken", "swopnil");
        jsonObject.add("params", params__);
        System.out.println(jsonObject);
        String jsonstring = String.valueOf(jsonObject);
        byte[]byte_string = jsonstring.getBytes();
        System.out.println(byte_string);

        String unpacked_ = new String(byte_string);
        System.out.println(unpacked_);

        JsonObject json_ = Json.parse(unpacked_).asObject();
        String method_ = json_.get("method").asString();
        String params___ = json_.get("params").asObject().get("toToken").asString();
        System.out.println(method_);

    }
}
