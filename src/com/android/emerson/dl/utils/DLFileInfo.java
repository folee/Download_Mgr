package com.android.emerson.dl.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class DLFileInfo implements Parcelable {
	private int		fileId;
	private String	fileSize;
	private String	fileUrl;
	private String	filePath;
	private String	fileName;
	private String	fileType;
	private String	isPaused;
	private String	speed;
	private String	progress;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("fileId       = ").append(fileId).append("\n");
		sb.append("fileSize   	= ").append(fileSize).append("\n");
		sb.append("fileUrl   	= ").append(fileUrl).append("\n");
		sb.append("filePath   	= ").append(filePath).append("\n");
		sb.append("fileName   	= ").append(fileName).append("\n");
		sb.append("fileType   	= ").append(fileType).append("\n");
		sb.append("isPaused   	= ").append(isPaused).append("\n");
		sb.append("speed     	= ").append(speed).append("\n");
		sb.append("progress   	= ").append(progress).append("\n");
		return sb.toString();
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getIsPaused() {
		return isPaused;
	}

	public void setIsPaused(String isPaused) {
		this.isPaused = isPaused;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public DLFileInfo() {}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(fileId);
		dest.writeString(fileSize);
		dest.writeString(fileUrl);
		dest.writeString(filePath);
		dest.writeString(fileName);
		dest.writeString(fileType);
		dest.writeString(isPaused);
		dest.writeString(speed);
		dest.writeString(progress);
	}

	public DLFileInfo(Parcel source) {
		this.fileId = source.readInt();
		this.fileSize = source.readString();
		this.fileUrl = source.readString();
		this.filePath = source.readString();
		this.fileName = source.readString();
		this.fileType = source.readString();
		this.isPaused = source.readString();
		this.speed = source.readString();
		this.progress = source.readString();
	}

	public static final Parcelable.Creator<DLFileInfo>	CREATOR	= new Parcelable.Creator<DLFileInfo>() {

																	@Override
																	public DLFileInfo createFromParcel(Parcel source) {
																		return new DLFileInfo(source);
																	}

																	@Override
																	public DLFileInfo[] newArray(int size) {
																		return new DLFileInfo[size];
																	}

																};
}
