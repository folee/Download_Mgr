package com.android.emerson.dl.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.emerson.dl.utils.DLFileInfo;
import com.android.emerson.dl.utils.DownloadValues;


public class DownloadReceiver extends BroadcastReceiver {
	public DownloadListener			downloadListener		= null;
	public DownloadErrorListener	downloadErrorListener	= null;

	@Override
	public void onReceive(Context context, Intent intent) {
		handleIntent(context, intent);
	}

	private void handleIntent(Context context, Intent intent) {

		if (intent != null && intent.getAction().equals(DownloadValues.Actions.BROADCAST_RECEIVER_ACTION)) {
			int type = intent.getIntExtra(DownloadValues.TYPE, -1);
			DLFileInfo dLFileInfo = intent.getParcelableExtra(DownloadValues.APPINFO);

			switch (type) {
			case DownloadValues.Types.ADD:
				boolean isPaused = intent.getBooleanExtra(DownloadValues.IS_PAUSED, false);
				if (downloadListener != null)
					downloadListener.handleAddAction(dLFileInfo, intent, isPaused);
				break;
			case DownloadValues.Types.COMPLETE:
				if (downloadListener != null)
					downloadListener.handleCompletedAction(dLFileInfo, intent);
				break;
			case DownloadValues.Types.PROCESS:
				if (downloadListener != null)
					downloadListener.handleProgress(dLFileInfo, intent);
				break;
			case DownloadValues.Types.ERROR:
				int errorCode = intent.getIntExtra(DownloadValues.ERROR_CODE, DownloadTask.ERROR_UNCATCHED);
				String errorInfo = getErrorInfo(errorCode);
				handleError(dLFileInfo, errorCode, errorInfo);
				break;
			default:
				break;
			}
		}
	}

	private void handleError(DLFileInfo dLFileInfo, int errorCode, String errorInfo) {
		if (downloadErrorListener != null) {
			downloadErrorListener.downloadErrorActions(dLFileInfo, errorCode, errorInfo);
		}
	}

	private String getErrorInfo(int errorCode) {
		String errorInfo = "";
		switch (errorCode) {
		case DownloadTask.ERROR_UNKONW:
			errorInfo = DownloadTask.ERROR_UNKONW_INFO;
			break;
		case DownloadTask.ERROR_BLOCK_INTERNET:
			errorInfo = DownloadTask.ERROR_BLOCK_INTERNET_INFO;
			break;
		case DownloadTask.ERROR_TIME_OUT:
			errorInfo = DownloadTask.ERROR_TIME_OUT_INFO;
			break;
		case DownloadTask.ERROR_FILE_EXIST:
			errorInfo = DownloadTask.ERROR_FILE_EXIST_INFO;
			break;
		case DownloadTask.ERROR_SD_NO_MEMORY:
			errorInfo = DownloadTask.ERROR_SD_NO_MEMORY_INFO;
			break;
		}
		return errorInfo;
	}

	public interface DownloadListener {
		void handleAddAction(DLFileInfo dLFileInfo, Intent intent, boolean isPaused);

		void handleCompletedAction(DLFileInfo dLFileInfo, Intent intent);

		void handleProgress(DLFileInfo dLFileInfo, Intent intent);
	}

	public void setDownloadListener(DownloadListener downloadListener) {
		this.downloadListener = downloadListener;
	}

	public interface DownloadErrorListener {
		void downloadErrorActions(DLFileInfo dLFileInfo, int errorCode, String errorInfo);
	}

	public void setDownloadErrorListener(DownloadErrorListener downloadErrorListener) {
		this.downloadErrorListener = downloadErrorListener;
	}
}
