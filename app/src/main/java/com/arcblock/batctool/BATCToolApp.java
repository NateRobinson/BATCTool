package com.arcblock.batctool;

import android.app.Application;

import com.apollographql.apollo.fetcher.ApolloResponseFetchers;
import com.arcblock.corekit.ABCoreKitClient;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

import static com.arcblock.corekit.config.CoreKitConfig.ApiType.API_TYPE_BTC;

public class BATCToolApp extends Application {

    private ABCoreKitClient mABCoreClient;

    public static BATCToolApp INSTANCE = null;

    public static BATCToolApp getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        INSTANCE = this;
        Timber.plant(new Timber.DebugTree());

        mABCoreClient = ABCoreKitClient.builder(this, API_TYPE_BTC)
                .setOpenOkHttpLog(true)
                .setDefaultResponseFetcher(ApolloResponseFetchers.CACHE_AND_NETWORK)
                .build();
    }

    @NotNull
    public ABCoreKitClient abCoreKitClient() {
        if (mABCoreClient == null) {
            throw new RuntimeException("Please init corekit first.");
        }
        return mABCoreClient;
    }
}
