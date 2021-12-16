package de.shd.spotify;

import de.core.serialize.Serializable;
import de.core.serialize.annotation.Element;

public class AccessToken implements Serializable {

	@Element(name="access_token") protected String accessToken;
	@Element(name="token_type") protected String tokenType;
	@Element(name="expires_in") protected int expiresIn;
	
	protected long createdAt=System.currentTimeMillis();

	public String getAccessToken() {
		return accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}
	
	public boolean isExpired() {
		return System.currentTimeMillis()>(createdAt+expiresIn);
	}
}
