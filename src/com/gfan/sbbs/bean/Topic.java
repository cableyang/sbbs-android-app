package com.gfan.sbbs.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gfan.sbbs.http.HttpException;

public class Topic implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	private String boardName;
	private String title;
	private String author;
	private String popularity;
	private String url;
	private String content;
	private String time;
	private boolean isOnTop;
	private String quote;
	private String quoter;
	private String sourceIP;
	private String num;
	private int id; 
	private boolean unRead, noresp, hasAtt,isAnonymous;
	private int replies;
	private int gid;
	private int reid;
	private List<Attachment> attList;

	public String getTime() {
		return time;
	}

	public Topic setTime(String time) {
		this.time = time;
		return this;
	}


	public Topic() {
		super();
	}

	public Topic(String boardName, String title, String author,
			String popularity, String url) {
		super();
		this.boardName = boardName;
		this.title = title;
		this.author = author;
		this.popularity = popularity;
		this.url = url;
	}

	

	public String getPopularity() {
		return popularity;
	}

	public Topic setPopularity(String popularity) {
		this.popularity = popularity;
		return this;
	}

	public String getAuthor() {
		return author;
	}

	public Topic setAuthor(String author) {
		this.author = author;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public Topic setTitle(String title) {
		this.title = title;
		return this;
	}

	@Override
	public String toString() {
		return "Topic [talkSpace=" + boardName + ", title=" + title
				+ ", author=" + author + ", popularity=" + popularity
				+ ", url=" + url + ", time=" + time + "content=" + content
				+ "]";
	}

	public Topic setContent(String content) {
		this.content = content;
		return this;
	}

	public String getContent() {
		return content;
	}

	public Topic setOnTop(boolean isOnTop) {
		this.isOnTop = isOnTop;
		return this;
	}

	public boolean isOnTop() {
		return isOnTop;
	}

	public Topic setQuote(String quote) {
		this.quote = quote;
		return this;
	}

	public String getQuote() {
		return quote;
	}

	public Topic setSourceIP(String sourceIP) {
		this.sourceIP = sourceIP;
		return this;
	}

	public String getSourceIP() {
		return sourceIP;
	}

	public Topic setNum(String num) {
		this.num = num;
		return this;
	}

	public String getNum() {
		return num;
	}

	public Topic setUnRead(boolean unRead) {
		this.unRead = unRead;
		return this;
	}

	public boolean isUnRead() {
		return this.unRead;
	}

	public Topic setUrl(String url) {
		this.url = url;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public Topic setId(int id) {
		this.id = id;
		return this;
	}

	public int getId() {
		return id;
	}

	public Topic setGid(int gid) {
		this.gid = gid;
		return this;
	}

	public int getGid() {
		return gid;
	}

	public Topic setReid(int reid) {
		this.reid = reid;
		return this;
	}
	
	public Topic setReid(String reid){
		this.reid = Integer.valueOf(reid.toString().trim()).intValue();
		return this;
	}

	public int getReid() {
		return reid;
	}

	public void setQuoter(String quoter) {
		this.quoter = quoter;
	}

	public String getQuoter() {
		return quoter;
	}

	public void setReplies(int replies) {
		this.replies = replies;
	}

	public int getReplies() {
		return replies;
	}

	public Topic setAttList(List<Attachment> list) {
		this.attList = list;
		return this;
	}

	public List<Attachment> getAttList() {
		return this.attList;
	}

	public void setHasAtt(boolean hasAtt) {
		this.hasAtt = hasAtt;
	}

	public boolean isHasAtt() {
		return hasAtt;
	}

	public static Topic parseTopic(JSONObject threadJson) throws JSONException {
		Topic topic = new Topic();
		int id = threadJson.getInt("id");
		String board = threadJson.getString("board");
		String author = threadJson.getString("author");
		long milliseconds = threadJson.getLong("time");
		String title = threadJson.getString("title");
		if (threadJson.has("reid")) {
			int reid = threadJson.getInt("reid");
			topic.setReid(reid);
		}
		if (threadJson.has("read")) {
			String read = threadJson.getString("read");
			topic.setPopularity(read);
		}
		if (threadJson.has("replies")) {
			int replies = threadJson.getInt("replies");
			topic.setReplies(replies);
		}
		if (threadJson.has("gid")) {
			int gid = threadJson.getInt("gid");
			topic.setGid(gid);
		}
		String time;
		if (milliseconds < 30000000) {
			time = String.valueOf(milliseconds);
		} else {
			Date date = new Date(milliseconds * 1000);
			SimpleDateFormat format = new SimpleDateFormat(
					"MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
			time = format.format(date);
		}
		if (threadJson.has("content")) {
			String content = threadJson.getString("content");
			topic.setContent(content);
		}
		topic.setAuthor(author).setId(id).setBoardName(board).setTime(time)
				.setTitle(title);
		if (threadJson.has("unread")) {
			boolean unread = threadJson.getBoolean("unread");
			topic.setUnRead(unread);
		}
		if (threadJson.has("top")) {
			boolean top = threadJson.getBoolean("top");
			topic.setOnTop(top);
		}
		StringBuffer sb = new StringBuffer();
		if (threadJson.has("quoter")) {
			String quoter = threadJson.getString("quoter");
			topic.setQuoter(quoter);
		}
		if (threadJson.has("quote")) {
			String quote = threadJson.getString("quote");
			sb.append(quote);
		}
		if (threadJson.has("attachments")) {
			topic.setHasAtt(true);
			JSONArray attArray = threadJson.getJSONArray("attachments");
			List<Attachment> list = new ArrayList<Attachment>();
			for (int i = 0, len = attArray.length(); i < len; i++) {
				Attachment att = Attachment.parseAtt(attArray.getJSONObject(i));
				list.add(att);
			}
			topic.setAttList(list);
		} else {
			topic.setHasAtt(false);
		}
		topic.setQuote(sb.toString());
		return topic;
	}
	
	public static Topic getTopic(JSONObject obj) throws HttpException{
		try {
			JSONObject threadJson = obj.getJSONObject("topic");
			return parseTopic(threadJson);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
	}

	public static List<Topic> parseTopicList(JSONObject obj)
			throws HttpException {
		List<Topic> topicList = new ArrayList<Topic>();
		Topic topic;
		try {

			JSONArray jsonArray = obj.getJSONArray("topics");
			for (int i = 0, len = jsonArray.length(); i < len; i++) {
				JSONObject topicJson = jsonArray.getJSONObject(i);
				topic = Topic.parseTopic(topicJson);
				topicList.add(topic);
			}
		} catch (JSONException jse) {
			throw new HttpException(jse.getMessage(), jse);
		}
		return topicList;
	}
	
	public static List<Topic> parseNoticeList(JSONArray topicArray) throws HttpException{
		List<Topic> topicList = new ArrayList<Topic>();
		Topic topic;
		try{
		for(int i = 0,len = topicArray.length();i < len;i++){
			topic = new Topic();
			JSONObject topicJson = topicArray.getJSONObject(i);
			String board = topicJson.getString("board");
			int id = topicJson.getInt("id");
			String user = topicJson.getString("user");
			String title = topicJson.getString("title");
			topic.setAuthor(user).setId(id).setBoardName(board)
					.setTitle(title);
			topicList.add(topic);
		}}catch(JSONException e){
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
		return topicList;
	}

	public void setNoresp(boolean noresp) {
		this.noresp = noresp;
	}

	public boolean isNoresp() {
		return noresp;
	}

	public Topic setBoardName(String boardName) {
		this.boardName = boardName;
		return this;
	}

	public String getBoardName() {
		return boardName;
	}

	public Topic setAnonymous(boolean isAnonymous) {
		this.isAnonymous = isAnonymous;
		return this;
	}

	public boolean isAnonymous() {
		return isAnonymous;
	}
}
