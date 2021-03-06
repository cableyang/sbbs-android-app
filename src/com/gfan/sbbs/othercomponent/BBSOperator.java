package com.gfan.sbbs.othercomponent;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.gfan.sbbs.bean.Board;
import com.gfan.sbbs.bean.Mail;
import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.bean.User;
import com.gfan.sbbs.http.HttpClient;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.http.Response;

public class BBSOperator {

	private HttpClient mClient;
//	private static final int IMAGE_MAX_WIDTH = 720;
//	private static final int IMAGE_MAX_HEIGHT = 1200;
	private static BBSOperator bbsOP = null;
	private static final String TAG = "BBSOperator";

	private BBSOperator() {
		mClient = new HttpClient();
	}
	
	public static BBSOperator getInstance(){
		if(null == bbsOP){
			bbsOP = new BBSOperator();
		}
		return bbsOP;
	}

	/**Login task
	 * 
	 * @param name
	 * @param passWord
	 * @return
	 * @throws HttpException
	 */
	public User doLogin(String name, String passWord) throws HttpException {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("user", name));
		params.add(new BasicNameValuePair("pass", passWord));
		return User.parseLogin(getJsonSuccess(SBBSConstants.LOGIN_URL, params));
	}

	/**
	 * 
	 * @param url
	 * @param board
	 * @param title
	 * @param content
	 * @param reid
	 * @return
	 * @throws HttpException
	 */

	public Topic doPost(String url, String board, String title, String content,
			String reid) throws HttpException {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("board", board));
		params.add(new BasicNameValuePair("title", title));
		params.add(new BasicNameValuePair("content", content));
		params.add(new BasicNameValuePair("reid", reid));
		/**ToDo
		 * 增加是否匿名选项
		 */
		if("Psychology".equals(board)){			
			params.add(new BasicNameValuePair("anony", "true"));
		}
		Log.i(TAG, "reid is " + reid);
		return Topic.getTopic(getJsonSuccess(url, params));
	}

	/**
	 * do upload task
	 * @param url
	 * @param fileUrl
	 * @param params
	 * @throws HttpException
	 */
	public void doUploadAttachment(String url, String fileUrl, List<BasicNameValuePair> params) throws HttpException{
		post(url,new File(fileUrl),null);
	}
	/**
	 * 发帖
	 * @param sendUrl
	 * @param topic
	 * @return
	 * @throws HttpException
	 */
	public Topic doPost(String sendUrl,Topic topic ) throws HttpException{
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		String reID = Integer.valueOf(topic.getReid()).toString();
		params.add(new BasicNameValuePair("board", topic.getBoardName()));
		params.add(new BasicNameValuePair("title", topic.getTitle()));
		params.add(new BasicNameValuePair("content", topic.getContent()));
		params.add(new BasicNameValuePair("reid", reID));
		/**TODO
		 * 增加是否匿名选项
		 */
		if(topic.isAnonymous()){			
			params.add(new BasicNameValuePair("anony", "true"));
		}
		Log.i(TAG, "reid is " + topic.getReid());
		return Topic.getTopic(getJsonSuccess(sendUrl, params));
	}
	/**
	 * 
	 * @param url
	 * @param board
	 * @param title
	 * @param content
	 * @param id
	 * @return
	 * @throws HttpException
	 */
	public Topic doEdit(String url, String board, String title, String content,
			String id) throws HttpException {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("board", board));
		params.add(new BasicNameValuePair("title", title));
		params.add(new BasicNameValuePair("content", content));
		params.add(new BasicNameValuePair("id", id));
		return Topic.getTopic(getJsonSuccess(url, params));
	}

	public Topic doEdit(String url,Topic topic) throws HttpException{
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("board", topic.getBoardName()));
		params.add(new BasicNameValuePair("title", topic.getTitle()));
		params.add(new BasicNameValuePair("content", topic.getContent()));
		params.add(new BasicNameValuePair("id", Integer.valueOf(topic.getId()).toString()));
		return Topic.getTopic(getJsonSuccess(url, params));
		
	}
	
	public boolean doPostMail(String url, String user, String title,
			String content, String reid) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("user", user));
		params.add(new BasicNameValuePair("title", title));
		params.add(new BasicNameValuePair("content", content));
		params.add(new BasicNameValuePair("reid", reid));
		try {
			getJsonSuccess(url, params);
		} catch (HttpException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}

	public User getUserProfile(String url) throws HttpException {
		return User.getUser(getJsonSuccess(url));
	}

	/**
	 * get online friends, and maybe all friends
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 */
	public List<User> getFriends(String url) throws HttpException {
		return User.parseOnlineUser(getJsonSuccess(url));
	}

	/**
	 * used for getHotList, threadList etc FIXME here,blacklist is not under
	 * consideration
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 */
	public List<Topic> getTopicList(String url) throws HttpException {
		return Topic.parseTopicList(getJsonSuccess(url));
	}

	/**
	 * used for get mailList
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 */
	public List<Mail> getMailList(String url) throws HttpException {
		return Mail.parseMailList(getJsonSuccess(url));
	}

	/**
	 * used for get mail content
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 */
	public Mail getMail(String url) throws HttpException {
		return Mail.parseJson(getJsonSuccess(url));
	}

	/**
	 * get new notice
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 */
	public HashMap<String, Object> getNoticeList(String url)
			throws HttpException {
		HashMap<String, Object> noticeArray = new HashMap<String, Object>();
		JSONObject obj;
		obj = getJsonSuccess(url);
		List<Mail> mailList = new ArrayList<Mail>();
		List<Topic> atsList = new ArrayList<Topic>();
		List<Topic> replyList = new ArrayList<Topic>();
		try {
			if (obj.has("mails")) {
				JSONArray mailArray = obj.getJSONArray("mails");
				mailList = Mail.parseNoticeMailList(mailArray);

			}
			if (obj.has("ats")) {
				JSONArray atsArray = obj.getJSONArray("ats");
				atsList = Topic.parseNoticeList(atsArray);

			}
			if (obj.has("replies")) {
				JSONArray atsArray = obj.getJSONArray("replies");
				replyList = Topic.parseNoticeList(atsArray);

			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
		noticeArray.put("mail", mailList);
		noticeArray.put("ats", atsList);
		noticeArray.put("reply", replyList);
		return noticeArray;
	}

	/**
	 * get all boards
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 */
	public List<List<Board>> getAllBoards(String url) throws HttpException {
		List<List<Board>> allBoardList = new ArrayList<List<Board>>();
		try {
			JSONObject obj = getJsonSuccess(url);
			JSONArray groupArray = obj.getJSONArray("boards");
			for (int i = 0, len = groupArray.length(); i < len; i++) {
				JSONObject groupJson = groupArray.getJSONObject(i);
				JSONArray boardsJson = groupJson.getJSONArray("boards");
				List<Board> list = Board.parseBoardArray(boardsJson, false);
				allBoardList.add(list);
			}
		} catch (JSONException e) {
			throw new HttpException(e.getMessage(), e);
		}
		return allBoardList;
	}

	/**
	 * used for add/delete events,just need a true/false result
	 * 
	 * @param url
	 * @return
	 */
	public boolean getBoolean(String url) {
		try {
			getJsonSuccess(url);
		} catch (HttpException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * get favList,sorted according to isHasRead
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 */
	public List<Board> getFavList(String url) throws HttpException {
		return Board.getSortedBoardList(getJsonSuccess(url));
	}

	/**
	 * get board list,used for the search result or sections
	 * 
	 * @param url
	 * @return
	 * @throws HttpException
	 */
	public List<Board> getBoardList(String url) throws HttpException {
		return Board.getBoardList(getJsonSuccess(url), false);
	}

	public Bitmap getBitmap(String url) throws HttpException {
		InputStream inStream = get(url).asStream();
		Bitmap bitmap;
		try {
			bitmap = BitmapFactory.decodeStream(inStream);
		} catch (OutOfMemoryError oome) {
			Log.e(TAG, "OutofMemoryError");
			throw new HttpException(oome.getMessage());
		}
		return bitmap;
	}

	public Response get(String url) throws HttpException {
		return mClient.httpRequest(url);
	}

	public Response post(String url, List<BasicNameValuePair> params)
			throws HttpException {
		return mClient.httpRequest(url, null, params);
	}

	/**
	 * attachment upload
	 * @param url
	 * @param file
	 * @param params
	 * @return
	 * @throws HttpException
	 */
	public Response post(String url, File file, List<BasicNameValuePair> params)
			throws HttpException {
		return mClient.httpRequest(url, file, params);
	}

	public JSONObject getJson(String url, List<BasicNameValuePair> params)
			throws HttpException {
		return post(url, params).asJSONObject();
	}

	public JSONObject getJson(String url) throws HttpException {
		return get(url).asJSONObject();
	}

	public JSONObject getJson(String url, File file,
			List<BasicNameValuePair> params) throws HttpException {
		return post(url, file, params).asJSONObject();
	}

	public JSONObject getJsonSuccess(String url, List<BasicNameValuePair> params)
			throws HttpException {
		boolean success = false;
		JSONObject obj = getJson(url, params);
		try {
			success = obj.getBoolean("success");

			if (success) {
				Log.i(TAG, "reply or post success");
				return obj;
			} else {
				String error = obj.getString("error");
				Log.i(TAG, "reply or post error");
				throw new HttpException(error);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
	}

	public JSONObject getJsonSuccess(String url) throws HttpException {
		boolean success = false;
		JSONObject obj = getJson(url);
		try {
			success = obj.getBoolean("success");

			if (success) {
				return obj;
			} else {
				String error = obj.getString("error");
				throw new HttpException(error);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
	}

	public JSONObject getJsonSuccess(String url, File mFile,
			List<BasicNameValuePair> params) throws HttpException {
		boolean success = false;
		Log.i(TAG, "mFile's path is " + mFile.getAbsolutePath());
		JSONObject obj = getJson(url, mFile, params);
		try {
			success = obj.getBoolean("success");

			if (success) {
				return obj;
			} else {
				String error = obj.getString("error");
				throw new HttpException(error);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
	}

}
