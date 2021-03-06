package com.android.emerson.dl.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.conn.ConnectTimeoutException;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.util.Log;

import com.android.emerson.dl.exception.FileAlreadyExistException;
import com.android.emerson.dl.exception.NoMemoryException;
import com.android.emerson.dl.utils.ConfigUtils;
import com.android.emerson.dl.utils.DLFileInfo;
import com.android.emerson.dl.utils.Util;


//import org.apache.http.conn.ConnectTimeoutException;

public class DownloadTask implements Runnable{
	private static final String		TAG										= "DownloadTask";
	public static final String		TEMP_SUFFIX								= ".download";

	// 以下属性在DownloadHelper类提供对应的设置函数
	public static int				TIME_OUT								= 30000;
	public static int				BUFFER_SIZE								= 1024 * 8;
	public static int				MAX_TASK_COUNT							= 100;
	public static int				MAX_DOWNLOAD_THREAD_COUNT				= 5;
	public static boolean			DEBUG									= true;

	private File					file;
	private File					tempFile;
	//	private String					url;
	private DLFileInfo				dLFileInfo;
	private RandomAccessFile		outputStream;
	private DownloadTaskListener	listener;
	private Context					context;

	private long					downloadSize;
	private long					previousFileSize;
	private long					totalSize;
	private long					downloadPercentPre;
	private long					downloadPercent;
	private long					networkSpeed;
	private long					previousTime;
	private long					totalTime;
	public int						errorCode								= -1;
	public String					errorInfo								= null;
	private Throwable				error									= null;
	private boolean					interrupt								= false;

	/** 未捕获的异常 */
	public static final int			ERROR_UNCATCHED							= -1;
	/** 未知错误,停止下载 */
	public static final int			ERROR_UNKONW							= 0;
	public static final String		ERROR_UNKONW_INFO						= "文件IO流异常!";

	/** 网络无法连接,等待下载 */
	public static final int			ERROR_BLOCK_INTERNET					= 1;
	public static final String		ERROR_BLOCK_INTERNET_INFO				= "无法连接网络!";

	/** 网络连接超时,等待下载 */
	public static final int			ERROR_TIME_OUT							= 2;
	public static final String		ERROR_TIME_OUT_INFO						= "网络连接超时!";

	/** 文件已存在,停止下载 */
	public static final int			ERROR_FILE_EXIST						= 3;
	public static final String		ERROR_FILE_EXIST_INFO					= "文件已存在，无需重复下载!";

	/** SD存储空间不足,停止下载 */
	public static final int			ERROR_SD_NO_MEMORY						= 4;
	public static final String		ERROR_SD_NO_MEMORY_INFO					= "SD卡存储空间不足，中断下载!";

	/** 没有发现SD卡 */
	public static final int			ERROR_NOT_FOUND_SDCARD					= 5;
	public static final String		ERROR_NOT_FOUND_SDCARD_INFO				= "未发现SD卡!";

	/** SD卡不能读写 */
	public static final int			ERROR_CANNOT_WRITEORREAD_SDCARD			= 6;
	public static final String		ERROR_CANNOT_WRITEORREAD_SDCARD_INFO	= "SD卡不能读写!";

	/** 任务列表已满 */
	public static final int			ERROR_MORE_TASK_COUNT					= 7;
	public static final String		ERROR_MORE_TASK_COUNT_INFO				= "任务列表已满!";

	private final class ProgressReportingRandomAccessFile extends RandomAccessFile {
		private int	progress	= 0;

		public ProgressReportingRandomAccessFile(File file, String mode) throws FileNotFoundException {

			super(file, mode);
		}

		@Override
		public void write(byte[] buffer, int offset, int count) throws IOException {

			super.write(buffer, offset, count);
			progress += count;
			publishProgress(progress);
		}
	}

