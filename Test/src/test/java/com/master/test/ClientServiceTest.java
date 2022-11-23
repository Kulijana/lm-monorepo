package com.master.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.master.LockManagerApplication;
import com.master.StoreServiceApplication;
import org.junit.jupiter.api.Test;
import com.master.ClientService;

import java.io.IOException;

public class ClientServiceTest {

    @Test
    void simpleLockingTest() throws IOException, InterruptedException {
        ClientService service = new ClientService();
        LockManagerApplication.main(new String[0]);
        StoreServiceApplication.main(new String[0]);

        assertEquals(100,service.getStorage("item1", "case1", 0));
    }
}
