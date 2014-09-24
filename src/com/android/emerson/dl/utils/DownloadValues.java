package com.android.emerson.dl.utils;

public class DownloadValues {
	// only for test

	public static String[]		mp3url				= { "http://116.228.215.8:8180/upload/3/ita_2_.mp3?AdID=null&SNInfo=null&ModelID=null&IP=116.231.118.64" };
	
	
	public static String[]		imgurl				= { "http://116.228.215.8:8180/upload/3/ta_2_.jpg?AdID=null&SNInfo=null&ModelID=null&IP=116.231.118.64" };

	public static String[]		url					= { "http://img.yingyonghui.com/apk/16457/com.rovio.angrybirdsspace.ads.1332528395706.apk",
			"http://img.yingyonghui.com/apk/15951/com.galapagossoft.trialx2_winter.1328012793227.apk",
			"http://cdn1.down.apk.gfan.com/asdf/Pfiles/2012/3/26/181157_0502c0c3-f9d1-460b-ba1d-a3bad959b1fa.apk",
			"http://static.nduoa.com/apk/258/258681/com.gameloft.android.GAND.GloftAsp6.asphalt6.apk",
			"http://cdn1.down.apk.gfan.com/asdf/Pfiles/2011/12/5/100522_b73bb8d2-2c92-4399-89c7-07a9238392be.apk",
			"http://file.m.163.com/app/free/201106/16/com.gameloft.android.TBFV.GloftGTHP.ML.apk" };

	/** 用于传递和获取Types动作标识值的主键 */
	public static final String	TYPE				= "type";

	/**
	 * 用于传递和获取 "下载速度|资源已下载大小|资源总共大小" 的主键
	 */
	public static final String	PROCESS_SPEED		= "process_speed";

	/** 用户传递和获取下载百分比的主键 */
	public static final String	PROCESS_PROGRESS	= "process_progress";

	/** 记录每次添加到下载队列的资源URL */
	//	public static final String	URL					= "url";

	public static final String	APPINFO				= "appInfo";

	public static final String	ERROR_CODE			= "error_code";
	public static final String	ERROR_INFO			= "error_info";
	public static final String	IS_PAUSED			= "is_paused";

	public class Types {
		/** 下载进度更新标识 */
		public static final int	PROCESS		= 0;

		/** 下载成功标识 */
		public static final int	COMPLETE	= 1;

		/**
		 * 重新继续下载队列中所有任务， 如应用重启后需唤醒所有下载任务
		 */
		public static final int	START		= 2;

		/** 暂停下载某个下载任务 */
		public static final int	PAUSE		= 3;

		/** 将一个下载任务从队列中移除 */
		public static final int	DELETE		= 4;

		/** 暂停的下载任务重新唤醒标识 */
		public static final int	CONTINUE	= 5;

		/** 添加新的下载任务标识 */
		public static final int	ADD			= 6;

		/** 停止下载队列中所有任务 */
		public static final int	STOP		= 7;

		/** 下载异常标识 */
		public static final int	ERROR		= 8;
	}

	public class Actions {
		/** 启动下载服务的Intent Action */
		public static final String	DOWNLOAD_SERVICE_ACTION		= "com.emerson.download.services.IDownloadService";

		/** 下载广播发送的Intent Action */
		public static final String	BROADCAST_RECEIVER_ACTION	= "com.emerson.download.DownloadMgr";
	}
}
