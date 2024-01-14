package org.finos.waltz.model.authentication;


import org.immutables.value.Value;

/**
 * This object is typically created from the following properties:
 * 
 * <ul>
 *  <li> oauth.token_url </li>
  * <li> oauth.userinfo_url </li>
  * <li> oauth.code_verifier </li>
  * <li> oauth.redirect_uri </li>
 * </ul>
 */
@Value.Immutable
public abstract class OAuthConfiguration {

    public abstract String tokenUrl();
    public abstract String userInfoUrl();
    public abstract String codeVerifier();
    public abstract String redirectUri();

}
