/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gfan.sbbs.utils.images;

/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.http.ResponseException;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;

/**
 * Manages retrieval and storage of icon images. Use the put method to download
 * and store images. Use the get method to retrieve images from the manager.
 */
public class ImageManager implements ImageCache {
	private static final String TAG = "ImageManager";

	// ����Ŀǰ�����֧��596px, ������ͬ����С
	// ���߶�Ϊ1192px, ������н�ȡ
	//TODO �޸�ʹ֮���BBS
	public static final int DEFAULT_COMPRESS_QUALITY = 90;
	public static final int IMAGE_MAX_WIDTH = 596;
	public static final int IMAGE_MAX_HEIGHT = 1192;

	private Context mContext;
	// In memory cache.
	private Map<String, SoftReference<Bitmap>> mCache;
	// MD5 hasher.
	private MessageDigest mDigest;

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public ImageManager(Context context) {
		mContext = context;
		mCache = new HashMap<String, SoftReference<Bitmap>>();

		try {
			mDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// This shouldn't happen.
			throw new RuntimeException("No MD5 algorithm.");
		}
	}

	public void setContext(Context context) {
		mContext = context;
	}

	private String getHashString(MessageDigest digest) {
		StringBuilder builder = new StringBuilder();

		for (byte b : digest.digest()) {
			builder.append(Integer.toHexString((b >> 4) & 0xf));
			builder.append(Integer.toHexString(b & 0xf));
		}

		return builder.toString();
	}

	// MD5 hases are used to generate filenames based off a URL.
	private String getMd5(String url) {
		mDigest.update(url.getBytes());

		return getHashString(mDigest);
	}

