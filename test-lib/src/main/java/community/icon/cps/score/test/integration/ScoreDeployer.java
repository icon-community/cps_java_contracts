package community.icon.cps.score.test.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import community.icon.cps.score.test.integration.model.Score;
import foundation.icon.jsonrpc.Address;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ScoreDeployer {

    private String contracts;
    private  CPS cps;

    public ScoreDeployer(CPS cps, String contracts){
        this.contracts = contracts;
        this.cps = cps;
    }

    public Map<String, Address> deployContracts() throws IOException, InterruptedException {
        Map<Float, List<Score>> scores = readSCOREs();

        ExecutorService exec = Executors.newFixedThreadPool(10);

        Map<String, foundation.icon.jsonrpc.Address> addresses = new HashMap<>();

        addresses.put("owner", foundation.icon.jsonrpc.Address.of(cps.owner));

        for (Map.Entry<Float, List<Score>> entry : scores.entrySet()) {
            Map<String, Future<Address>> result = new HashMap<>();
            for (Score score : entry.getValue()) {
                score.setPath(cps.getScorePath(score.getContract()));
                System.out.println("deploying contract " + score.getName() + " :: " + score.getPath());
                Thread.sleep(200);
                Map<String, String> addressParams = score.getAddressParams();

                for (Map.Entry<String, String> params : addressParams.entrySet()) {
                    String key = params.getKey();
                    String value = params.getValue();
                    score.addParams(key, addresses.get(value));
                }

                result.put(score.getName(), exec.submit(cps.deploy(score)));
            }

            for (Map.Entry<String, Future<Address>> futureEntry : result.entrySet()) {
                try {
                    foundation.icon.jsonrpc.Address address = futureEntry.getValue().get();
                    String name = futureEntry.getKey();
                    addresses.put(name, address);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(futureEntry.getKey() + " -- " + e.getMessage());
                }
            }
        }

        exec.shutdown();

        return addresses;
    }



    private Map<Float, List<Score>> readSCOREs() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        InputStream is = this.getClass()
                .getClassLoader()
                .getResourceAsStream(this.contracts);

        List<Score> list = objectMapper.readValue(is, new TypeReference<List<Score>>() {
        });
        return list.stream()
                .sorted((x, y) -> Float.compare(y.getOrder(), x.getOrder()))
                .collect(Collectors.groupingBy(Score::getOrder, Collectors.toList()));

    }
}