	public DownloadTask(Context context, DLFileInfo dLFileInfo, DownloadTaskListener listener) throws MalformedURLException {
		super();
		this.dLFileInfo = dLFileInfo;
		this.listener = listener;
		this.file = new File(dLFileInfo.getFilePath(), dLFileInfo.getFileName());
		this.tempFile = new File(dLFileInfo.getFilePath(), dLFileInfo.getFileName() + TEMP_SUFFIX);
		this.context = context;
	}

	public boolean isInterrupt() {
		return interrupt;
	}
	
	public void setInterrupt(boolean interrupt) {
		this.interrupt = interrupt;
	}

	public DLFileInfo getDLFileInfo() {
		return dLFileInfo;
	}

	public long getDownloadPercent() {

		return downloadPercent;
	}

	public long getDownloadSize() {

		return downloadSize + previousFileSize;
	}

	public long getTotalSize() {

		return totalSize;
	}

	public long getDownloadSpeed() {

		return this.networkSpeed;
	}

	public long getTotalTime() {

		return this.totalTime;
	}

	public DownloadTaskListener getListener() {

		return this.listener;
	}
	
	private void publishProgress(int... progress){
		if (progress.length > 1) {
			totalSize = progress[1];
			if (totalSize == -1) {
				if (listener != null)
					listener.errorDownload(this, errorCode, errorInfo);
			}
			else {

			}
		}
		else {
			totalTime = System.currentTimeMillis() - previousTime;
			downloadSize = progress[0];
			downloadPercent = (downloadSize + previousFileSize) * 100 / totalSize;
			networkSpeed = downloadSize / totalTime;
			if (listener != null && (downloadPercent - downloadPercentPre) >= 1) {
				downloadPercentPre = downloadPercent;
				dLFileInfo.setProgress((int) downloadPercent);
				listener.updateProcess(this);
			}
		}
	}
	
	private void preExecute(){
		previousTime = System.currentTimeMillis();
		
		String index = Integer.toHexString(dLFileInfo.getFileUrl().hashCode());
		ConfigUtils.storeURL(context, index, dLFileInfo);
		
		if (listener != null)
			listener.preDownload(this);
	}
	
	private long doInBackground(){
		long result = -1;
		try {
			getFileSize();
			result = download();
		} catch (NetworkErrorException e) {
			errorCode = ERROR_BLOCK_INTERNET;
			errorInfo = ERROR_BLOCK_INTERNET_INFO;
			error = e;
		} catch (FileAlreadyExistException e) {
			errorCode = ERROR_FILE_EXIST;
			errorInfo = ERROR_FILE_EXIST_INFO;
			error = e;
		} catch (NoMemoryException e) {
			errorCode = ERROR_SD_NO_MEMORY;
			errorInfo = ERROR_SD_NO_MEMORY_INFO;
			error = e;
		} catch (IOException e) {
			if (errorCode == ERROR_TIME_OUT) {
				errorInfo = ERROR_TIME_OUT_INFO;
			}
			else {
				errorCode = ERROR_UNKONW;
				errorInfo = ERROR_UNKONW_INFO;
			}
			error = e;

		} finally {
			//			if (client != null) {
			//				client.close();
			//			}
		}

		return result;
	}
	
	private void onFinish(Long result){
		if (result == -1 || interrupt || error != null) {
			if (DEBUG && error != null) {
				Log.v(TAG, "Download failed." + error.getMessage());
			}
			if (listener != null) {
				listener.errorDownload(this, errorCode, errorInfo);
			}
			return;
		}
		// finish download
		Log.v(TAG, "Download totalSize = " + totalSize);
		Log.v(TAG, "Download tempFile.length() = " + tempFile.length());
		/*if (totalSize == tempFile.length()) {
			tempFile.renameTo(file);
		}*/
		tempFile.renameTo(file);
		//BgInstallApk.shellComm("chmod","777", file.getAbsolutePath());

		if (listener != null)
			listener.finishDownload(this);
	}
	
	public void onCancelled(){
		setInterrupt(true);
	}
	
