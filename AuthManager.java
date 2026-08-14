package com.tgdownloader.app;

import android.content.Context;
import android.os.Build;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class AuthManager {
    public enum State { LOADING, PHONE, CODE, PASSWORD, READY, ERROR }

    private final Context context;
    private Client client;
    private volatile State state = State.LOADING;
    private volatile String error = "";
    private Runnable listener;

    public AuthManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void attach(Client client) {
        this.client = client;
    }

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    public State state() { return state; }
    public String error() { return error; }

    private void change(State s) {
        state = s;
        if (listener != null) listener.run();
    }

    public void handleUpdate(TdApi.Object update) {
        if (!(update instanceof TdApi.UpdateAuthorizationState)) return;

        TdApi.AuthorizationState s =
            ((TdApi.UpdateAuthorizationState) update).authorizationState;

        if (s instanceof TdApi.AuthorizationStateWaitTdlibParameters) {
            sendParameters();
        } else if (s instanceof TdApi.AuthorizationStateWaitPhoneNumber) {
            change(State.PHONE);
        } else if (s instanceof TdApi.AuthorizationStateWaitCode) {
            change(State.CODE);
        } else if (s instanceof TdApi.AuthorizationStateWaitPassword) {
            change(State.PASSWORD);
        } else if (s instanceof TdApi.AuthorizationStateReady) {
            change(State.READY);
        } else if (s instanceof TdApi.AuthorizationStateClosed) {
            change(State.ERROR);
        }
    }

    private void sendParameters() {
        TdApi.SetTdlibParameters p = new TdApi.SetTdlibParameters();
        p.databaseDirectory =
            new java.io.File(context.getFilesDir(), "tdlib").getAbsolutePath();
        p.useMessageDatabase = true;
        p.useSecretChats = false;
        p.apiId = BuildConfig.TELEGRAM_API_ID;
        p.apiHash = BuildConfig.TELEGRAM_API_HASH;
        p.systemLanguageCode = "en";
        p.deviceModel = Build.MODEL;
        p.systemVersion = Build.VERSION.RELEASE;
        p.applicationVersion = BuildConfig.VERSION_NAME;
        p.enableStorageOptimizer = true;

        client.send(p, result -> {
            if (result instanceof TdApi.Error) {
                error = ((TdApi.Error) result).message;
                change(State.ERROR);
            }
        });
    }

    public void phone(String number) {
        client.send(
            new TdApi.SetAuthenticationPhoneNumber(number, null),
            result -> {
                if (result instanceof TdApi.Error) {
                    error = ((TdApi.Error) result).message;
                    change(State.ERROR);
                }
            }
        );
    }

    public void code(String code) {
        client.send(
            new TdApi.CheckAuthenticationCode(code),
            result -> {
                if (result instanceof TdApi.Error) {
                    error = ((TdApi.Error) result).message;
                    change(State.ERROR);
                }
            }
        );
    }

    public void password(String password) {
        client.send(
            new TdApi.CheckAuthenticationPassword(password),
            result -> {
                if (result instanceof TdApi.Error) {
                    error = ((TdApi.Error) result).message;
                    change(State.ERROR);
                }
            }
        );
    }

    public void logout() {
        client.send(new TdApi.LogOut(), result -> {});
    }
}
