package community.icon.cps.score.CPSTreasury;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

public class test {
    public static void main(String[] args){
        // Create raw data.
        String params = "" + "{\"method\":\"_swap_icx\"}";
        byte[] data = params.getBytes();

        System.out.println(data);

        String unpacked = new String(data);
        System.out.println(unpacked);

        JsonObject json = Json.parse(unpacked).asObject();
        String method = json.get("method").asString();
        System.out.println(method);
    }
}
