package common.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.dto.store.StoreRequest;
import common.dto.store.StoreResponse;
import io.reactivex.rxjava3.core.Maybe;
import org.json.JSONObject;

import java.io.IOException;
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


    private final String url_buy = "http://localhost:" + "9002" + "/distributed/buy";

    private final String url_status = "http://localhost:" + "9002" + "/distributed/status";
    private ObjectMapper mapper;

    public StoreMessenger(){
        mapper = new ObjectMapper();
    }

    public Maybe<Boolean> requestBuy(StoreRequest request) throws IOException, InterruptedException {

        JSONObject obj = new JSONObject(request);

        System.out.println("This is how a new json object looks, possible error");
        System.out.println(obj);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .uri(URI.create(url_buy))
                .header("Content-Type", "application/json")
                .header("dapr-app-id", "store-service")
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("This is the string being sent back to us: " + response.body());
        try {
            StoreResponse storeResponse = mapper.readValue(response.body(), StoreResponse.class);
            return Maybe.just(storeResponse.isAllowed());
        }catch (Exception ex){
            return Maybe.error(ex);
        }
    }

    public Maybe<Integer> requestStatus(StoreRequest request){

        JSONObject obj = new JSONObject(request);
        System.out.println("This is how a new json object looks, possible error");
        System.out.println(obj);

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

}
