package data.playlists;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

import java.io.IOException;

import org.apache.hc.core5.http.ParseException;


public class Playlist {
	private static final String accessToken = "AQB0RIRKfMayAslnvOHAUVSAXgvGIOgOdnUwGmZvqOgCG_CZyNN1Tzc2ZSDMpERF5RRSZIsRIL6EdPBmQy0_6z_sxSR9H3O3EJoGLJzW1o5-QQcJViezWrXTBmlS4x1CNUpvDEUo3PuYoK_2XP4jlGl0gTNJ2r3afloPJOhbHQ4oqQPVMGim1rY4yfb35NfAboyFHLccK8i4";
	
	private static final SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();
	private static final GetListOfCurrentUsersPlaylistsRequest playlistRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();
	
	public static void getListOfCurrentUsersPlaylist() {
		try {
			final Paging<PlaylistSimplified> playlists = playlistRequest.execute();
			System.out.println("Total: " + playlists.getTotal());
		} catch (ParseException | SpotifyWebApiException | IOException e) {
			System.out.println("Error: " + e.getMessage()); 
		}
	}
}
