package com.download.main;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.emerson.dl.utils.DLFileInfo;
import com.android.emerson.dl.utils.DownloadValues;

public class DownloadListAdapter extends BaseAdapter {

	private Context								mContext;
	private ArrayList< DLFileInfo>	dataList;

	public DownloadListAdapter(Context context) {
		mContext = context;
		dataList = new ArrayList<DLFileInfo>();
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addItem(DLFileInfo dLFileInfo) {
		addItem(dLFileInfo, false);
	}

	public void addItem(DLFileInfo dLFileInfo, boolean isPaused) {
		boolean isExist = false;
		for(DLFileInfo info : dataList){
			if(info.getFileUrl().equals(dLFileInfo.getFileUrl())){
				isExist = true;
				break;
			}
		}
		if(!isExist){
			dataList.add(dLFileInfo);
			this.notifyDataSetChanged();
		}
	}

	public void removeItem(DLFileInfo dLFileInfo) {
		String tmp;
		for (int i = 0; i < dataList.size(); i++) {
			tmp = dataList.get(i).getFileUrl();
			if (tmp.equals(dLFileInfo.getFileUrl())) {
				dataList.remove(i);
				this.notifyDataSetChanged();
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.download_list_item, null);
		}

		DLFileInfo itemData = dataList.get(position);
		String url = itemData.getFileUrl();
		convertView.setTag(url);

		ViewHolder viewHolder = new ViewHolder(convertView);
		viewHolder.setData(itemData);

		viewHolder.continueButton.setOnClickListener(new DownloadBtnListener(itemData, viewHolder));
		viewHolder.pauseButton.setOnClickListener(new DownloadBtnListener(itemData, viewHolder));
		viewHolder.deleteButton.setOnClickListener(new DownloadBtnListener(itemData, viewHolder));

		return convertView;
	}

	private class DownloadBtnListener implements View.OnClickListener {
		private DLFileInfo dLFileInfo;
		private ViewHolder	mViewHolder;

		public DownloadBtnListener(DLFileInfo dLFileInfo, ViewHolder viewHolder) {
			this.dLFileInfo = dLFileInfo;
			this.mViewHolder = viewHolder;
		}

		@Override
		public void onClick(View v) {
			Intent downloadIntent = new Intent(DownloadValues.Actions.DOWNLOAD_SERVICE_ACTION);

			switch (v.getId()) {
			case R.id.btn_continue:
				// mDownloadManager.continueTask(mPosition);
				downloadIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.CONTINUE);
				downloadIntent.putExtra(DownloadValues.APPINFO, dLFileInfo);
				mContext.startService(downloadIntent);

				mViewHolder.continueButton.setVisibility(View.GONE);
				mViewHolder.pauseButton.setVisibility(View.VISIBLE);
				break;
			case R.id.btn_pause:
				// mDownloadManager.pauseTask(mPosition);
				downloadIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.PAUSE);
				downloadIntent.putExtra(DownloadValues.APPINFO, dLFileInfo);
				mContext.startService(downloadIntent);

				mViewHolder.continueButton.setVisibility(View.VISIBLE);
				mViewHolder.pauseButton.setVisibility(View.GONE);
				break;
			case R.id.btn_delete:
				// mDownloadManager.deleteTask(mPosition);
				downloadIntent.putExtra(DownloadValues.TYPE, DownloadValues.Types.DELETE);
				downloadIntent.putExtra(DownloadValues.APPINFO, dLFileInfo);
				mContext.startService(downloadIntent);

				removeItem(dLFileInfo);
				break;
			}
		}
	}
}