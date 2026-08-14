package com.tgdownloader.app;

import android.content.Context;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DownloadManager {
    private final Context context;
    private final Client client;
    private final Queue<DownloadTask> queue = new ArrayDeque<>();
    private final List<DownloadTask> all = new ArrayList<>();
    private DownloadTask current;
    private Runnable listener;

    public DownloadManager(Context context, Client client) {
        this.context = context.getApplicationContext();
        this.client = client;
    }

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    public List<DownloadTask> items() {
        return new ArrayList<>(all);
    }

    public synchronized void add(VideoItem v) {
        DownloadTask t = new DownloadTask(v.fileId, v.name, v.size);
        all.add(t);
        queue.offer(t);
        notifyChanged();
        startNext();
    }

    public synchronized void addAll(List<VideoItem> videos) {
        for (VideoItem v : videos) {
            DownloadTask t = new DownloadTask(v.fileId, v.name, v.size);
            all.add(t);
            queue.offer(t);
        }
        notifyChanged();
        startNext();
    }

    private synchronized void startNext() {
        if (current != null) return;
        current = queue.poll();
        if (current == null) return;

        current.state = DownloadTask.DOWNLOADING;
        notifyChanged();

        client.send(
            new TdApi.DownloadFile(
                current.fileId,
                32,
                0,
                0,
                false
            ),
            result -> {
                if (result instanceof TdApi.Error) {
                    current.state = DownloadTask.FAILED;
                    current.error = ((TdApi.Error) result).message;
                    finishCurrent();
                }
            }
        );
    }

    public synchronized void pauseCurrent() {
        if (current == null) return;
        client.send(
            new TdApi.CancelDownload(current.fileId, false),
            result -> {}
        );
        current.state = DownloadTask.PAUSED;
        notifyChanged();
    }

    public synchronized void resume(DownloadTask task) {
        if (task.state != DownloadTask.PAUSED &&
            task.state != DownloadTask.FAILED) return;

        task.state = DownloadTask.QUEUED;
        task.error = "";
        queue.offer(task);
        notifyChanged();
        startNext();
    }

    public synchronized void cancelCurrent() {
        if (current == null) return;
        client.send(
            new TdApi.CancelDownload(current.fileId, false),
            result -> {}
        );
        current.state = DownloadTask.CANCELLED;
        finishCurrent();
    }

    private synchronized void finishCurrent() {
        current = null;
        notifyChanged();
        startNext();
    }

    public void handleUpdate(TdApi.Object update) {
        if (!(update instanceof TdApi.UpdateFile)) return;

        TdApi.File f = ((TdApi.UpdateFile) update).file;

        synchronized (this) {
            if (current == null || current.fileId != f.id) return;

            long total = f.size;
            long done = f.local.downloadedSize;

            current.progress = total > 0
                ? (int)Math.min(100, done * 100L / total)
                : 0;

            if (f.local.isDownloadingCompleted) {
                File src = new File(f.local.path);
                boolean saved = VideoSaver.save(
                    context,
                    src,
                    current.fileName
                );

                current.state = saved
                    ? DownloadTask.COMPLETED
                    : DownloadTask.FAILED;

                if (!saved) current.error = "Could not save video";
                current.progress = saved ? 100 : current.progress;
                finishCurrent();
                return;
            }

            notifyChanged();
        }
    }

    private void notifyChanged() {
        if (listener != null) listener.run();
    }
}
