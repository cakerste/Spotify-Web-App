package authorization.authorization_code;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthorizationCode {
  private static final String clientId = "a6121e25ede04a19acd6d54a13253e66";
  private static final String clientSecret = "5fef0a96b0f744aa8c00c2b1aa4bea55";
  private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8888/callback");
  private static String code = 
		  "AQAp5SiHcUZgoDbBYhvloHJ-eyXLKWgyPfnnD-Fjvw6gDTugRL5DVCIk88WQWzcwTJ22VufAJ7EcF8iNKCGWE5iXXyJ61ODa4r0apR9-2pI7ApBV1ZpLtoqJgsG-jquL8ljiz_oj4bTLSCajO_i25G0TBZasginoTQvo7uNsmERAeoTPthw098ZJQJsH9XzzHA0bGoO2cjRqgtjwn5sVvjmXdOju3_wteklj&state=x4xkmn9pu3j6ukrs8n";
  private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
    .setClientId(clientId)
    .setClientSecret(clientSecret)
    .setRedirectUri(redirectUri)
    .build();
  
  private static AuthorizationCodeRequest authorizationCodeRequest;
  
  public static void getAuthCode() {
	  URI uri = spotifyApi.authorizationCodeUri().build().execute();
	  //code = uri.toString();
	  authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
  }

  public static void main(String[] args) {
	    authorizationCode_Sync();
	    authorizationCode_Async();
  }
  
  public static void authorizationCode_Sync() {
    try {
      getAuthCode();
      final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

      // Set access and refresh token for further "spotifyApi" object usage
      spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
      spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

      System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  public static void authorizationCode_Async() {
    try {
      final CompletableFuture<AuthorizationCodeCredentials> authorizationCodeCredentialsFuture = authorizationCodeRequest.executeAsync();

      // Thread free to do other tasks...

      // Example Only. Never block in production code.
      final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeCredentialsFuture.join();

      // Set access and refresh token for further "spotifyApi" object usage
      spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
      spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

      System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());
    } catch (CompletionException e) {
      System.out.println("Error: " + e.getCause().getMessage());
    } catch (CancellationException e) {
      System.out.println("Async operation cancelled.");
    }
  }
}