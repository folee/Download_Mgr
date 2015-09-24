package com.download.main;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.emerson.dl.utils.DLFileInfo;
import com.android.emerson.dl.utils.DownloadUtils;

public class ViewHolder {

//	public static final int	KEY_URL			= 0;
//	public static final int	KEY_SPEED		= 1;
//	public static final int	KEY_PROGRESS	= 2;
//	public static final int	KEY_IS_PAUSED	= 3;

	public TextView			titleText;
	public ProgressBar		progressBar;
	public TextView			speedText;
	public Button			pauseButton;
	public Button			deleteButton;
	public Button			continueButton;

	private boolean			hasInited		= false;

	public ViewHolder(View parentView) {
		if (parentView != null) {
			titleText = (TextView) parentView.findViewById(R.id.title);
			speedText = (TextView) parentView.findViewById(R.id.speed);
			progressBar = (ProgressBar) parentView.findViewById(R.id.progress_bar);
			pauseButton = (Button) parentView.findViewById(R.id.btn_pause);
			deleteButton = (Button) parentView.findViewById(R.id.btn_delete);
			continueButton = (Button) parentView.findViewById(R.id.btn_continue);
			hasInited = true;
		}
	}

//	public static HashMap<Integer, DLFileInfo> getItemDataMap(DLFileInfo dLFileInfo) {
//		HashMap<Integer, DLFileInfo> item = new HashMap<Integer, DLFileInfo>();
//		item.put(KEY_URL, dLFileInfo);
//		return item;
//	}

	public void setData(DLFileInfo dLFileInfo) {
		if (hasInited) {
			titleText.setText(DownloadUtils.getFileNameFromUrl(dLFileInfo.getFileUrl()));
			//speedText.setText(dLFileInfo.getSpeed());
			String progress = dLFileInfo.getProgress() + "";
			if (TextUtils.isEmpty(progress)) {
				progressBar.setProgress(0);
			}
			else {
				progressBar.setProgress(Integer.parseInt(progress));
			}
			if (dLFileInfo.getStatus() == DLFileInfo.STATUS_DOWNLOAD_STOP) {
				onPause();
			}
		}
	}

	public void onPause() {
		if (hasInited) {
			pauseButton.setVisibility(View.GONE);
			continueButton.setVisibility(View.VISIBLE);
		}
	}

//	public void setData(String url, String speed, String progress) {
//		setData(url, speed, progress, false + "");
//	}
//
//	public void setData(String url, String speed, String progress, String isPaused) {
//		if (hasInited) {
//			HashMap<Integer, String> item = getItemDataMap(url, speed, progress, isPaused);
//
//			titleText.setText(DownloadUtils.getFileNameFromUrl(item.get(KEY_URL)));
//			speedText.setText(speed);
//			if (TextUtils.isEmpty(progress)) {
//				progressBar.setProgress(0);
//			}
//			else {
//				progressBar.setProgress(Integer.parseInt(item.get(KEY_PROGRESS)));
//			}
//
//		}
//	}

	// public void bindTask(DownloadTask task) {
	// if (hasInited) {
	// titleText.setText(DownloadUtils.getFileNameFromUrl(task.getUrl()));
	// speedText.setText(task.getDownloadSpeed() + "kbps | "
	// + task.getDownloadSize() + " / " + task.getTotalSize());
	// progressBar.setProgress((int) task.getDownloadPercent());
	// if (task.isInterrupt()) {
	// onPause();
	// }
	// }
	// }

}
