package de.shd.spotify;

import java.lang.management.OperatingSystemMXBean;
import java.net.URLEncoder;
import java.util.HashMap;

import de.core.http.Http;
import de.core.http.HttpRequest;
import de.core.http.HttpResponse;
import de.core.http.HttpServer;
import de.core.http.connector.DefaultConnector;
import de.core.http.handler.AbstractHttpRequestHandler;
import de.core.http.handler.FixedLengthHttpResponse;
import de.core.log.Logger;
import de.core.serialize.Coding;
import de.core.service.Service;
import de.core.utils.Streams;

public class Spotify implements Service {
	
	public static final String GRANT_TYPE_CLIENT_CREDENTIALS="client_credentials";
	public static final String GRANT_TYPE_AUTHORIZATION_CODE="authorization_code";
	
	private class SpotifyRequestHanderler extends AbstractHttpRequestHandler {
		
		public SpotifyRequestHanderler() {
			super("/spotify");
		}

		@Override
		public HttpResponse handleRequest(HttpRequest paramHttpRequest) {
			String accesscode=paramHttpRequest.getRequestParameter("code");
			fetchToken("authorization_code", accesscode);
			
			
			return new FixedLengthHttpResponse("ok".getBytes(), 200);
		}

		@Override
		public boolean keepAlive() {
			return false;
		}
		
	}

	Logger logger=Logger.createLogger("SpotifyAPI");
	
	protected String spotifyLoginHost="https://accounts.spotify.com/api";
	protected String spotifyApiHost="https://api.spotify.com/v1";
	protected final String tokenEndpoint="/token";
	protected String scope="user-modify-playback-state user-read-private user-read-email";
	protected String clientId="60bbf6b6d3054ac9a9ee203f30cfefad";
	protected String clientSecret="2d6c7bcba43843beb94ed6594df9533a";
	protected String grantType;
	protected String redirectURL="http://localhost:7000/spotify";
	protected SpotifyRequestHanderler httpHandler=new SpotifyRequestHanderler();
	
	protected AccessToken accessToken;
	
	public Spotify() {
		httpHandler.registerHttpRequestHandler();
	}
	
	protected void fetchToken(String grantType,String code) {
		HashMap<String,String>header=new HashMap<>();
		header.put("Authorization", "Basic " + Coding.toBase64((clientId+":"+clientSecret).getBytes()));
		header.put(Http.CONTENT_TYPE, Http.CONTENT_TYPE_FORM_URLENCODED);
		try {
			String data="grant_type="+grantType
						+(code!=null?"&code="+code:"")
						+"&redirect_uri="+URLEncoder.encode("http://localhost:7000/spotify#done","UTF-8");
			
			byte[] response=Http.post(spotifyLoginHost+tokenEndpoint, header, data.getBytes());
			this.accessToken=Coding.decode(response,"json", AccessToken.class);
		} catch (Throwable t) {
			accessToken=null;
			logger.error("failed to fetch access token", t);
		}
	}
	
	public void fetchToken() {
		try {
			if(GRANT_TYPE_CLIENT_CREDENTIALS.equals(this.grantType)) {
				
			} else {
				String loginUrl="https://accounts.spotify.com/authorize?response_type=code&client_id="+clientId
						+"&scope="+URLEncoder.encode(scope, "UTF-8")
						+"&redirect_uri="+URLEncoder.encode(redirectURL,"UTF-8");
				System.out.println();
			}
			
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void listDevices() throws Exception {
		System.out.println(callApi("/me/player/devices"));
	}
	
	protected byte[] callApi(String url) throws Exception {
		if(accessToken!=null) {
			HashMap<String,String> header=new HashMap<>();
			header.put(Http.CONTENT_TYPE, "application/json");
			header.put("Authorization", "Bearer "+accessToken.getAccessToken());
			return Streams.readAll(Http.get(redirectURL,  header).getContent());
		}
		return null;
	}
	
	public static void main(String[] args) throws InterruptedException {
		HttpServer http=new HttpServer();
		DefaultConnector connector=new DefaultConnector();
		connector.init(http);
		http.addConnector(connector);
		http.start();
		new Spotify().fetchToken();
		
		
		Thread.sleep(Integer.MAX_VALUE);
	}

}
