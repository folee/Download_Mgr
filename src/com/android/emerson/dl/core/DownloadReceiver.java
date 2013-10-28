package com.android.emerson.dl.core;

import com.android.emerson.dl.utils.DownloadValues;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadReceiver extends BroadcastReceiver {
	public DownloadListener      downloadListener = null;
	public DownloadErrorListener downloadErrorListener = null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		handleIntent(context, intent);
	}
	
	private void handleIntent(Context context, Intent intent) {

        if (intent != null
                && intent.getAction().equals(DownloadValues.Actions.BROADCAST_RECEIVER_ACTION)) {
            int type = intent.getIntExtra(DownloadValues.TYPE, -1);
            String url;

            switch (type) {
                case DownloadValues.Types.ADD:
                    url = intent.getStringExtra(DownloadValues.URL);
                    boolean isPaused = intent.getBooleanExtra(DownloadValues.IS_PAUSED, false);
                    if(downloadListener != null)
                    	downloadListener.handleAddAction(url, intent, isPaused);
                    break;
                case DownloadValues.Types.COMPLETE:
                    url = intent.getStringExtra(DownloadValues.URL);
                    if(downloadListener != null)
                    	downloadListener.handleCompletedAction(url, intent);
                    break;
                case DownloadValues.Types.PROCESS:
                    url = intent.getStringExtra(DownloadValues.URL);
                    if(downloadListener != null)
                    	downloadListener.handleProgress(url, intent);
                    break;
                case DownloadValues.Types.ERROR:
                    url = intent.getStringExtra(DownloadValues.URL);
                    int errorCode = intent.getIntExtra(DownloadValues.ERROR_CODE, DownloadTask.ERROR_UNCATCHED);
                    String errorInfo = getErrorInfo(errorCode);
                    handleError(url, errorCode, errorInfo);
                    break;
                default:
                    break;
            }
        }
    }
	
	private void handleError(String url, int errorCode, String errorInfo) {
		if(downloadErrorListener != null) {
			downloadErrorListener.downloadErrorActions(url, errorCode, errorInfo);
		}
    }
	
	private String getErrorInfo(int errorCode) {
		String errorInfo = "";
		switch(errorCode){
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
		void handleAddAction(String url, Intent intent, boolean isPaused);
		void handleCompletedAction(String url, Intent intent);
		void handleProgress(String url, Intent intent);
	}
	public void setDownloadListener(DownloadListener downloadListener){
		this.downloadListener = downloadListener;
	}
	
	public interface DownloadErrorListener {
		void downloadErrorActions(String url, int errorCode, String errorInfo);
	}
	public void setDownloadErrorListener(DownloadErrorListener downloadErrorListener){
		this.downloadErrorListener = downloadErrorListener;
	}
}
