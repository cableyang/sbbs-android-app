package com.gfan.sbbs.http;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

public class HttpClient {
	private DefaultHttpClient mClient;
	private static final String TAG = "httpClient";

	public HttpClient() {
		onPrepareHttpClient();
	}

	public void onPrepareHttpClient() {
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setTimeout(params, 5000);
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 10000);

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schReg.register(new Scheme("https",
				SSLSocketFactory.getSocketFactory(), 443));
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
				params, schReg);
		mClient = new DefaultHttpClient(conMgr, params);

	}

	public Response httpRequest(String url, File file,
			List<BasicNameValuePair> postParams) throws HttpException {
		Log.i(TAG, "url need to process is "+url);
		HttpResponse response = null;
		Response res = null;
		HttpPost post = new HttpPost(url);
		HttpEntity entity = null;
		try {
			if (null != file) {
				entity = createMultipartEntity("file", file, postParams);
			} else if (null != postParams) {
				entity = new UrlEncodedFormEntity(postParams, HTTP.UTF_8);
			}
			post.setEntity(entity);
			response = mClient.execute(post);
			res = new Response(response);
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			throw new HttpException(e.getMessage(), e);
		}
		
	}

	public Response httpRequest(String url) throws HttpException {
		Log.i(TAG, "url need to process is "+url);
		HttpResponse response = null;
		Response res = null;
		HttpGet get = new HttpGet(url);
		get.addHeader("Accept-Encoding", "gzip, deflate");
		get.addHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		get.addHeader("Accept-Language", "en-us,en;q=0.5");
		try {
			response = mClient.execute(get);
			int statusCode = response.getStatusLine().getStatusCode();
			if(404 == statusCode){
				throw new HttpException("404 not found", statusCode);
			}
			res = new Response(response);
			return res;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new HttpException(ioe.getMessage(), ioe);
		}
	}

	@SuppressWarnings("unused")
	private URI createURI(String url) throws HttpException {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage());
			throw new HttpException("Invalid URL.");
		}

		return uri;
	}
	

	private MultipartEntity createMultipartEntity(String filename, File file,
			List<BasicNameValuePair> postParams)
			throws UnsupportedEncodingException {
		MultipartEntity entity = new MultipartEntity();
		// Don't try this. Server does not appear to support chunking.
		// entity.addPart("media", new InputStreamBody(imageStream, "media"));

		entity.addPart(filename, new FileBody(file));
		if(null == postParams){
			return entity;
		}
		
		for (BasicNameValuePair param : postParams) {
			entity.addPart(param.getName(), new StringBody(param.getValue()));
		}
		return entity;
	}
}
