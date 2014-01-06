package com.gfan.sbbs.utils;

import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.text.TextUtils;

public class StringUtils {
	private static final String BASE_PATH = "sbbandroid";
	
	public static boolean contains(String target,String[] array){
		if(array == null || array.length == 0){
			return false;
		}
		target = target.trim().toLowerCase();
		for(String s:array){
			if(TextUtils.isEmpty(s)){
				return false;
			}
			s = s.trim().toLowerCase();
			if(s.equals(target)){
				return true;
			}
		}
		return false;
	}
	public static String toString(String[] array){
		if(null == array){
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for(String s:array){
			sb.append(s+" ");
		}
		return sb.toString();
	}
	/**
	 * from fanfoudroid
	 * @return
	 * @throws IOException
	 */
	public static File getBasePath() throws IOException {
		File basePath = new File(Environment.getExternalStorageDirectory(),
				BASE_PATH);

		if (!basePath.exists()) {
			if (!basePath.mkdirs()) {
				throw new IOException(String.format("%s cannot be created!",
						basePath.toString()));
			}
		}

		if (!basePath.isDirectory()) {
			throw new IOException(String.format("%s is not a directory!",
					basePath.toString()));
		}

		return basePath;
	}
	
}
