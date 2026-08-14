package com.tgdownloader.app;

import android.content.Context;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class TelegramEngine {
    private final Context context;
    private Client client;
    private AuthManager auth;
    private DownloadManager downloads;
    private ChannelManager channels;

    public TelegramEngine(Context context) {
        this.context = context.getApplicationContext();
    }

    public void start() {
        auth = new AuthManager(context);

        client = Client.create(
            this::onUpdate,
            null,
            null
        );

        auth.attach(client);

        downloads = new DownloadManager(context, client);
        channels = new ChannelManager(client);
    }

    private void onUpdate(TdApi.Object update) {
        auth.handleUpdate(update);
        downloads.handleUpdate(update);
    }

    public Client client() { return client; }
    public AuthManager auth() { return auth; }
    public DownloadManager downloads() { return downloads; }
    public ChannelManager channels() { return channels; }
}
