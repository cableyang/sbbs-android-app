package com.gfan.sbbs.dao.topic;

import com.gfan.sbbs.bean.Topic;
import com.gfan.sbbs.http.HttpException;
import com.gfan.sbbs.othercomponent.BBSOperator;
import com.gfan.sbbs.othercomponent.MyApplication;
import com.gfan.sbbs.othercomponent.SBBSConstants;



public class NewTopicSendDao {
	private String sendTopicUrl, editTopicUrl;
	
	private static final String TAG = NewTopicSendDao.class.getName();


	public NewTopicSendDao() {
		sendTopicUrl = SBBSConstants.BASE_API_URL + "/topic/post.json?type="
				+ SBBSConstants.CLIENT_TYPE_ANDROID + "&token="
				+ MyApplication.getInstance().getToken();
		editTopicUrl = SBBSConstants.BASE_API_URL + "/topic/edit.json?type="
				+ SBBSConstants.CLIENT_TYPE_ANDROID + "&token="
				+ MyApplication.getInstance().getToken();
	}

	public Topic sendNewTopic(Topic topic) throws HttpException {
			return BBSOperator.getInstance().doPost(sendTopicUrl, topic);
	}

	public Topic editTopicDao(Topic topic) throws HttpException {
		return BBSOperator.getInstance().doEdit(editTopicUrl, topic);
	}


}
