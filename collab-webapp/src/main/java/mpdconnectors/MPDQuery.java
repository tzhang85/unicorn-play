package mpdconnectors;
//holy crap I edited this in eclipse
import querybot.IQuery;
import objects.AlbumItem;
import objects.ArtistItem;
import objects.PlaylistItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bff.javampd.*;
import org.bff.javampd.objects.*;
import org.bff.javampd.exception.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MPDQuery implements IQuery{
	private static final Logger logger = LogManager.getLogger();
	
	private SongConverter converter = SongConverter.getConverter();

	private Database database;

	public MPDQuery() {
		//this is just a stub to satisfy the compiler temporarily
		database = MPDWrapper.getMPD().getDatabase();
	}

	//refactored so that hashtable construction happens all in the SongConverter

	public Map<String, Object> searchAny(String criteria){
		Map<String, Object> ret = null;
		try{
			ret = songListToMap(database.findAny(criteria));
		}
		catch(Exception e) {
			logger.warn(e.getMessage());
		}
		return ret;
	}

	public Map<String, Object> searchArtist(String artist){
		Map<String, Object> ret = null;
		try{
			ret = songListToMap(database.findArtist(artist));
		}
		catch(Exception e) {
			logger.warn(e.getMessage());
		}
		return ret;
	}

	public Map<String, Object> searchGenre(String genre){
		Map<String, Object> ret = null;
		try{
			ret = songListToMap(database.findGenre(genre));
		}
		catch(Exception e) {
			logger.warn(e.getMessage());
		}
		return ret;
	}

	public Map<String, Object> searchAlbum(String album){
		Map<String, Object> ret = null;
		try{
			ret = songListToMap(database.findAlbum(album));
		}
		catch(Exception e) {
			logger.warn(e.getMessage());
		}
		return ret;
	}

	public Map<String, Object> searchSong(String song){
		Map<String, Object> ret = null;
		try{
			ret = songListToMap(database.findTitle(song));
		}
		catch(Exception e) {
			logger.warn(e.getMessage());
		}
		return ret;
	}
	
	public Map<String, Object> listAll() {
		Map<String, Object> map = null;
		try{
			map = songListToMap(database.listAllSongs());
		}
		catch(Exception e) {
			logger.warn(e.getMessage());
		}
		return map;
	}

	public PlaylistItem getSong(String hash) {
		return converter.hashToPlaylistItem(hash);
	}

	@Override
	public Map<String, Object> searchforArtists(String artist) {
		System.out.println("searchForArtists("+artist+") called");
		Map<String, Object> ret = null;
		try{
			if(artist.equals("")){
				ret = artistsToMap(database.listAllArtists());
			}
			else{
				//search by artist to return a list of songs. There is no direct support for 
				//searching by string to return a list of MPDArtists.
				Collection<MPDSong> songList = database.findArtist(artist);
				Collection<String> artistList = new ArrayList<String>();
				
				//filter out just the artists to a list
				for(MPDSong song : songList) {
					if( !artistList.contains(song.getArtistName())){
						artistList.add(song.getArtistName());
					}
				}
				
				ret = artistStringsToMap(artistList);
			}
		}
		catch(Exception e) {
			logger.warn(e.getMessage());
		}
		return ret;
	}



	@Override
	public Map<String, Object> listAllAlbumsByArtist(String artist) {
		System.out.println("listAllAlbumsByArtist("+artist+") called");
		Collection<AlbumItem> albumItemList = new ArrayList<AlbumItem>();
		Map<String, Object> ret = null;
		
		try{
			Collection<MPDAlbum> mpdAlbumList = database.listAllAlbums();

			if(artist.equals("")){
				for(MPDAlbum album : mpdAlbumList) {
					albumItemList.add(new AlbumItem(album.getArtistName(), album.getName()));
				}
			}
			else{
				//Keep only those albums that have the specified artist name
				for(MPDAlbum album : mpdAlbumList) {
					if(album.getArtistName().equals(artist)) {
						albumItemList.add(new AlbumItem(artist, album.getName()));	
					}
				}
			}
			ret = albumItemsToMap(albumItemList);
		}
		catch(Exception e) {
			logger.warn(e.getMessage());
		}
		return ret;
	}

	private Map<String, Object> albumItemsToMap(Collection<AlbumItem> albumItemList) {
		List<Map<String, Object>> set = new ArrayList<Map<String, Object>>();
		
		for(AlbumItem albumItem : albumItemList) {
			set.add(albumItem.getMap());
		}
		
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("list", set);
		return ret;
	}
	
	
	private Map<String, Object> songListToMap(Collection<MPDSong> songList) {
		List<Map<String, Object>> set = new ArrayList<Map<String, Object>>();//list of playlist objects (maps)
	
		for(MPDSong song : songList) {
			set.add(converter.mpdSongToPlaylistItem(song).getMap());
		}
		
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("list", set);//Only actual parameter for this

		return ret;
	}

	private Map<String, Object> artistsToMap(
		Collection<MPDArtist> listAllArtists) {
		Collection<String> artistToString = new ArrayList<String>();
		
		for(MPDArtist artist : listAllArtists) {
			artistToString.add(artist.getName());
		}
		
		return artistStringsToMap(artistToString);
	}

	private Map<String, Object> artistStringsToMap(Collection<String> artistList) {
		List<Map<String, Object>> set = new ArrayList<Map<String, Object>>();
		
		for(String artist : artistList) {
			set.add(new ArtistItem(artist).getMap());
		}
		
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("list", set);
		return ret;
	}
}