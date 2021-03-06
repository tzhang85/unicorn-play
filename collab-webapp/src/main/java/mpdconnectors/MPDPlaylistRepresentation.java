package mpdconnectors;

import moderator.PlaylistStateManager;
import moderator.IPlaylistUpdate;
import objects.PlaylistItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bff.javampd.*;
import org.bff.javampd.objects.*;

import java.util.Timer;
import java.util.TimerTask;

public class MPDPlaylistRepresentation{
	private static final Logger logger = LogManager.getLogger();
	
	SongConverter converter = SongConverter.getConverter();

	MPDSong current;
	MPDSong next;
	MPD mpd;
	Playlist playlist;
	IPlaylistUpdate playlistStateManager;
	Timer timer;

	public MPDPlaylistRepresentation(PlaylistStateManager playlistStateManager) {
		logger.debug("MPDPlaylistRepresentation INSTANTIATED");
		this.playlistStateManager = playlistStateManager;
		this.timer = new Timer();
		mpd = MPDWrapper.getMPD();
		try{
			this.playlist = mpd.getPlaylist();
			mpd.getAdmin().updateDatabase();
		}
		catch (Exception e){
			logger.error(e.getMessage());
		}
		current = null;
		next = null;

		//don't play yet!
	}

	public void addSong(PlaylistItem item) {
		logger.debug("addSong(PlaylistItem) called");
		try{
		MPDSong song = SongConverter.getConverter().playlistItemToMPDSong(item);
		addSong(song);
		}
		catch(Exception e){
			logger.error(e.getMessage());
		}
	}

	public void addSong(MPDSong song) {
		try{
		logger.debug("addSong(MPDSong) called");
		playlist.addSong(song);
		mpd.getPlayer().play();
		}
		catch(Exception e){
			logger.error(e.getMessage());
		}
	}
	
	public boolean setCurrent(PlaylistItem item){
		logger.debug("setCurrent(PlaylistItem"+ item.getSong() +")");
		MPDSong song = SongConverter.getConverter().playlistItemToMPDSong(item);
		return setCurrent(song);
	}

	public boolean setCurrent(MPDSong song) {
		logger.debug("setCurrent(MPDSong"+ song.getName() +")");
		boolean ret = true;
		if(current == null) {
			current = song;
			try{
				playlist.addSong(song);
				mpd.getPlayer().play();
			}
			catch(Exception e) {
				logger.warn("Song not found");
				ret = false;
			}
		}
		else {
			try{
				//if nothing is on the playlist
				if(playlist.getSongList().isEmpty()) {
					playlist.addSong(song);
					mpd.getPlayer().play();
					timerUpdate(500);
				}
				else{ //something is on the playlist, 
					  //we're swapping out the track that's currently playing
					playlist.swap(song, 0);
				}
			}
			catch(Exception e){
				logger.warn("Song not found");
				ret = false;
			}
		}

		logger.debug("current = "+current.getName());
		//logger.debug("next = "+next.getName());

		return ret;
	}

	public boolean setNext(PlaylistItem item) {
		logger.debug("setNext(PlaylistItem"+ item.getSong() +")");
		MPDSong song = SongConverter.getConverter().playlistItemToMPDSong(item);
		return setNext(song);
	}
	
	public boolean setNext(MPDSong song){
		logger.debug("setNext(MPDSong"+ song.getName() +")");
		next = song;
		try {
			if(playlist.getSongList().size() > 1){
				playlist.swap(next, 1);
			} else {
				playlist.addSong(song);
			}
		}
		catch (Exception e) {
			logger.warn(e);
		}

		return true;
	}

	////////////////////////////////////////////////////////////////////////

	class ChangePoller extends TimerTask {
		long lastTime = 0;
		long thisTime = 0;

		public void run() {
			logger.debug("RUN METHOD OF CHANGEPOLLER CALLED OMFG");
			try{
				//System.out.println("inside the try block");
				thisTime = (long) mpd.getPlayer().getElapsedTime();

				if(thisTime < lastTime){
					lastTime = thisTime;
					setCurrent(next);
					setNext(converter.playlistItemToMPDSong(playlistStateManager.getNext()));

					playlistStateManager.popCurrentlyPlaying();
					if(playlistStateManager.verifyCurrentlyPlaying(converter.mpdSongToPlaylistItem(current)) == false) {
						playlistStateManager.forceCurrentlyPlaying(converter.mpdSongToPlaylistItem(current));
					}

					timerUpdate(500);
				}
				else {
					//System.out.println("do I have a logical error?? Look:" + thisTime);
				}
			}
			catch (Exception e) {
				//System.out.println("it didn't work");
			}
		}
	}
	
	private void timerUpdate(int milliseconds) {
		timer.cancel();
		//timer.purge();
		timer.schedule(new ChangePoller(), (current.getLength()*1000 - 4000), 500);
	}
}