package com.download.main;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.emerson.dl.core.DownloadHelper;
import com.android.emerson.dl.core.DownloadHelper.PreDownloadStatusListener;
import com.android.emerson.dl.core.DownloadReceiver;
import com.android.emerson.dl.core.DownloadReceiver.DownloadErrorListener;
import com.android.emerson.dl.core.DownloadReceiver.DownloadListener;
import com.android.emerson.dl.core.DownloadTask;
import com.android.emerson.dl.utils.ConfigUtils;
import com.android.emerson.dl.utils.DLFileInfo;
import com.android.emerson.dl.utils.DownloadValues;

public class DownloadListActivity extends Activity {

	private String TAG = DownloadListActivity.class.getSimpleName();
	private ListView			downloadList;
	private Button				addButton;

	private DownloadListAdapter	downloadListAdapter;
	private DownloadReceiver	mReceiver;

	private int					urlIndex	= 0;
	
	private DLFileInfo getNewDlFile(){
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

		addButton = (Button) findViewById(R.id.btn_add);
		addButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DownloadHelper.addNewTask(DownloadListActivity.this, getNewDlFile(),
						new PreDownloadStatusListener() {

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

		mReceiver = new DownloadReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(DownloadValues.Actions.BROADCAST_RECEIVER_ACTION);
		registerReceiver(mReceiver, filter);

		mReceiver.setDownloadListener(new DownloadListener() {

			@Override
			public void handleAddAction(DLFileInfo dLFileInfo, Intent intent, boolean isPaused) {
				Log.v(TAG, "add new download task");
				if (!TextUtils.isEmpty(dLFileInfo.getFileUrl())) {
					downloadListAdapter.addItem(dLFileInfo, isPaused);
				}
			}

			@Override
			public void handleCompletedAction(DLFileInfo dLFileInfo, Intent intent) {
				Log.v(TAG, "download completed");
				if (!TextUtils.isEmpty(dLFileInfo.getFileUrl())) {
					downloadListAdapter.removeItem(dLFileInfo);
				}
			}

			@Override
			public void handleProgress(DLFileInfo dLFileInfo, Intent intent) {
				View taskListItem = downloadList.findViewWithTag(dLFileInfo.getFileUrl());
				ViewHolder viewHolder = new ViewHolder(taskListItem);
				
//				intent.getStringExtra(DownloadValues.PROCESS_SPEED)
//				intent.getStringExtra(DownloadValues.PROCESS_PROGRESS)
				
				dLFileInfo.setSpeed(intent.getStringExtra(DownloadValues.PROCESS_SPEED));
				dLFileInfo.setProgress(intent.getStringExtra(DownloadValues.PROCESS_PROGRESS));
				viewHolder.setData(dLFileInfo);
			}
		});
		mReceiver.setDownloadErrorListener(new DownloadErrorListener() {
			@Override
			public void downloadErrorActions(DLFileInfo dLFileInfo, int errorCode, String errorInfo) {
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
		});
	}

	@Override
	protected void onDestroy() {

		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
}
