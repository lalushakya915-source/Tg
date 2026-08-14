package com.tgdownloader.app;

import android.app.Application;

public class TGDownloaderApp extends Application {
    private TelegramEngine engine;

    @Override
    public void onCreate() {
        super.onCreate();
        engine = new TelegramEngine(this);
        engine.start();
    }

    public TelegramEngine getEngine() {
        return engine;
    }
}
