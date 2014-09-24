package com.android.emerson.dl.core;

import android.content.Context;
import android.content.Intent;

import com.android.emerson.dl.utils.DLFileInfo;
import com.android.emerson.dl.utils.DownloadValues;

public class DownloadHelper {
	public static PreDownloadStatusListener	preStatusListener	= null;

	public static void startAllTask(Context context) {
		Intent downloadIntent = new Intent(DownloadValues.Actions.DOWNLOAD_SERVICE_ACTION);
		downloadIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.START);
		context.startService(downloadIntent);
	}

	public static void addNewTask(Context context, DLFileInfo dLFileInfo, PreDownloadStatusListener preListener) {
		preStatusListener = preListener;
		Intent downloadIntent = new Intent(DownloadValues.Actions.DOWNLOAD_SERVICE_ACTION);
		downloadIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.ADD);
		downloadIntent.putExtra(DownloadValues.APPINFO, dLFileInfo);
		context.startService(downloadIntent);
	}
	
	public static void stopAllTask(Context context){
		Intent downloadIntent = new Intent(DownloadValues.Actions.DOWNLOAD_SERVICE_ACTION);
		downloadIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.STOP);
		context.startService(downloadIntent);
	}

	/**
	 * 下载连接超时时长设置,默认为3000ms.({@link DownloadTask.TIME_OUT})
	 * 
	 * @param time
	 *            下载URL连接超时时长(ms)
	 */
	public void setDownloadTimeOut(int time) {
		DownloadTask.TIME_OUT = time;
	}

	/**
	 * 下载缓冲区长度设置,默认大小为1024 * 8.({@link DownloadTask.BUFFER_SIZE})
	 * 
	 * @param bufferSize
	 *            下载缓冲区大小
	 */
	public void setDownloadBufferSize(int bufferSize) {
		DownloadTask.BUFFER_SIZE = bufferSize;
	}

	/**
	 * 是否打开下载DEBUG调试模式，默认打开.({@link DownloadTask.DEBUG})
	 * 
	 * @param isDebug
	 */
	public void openDownloadDebug(boolean isDebug) {
		DownloadTask.DEBUG = isDebug;
	}

	/**
	 * 设置下载任务队列长度,默认为100.({@link DownloadTask.MAX_TASK_COUNT})
	 * 
	 * @param maxCount
	 *            任务队列最大长度
	 */
	public void setMaxTaskCount(int maxCount) {
		DownloadTask.MAX_TASK_COUNT = maxCount;
	}

	/**
	 * 设置最大允许下载线程同时运行的个数,默认为1.({@link DownloadTask.MAX_DOWNLOAD_THREAD_COUNT} )
	 * 
	 * @param threadCount
	 *            最大同时下载线程数
	 */
	public void setMaxDownloadThreadCount(int threadCount) {
		DownloadTask.MAX_DOWNLOAD_THREAD_COUNT = threadCount;
	}

	/**
	 * 下载条件是否满足的监听类.({@link DownloadService.onCreate})</br>
	 * 下载前检查SD卡是否存在和是否允许读写，以及检查下载任务队列是否已满.</br> SD卡不存在或没有读写权限或任务已满将不进行下载
	 */
	public interface PreDownloadStatusListener {
		void notFoundSDCard(int errorCode, String errorInfo);

		void sdCardCannotWriteOrRead(int errorCode, String errorInfo);

		void moreTaskCount(int errorCode, String errorInfo);
	}
}
