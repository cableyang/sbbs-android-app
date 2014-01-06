package com.gfan.sbbs.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.gfan.sbbs.http.HttpException;

public class Attachment  implements Serializable  {

	private static final long serialVersionUID = 1L;

	private Integer id, pos, size;
	private String fileName, url;
	private boolean isImage;
//	private ImageLoadListener mImageLoadListener;

	public Attachment(Integer id, Integer pos, Integer size, String fileName,
			String url) {
		this.id = id;
		this.pos = pos;
		this.size = size;
		this.fileName = fileName;
		this.url = url;
	}
	
	public Attachment(){
		
	}

	public boolean isImage() {
		if (null == fileName){
			fileName = url;
		}
		isImage = fileName.toLowerCase().endsWith("png") || fileName.toLowerCase().endsWith("jpg")
				|| fileName.toLowerCase().endsWith("jpeg") || fileName.toLowerCase().endsWith("gif")
				|| fileName.toLowerCase().endsWith("bmp");
		return isImage;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPos() {
		return pos;
	}

	public void setPos(Integer pos) {
		this.pos = pos;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getFileName() {
		return fileName;
	}

	public Attachment setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public Attachment setUrl(String url) {
		this.url = url;
		return this;
	}

	public static Attachment parseAtt(JSONObject obj) throws JSONException {
		int id = obj.getInt("id");
		int pos = obj.getInt("pos");
		int size = obj.getInt("size");
		String fileName = obj.getString("filename");
		String url = obj.getString("url");
		return new Attachment(id, pos, size, fileName, url);
	}
	
	public static List<Attachment> parseAttList(JSONObject obj) throws HttpException{
		List<Attachment> attList = new ArrayList<Attachment>();
		Attachment att;
		try{
			JSONArray attArray = obj.getJSONArray("attachments");
			for (int i = 0, len = attArray.length(); i < len; i++) {
				JSONObject attJson = attArray.getJSONObject(i);
				att = Attachment.parseAtt(attJson);
				attList.add(att);
			}
		}catch(JSONException jse){
			throw new HttpException(jse.getMessage(),jse);
		}
		return attList;
	}
	
	public interface ImageLoadListener{
		public void onPreImageLoad();
		public void onPostImageLoad();
	};
}