	// Looks to see if an image is in the file system.
	private Bitmap lookupFile(String url) {
		String hashedUrl = getMd5(url);
		FileInputStream fis = null;

		try {
			fis = mContext.openFileInput(hashedUrl);
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			// Not there.
			return null;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// Ignore.
				}
			}
		}
	}

	/**
	 * Downloads a file
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 * @throws IOException 
	 * @throws ResponseException 
	 * @throws ClientProtocolException 
	 */
	public Bitmap downloadImage(String url) throws  HttpException {
		Log.d(TAG, "Fetching image: " + url);
		InputStream inStream = BBSOperator.getInstance().get(url).asStream();
		
		return BitmapFactory.decodeStream(inStream);
	}

	public Bitmap downloadImage2(String url) throws HttpException  {
		Log.d(TAG, "[NEW]Fetching image: " + url);
		InputStream inStream = BBSOperator.getInstance().get(url).asStream();
		String file = writeToFile(inStream, getMd5(url));
		try {
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
		Bitmap bitmap = BitmapFactory.decodeFile(file);
		if(null == bitmap){
			Log.e(TAG, "here bitmap is null");
			Log.e(TAG, file);
		}
		bitmap = resizeBitmap(bitmap, IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT);
		return bitmap;
	}

	/**
	 * ����Զ��ͼƬ -> ת��ΪBitmap -> д�뻺����.
	 * 
	 * @param url
	 * @param quality
	 *            image quality 1��100
	 * @throws HttpException
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public void put(String url, int quality, boolean forceOverride) throws HttpException
			{
		if (!forceOverride && contains(url)) {
			// Image already exists.
			return;

			// TODO: write to file if not present.
		}

		Bitmap bitmap = downloadImage(url);
		if (bitmap != null) {
			put(url, bitmap, quality); // file cache
		} else {
			Log.w(TAG, "Retrieved bitmap is null.");
		}
	}

	/**
	 * ���� put(String url, int quality)
	 * 
	 * @param url
	 * @throws HttpException
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public void put(String url) throws HttpException {
		put(url, DEFAULT_COMPRESS_QUALITY, false);
	}

	/**
	 * ������File -> ת��ΪBitmap -> д�뻺����. ���ͼƬ��С����MAX_WIDTH/MAX_HEIGHT, �򽫻��ͼƬ����.
	 * 
	 * @param file
	 * @param quality
	 *            ͼƬ����(0~100)
	 * @param forceOverride
	 * @throws IOException
	 */
	public void put(File file, int quality, boolean forceOverride)
			throws IOException {
		if (!file.exists()) {
			Log.w(TAG, file.getName() + " is not exists.");
			return;
		}
		if (!forceOverride && contains(file.getPath())) {
			// Image already exists.
			Log.d(TAG, file.getName() + " is exists");
			return;
			// TODO: write to file if not present.
		}

		Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
		// bitmap = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT);

		if (bitmap == null) {
			Log.w(TAG, "Retrieved bitmap is null.");
		} else {
			put(file.getPath(), bitmap, quality);
		}
	}

	/**
	 * ��Bitmapд�뻺����.
	 * 
	 * @param filePath
	 *            file path
	 * @param bitmap
	 * @param quality
	 *            1~100
	 */
	public void put(String file, Bitmap bitmap, int quality) {
		synchronized (this) {
			mCache.put(file, new SoftReference<Bitmap>(bitmap));
		}

		writeFile(file, bitmap, quality);
	}

	/**
	 * ���� put(String file, Bitmap bitmap, int quality)
	 * 
	 * @param filePath
	 *            file path
	 * @param bitmap
	 * @param quality
	 *            1~100
	 */
	@Override
	public void put(String file, Bitmap bitmap) {
		put(file, bitmap, DEFAULT_COMPRESS_QUALITY);
	}

	/**
	 * ��Bitmapд�뱾�ػ����ļ�.
	 * 
	 * @param file
	 *            URL/PATH
	 * @param bitmap
	 * @param quality
	 */
	private void writeFile(String file, Bitmap bitmap, int quality) {
		if (bitmap == null) {
			Log.w(TAG, "Can't write file. Bitmap is null.");
			return;
		}

		BufferedOutputStream bos = null;
		try {
//			String hashedUrl = getMd5(file);
			String hashedUrl = file;
			bos = new BufferedOutputStream(mContext.openFileOutput(hashedUrl,
					Context.MODE_PRIVATE));
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos); // PNG
			Log.d(TAG, "Writing file: " + file);
		} catch (IOException ioe) {
			Log.e(TAG, ioe.getMessage());
		} finally {
			try {
				if (bos != null) {
					bitmap.recycle();
					bos.flush();
					bos.close();
				}
				// bitmap.recycle();
			} catch (IOException e) {
				Log.e(TAG, "Could not close file.");
			}
		}
	}

	private String writeToFile(InputStream is, String filename) {
		Log.d("LDS", "new write to file");
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		try {
			in = new BufferedInputStream(is);
			out = new BufferedOutputStream(mContext.openFileOutput(filename,
					Context.MODE_PRIVATE));
			byte[] buffer = new byte[1024];
			int l;
			while ((l = in.read(buffer)) != -1) {
				out.write(buffer, 0, l);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null) {
					Log.d("LDS", "new write to file to -> " + filename);
					out.flush();
					out.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return mContext.getFilesDir() + "/" + filename;
	}

	public Bitmap get(File file) {
		return get(file.getPath());
	}

	/**
	 * �жϻ��������Ƿ���ڸ��ļ���Ӧ��bitmap
	 */
	public boolean isContains(String file) {
		return mCache.containsKey(file);
	}

	/**
	 * ���ָ��file/URL��Ӧ��Bitmap�������ұ����ļ��������ֱ��ʹ�ã�����ȥ���ϻ�ȡ
	 * 
	 * @param file
	 *            file URL/file PATH
	 * @param bitmap
	 * @param quality
	 * @throws HttpException
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public Bitmap safeGet(String file) throws HttpException  {
		Bitmap bitmap = lookupFile(file); // first try file.

		if (bitmap != null) {
			synchronized (this) { // memory cache
				mCache.put(file, new SoftReference<Bitmap>(bitmap));
			}
			return bitmap;
		} else { // get from web
			String url = file;
			bitmap = downloadImage2(url);

			// ע�͵��Բ����µ�д���ļ�����
			// put(file, bitmap); // file Cache
			return bitmap;
		}
	}

	/**
	 * �ӻ������ж�ȡ�ļ�
	 * 
	 * @param file
	 *            file URL/file PATH
	 * @param bitmap
	 * @param quality
	 */
	@Override
	public Bitmap get(String file) {
		SoftReference<Bitmap> ref;
		Bitmap bitmap;

		// Look in memory first.
		synchronized (this) {
			ref = mCache.get(file);
		}

		if (ref != null) {
			bitmap = ref.get();

			if (bitmap != null) {
				return bitmap;
			}
		}

		// Now try file.
		bitmap = lookupFile(file);

		if (bitmap != null) {
			synchronized (this) {
				mCache.put(file, new SoftReference<Bitmap>(bitmap));
			}

			return bitmap;
		}

		// TODO: why?
		// upload: see profileImageCacheManager line 96
		Log.w(TAG, "Image is missing: " + file);
		// return the default photo
		return mDefaultBitmap;
	}

	public boolean contains(String url) {
		return get(url) != mDefaultBitmap;
	}

	public void clear() {
		String[] files = mContext.fileList();

		for (String file : files) {
			mContext.deleteFile(file);
		}

		synchronized (this) {
			mCache.clear();
		}
	}

	public void cleanup(HashSet<String> keepers) {
		String[] files = mContext.fileList();
		HashSet<String> hashedUrls = new HashSet<String>();

		for (String imageUrl : keepers) {
			hashedUrls.add(getMd5(imageUrl));
		}

		for (String file : files) {
			if (!hashedUrls.contains(file)) {
				Log.d(TAG, "Deleting unused file: " + file);
				mContext.deleteFile(file);
			}
		}
	}

	/**
	 * Compress and resize the Image
	 * 
	 * <br />
	 * ��Ϊ����ͼƬ��С�ͳߴ����, ���񶼻��ͼƬ����һ������ѹ��, ���Ա���ѹ��Ӧ�� ����ͼƬ���ᱻ����ѹ������ɵ�ͼƬ�������
	 * 
	 * @param targetFile
	 * @param quality
	 *            , 0~100, recommend 100
	 * @return
	 * @throws IOException
	 */
	public File compressImage(File targetFile, int quality) throws IOException {
		String filepath = targetFile.getAbsolutePath();

		// 1. Calculate scale
		int scale = 1;
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filepath, o);
		if (o.outWidth > IMAGE_MAX_WIDTH || o.outHeight > IMAGE_MAX_HEIGHT) {
			scale = (int) Math.pow(
					2.0,
					(int) Math.round(Math.log(IMAGE_MAX_WIDTH
							/ (double) Math.max(o.outHeight, o.outWidth))
							/ Math.log(0.5)));
			// scale = 2;
		}
		Log.d(TAG, scale + " scale");

		// 2. File -> Bitmap (Returning a smaller image)
		o.inJustDecodeBounds = false;
		o.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeFile(filepath, o);

		// 2.1. Resize Bitmap
		// bitmap = resizeBitmap(bitmap, IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT);

		// 3. Bitmap -> File
		writeFile(targetFile.getName(), bitmap, quality);
		Log.i(TAG, "TargetFile's name is "+targetFile.getName());
		// 4. Get resized Image File
//		String filePath = getMd5(targetFile.getAbsolutePath());
		String filePath = targetFile.getName();
		Log.i(TAG, targetFile.getAbsolutePath());
		File compressedImage = mContext.getFileStreamPath(filePath);
		return compressedImage;
	}

	/**
	 * ���ֳ������СBitmap
	 * 
	 * @param bitmap
	 * @param maxWidth
	 * @param maxHeight
	 * @param quality
	 *            1~100
	 * @return
	 */
	public Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
		Log.i(TAG, "resizing a bitmap");
		if(null == bitmap){
			return null;
		}
		int originWidth = bitmap.getWidth();
		int originHeight = bitmap.getHeight();

		// no need to resize
		if (originWidth < maxWidth && originHeight < maxHeight) {
			return bitmap;
		}

		int newWidth = originWidth;
		int newHeight = originHeight;
		Bitmap newBitmap = null;

		// ��ͼƬ���, �򱣳ֳ��������ͼƬ
		if (originWidth > maxWidth) {
			newWidth = maxWidth;

			double i = originWidth * 1.0 / maxWidth;
			newHeight = (int) Math.floor(originHeight / i);

			newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight,
					true);
		}

		// ��ͼƬ��, ����в���ȡ
		if (newHeight > maxHeight) {
			newHeight = maxHeight;

			int half_diff = (int) ((originHeight - maxHeight) / 2.0);
			newBitmap = Bitmap.createBitmap(bitmap, 0, half_diff, newWidth,
					newHeight);
		}

		bitmap.recycle();
		Log.d(TAG, newWidth + " width");
		Log.d(TAG, newHeight + " height");

		return newBitmap;
	}

}
