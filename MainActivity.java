package com.tgdownloader.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import org.drinkless.tdlib.TdApi;

public class MainActivity extends AppCompatActivity {
    private TGDownloaderApp app;
    private TelegramEngine engine;

    private LinearLayout root;
    private EditText input;
    private Button action;
    private TextView status;
    private LinearLayout list;
    private long chatId = 0;
    private long fromMessageId = 0;
    private final List<VideoItem> videos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        app = (TGDownloaderApp)getApplication();
        engine = app.getEngine();

        engine.auth().setListener(this::render);
        engine.downloads().setListener(this::renderDownloads);

        render();
    }

    private void render() {
        runOnUiThread(() -> {
            root = new LinearLayout(this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(24,24,24,24);

            TextView title = new TextView(this);
            title.setText("TGDownloader");
            title.setTextSize(26);
            root.addView(title);

            status = new TextView(this);
            status.setPadding(0,12,0,12);
            root.addView(status);

            AuthManager.State s = engine.auth().state();

            if (s == AuthManager.State.PHONE ||
                s == AuthManager.State.CODE ||
                s == AuthManager.State.PASSWORD) {
                showAuth(s);
            } else if (s == AuthManager.State.READY) {
                showDownloader();
            } else if (s == AuthManager.State.ERROR) {
                status.setText("Error: " + engine.auth().error());
            } else {
                status.setText("Connecting to Telegram...");
            }

            setContentView(root);
        });
    }

    private void showAuth(AuthManager.State s) {
        input = new EditText(this);
        input.setSingleLine(true);

        if (s == AuthManager.State.PHONE) {
            input.setHint("+919876543210");
            action = new Button(this);
            action.setText("Send OTP");
            action.setOnClickListener(v ->
                engine.auth().phone(input.getText().toString().trim())
            );
        } else if (s == AuthManager.State.CODE) {
            input.setHint("Telegram OTP");
            action = new Button(this);
            action.setText("Verify OTP");
            action.setOnClickListener(v ->
                engine.auth().code(input.getText().toString().trim())
            );
        } else {
            input.setHint("2FA password");
            input.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            action = new Button(this);
            action.setText("Login");
            action.setOnClickListener(v ->
                engine.auth().password(input.getText().toString())
            );
        }

        root.addView(input);
        root.addView(action);
    }

    private void showDownloader() {
        status.setText("Logged in");

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        EditText channel = new EditText(this);
        channel.setHint("@channel_username");
        row.addView(channel, new LinearLayout.LayoutParams(
            0, -2, 1
        ));

        Button find = new Button(this);
        find.setText("Find videos");
        row.addView(find);

        root.addView(row);

        Button selectAll = new Button(this);
        selectAll.setText("Select all loaded videos");
        root.addView(selectAll);

        Button download = new Button(this);
        download.setText("Download selected");
        root.addView(download);

        Button service = new Button(this);
        service.setText("Keep downloads running");
        service.setOnClickListener(v -> {
            Intent i = new Intent(this, DownloadService.class);
            ContextCompat.startForegroundService(this, i);
        });
        root.addView(service);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(
            -1, 0, 1
        ));

        find.setOnClickListener(v -> {
            String u = channel.getText().toString();
            engine.channels().resolve(u, result -> {
                if (result instanceof TdApi.Chat) {
                    chatId = ((TdApi.Chat)result).id;
                    fromMessageId = 0;
                    videos.clear();
                    loadPage();
                } else if (result instanceof TdApi.Error) {
                    runOnUiThread(() ->
                        Toast.makeText(
                            this,
                            ((TdApi.Error)result).message,
                            Toast.LENGTH_LONG
                        ).show()
                    );
                }
            });
        });

        selectAll.setOnClickListener(v -> {
            for (VideoItem item : videos) item.selected = true;
            renderVideos();
        });

        download.setOnClickListener(v -> {
            ArrayList<VideoItem> selected = new ArrayList<>();
            for (VideoItem item : videos)
                if (item.selected) selected.add(item);

            engine.downloads().addAll(selected);

            Toast.makeText(
                this,
                "Added " + selected.size() + " videos",
                Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void loadPage() {
        engine.channels().loadVideos(
            chatId,
            fromMessageId,
            result -> {
                if (result instanceof TdApi.FoundChatMessages) {
                    TdApi.FoundChatMessages found =
                        (TdApi.FoundChatMessages) result;

                    videos.addAll(ChannelManager.parseVideos(found));

                    if (found.messages.length > 0) {
                        fromMessageId =
                            found.messages[found.messages.length - 1].id;
                    }

                    runOnUiThread(this::renderVideos);
                }
            }
        );
    }

    private void renderVideos() {
        if (list == null) return;

        list.removeAllViews();

        for (VideoItem v : videos) {
            CheckBox box = new CheckBox(this);
            box.setText(
                v.name + "  (" +
                (v.size / (1024 * 1024)) +
                " MB)"
            );
            box.setChecked(v.selected);
            box.setOnCheckedChangeListener(
                (button, checked) -> v.selected = checked
            );
            list.addView(box);
        }

        Button more = new Button(this);
        more.setText("Load older videos");
        more.setOnClickListener(v -> loadPage());
        list.addView(more);
    }

    private void renderDownloads() {
        // Download state can be wired to a RecyclerView/ViewModel
        // in the next UI refinement. Core download state is retained
        // by DownloadManager.
    }
}
