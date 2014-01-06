package com.gfan.sbbs.bean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gfan.sbbs.http.HttpException;

public class Mail implements Serializable {
	private static final long serialVersionUID = 1L;

	private String from;
	private String title;
	private String content;
	private String date;
	private String quote;
	private String num;
	private boolean unRead;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setNum(String num) {
		this.num = num;
	}

	public String getNum() {
		return num;
	}

	public void setQuote(String quote) {
		this.quote = quote;
	}

	public String getQuote() {
		return quote;
	}

	public void setUnRead(boolean unRead) {
		this.unRead = unRead;
	}

	public boolean isUnRead() {
		return unRead;
	}

	public static Mail parseJson(JSONObject obj) throws HttpException {
		Mail mail = new Mail();
		
		try {
			JSONObject mailJson = obj.getJSONObject("mail");
			String title = mailJson.getString("title");
			String mailContent = mailJson.getString("content");
			String quote = mailJson.getString("quote");
			String num = mailJson.getString("id");
			String author = mailJson.getString("author");
			long milliseconds = mailJson.getLong("time");
			Date date = new Date(milliseconds * 1000);
			SimpleDateFormat format = new SimpleDateFormat(
					"MMM dd hh:mm:ss yyyy", Locale.ENGLISH);
			String time = format.format(date);
			mail.setDate(time);
			mail.setTitle(title);
			mail.setNum(num);
			mail.setContent(mailContent);
			mail.setQuote(quote);
			mail.setFrom(author);
		} catch (JSONException jse) {
			jse.printStackTrace();
			throw new HttpException(jse.getMessage(), jse);
		}

		return mail;
	}

	public static Mail parseListMail(JSONObject mailJson) throws HttpException{
		Mail mail = new Mail();
		try{
		String num = mailJson.getString("id");
		boolean unread = mailJson.getBoolean("unread");
		String author = mailJson.getString("author");
		long milliseconds = mailJson.getLong("time") * 1000;
		Date date = new Date(milliseconds);
		SimpleDateFormat format = new SimpleDateFormat(
				"MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
		String time = format.format(date);
		String title = mailJson.getString("title");
		mail.setDate(time);
		mail.setFrom(author);
		mail.setNum(num);
		mail.setTitle(title);
		mail.setUnRead(unread);}catch(JSONException e){
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
		return mail;
	}
	
	public static List<Mail> parseNoticeMailList(JSONArray mailArray) throws HttpException{
		List<Mail> mailList = new ArrayList<Mail>();
		try{
		for (int i = 0, len = mailArray.length(); i < len; i++) {
			JSONObject mailJson = mailArray.getJSONObject(i);
			Mail mail = new Mail();
			String id = mailJson.getString("id");
			String sender = mailJson.getString("sender");
			String title = mailJson.getString("title");
			mail.setFrom(sender);
			mail.setNum(id);
			mail.setTitle(title);
			mailList.add(mail);
		}}catch(JSONException jse){
			jse.printStackTrace();
			throw new HttpException(jse.getMessage(), jse);
		}
		return mailList;
	}
	
	public static List<Mail> parseMailList(JSONObject obj) throws HttpException {
		List<Mail> mailList = new ArrayList<Mail>();
		Mail mail;
		try {
			JSONArray mailArray = obj.getJSONArray("mails");
			for (int i = 0, len = mailArray.length(); i < len; i++) {
				JSONObject mailJson = mailArray.getJSONObject(i);
				mail = parseListMail(mailJson);
				mailList.add(mail);
			}
		} catch (JSONException jse) {
			jse.printStackTrace();
			throw new HttpException(jse.getMessage(), jse);
		}
		return mailList;
	}

}
