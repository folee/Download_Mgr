package com.android.emerson.dl.core;

import com.android.emerson.dl.core.DownloadManager.SDCardAndCountStatusListener;
import com.android.emerson.dl.utils.DownloadValues;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

public class DownloadService extends Service {

	private DownloadManager	mDownloadManager;

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDownloadManager = new DownloadManager(this);
		mDownloadManager.setSDCardAndCountStatusListener(new SDCardAndCountStatusListener() {
			@Override
			public void notFoundSDCard(int errorCode, String errorInfo) {
				if (DownloadHelper.preStatusListener != null) {
					DownloadHelper.preStatusListener.notFoundSDCard(errorCode, errorInfo);
				}
			}

			@Override
			public void sdCardCannotWriteOrRead(int errorCode, String errorInfo) {
				if (DownloadHelper.preStatusListener != null) {
					DownloadHelper.preStatusListener.sdCardCannotWriteOrRead(errorCode, errorInfo);
				}
			}

			@Override
			public void moreTaskCount(int errorCode, String errorInfo) {
				if (DownloadHelper.preStatusListener != null) {
					DownloadHelper.preStatusListener.moreTaskCount(errorCode, errorInfo);
				}
			}
		});
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (intent != null && intent.getAction().equals(DownloadValues.Actions.DOWNLOAD_SERVICE_ACTION)) {
			int type = intent.getIntExtra(DownloadValues.TYPE, -1);
			String url;

			switch (type) {
			case DownloadValues.Types.START:
				if (!mDownloadManager.isRunning()) {
					mDownloadManager.startManage();
				}
				else {
					mDownloadManager.reBroadcastAddAllTask();
				}
				break;
			case DownloadValues.Types.ADD:
				url = intent.getStringExtra(DownloadValues.URL);
				if (!TextUtils.isEmpty(url) && !mDownloadManager.hasTask(url)) {
					mDownloadManager.addTask(url);
				}
				break;
			case DownloadValues.Types.CONTINUE:
				url = intent.getStringExtra(DownloadValues.URL);
				if (!TextUtils.isEmpty(url)) {
					mDownloadManager.continueTask(url);
				}
				break;
			case DownloadValues.Types.DELETE:
				url = intent.getStringExtra(DownloadValues.URL);
				if (!TextUtils.isEmpty(url)) {
					mDownloadManager.deleteTask(url);
				}
				break;
			case DownloadValues.Types.PAUSE:
				url = intent.getStringExtra(DownloadValues.URL);
				if (!TextUtils.isEmpty(url)) {
					mDownloadManager.pauseTask(url);
				}
				break;
			case DownloadValues.Types.STOP:
				mDownloadManager.close();
				break;
			default:
				break;
			}
		}
	}
}
