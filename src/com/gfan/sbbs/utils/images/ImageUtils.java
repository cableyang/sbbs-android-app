package com.gfan.sbbs.utils.images;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.gfan.sbbs.othercomponent.MyApplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class ImageUtils {

	private static final int IMAGE_MAX_WIDTH = 800;
	private static final int IMAGE_MAX_HEIGHT = 1280;

	private static final String TAG = "ImageUtils";

	public static Bitmap createRoundBitmap(Bitmap bitmap, float roundPx) {
		Bitmap out = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(out);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return out;
	}
	/**
	 * compressed imageFiles for upload
	 * 
	 * @param imageFile
	 * @param quality
	 * @return File compressed imageFiles
	 */
	public static void compressImages(String pathName, int quality,
			Context context) {

//		String pathName = imageFile.getAbsolutePath();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

		int scale = 1;
		if (options.outWidth > IMAGE_MAX_WIDTH) {
			scale = (int) Math.pow(
					2.0,
					(int) Math.round(Math.log(IMAGE_MAX_WIDTH
							/ (double) Math.max(IMAGE_MAX_HEIGHT,
									IMAGE_MAX_WIDTH))
							/ Math.log(0.5)));
			options.inSampleSize = scale;
		}

		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(pathName, options);
		BufferedOutputStream bos;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(pathName));
			bitmap.compress(CompressFormat.JPEG, quality, bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
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
		if (null == bitmap) {
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
