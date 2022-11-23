package common.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.dto.LockRequest;
import common.dto.LockResponse;
import common.dto.LockType;
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

    private ObjectMapper mapper;

    public LockMessenger(){
        mapper = new ObjectMapper();
    }

    public boolean multipleAttemptLock(String DBID, String TID, LockType lock, int attempts, int timeout) throws IOException, InterruptedException {
        boolean acquiredLock = requestLock(DBID, TID, lock);
        while (attempts > 0 && !acquiredLock){
            attempts--;
            Thread.sleep(timeout);
            acquiredLock = requestLock(DBID, TID, lock);
        }
        return acquiredLock;
    }

    public boolean requestLock(String DBID, String TID, LockType lock) throws IOException, InterruptedException {
        LockRequest lockRequest = new LockRequest();
        lockRequest.DBID = DBID;
        lockRequest.type = lock;
        lockRequest.TID = TID;

        JSONObject obj = new JSONObject();
        obj.put("DBID", lockRequest.DBID);
        obj.put("TID", lockRequest.TID);
        obj.put("type", lockRequest.type);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .uri(URI.create(url_lock))
                .header("Content-Type", "application/json")
                .header("dapr-app-id", "lock-manager")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        LockResponse lockResponse = mapper.readValue(response.body(), LockResponse.class);
        return lockResponse.allowed;
    }

    public void unlock(String TID) throws IOException, InterruptedException {

        JSONObject obj = new JSONObject();
        obj.put("TID", TID);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                .uri(URI.create(url_unlock))
                .header("Content-Type", "application/json")
                .header("dapr-app-id", "lock-manager")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
