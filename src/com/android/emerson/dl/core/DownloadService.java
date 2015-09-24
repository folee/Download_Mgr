package com.android.emerson.dl.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.android.emerson.dl.core.DownloadManager.SDCardAndCountStatusListener;
import com.android.emerson.dl.utils.DLFileInfo;
import com.android.emerson.dl.utils.DownloadValues;

public class DownloadService extends Service {

	private DownloadManager	mDownloadManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDownloadManager = DownloadManager.getInstance(this);
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
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction().equals(DownloadValues.Actions.DOWNLOAD_SERVICE_ACTION)) {
			int type = intent.getIntExtra(DownloadValues.TYPE, -1);

			DLFileInfo dLFileInfo = intent.getParcelableExtra(DownloadValues.APPINFO);
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
				if (!TextUtils.isEmpty(dLFileInfo.getFileUrl()) && !mDownloadManager.hasTask(dLFileInfo.getFileUrl())) {
					mDownloadManager.addTask(dLFileInfo);
				}
				break;
			case DownloadValues.Types.CONTINUE:
				if (!TextUtils.isEmpty(dLFileInfo.getFileUrl())) {
					mDownloadManager.continueTask(dLFileInfo);
				}
				break;
			case DownloadValues.Types.DELETE:
				if (!TextUtils.isEmpty(dLFileInfo.getFileUrl())) {
					mDownloadManager.deleteTask(dLFileInfo);
				}
				break;
			case DownloadValues.Types.PAUSE:
				if (!TextUtils.isEmpty(dLFileInfo.getFileUrl())) {
					mDownloadManager.pauseTask(dLFileInfo);
				}
				break;
			case DownloadValues.Types.STOP:
				mDownloadManager.close();
				stopSelf();
				break;
			default:
				break;
			}
		}
		return START_FLAG_REDELIVERY;
	}

}
