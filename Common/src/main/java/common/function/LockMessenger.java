package common.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.dto.lockmanager.LockRequest;
import common.dto.lockmanager.LockResponse;
import io.reactivex.rxjava3.core.Maybe;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LockMessenger {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String url_lock = "http://localhost:" + "9001" + "/locks";
    private final String url_unlock = "http://localhost:" + "9001" + "/unlocks";

    private int attempts;

    private int timeout;


    private ObjectMapper mapper;

    public LockMessenger(){
        mapper = new ObjectMapper();
        timeout = 0;
        attempts = 1;
    }

    public LockMessenger(int attempts, int timeout){
        mapper = new ObjectMapper();
        this.attempts = attempts;
        this.timeout = timeout;
    }



    public Maybe<Boolean> multipleAttemptLock(LockRequest request){
        try {
            boolean acquiredLock = requestLock(request);
            while (attempts > 0 && !acquiredLock) {
                attempts--;
                Thread.sleep(timeout);
                acquiredLock = requestLock(request);
            }
            return Maybe.just(acquiredLock);
        }catch(Exception ex){
            return Maybe.error(ex);
        }
    }

    public boolean requestLock(LockRequest request) throws IOException, InterruptedException {

        JSONObject obj = new JSONObject(request);
//        System.out.println("Request to LM: " + obj);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .uri(URI.create(url_lock))
                .header("Content-Type", "application/json")
                .header("dapr-app-id", "lock-manager")
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        LockResponse lockResponse = mapper.readValue(response.body(), LockResponse.class);
        return lockResponse.allowed;
    }

    public void unlock(String TID) throws IOException, InterruptedException {

        JSONObject obj = new JSONObject();
        obj.put("tid", TID);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .uri(URI.create(url_unlock))
                .header("Content-Type", "application/json")
                .header("dapr-app-id", "lock-manager")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
