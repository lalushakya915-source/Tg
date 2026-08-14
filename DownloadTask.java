package com.tgdownloader.app;

public class DownloadTask {
    public static final int QUEUED = 0;
    public static final int DOWNLOADING = 1;
    public static final int PAUSED = 2;
    public static final int COMPLETED = 3;
    public static final int FAILED = 4;
    public static final int CANCELLED = 5;

    public final int fileId;
    public final String fileName;
    public final long size;

    public volatile int state = QUEUED;
    public volatile int progress = 0;
    public volatile String error = "";

    public DownloadTask(int fileId, String fileName, long size) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.size = size;
    }
}
