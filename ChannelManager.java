package com.tgdownloader.app;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.util.ArrayList;
import java.util.List;

public class ChannelManager {
    private final Client client;

    public ChannelManager(Client client) {
        this.client = client;
    }

    public void resolve(String username, Client.ResultHandler handler) {
        String clean = username.trim().replaceFirst("^@", "");
        client.send(new TdApi.SearchPublicChat(clean), handler);
    }

    public void loadVideos(long chatId, long fromMessageId,
                           Client.ResultHandler handler) {
        client.send(
            new TdApi.SearchChatMessages(
                chatId,
                "",
                null,
                fromMessageId,
                0,
                100,
                new TdApi.SearchMessagesFilterVideo(),
                0
            ),
            handler
        );
    }

    public static List<VideoItem> parseVideos(TdApi.FoundChatMessages result) {
        List<VideoItem> out = new ArrayList<>();
        for (TdApi.Message m : result.messages) {
            if (m.content instanceof TdApi.MessageVideo) {
                TdApi.MessageVideo mv = (TdApi.MessageVideo) m.content;
                TdApi.Video v = mv.video;
                if (v != null && v.video != null) {
                    out.add(new VideoItem(
                        m.chatId,
                        m.id,
                        v.video.id,
                        v.fileName,
                        v.video.size,
                        v.duration
                    ));
                }
            }
        }
        return out;
    }
}
