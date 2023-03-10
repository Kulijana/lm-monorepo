package common.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.dto.store.StoreRequest;
import common.dto.store.StoreResponse;
import io.reactivex.rxjava3.core.Maybe;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class StoreMessenger {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();


    private String url_buy;

    private String url_status;

    private String url_rollback;

    private String url_scenario;
    private ObjectMapper mapper;

    private String address;

    public StoreMessenger(String address){
        mapper = new ObjectMapper();
        this.address = address;
        url_buy = "http://localhost:" + "9002" + "/" + address + "/buy";
        url_status = "http://localhost:" + "9002" + "/" + address + "/status";
        url_rollback = "http://localhost:" + "9002" + "/" + address + "/rollback";
        url_scenario = "http://localhost:" + "9002" + "/" + address + "/scenario";
    }

    public Maybe<Boolean> requestBuy(StoreRequest request){
        try {
            JSONObject obj = new JSONObject(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                    .uri(URI.create(url_buy))
                    .header("Content-Type", "application/json")
                    .header("dapr-app-id", "store-service")
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("This is the string being sent back to us: " + response.body());

            StoreResponse storeResponse = mapper.readValue(response.body(), StoreResponse.class);
            return Maybe.just(storeResponse.isAllowed());
        }catch (Exception ex){
            return Maybe.error(ex);
        }
    }

    public Maybe<Integer> requestStatus(StoreRequest request){

        JSONObject obj = new JSONObject(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .uri(URI.create(url_status))
                .header("Content-Type", "application/json")
                .header("dapr-app-id", "store-service")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            StoreResponse storeResponse = mapper.readValue(response.body(), StoreResponse.class);
            return Maybe.just(storeResponse.getAmount());
        }catch (Exception ex){
            return Maybe.error(ex);
        }
    }

    public Maybe<Boolean> createScenario(int productCount, int productAmount){

//        hacked together, can be upgraded
        StoreRequest request = new StoreRequest();
        request.setProductId(String.valueOf(productCount));
        request.setAmount(productAmount);

        JSONObject obj = new JSONObject(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .uri(URI.create(url_scenario))
                .header("Content-Type", "application/json")
                .header("dapr-app-id", "store-service")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            StoreResponse storeResponse = mapper.readValue(response.body(), StoreResponse.class);
            return Maybe.just(storeResponse.isAllowed());
        }catch (Exception ex){
            return Maybe.error(ex);
        }
    }

    public Maybe<Boolean> rollback(StoreRequest request){
        JSONObject obj = new JSONObject(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .uri(URI.create(url_rollback))
                .header("Content-Type", "application/json")
                .header("dapr-app-id", "store-service")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            StoreResponse storeResponse = mapper.readValue(response.body(), StoreResponse.class);
            return Maybe.just(storeResponse.isAllowed());
        }catch (Exception ex){
            return Maybe.error(ex);
        }
    }

}
