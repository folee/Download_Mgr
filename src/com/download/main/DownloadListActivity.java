package com.download.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.emerson.dl.core.DownloadHelper;
import com.android.emerson.dl.core.DownloadHelper.PreDownloadStatusListener;
import com.android.emerson.dl.core.DownloadManager;
import com.android.emerson.dl.core.DownloadTask;
import com.android.emerson.dl.utils.ConfigUtils;
import com.android.emerson.dl.utils.DLFileInfo;
import com.android.emerson.dl.utils.DownloadValues;

public class DownloadListActivity extends Activity {

	private String				TAG			= DownloadListActivity.class.getSimpleName();
	private ListView			downloadList;
	private Button				addButton;

	private DownloadListAdapter	downloadListAdapter;

	private int					urlIndex	= 0;

	private Handler				mHandler	= new Handler() {
												@Override
												public void handleMessage(Message msg) {
													DownloadTask task = null;
													if (msg.obj instanceof DownloadTask) {
														task = (DownloadTask) msg.obj;
													}
													switch (msg.what) {
													case DownloadValues.Types.ADD:
														handleAdd(task.getDLFileInfo());
														break;
													case DownloadValues.Types.COMPLETE:
														handleCompletedAction(task.getDLFileInfo());
														break;
													case DownloadValues.Types.PROCESS:
														handleProgress(task.getDLFileInfo());
														break;
													case DownloadValues.Types.ERROR:
														handleError(task.getDLFileInfo(), task.errorCode, task.errorInfo);
														break;
													default:
														break;
													}
												}
											};

	/**
	 * 添加下载文件
	 * 
	 * @param dLFileInfo
	 * @see [类、类#方法、类#成员]
	 */
	private void handleAdd(DLFileInfo dLFileInfo) {
		String filePath = dLFileInfo.getFilePath() + dLFileInfo.getFileName();
		Log.d(TAG, "Download AddAction ---> filePath = " + filePath);
		Log.v(TAG, "add new download task");
		if (!TextUtils.isEmpty(dLFileInfo.getFileUrl())) {
			downloadListAdapter.addItem(dLFileInfo);
		}
	}

	/**
	 * 文件下载成功
	 * 
	 * @param dLFileInfo
	 * @see [类、类#方法、类#成员]
	 */
	private void handleCompletedAction(DLFileInfo dLFileInfo) {
		String filePath = dLFileInfo.getFilePath() + dLFileInfo.getFileName();
		Log.d(TAG, "Download CompletedAction ---> filePath = " + filePath);
		Toast.makeText(DownloadListActivity.this, "Download completed", Toast.LENGTH_SHORT).show();
		if (!TextUtils.isEmpty(dLFileInfo.getFileUrl())) {
			downloadListAdapter.removeItem(dLFileInfo);
		}

	}

	/**
	 * 
	 * 更新当前apk下载进度
	 * 
	 * @param dLFileInfo
	 * @param speed
	 * @param progress
	 * @see [类、类#方法、类#成员]
	 */
	private void handleProgress(DLFileInfo dLFileInfo) {
		//String filePath = dLFileInfo.getFilePath() + dLFileInfo.getFileName();
		//Log.d(TAG, "Download ProgressAction ---> filePath = " + filePath);
		View taskListItem = downloadList.findViewWithTag(dLFileInfo.getFileUrl());
		ViewHolder viewHolder = new ViewHolder(taskListItem);
		viewHolder.setData(dLFileInfo);
	}

	private void handleError(DLFileInfo dLFileInfo, int errorCode, String errorInfo) {
		String filePath = dLFileInfo.getFilePath() + dLFileInfo.getFileName();
		Log.d(TAG, "Download ErrorAction ---> filePath = " + filePath);
		
		switch (errorCode) {
		case DownloadTask.ERROR_UNKONW:
			Toast.makeText(DownloadListActivity.this, errorInfo, Toast.LENGTH_SHORT).show();
			break;
		case DownloadTask.ERROR_BLOCK_INTERNET:
			Toast.makeText(DownloadListActivity.this, errorInfo, Toast.LENGTH_SHORT).show();
			View taskListItem = downloadList.findViewWithTag(dLFileInfo.getFileUrl());
			ViewHolder viewHolder = new ViewHolder(taskListItem);
			viewHolder.onPause();
			break;
		case DownloadTask.ERROR_TIME_OUT:
			Toast.makeText(DownloadListActivity.this, errorInfo, Toast.LENGTH_SHORT).show();
			View timeoutItem = downloadList.findViewWithTag(dLFileInfo.getFileUrl());
			ViewHolder timeoutHolder = new ViewHolder(timeoutItem);
			timeoutHolder.onPause();
			break;
		case DownloadTask.ERROR_FILE_EXIST:
			Toast.makeText(DownloadListActivity.this, errorInfo, Toast.LENGTH_SHORT).show();
			break;
		case DownloadTask.ERROR_SD_NO_MEMORY:
			Toast.makeText(DownloadListActivity.this, errorInfo, Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private DLFileInfo getNewDlFile() {
		DLFileInfo dLFileInfo = new DLFileInfo();
		dLFileInfo.setFilePath(ConfigUtils.FILE_PATH);
		dLFileInfo.setFileUrl(DownloadValues.url[urlIndex]);
		dLFileInfo.setFileType("apk");
		dLFileInfo.setFileName(Integer.toHexString(dLFileInfo.getFileUrl().hashCode()) + "." + dLFileInfo.getFileType());

		return dLFileInfo;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_list_activity);
		ConfigUtils.InitPath(this);
		downloadList = (ListView) findViewById(R.id.download_list);
		downloadListAdapter = new DownloadListAdapter(this);
		downloadList.setAdapter(downloadListAdapter);
		//将当前类的Handler传入下载管理类，在页面销毁是移除
		DownloadManager.getInstance(getApplication()).addHandler(mHandler);
		
		addButton = (Button) findViewById(R.id.btn_add);
		addButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DownloadHelper.addNewTask(DownloadListActivity.this, getNewDlFile(), new PreDownloadStatusListener() {

					@Override
					public void notFoundSDCard(int errorCode, String errorInfo) {
						Toast.makeText(DownloadListActivity.this, "没有SD卡", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void sdCardCannotWriteOrRead(int errorCode, String errorInfo) {
						Toast.makeText(DownloadListActivity.this, "不能读写SD卡", Toast.LENGTH_SHORT).show();

					}

					@Override
					public void moreTaskCount(int errorCode, String errorInfo) {
						Toast.makeText(DownloadListActivity.this, "任务列表已满", Toast.LENGTH_SHORT).show();
					}

				});

				urlIndex++;
				if (urlIndex >= DownloadValues.url.length) {
					urlIndex = 0;
				}
			}
		});

		DownloadHelper.startAllTask(DownloadListActivity.this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//在页面销毁时移除
		DownloadManager.getInstance(getApplication()).clearHandler(mHandler);
	}
	
}
