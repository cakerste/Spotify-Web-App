package authorization.authorization_code;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.PagingCursorbased;
import com.wrapper.spotify.model_objects.specification.PlayHistory;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import edu.ncsu.csc316.dsa.list.List;
import edu.ncsu.csc316.dsa.list.SinglyLinkedList;
import edu.ncsu.csc316.dsa.sorter.MergeSorter;
import edu.ncsu.csc316.dsa.sorter.Sorter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

import org.apache.hc.core5.http.ParseException;

public class AuthorizationCodeUri {
  private static final String clientId = "a6121e25ede04a19acd6d54a13253e66";
  private static final String clientSecret = "5fef0a96b0f744aa8c00c2b1aa4bea55";
  private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8888/callback");
  public static final int LIMIT = 50;
  public static final int RECS_PER_ARTIST = 3;
  public static final int NUM_OF_ARTISTS = 3;

  private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
    .setClientId(clientId)
    .setClientSecret(clientSecret)
    .setRedirectUri(redirectUri)
    .build();
  private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
          .state("x4xkmn9pu3j6ukrs8n")
          .scope("user-read-recently-played")
          .show_dialog(true)
    .build();
  
  private static AuthorizationCodeRequest authorizationCodeRequest;

  	public static void login() {
	  	final URI uri = authorizationCodeUriRequest.execute();
	  
	  	try {
			java.awt.Desktop.getDesktop().browse(uri);
			
			Scanner scan = new Scanner(System.in);
			System.out.println("Enter URL that you were redirected to: ");
			String code = scan.next();
			code = code.substring(36, code.length() - 25);
			authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
			scan.close();
			AuthorizationCodeCredentials authCodeCredentials = authorizationCodeRequest.execute();
			spotifyApi.setAccessToken(authCodeCredentials.getAccessToken());
			spotifyApi.setRefreshToken(authCodeCredentials.getRefreshToken());
		} catch (IOException | SpotifyWebApiException | ParseException e) {
			System.out.println("Error logging in: " + e.getMessage());
		}
  	}
  	public static PagingCursorbased<PlayHistory> getRecentTracks() {
  		try {
			return spotifyApi.getCurrentUsersRecentlyPlayedTracks().limit(LIMIT).build().execute();
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			System.out.println("Error getting the recent tracks");
			return null;
		}
  	}
  	public static String getHTML(URL url) {
  		try {
  			HttpURLConnection con = (HttpURLConnection)url.openConnection();
  			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
  			String inputLine;
  			StringBuffer content = new StringBuffer();
  			while ((inputLine = in.readLine()) != null) {
  				content.append(inputLine);
  				content.append("\n");
  			}
  			in.close();
  			return content.toString();
  		} catch (IOException e) {
  			System.out.println("Error reading webpage");
  			return null;
  		}
  		
  	}
  	public static void authorizationCodeUri_Sync() {
	    PagingCursorbased<PlayHistory> recentTracks = getRecentTracks();
		PlayHistory[] tracks = recentTracks.getItems();
		List<RecentArtist> artists = new SinglyLinkedList<RecentArtist>();
		for (int i = 0; i < LIMIT; i++) {
			String a = tracks[i].getTrack().getArtists()[0].getName();
			boolean inList = false;
			for (RecentArtist rec : artists) {
				if (a.equals(rec.getArtist())) {
					rec.incrementFrequency();
					inList = true;
				}
			}
			if (!inList) {
				artists.addLast(new RecentArtist(a, 1));
			}
		}
		RecentArtist[] artistsArray = new RecentArtist[artists.size()];
		for (int i = 0; i < artists.size(); i++) {
			artistsArray[i] = artists.get(i);
		}
		
		Sorter<RecentArtist> sorter = new MergeSorter<>();
		sorter.sort(artistsArray);
		for (int i = 0; i < NUM_OF_ARTISTS; i++) {
			URL url = createURL(artistsArray[i].getArtist());
			String content = getHTML(url);
			List<String> artistRecs = parseHTML(content.toString());
			for (String artist : artistRecs) {
				System.out.println(artist);
			}
		}
  	}
  
  	private static URL createURL(String artist) {
  		StringBuffer str = new StringBuffer("https://www.music-map.com/");
		for (int i = 0; i < artist.length(); i++) {
			if (artist.charAt(i) == ' ') {
				str.append('+');
			} else if (artist.charAt(i) != '.') {
				str.append(artist.charAt(i));
			}
		}
		try {
			return new URL(str.toString());
		} catch (MalformedURLException e) {
			System.out.println("Invalid artist");
			return null;
		}
	}
  
    private static List<String> parseHTML(String html) {
	    List<String> artistRecs = new SinglyLinkedList<String>();
	    for (int i = 1; i <= RECS_PER_ARTIST; i++) {
		    String id = "id=s" + i;
		    int idx = html.indexOf(id) + 6;
		    StringBuffer artistName = new StringBuffer();
		    boolean endOfName = false;
		    while (!endOfName) {
			    if (html.charAt(idx) == '<') {
				    endOfName = true;
			    } else {
				    artistName.append(html.charAt(idx));
			    }
			    idx++;
		    }
		    artistRecs.addLast(artistName.toString());
	    }
	    return artistRecs;
    }
  
  	private static class RecentArtist implements Comparable {
  		private String artist;
	    private int frequency;
	  
	  public RecentArtist(String artist, int frequency) {
		  this.artist = artist;
		  this.frequency = frequency;
	  }
	  
	  public String getArtist() {
		  return this.artist;
	  }
	  
	  public void incrementFrequency() {
		  this.frequency++;
	  }
	  
	  public int getFrequency() {
		  return this.frequency;
	  }

	  @Override
	  public int compareTo(Object o) {
		  RecentArtist oArtist = (RecentArtist)o;
		  if (this.getFrequency() > oArtist.getFrequency()) {
			  return -1;
		  } else if (this.getFrequency() < oArtist.getFrequency()) {
			  return 1;
		  }
		  return 0;
	  }
	  
    }
  	public static void main(String[] args) {
		login();
	    authorizationCodeUri_Sync();
	}
}