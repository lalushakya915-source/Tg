package com.tgdownloader.app;

public class VideoItem {
    public final long chatId;
    public final long messageId;
    public final int fileId;
    public final String name;
    public final long size;
    public final int duration;
    public boolean selected;

    public VideoItem(long chatId, long messageId, int fileId,
                     String name, long size, int duration) {
        this.chatId = chatId;
        this.messageId = messageId;
        this.fileId = fileId;
        this.name = name == null || name.isEmpty() ? ("video_" + fileId + ".mp4") : name;
        this.size = size;
        this.duration = duration;
    }
}