	@Override
	public void run() {
		preExecute();
		onFinish(doInBackground());
	}
	
	private boolean getFileSize() {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(dLFileInfo.getFileUrl());
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			totalSize = conn.getContentLength();
			conn.connect();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				conn.disconnect();
		}
		return false;
	}

	private long download() throws NetworkErrorException, IOException, FileAlreadyExistException, NoMemoryException {

		if (DEBUG) {
			Log.v(TAG, "totalSize: " + totalSize);
		}

		/*
		 * check net work
		 */
		if (!Util.isNetworkAvailable(context)) {
			throw new NetworkErrorException(ERROR_BLOCK_INTERNET_INFO);
		}

		URL url = new URL(dLFileInfo.getFileUrl());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		/*
		 * check file length
		 */
		if (file.exists() && totalSize == file.length()) {
			if (DEBUG) {
				Log.v(TAG, "Output file already exists. Skipping download.");
			}

			return totalSize;
			//throw new FileAlreadyExistException(ERROR_FILE_EXIST_INFO);
		}
		else if (tempFile.exists()) {

			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Referer", url.toString());
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Range", "bytes=" + tempFile.length() + "-");
			previousFileSize = tempFile.length();

			if (DEBUG) {
				Log.v(TAG, "File is not complete, download now.");
				Log.v(TAG, "File length:" + tempFile.length() + " totalSize:" + totalSize);
			}
		}

		/*
		 * check memory
		 */
		//		long storage = DownloadUtils.getAvailableStorage();
		//		if (DEBUG) {
		//			Log.i(TAG, "storage:" + storage + " totalSize:" + totalSize);
		//		}
		//
		//		if (totalSize - tempFile.length() > storage) {
		//			throw new NoMemoryException(ERROR_SD_NO_MEMORY_INFO);
		//		}

		/*
		 * start download
		 */
		outputStream = new ProgressReportingRandomAccessFile(tempFile, "rw");

		publishProgress(0, (int) totalSize);

		InputStream input = conn.getInputStream();
		int bytesCopied = copy(input, outputStream);

		if ((previousFileSize + bytesCopied) != totalSize && totalSize != -1 && !interrupt) {
			//            throw new IOException("Download incomplete: " + bytesCopied + " != " + totalSize);
			throw new IOException(ERROR_UNKONW_INFO);
		}

		if (DEBUG) {
			Log.v(TAG, "Download completed successfully.");
		}

		return bytesCopied;

	}

	public int copy(InputStream input, RandomAccessFile out) throws IOException, NetworkErrorException {

		if (input == null || out == null) {
			return -1;
		}

		byte[] buffer = new byte[BUFFER_SIZE];

		BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
		if (DEBUG) {
			Log.v(TAG, "length" + out.length());
		}

		int count = 0, n = 0;
		long errorBlockTimePreviousTime = -1, expireTime = 0;

		try {

			out.seek(out.length());

			while (!interrupt) {
				n = in.read(buffer, 0, BUFFER_SIZE);
				if (n == -1) {
					break;
				}
				out.write(buffer, 0, n);
				count += n;

				/*
				 * check network
				 */
				if (!Util.isNetworkAvailable(context)) {
					throw new NetworkErrorException(ERROR_BLOCK_INTERNET_INFO);
				}

				if (networkSpeed == 0) {
					if (errorBlockTimePreviousTime > 0) {
						expireTime = System.currentTimeMillis() - errorBlockTimePreviousTime;
						if (expireTime > TIME_OUT) {
							errorCode = ERROR_TIME_OUT;
							throw new ConnectTimeoutException(ERROR_TIME_OUT_INFO);
						}
					}
					else {
						errorBlockTimePreviousTime = System.currentTimeMillis();
					}
				}
				else {
					expireTime = 0;
					errorBlockTimePreviousTime = -1;
				}
			}
		} finally {
			out.close();
			in.close();
			input.close();
		}
		return count;

	}

}
