
package com.android.emerson.dl.core;

import com.android.emerson.dl.utils.ConfigUtils;
import com.android.emerson.dl.utils.DownloadUtils;
import com.android.emerson.dl.utils.DownloadValues;

import android.content.Context;
import android.content.Intent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DownloadManager extends Thread {
	private SDCardAndCountStatusListener statusListener = null;

    private Context mContext;

    private TaskQueue mTaskQueue;
    private List<DownloadTask> mDownloadingTasks;
    private List<DownloadTask> mPausingTasks;

    private Boolean isRunning = false;

    public DownloadManager(Context context) {

        mContext = context;
        mTaskQueue = new TaskQueue();
        mDownloadingTasks = new ArrayList<DownloadTask>();
        mPausingTasks = new ArrayList<DownloadTask>();
    }

    public void startManage() {

        isRunning = true;
        this.start();
        checkUncompleteTasks();
    }

    public void close() {

        isRunning = false;
        pauseAllTask();
        this.stop();
    }

    public boolean isRunning() {

        return isRunning;
    }

    @Override
    public void run() {

        super.run();
        while (isRunning) {
            DownloadTask task = mTaskQueue.poll();
            mDownloadingTasks.add(task);
            task.execute();
        }
    }

    public void addTask(String url) {
        if (!DownloadUtils.isSDCardPresent()) {
        	if(statusListener != null)
        		statusListener.notFoundSDCard(DownloadTask.ERROR_NOT_FOUND_SDCARD, DownloadTask.ERROR_NOT_FOUND_SDCARD_INFO);
            return;
        }

        if (!DownloadUtils.isSdCardWrittenable()) {
        	if(statusListener != null)
        		statusListener.sdCardCannotWriteOrRead(DownloadTask.ERROR_CANNOT_WRITEORREAD_SDCARD, DownloadTask.ERROR_CANNOT_WRITEORREAD_SDCARD_INFO);
            return;
        }

        if (getTotalTaskCount() >= DownloadTask.MAX_TASK_COUNT) {
        	if(statusListener != null)
        		statusListener.moreTaskCount(DownloadTask.ERROR_MORE_TASK_COUNT, DownloadTask.ERROR_MORE_TASK_COUNT_INFO);
            return;
        }

        try {
			DownloadUtils.mkdir();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        try {
            addTask(newDownloadTask(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private void addTask(DownloadTask task) {

        broadcastAddTask(task.getUrl());

        mTaskQueue.offer(task);

        if (!this.isAlive()) {
            this.startManage();
        }
    }

    private void broadcastAddTask(String url) {

        broadcastAddTask(url, false);
    }

    private void broadcastAddTask(String url, boolean isInterrupt) {

        Intent nofityIntent = new Intent(DownloadValues.Actions.BROADCAST_RECEIVER_ACTION);
        nofityIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.ADD);
        nofityIntent.putExtra(DownloadValues.URL, url);
        nofityIntent.putExtra(DownloadValues.IS_PAUSED, isInterrupt);
        mContext.sendBroadcast(nofityIntent);
    }

    public void reBroadcastAddAllTask() {

        DownloadTask task;
        for (int i = 0; i < mDownloadingTasks.size(); i++) {
            task = mDownloadingTasks.get(i);
            broadcastAddTask(task.getUrl(), task.isInterrupt());
        }
        for (int i = 0; i < mTaskQueue.size(); i++) {
            task = mTaskQueue.get(i);
            broadcastAddTask(task.getUrl());
        }
        for (int i = 0; i < mPausingTasks.size(); i++) {
            task = mPausingTasks.get(i);
            broadcastAddTask(task.getUrl());
        }
    }

    public boolean hasTask(String url) {

        DownloadTask task;
        for (int i = 0; i < mDownloadingTasks.size(); i++) {
            task = mDownloadingTasks.get(i);
            if (task.getUrl().equals(url)) {
                return true;
            }
        }
        for (int i = 0; i < mTaskQueue.size(); i++) {
            task = mTaskQueue.get(i);
        }
        return false;
    }

    public DownloadTask getTask(int position) {

        if (position >= mDownloadingTasks.size()) {
            return mTaskQueue.get(position - mDownloadingTasks.size());
        } else {
            return mDownloadingTasks.get(position);
        }
    }

    public int getQueueTaskCount() {

        return mTaskQueue.size();
    }

    public int getDownloadingTaskCount() {

        return mDownloadingTasks.size();
    }

    public int getPausingTaskCount() {

        return mPausingTasks.size();
    }

    public int getTotalTaskCount() {

        return getQueueTaskCount() + getDownloadingTaskCount() + getPausingTaskCount();
    }

    public void checkUncompleteTasks() {

        List<String> urlList = ConfigUtils.getURLArray(mContext);
        if (urlList.size() >= 0) {
            for (int i = 0; i < urlList.size(); i++) {
                addTask(urlList.get(i));
            }
        }
    }

    public synchronized void pauseTask(String url) {

        DownloadTask task;
        for (int i = 0; i < mDownloadingTasks.size(); i++) {
            task = mDownloadingTasks.get(i);
            if (task != null && task.getUrl().equals(url)) {
                pauseTask(task);
            }
        }
    }

    public synchronized void pauseAllTask() {

        DownloadTask task;

        for (int i = 0; i < mTaskQueue.size(); i++) {
            task = mTaskQueue.get(i);
            mTaskQueue.remove(task);
            mPausingTasks.add(task);
        }

        for (int i = 0; i < mDownloadingTasks.size(); i++) {
            task = mDownloadingTasks.get(i);
            if (task != null) {
                pauseTask(task);
            }
        }
    }

    public synchronized void deleteTask(String url) {

        DownloadTask task;
        for (int i = 0; i < mDownloadingTasks.size(); i++) {
            task = mDownloadingTasks.get(i);
            if (task != null && task.getUrl().equals(url)) {
                File file = new File(DownloadUtils.FILE_ROOT
                        + DownloadUtils.getFileNameFromUrl(task.getUrl()));
                if (file.exists())
                    file.delete();

                task.onCancelled();
                completeTask(task);
                return;
            }
        }
        for (int i = 0; i < mTaskQueue.size(); i++) {
            task = mTaskQueue.get(i);
            if (task != null && task.getUrl().equals(url)) {
                mTaskQueue.remove(task);
            }
        }
        for (int i = 0; i < mPausingTasks.size(); i++) {
            task = mPausingTasks.get(i);
            if (task != null && task.getUrl().equals(url)) {
                mPausingTasks.remove(task);
            }
        }
    }

    public synchronized void continueTask(String url) {

        DownloadTask task;
        for (int i = 0; i < mPausingTasks.size(); i++) {
            task = mPausingTasks.get(i);
            if (task != null && task.getUrl().equals(url)) {
                continueTask(task);
            }

        }
    }

    public synchronized void pauseTask(DownloadTask task) {

        if (task != null) {
            task.onCancelled();

            // move to pausing list
            String url = task.getUrl();
            try {
                mDownloadingTasks.remove(task);
                task = newDownloadTask(url);
                mPausingTasks.add(task);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
    }

    public synchronized void continueTask(DownloadTask task) {

        if (task != null) {
            mPausingTasks.remove(task);
            mTaskQueue.offer(task);
        }
    }

    public synchronized void completeTask(DownloadTask task) {

        if (mDownloadingTasks.contains(task)) {
            ConfigUtils.clearURL(mContext, mDownloadingTasks.indexOf(task));
            mDownloadingTasks.remove(task);

            // notify list changed
            Intent nofityIntent = new Intent(DownloadValues.Actions.BROADCAST_RECEIVER_ACTION);
            nofityIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.COMPLETE);
            nofityIntent.putExtra(DownloadValues.URL, task.getUrl());
            mContext.sendBroadcast(nofityIntent);
        }
    }

    /**
     * Create a new download task with default config
     * 
     * @param url
     * @return
     * @throws MalformedURLException
     */
    private DownloadTask newDownloadTask(String url) throws MalformedURLException {

        DownloadTaskListener taskListener = new DownloadTaskListener() {

            @Override
            public void updateProcess(DownloadTask task) {

                Intent updateIntent = new Intent(DownloadValues.Actions.BROADCAST_RECEIVER_ACTION);
                updateIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.PROCESS);
                updateIntent.putExtra(DownloadValues.PROCESS_SPEED, task.getDownloadSpeed() + "kbps | "
                        + task.getDownloadSize() + " / " + task.getTotalSize());
                updateIntent.putExtra(DownloadValues.PROCESS_PROGRESS, task.getDownloadPercent() + "");
                updateIntent.putExtra(DownloadValues.URL, task.getUrl());
                mContext.sendBroadcast(updateIntent);
            }

            @Override
            public void preDownload(DownloadTask task) {

                ConfigUtils.storeURL(mContext, mDownloadingTasks.indexOf(task), task.getUrl());
            }

            @Override
            public void finishDownload(DownloadTask task) {

                completeTask(task);
            }

            @Override
            public void errorDownload(DownloadTask task, int errorCode, String errorInfo) {
                Intent errorIntent = new Intent(DownloadValues.Actions.BROADCAST_RECEIVER_ACTION);
                errorIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.ERROR);
                errorIntent.putExtra(DownloadValues.ERROR_CODE, errorCode);
                errorIntent.putExtra(DownloadValues.ERROR_INFO, errorInfo);
                errorIntent.putExtra(DownloadValues.URL, task.getUrl());
                mContext.sendBroadcast(errorIntent);
                if (errorCode != DownloadTask.ERROR_BLOCK_INTERNET 
                		&& errorCode != DownloadTask.ERROR_TIME_OUT) {
                	completeTask(task);
                }
            }
        };
        return new DownloadTask(mContext, url, DownloadUtils.FILE_ROOT, taskListener);
    }

    /**
     * A obstructed task queue
     */
    private class TaskQueue {
        private Queue<DownloadTask> taskQueue;

        public TaskQueue() {

            taskQueue = new LinkedList<DownloadTask>();
        }

        public void offer(DownloadTask task) {

            taskQueue.offer(task);
        }

        public DownloadTask poll() {

            DownloadTask task = null;
            while (mDownloadingTasks.size() >= DownloadTask.MAX_DOWNLOAD_THREAD_COUNT
                    || (task = taskQueue.poll()) == null) {
                try {
                    Thread.sleep(1000); // sleep
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return task;
        }

        public DownloadTask get(int position) {

            if (position >= size()) {
                return null;
            }
            return ((LinkedList<DownloadTask>) taskQueue).get(position);
        }

        public int size() {

            return taskQueue.size();
        }

        @SuppressWarnings("unused")
        public boolean remove(int position) {

            return taskQueue.remove(get(position));
        }

        public boolean remove(DownloadTask task) {

            return taskQueue.remove(task);
        }
    }

    
    public interface SDCardAndCountStatusListener {
		void notFoundSDCard(int errorCode, String errorInfo);
		void sdCardCannotWriteOrRead(int errorCode, String errorInfo);
		void moreTaskCount(int errorCode, String errorInfo);
	}
    public void setSDCardAndCountStatusListener(SDCardAndCountStatusListener statusListener) {
    	this.statusListener = statusListener;
    }
}
