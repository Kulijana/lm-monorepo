package common.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.dto.LockRequest;
import common.dto.LockResponse;
import common.dto.LockType;
import common.dto.store.StoreRequest;
import common.dto.store.StoreResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import io.reactivex.rxjava3.core.*;

public class StoreMessenger {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();


    private final String url_buy = "http://localhost:" + "9002" + "/buy";

    private final String url_status = "http://localhost:" + "9002" + "/status";
    private ObjectMapper mapper;

    public StoreMessenger(){
        mapper = new ObjectMapper();
    }

    public boolean requestBuy(String TID, int timeToProcess) throws IOException, InterruptedException {
        StoreRequest storeRequest = new StoreRequest();
        storeRequest.TID = TID;
        storeRequest.amount = 1;
        storeRequest.timeToProcess = timeToProcess;


        JSONObject obj = new JSONObject();
        obj.put("TID", storeRequest.TID);
        obj.put("amount", storeRequest.amount);
        obj.put("customerId", storeRequest.customerId);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .uri(URI.create(url_buy))
                .header("Content-Type", "application/json")
                .header("dapr-app-id", "store-service")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("This is the string being sent back to us: " + response.body());
        StoreResponse storeResponse = mapper.readValue(response.body(), StoreResponse.class);
        return storeResponse.isAllowed();
    }

    public Maybe<Integer> requestStatus(String TID, int timeToProcess) throws IOException, InterruptedException {
        StoreRequest storeRequest = new StoreRequest();
        storeRequest.TID = TID;
        storeRequest.amount = 0;
        storeRequest.customerId = "TODO";
        storeRequest.timeToProcess = timeToProcess;


        JSONObject obj = new JSONObject();
        obj.put("TID", storeRequest.TID);
        obj.put("amount", storeRequest.amount);
        obj.put("customerId", storeRequest.customerId);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .uri(URI.create(url_status))
                .header("Content-Type", "application/json")
                .header("dapr-app-id", "store-service")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        StoreResponse storeResponse = mapper.readValue(response.body(), StoreResponse.class);
        return storeResponse.getAmount();
    }

}
