package authorization.authorization_code;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.Artist;
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
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.hc.core5.http.ParseException;

/**
 * This program looks at your most recent 50 songs and gives you artist recommendations based on it
 * @author Collin Kersten
 *
 */
public class AuthorizationCodeUri {
	/** Change the first three variables to whatever you use in your project */
    private static final String clientId = "a6121e25ede04a19acd6d54a13253e66";
    private static final String clientSecret = "5fef0a96b0f744aa8c00c2b1aa4bea55";
    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8888/callback");
    /** the amount of songs to look at (Spotify API caps it at 50) */
    public static final int LIMIT = 50;
    /** the amount of recommendations per artist */
    public static final int RECS_PER_ARTIST = 3;
    /** Look at the top 3 artists for recommendations */
    public static final int NUM_OF_ARTISTS = 3;

    /** builds the spotify API with a unique client id, client secret, and redirect URI */
    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
      .setClientId(clientId)
      .setClientSecret(clientSecret)
      .setRedirectUri(redirectUri)
      .build();
    /** builds a URI for authorization */
    private static final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
          .state("x4xkmn9pu3j6ukrs8n")
          .scope("user-read-recently-played")
          .show_dialog(true)
    .build();
  
    private static AuthorizationCodeRequest authorizationCodeRequest;
    /**
     * opens spotify
     */
    public static void openSpotify() {
    	final URI uri = authorizationCodeUriRequest.execute();
    	try {
			java.awt.Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			System.out.println("Error opening link");
		}
    }
    /**
     * logs in to the api using the url that the user was directed to
     * @param code url that the user was redirected to
     * @return exit status, 0 for success 1 for failure
     */
  	public static int login(String code) {
	  	try {
			code = code.substring(36, code.length() - 25);
			authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
			AuthorizationCodeCredentials authCodeCredentials = authorizationCodeRequest.execute();
			spotifyApi.setAccessToken(authCodeCredentials.getAccessToken());
			spotifyApi.setRefreshToken(authCodeCredentials.getRefreshToken());
			return 0;
		} catch (IOException | SpotifyWebApiException | ParseException | IndexOutOfBoundsException e) {
			return 1;
		}
  	}
  	/**
  	 * gets the recent 50 tracks
  	 * @return recent tracks in a list
  	 */
  	private static PagingCursorbased<PlayHistory> getRecentTracks() {
  		try {
			return spotifyApi.getCurrentUsersRecentlyPlayedTracks().limit(LIMIT).build().execute();
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			System.out.println("Error getting the recent tracks");
			return null;
		}
  	}
  	/**
  	 * gets the HTML page for the certain artist from www.music-map.com
  	 * @param url to get the html page from
  	 * @return html page
  	 */
  	private static String getHTML(URL url) {
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
  	/**
  	 * Looks at the artists of the recent tracks and sorts them by most listened to artists first
  	 * @return array of the artists names, sorted
  	 */
  	public static String[] getArtists() {
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
		String[] artistsNameArray = new String[NUM_OF_ARTISTS];
		for (int i = 0; i < NUM_OF_ARTISTS; i++) {
			artistsNameArray[i] = artistsArray[i].getArtist();
		}
		return artistsNameArray;
  	}
  	/**
  	 * gets the artist recommendations in a string array
  	 * @return artist recommendations
  	 */
  	public static String[] authorizationCodeUri_Sync() {
  		String[] artistsArray = getArtists();
  		String[] artistRecsArray = new String[RECS_PER_ARTIST * NUM_OF_ARTISTS];
  		int j = 0;
		for (int i = 0; i < NUM_OF_ARTISTS; i++) {
			URL url = createURL(artistsArray[i]);
			String content = getHTML(url);
			List<String> artistRecs = parseHTML(content.toString());
			
			for (String artist : artistRecs) {
				artistRecsArray[j++] = artist; 
			}
		}
		return artistRecsArray;
  	}
  	
  	/**
  	 * opens up an artists profile on the spotify web player
  	 * @param name of the artist
  	 */
  	public static void getArtistInfo(String name) {
  		try {
			Artist[] a = spotifyApi.searchArtists(name).build().execute().getItems();
			URI url = new URI("https://open.spotify.com/artist/" + a[0].getId());
			java.awt.Desktop.getDesktop().browse(url);
		} catch (ParseException | SpotifyWebApiException | IOException | URISyntaxException e) {
			System.out.println(e.getMessage());
		}
  	}
  
  	/**
  	 * creates a URL by appending the artist name to the end of www.music-map.com
  	 * @param artist to search
  	 * @return newly created URL
  	 */
  	private static URL createURL(String artist) {
  		StringBuffer str = new StringBuffer("https://www.music-map.com/");
		for (int i = 0; i < artist.length(); i++) {
			if (artist.charAt(i) == ' ') {
				str.append('+');
			} else if (artist.charAt(i) == ',') { // Special case 1
				str.append("%2C");
			} else if (artist.charAt(i) != '.') { // Special case 2
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
  
  	/**
  	 * gets the artist recommendations from the HTML page
  	 * @param html page to look at
  	 * @return artist recommendations in a list
  	 */
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
  
    /**
     * Recent Artist class, used to keep track of the frequency, implements comparable so it is sortable
     * @author Collin Kersten
     *
     */
  	private static class RecentArtist implements Comparable {
  		private String artist;
	    private int frequency;
	  
	  /**
	   * Constructor, sets the artist name and frequency
	   * @param artist name
	   * @param frequency of the artist
	   */
	  public RecentArtist(String artist, int frequency) {
		  this.artist = artist;
		  this.frequency = frequency;
	  }
	  
	  /**
	   * gets the artist
	   * @return artist
	   */
	  public String getArtist() {
		  return this.artist;
	  }
	  /**
	   * increments the frequency
	   */
	  public void incrementFrequency() {
		  this.frequency++;
	  }
	  
	  /**
	   * gets the frequency
	   * @return frequency
	   */
	  public int getFrequency() {
		  return this.frequency;
	  }

	  /**
	   * compares two recentartists
	   * @param o other RecentArtist
	   * @return compare value
	   */
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
  	
  	/**
  	 * gets the users display name
  	 * @return display name
  	 */
  	public static String getUser() {
  		try {
			return spotifyApi.getCurrentUsersProfile().build().execute().getDisplayName();
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			return null;
		}
  	}
}