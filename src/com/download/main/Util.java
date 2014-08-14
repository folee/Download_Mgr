package com.download.main;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

public class Util {

	/**
	 * get SDCARD free storage space
	 * 
	 * @return
	 */
	public static long getAvailableExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		}
		else {
			return -1;
		}
	}
	
	/**
	 * SDCARD is exist or not
	 */
	public static boolean externalMemoryAvailable() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

}


