package com.android.emerson.dl.core;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.android.emerson.dl.utils.ConfigUtils;
import com.android.emerson.dl.utils.DLFileInfo;
import com.android.emerson.dl.utils.DownloadValues;

public class DownloadManager extends Thread {
	private SDCardAndCountStatusListener	statusListener	= null;

	private Context							mContext;

	private TaskQueue						mTaskQueue;
	private List<DownloadTask>				mDownloadingTasks;
	private List<DownloadTask>				mPausingTasks;

	private Boolean							isRunning		= false;
	// 固定五个线程来执行任务
	private ExecutorService					executorService	= Executors.newFixedThreadPool(5);
	private static DownloadManager			instance;
	
	/**
	 * 文件下载的Handler
	 */
	public List<Handler>			mDLHandler			= new ArrayList<Handler>();

	private static final String				Thread_Name		= "DownloadManager_Handler_Thread";
	private Handler							mHandler;

	private void procHandler() {
		HandlerThread mHandlerThread =  new HandlerThread(Thread_Name);
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				for (Handler handler : mDLHandler) {
					if(handler != null)
						handler.sendMessage(Message.obtain(handler, msg.what, msg.obj));
				}
			}
		};
	}

	/**
	 * 单一实例
	 */
	public static DownloadManager getInstance(Context context) {
		if (instance == null) {
			instance = new DownloadManager(context);
			ConfigUtils.InitPath(context);
		}
		return instance;
	}

	private DownloadManager(Context context) {
		mContext = context;
		mTaskQueue = new TaskQueue();
		mDownloadingTasks = new ArrayList<DownloadTask>();
		mPausingTasks = new ArrayList<DownloadTask>();
		procHandler();
	}
	
	public void addHandler(Handler handler) {
		Log.v("DownloadManager", "addHandler() ---> Before mDLHandler.size = " + mDLHandler.size());
		if (!mDLHandler.contains(handler)) {
			mDLHandler.add(handler);
			Log.v("DownloadManager", "addHandler() ---> After mDLHandler.size = " + mDLHandler.size());
		}
	}

	public void clearHandler(Handler handler) {
		Log.v("DownloadManager", "clearHandler() ---> Before mDLHandler.size = " + mDLHandler.size());
		if (mDLHandler.contains(handler)) {
			mDLHandler.remove(handler);
			Log.v("DownloadManager", "clearHandler() ---> After mDLHandler.size = " + mDLHandler.size());
		}
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
			//task.execute();
			executorService.submit(task);
		}
	}

	public void addTask(DLFileInfo dLFileInfo) {
		//		if (!DownloadUtils.isSDCardPresent()) {
		//			if (statusListener != null)
		//				statusListener.notFoundSDCard(DownloadTask.ERROR_NOT_FOUND_SDCARD, DownloadTask.ERROR_NOT_FOUND_SDCARD_INFO);
		//			return;
		//		}
		//
		//		if (!DownloadUtils.isSdCardWrittenable()) {
		//			if (statusListener != null)
		//				statusListener.sdCardCannotWriteOrRead(DownloadTask.ERROR_CANNOT_WRITEORREAD_SDCARD, DownloadTask.ERROR_CANNOT_WRITEORREAD_SDCARD_INFO);
		//			return;
		//		}
		//
		//		if (getTotalTaskCount() >= DownloadTask.MAX_TASK_COUNT) {
		//			if (statusListener != null)
		//				statusListener.moreTaskCount(DownloadTask.ERROR_MORE_TASK_COUNT, DownloadTask.ERROR_MORE_TASK_COUNT_INFO);
		//			return;
		//		}

		//		try {
		//			ConfigUtils.mkdir();
		//		} catch (IOException e1) {
		//			e1.printStackTrace();
		//		}

		try {
			addTask(newDownloadTask(dLFileInfo));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	private void addTask(DownloadTask task) {
		if(mHandler == null){
			procHandler();
		}
		
		mHandler.sendMessage(Message.obtain(mHandler, DownloadValues.Types.ADD, task));

		mTaskQueue.offer(task);

		if (!this.isAlive()) {
			//this.startManage();
			isRunning = true;
			this.start();
		}
	}

	private void broadcastAddTask(DLFileInfo dLFileInfo) {
		broadcastAddTask(dLFileInfo, false);
	}

	private void broadcastAddTask(DLFileInfo dLFileInfo, boolean isInterrupt) {

		Intent nofityIntent = new Intent(DownloadValues.Actions.BROADCAST_RECEIVER_ACTION);
		nofityIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.ADD);
		nofityIntent.putExtra(DownloadValues.APPINFO, dLFileInfo);
		nofityIntent.putExtra(DownloadValues.IS_PAUSED, isInterrupt);
		mContext.sendBroadcast(nofityIntent);
	}

	public void reBroadcastAddAllTask() {

		DownloadTask task;
		for (int i = 0; i < mDownloadingTasks.size(); i++) {
			task = mDownloadingTasks.get(i);
			broadcastAddTask(task.getDLFileInfo(), task.isInterrupt());
		}
		for (int i = 0; i < mTaskQueue.size(); i++) {
			task = mTaskQueue.get(i);
			broadcastAddTask(task.getDLFileInfo());
		}
		for (int i = 0; i < mPausingTasks.size(); i++) {
			task = mPausingTasks.get(i);
			broadcastAddTask(task.getDLFileInfo());
		}
	}

	public boolean hasTask(String url) {

		DownloadTask task;
		for (int i = 0; i < mDownloadingTasks.size(); i++) {
			task = mDownloadingTasks.get(i);
			if (task.getDLFileInfo().getFileUrl().equals(url)) {
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
		}
		else {
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

	public List<DLFileInfo> checkUncompleteTasks() {

		List<DLFileInfo> dlApkList = ConfigUtils.getURLArray(mContext);
		if (dlApkList.size() >= 0) {
			for (int i = 0; i < dlApkList.size(); i++) {
				DLFileInfo dLFileInfo = dlApkList.get(i);
				if (!TextUtils.isEmpty(dLFileInfo.getFileUrl()) && !hasTask(dLFileInfo.getFileUrl())) {
					addTask(dLFileInfo);
				}
				//addTask(urlList.get(i));
			}
		}

		return dlApkList;
	}

	public synchronized void pauseTask(DLFileInfo dLFileInfo) {

		DownloadTask task;
		for (int i = 0; i < mDownloadingTasks.size(); i++) {
			task = mDownloadingTasks.get(i);
			if (task != null && task.getDLFileInfo().getFileUrl().equals(dLFileInfo.getFileUrl())) {
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

	public synchronized void deleteTask(DLFileInfo dLFileInfo) {

		DownloadTask task;
		for (int i = 0; i < mDownloadingTasks.size(); i++) {
			task = mDownloadingTasks.get(i);
			if (task != null && task.getDLFileInfo().getFileUrl().equals(dLFileInfo.getFileUrl())) {
				File file = new File(dLFileInfo.getFilePath() + dLFileInfo.getFileName());
				if (file.exists())
					file.delete();

				task.onCancelled();
				completeTask(task);
				return;
			}
		}
		for (int i = 0; i < mTaskQueue.size(); i++) {
			task = mTaskQueue.get(i);
			if (task != null && task.getDLFileInfo().getFileUrl().equals(dLFileInfo.getFileUrl())) {
				mTaskQueue.remove(task);
			}
		}
		for (int i = 0; i < mPausingTasks.size(); i++) {
			task = mPausingTasks.get(i);
			if (task != null && task.getDLFileInfo().getFileUrl().equals(dLFileInfo.getFileUrl())) {
				mPausingTasks.remove(task);
			}
		}

		String index = Integer.toHexString(dLFileInfo.getFileUrl().hashCode());
		ConfigUtils.clearURL(mContext, index);
	}

	public synchronized void continueTask(DLFileInfo dLFileInfo) {

		DownloadTask task;
		for (int i = 0; i < mPausingTasks.size(); i++) {
			task = mPausingTasks.get(i);
			if (task != null && task.getDLFileInfo().getFileUrl().equals(dLFileInfo.getFileUrl())) {
				continueTask(task);
			}

		}
	}

	public synchronized void pauseTask(DownloadTask task) {

		if (task != null) {
			task.onCancelled();

			// move to pausing list
			try {
				mDownloadingTasks.remove(task);
				task = newDownloadTask(task.getDLFileInfo());
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
			Log.v("DownloadManager", "completeTask() --> ");
			String index = Integer.toHexString(task.getDLFileInfo().getFileUrl().hashCode());
			task.getDLFileInfo().setStatus(DLFileInfo.STATUS_DOWNLOAD_END);
			ConfigUtils.storeURL(mContext, index, task.getDLFileInfo());
			mDownloadingTasks.remove(task);
			mHandler.sendMessage(Message.obtain(mHandler, DownloadValues.Types.COMPLETE, task));
		}
		else {
			Log.v("DownloadManager", "DownloadManager completeTask() -->  mDownloadingTasks.contains(task) = False");
		}
	}

	/**
	 * Create a new download task with default config
	 * 
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	private DownloadTask newDownloadTask(DLFileInfo dLFileInfo) throws MalformedURLException {

		DownloadTaskListener taskListener = new DownloadTaskListener() {

			@Override
			public void updateProcess(DownloadTask task) {
				mHandler.sendMessage(Message.obtain(mHandler, DownloadValues.Types.PROCESS, task));
			}

			@Override
			public void preDownload(DownloadTask task) {
				String index = Integer.toHexString(task.getDLFileInfo().getFileUrl().hashCode());
				ConfigUtils.storeURL(mContext, index, task.getDLFileInfo());
				mHandler.sendMessage(Message.obtain(mHandler, DownloadValues.Types.ADD, task));
			}

			@Override
			public void finishDownload(DownloadTask task) {
				Log.v("DownloadManager", "finishDownload() --> ");
				completeTask(task);
			}

			@Override
			public void errorDownload(DownloadTask task, int errorCode, String errorInfo) {
				Intent errorIntent = new Intent(DownloadValues.Actions.BROADCAST_RECEIVER_ACTION);
				errorIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.ERROR);
				errorIntent.putExtra(DownloadValues.ERROR_CODE, errorCode);
				errorIntent.putExtra(DownloadValues.ERROR_INFO, errorInfo);
				errorIntent.putExtra(DownloadValues.APPINFO, task.getDLFileInfo());
				mContext.sendBroadcast(errorIntent);
				if (errorCode != DownloadTask.ERROR_BLOCK_INTERNET && errorCode != DownloadTask.ERROR_TIME_OUT) {
					completeTask(task);
				}
				
				mHandler.sendMessage(Message.obtain(mHandler, DownloadValues.Types.ERROR, task));
			}
		};
		return new DownloadTask(mContext, dLFileInfo, taskListener);
	}

	/**
	 * A obstructed task queue
	 */
	private class TaskQueue {
		private Queue<DownloadTask>	taskQueue;

		public TaskQueue() {

			taskQueue = new LinkedList<DownloadTask>();
		}

		public void offer(DownloadTask task) {

			taskQueue.offer(task);
		}

		public DownloadTask poll() {

			DownloadTask task = null;
			while (mDownloadingTasks.size() >= DownloadTask.MAX_DOWNLOAD_THREAD_COUNT || (task = taskQueue.poll()) == null) {
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
