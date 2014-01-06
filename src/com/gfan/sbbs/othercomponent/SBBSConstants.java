package com.gfan.sbbs.othercomponent;

import org.apache.http.protocol.HTTP;

public class SBBSConstants {
	public static final String SBBS_ENCODING = HTTP.UTF_8;
	public static final String LOGIN_URL = "http://bbs.seu.edu.cn/api/token.json";
	public static final String BASE_URL = "http://bbs.seu.edu.cn";
	public static final String BASE_API_URL = "http://bbs.seu.edu.cn/api";
	public static final String HOTURL = "http://bbs.seu.edu.cn/api/hot/topten.json";
	public static final String FAVURL = "http://bbs.seu.edu.cn/api/fav/get.json";
	public static final String MAILURL = "http://bbs.seu.edu.cn/api/mailbox/get.json";
	public static final String FRIENDS_URL = "http://bbs.seu.edu.cn/api/friends/get.json";
	public static final String HOT_SECTIONS = "http://bbs.seu.edu.cn/api/hot/section.json";
	public static final String BOARD_SECTIONS = "http://bbs.seu.edu.cn/api/sections.json";
	public static final int CLIENT_TYPE_ANDROID = 1;
}
