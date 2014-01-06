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

import android.util.Log;

import com.gfan.sbbs.http.HttpException;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;// �û���
	private String pwd;// ����
	private String nickName;// �ǳ�
	private String status;// ״̬�����Ķ������¡������еȵ�
	private String sourceIP;// ��ԴIP
	private String astrology;// ����
	private String loginTime;// ��¼����
	private String postTime;// ����������
	private String lifeValue;// ������
	private String performValue;// ����ֵ
	private String experience;// ����ֵ
	private String identity;// ��ݣ�Google Translate����ô�����-��-
	private String medalNum;// ѫ��
	private String gender;
	private String lastLoginTime;
	private String token;

	public User() {
		super();
	}

	public User(String id, String pwd) {
		super();
		this.id = id;
		this.pwd = pwd;
	}

	public String getId() {
		return id;
	}

	public User setId(String id) {
		this.id = id;
		return this;
	}

	public String getPwd() {
		return pwd;
	}

	public User setPwd(String pwd) {
		this.pwd = pwd;
		return this;
	}

	public String getNickName() {
		return nickName;
	}

	public User setNickName(String nickName) {
		this.nickName = nickName;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public User setStatus(String status) {
		this.status = status;
		return this;
	}

	public String getSourceIP() {
		return sourceIP;
	}

	public User setSourceIP(String sourceIP) {
		this.sourceIP = sourceIP;
		return this;
	}

	public String getAstrology() {
		return astrology;
	}

	public User setAstrology(String astrology) {
		this.astrology = astrology;
		return this;
	}

	public String getLoginTime() {
		return loginTime;
	}

	public User setLoginTime(String loginTime) {
		this.loginTime = loginTime;
		return this;
	}

	public String getPostTime() {
		return postTime;
	}

	public User setPostTime(String postTime) {
		this.postTime = postTime;
		return this;
	}

	public String getLifeValue() {
		return lifeValue;
	}

	public User setLifeValue(String lifeValue) {
		this.lifeValue = lifeValue;
		return this;
	}

	public String getPerformValue() {
		return performValue;
	}

	public User setPerformValue(String performValue) {
		this.performValue = performValue;
		return this;
	}

	public String getExperience() {
		return experience;
	}

	public User setExperience(String experience) {
		this.experience = experience;
		return this;
	}

	public String getIdentity() {
		return this.identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getMedalNum() {
		return medalNum;
	}

	public void setMedalNum(String medalNum) {
		this.medalNum = medalNum;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGender() {
		return gender;
	}

	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getLastLoginTime() {
		return lastLoginTime;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public static User parseJson(JSONObject userJson) throws JSONException {
		String id = userJson.getString("id");
		String nickName = userJson.getString("name");
		String level = userJson.getString("level");
		String posts = userJson.getString("posts");
		String perform = userJson.getString("perform");
		String experience = userJson.getString("experience");
		String logins = userJson.getString("logins");
		String life = userJson.getString("life");
		long milliseconds = userJson.getLong("lastlogin");
		Date date = new Date(milliseconds * 1000);
		SimpleDateFormat format = new SimpleDateFormat("MMM dd HH:mm:ss yyyy",
				Locale.ENGLISH);
		String time = format.format(date);
		String gender, astrology;
		if (userJson.has("gender")) {
			gender = userJson.getString("gender");
			Log.i("getUserProfileAPI", "gender is " + gender);
			astrology = userJson.getString("astro");
		} else {
			gender = "other";
			astrology = "other";
		}
		User user = new User();
		user.setNickName(nickName);
		user.setAstrology(astrology);
		user.setExperience(experience);
		user.setIdentity(level);
		user.setLifeValue(life);
		user.setPerformValue(perform);
		user.setPostTime(posts);
		user.setGender(gender);
		user.setLoginTime(logins);
		user.setId(id);
		user.setLastLoginTime(time);
		return user;
	}
	
	public static User getUser(JSONObject obj) throws HttpException{
		try {
			JSONObject userJson = obj.getJSONObject("user");
			return parseJson(userJson);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
	}

	public static User parseLogin(JSONObject obj) throws HttpException {
		User user = new User();

		try {
			String id = obj.getString("id");
			String name = obj.getString("name");
			String token = obj.getString("token");
			user.setId(id);
			user.setNickName(name);
			user.setToken(token);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
		return user;
	}

	public static List<User> parseOnlineUser(JSONObject obj)
			throws HttpException {
		JSONArray friendArray;
		List<User> friendList = new ArrayList<User>();
		try {
			friendArray = obj.getJSONArray("friends");
			friendList = new ArrayList<User>();
			for (int i = 0, len = friendArray.length(); i < len; i++) {
				JSONObject userJson = friendArray.getJSONObject(i);
				String userID = userJson.getString("id");
				String userFrom = userJson.getString("from");
				String nickName = userJson.getString("name");
				String mode = userJson.getString("mode");
				User user = new User();
				user.setId(userID).setNickName(nickName).setSourceIP(userFrom)
						.setStatus(mode);
				friendList.add(user);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
		
		return friendList;
	}

}
