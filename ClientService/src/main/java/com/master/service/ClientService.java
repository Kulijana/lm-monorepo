package com.master.service;

import common.dto.store.StoreRequest;
import io.reactivex.rxjava3.core.Maybe;

public interface ClientService {
    Maybe<Integer> getProductStorage(StoreRequest request);

    Maybe<Boolean> buyFromStore(StoreRequest request);

    Maybe<Integer> getBalance();

    boolean startTransaction();

    void endTransaction();
}
