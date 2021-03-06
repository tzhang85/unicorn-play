package moderator;

import mpdconnectors.MPDPlaylistRepresentation;
import objects.PlaylistItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bff.javampd.objects.*;

import java.util.HashMap;

public class PlaylistStateManager implements IPlaylistUpdate{
	private static final Logger logger = LogManager.getLogger();

	public ArrayList<PlaylistItem> playlist;
	private PlaylistModerator par;//so it can tell the moderator if there's a change from the MPD end
	private MPDPlaylistRepresentation updater;
	
	public PlaylistStateManager(PlaylistModerator ins){
		logger.info("PLAYLIST STATE MANAGER INSTANTIATED");
		playlist = new ArrayList<PlaylistItem>();
		par = ins;
		updater = new MPDPlaylistRepresentation(this);
	}
	
	//Appends to the end of the list 
	public boolean addTrack(PlaylistItem song){
		logger.debug("addTrack(song) called");
		// this is deprecated for the moment because we're not optimzing yet

		// if(playlist.isEmpty()) {
		// 	return addTrack(song, 0);
		// }
		// else if (playlist.size() == 1){
		// 	return addTrack(song, 1);
		// }
		// else {
		// 	return playlist.add(song);
		// }

		updater.addSong(song);
		return true;
	}

	public boolean addTrack(PlaylistItem item, int position) {
		logger.debug("addTrack(song, "+position+") called");
		// playlist.add(position, item);
		// if(playlist.get(position).equals(item)){
		// 	if(position == 0){//start of the list
		// 		updater.setCurrent(item);//notify the playlistrepresentation in some way
		// 	} else if (position == 1){
		// 		updater.setNext(item);
		// 	}
		// }
		// logger.debug("Track Added: " + item.getSinger() + " - " + item.getSong() + "\n");
		// logger.debug("PLAYLIST STATE: \n");
		// logger.debug(toString());
		// return (playlist.get(position).equals(item));
		return addTrack(item);
	}
	
	public String toString() {
		String ret = "stuff\n";
		for(int i = 0; i < playlist.size(); i++) {
			ret += (i + ": " + playlist.get(i).getSinger() + " - " + playlist.get(i).getSong() + "\n");
		}
		
		return ret;
	}
	
	public boolean removeTrack(PlaylistItem song) {
		logger.debug("removeTrack(song) called");
		return playlist.remove(song);
	}

	public boolean removeTrack(int position) {
		logger.debug("removeTrack("+ position +") called");
		PlaylistItem check = playlist.remove(position);
		if(check != null){
			if(position == 0){
				
			} else if(position == 1){
				
			}
		}
		return check != null;
	}
	
	//for onNewJoin or whatever that method is in the moderator
	public List<Map<String, Object>> getMap(){ //kinda crappy method name
		logger.debug("getMap() called");
		//for every item
		//get the Map<String, Object> and package them together
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		for(PlaylistItem item : playlist) {
			ret.add(item.getMap());
		}
		return ret;
	}
    //these need to be mirrored by the Moderator.
	@Override
	public void popCurrentlyPlaying() {
		logger.debug("popCurrentlyPlaying()");
		playlist.remove(0);//depending on how sendsync works this may be double-removing
		
		par.removeTop();
		//Should pull next from collaboration model
	}
	@Override
	public boolean verifyCurrentlyPlaying(PlaylistItem whatMPDIsCurrentlyPlaying) {
		logger.debug("verifyCurrentlyPlaying() called");
		logger.debug("PLAYLIST STATE at verifyCurrentlyPlaying(): \n");
		logger.debug(toString());
		
		boolean ret = false;
		
		if (playlist.get(0).equals(whatMPDIsCurrentlyPlaying)) {
			ret = true;
		}
		
		return ret;
	}
	@Override
	public void forceCurrentlyPlaying(PlaylistItem whatMPDIsCurrentlyPlaying) {
		logger.debug("forceCurrentlyPlaying() called");
		playlist.add(0, whatMPDIsCurrentlyPlaying);//depending on how sendsync works this may be double-adding
		par.addTop(whatMPDIsCurrentlyPlaying);
	}

	@Override
	public PlaylistItem getNext() {
		logger.debug("getNext() called");
		return playlist.get(1);
	}

	// //If the song is not in the list, it just adds the song. Otherwise, it promotes it the specified number of positions.
	// public boolean promoteTrack(PlaylistItem song, int positions){
	// 	return true;
	// }

	// public boolean pushToTop(PlaylistItem item) {
	// 	return true;
	// }

	// protected boolean pushNextToMPD(){
	// 	return true;
	// }

	// protected boolean updateTheMPDPlaylist(){
	// 	return true;
	// }

	// public boolean updateCurrentlyPlaying(PlaylistItem song){
	// 	try{
	// 		//FIXME to use a playlistItem and not a song
	// 		//playlist.set(0, song);
	// 	}
	// 	catch (Exception e) {
	// 		e.printStackTrace();
	// 	}

	// 	return true;
	// }


}