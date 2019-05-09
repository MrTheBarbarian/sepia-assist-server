package net.b07z.sepia.server.assist.services;

import java.net.URLEncoder;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.b07z.sepia.server.assist.assistant.CmdBuilder;
import net.b07z.sepia.server.assist.data.Card;
import net.b07z.sepia.server.assist.data.Card.ElementType;
import net.b07z.sepia.server.assist.data.Parameter;
import net.b07z.sepia.server.assist.interpreters.NluResult;
import net.b07z.sepia.server.assist.interviews.InterviewData;
import net.b07z.sepia.server.assist.parameters.ClientFunction;
import net.b07z.sepia.server.assist.parameters.MusicService;
import net.b07z.sepia.server.assist.server.Config;
import net.b07z.sepia.server.assist.services.ServiceBuilder;
import net.b07z.sepia.server.assist.services.ServiceInfo;
import net.b07z.sepia.server.assist.services.ServiceInterface;
import net.b07z.sepia.server.assist.services.ServiceResult;
import net.b07z.sepia.server.assist.tools.SpotifyApi;
import net.b07z.sepia.server.assist.services.ServiceInfo.Content;
import net.b07z.sepia.server.assist.services.ServiceInfo.Type;
import net.b07z.sepia.server.core.assistant.ACTIONS;
import net.b07z.sepia.server.core.assistant.CLIENTS;
import net.b07z.sepia.server.core.assistant.CLIENTS.Platform;
import net.b07z.sepia.server.core.assistant.CMD;
import net.b07z.sepia.server.core.assistant.PARAMETERS;
import net.b07z.sepia.server.core.data.Language;
import net.b07z.sepia.server.core.tools.Debugger;
import net.b07z.sepia.server.core.tools.Is;
import net.b07z.sepia.server.core.tools.JSON;

/**
 * A service to search music via client control actions.
 * 
 * @author Florian Quirin
 *
 */
public class MusicSearch implements ServiceInterface{
	
	//Define some sentences for testing:
	
	@Override
	public TreeSet<String> getSampleSentences(String lang){
		TreeSet<String> samples = new TreeSet<>();
		//GERMAN
		if (lang.equals(Language.DE.toValue())){
			samples.add("Spiele All Along the Watchtower von Jimi Hendrix auf Spotify.");
			
		//OTHER
		}else{
			samples.add("Play All Along the Watchtower by Jimi Hendrix on Spotify.");
		}
		return samples;
	}
	
	//Basic service setup:

	@Override
	public ServiceInfo getInfo(String language) {
		ServiceInfo info = new ServiceInfo(Type.plain, Content.data, false);
		
		//Command
		String CMD_NAME = CMD.MUSIC;
		info.setIntendedCommand(CMD_NAME);
				
		//Parameters:
		
		//optional
		Parameter p1 = new Parameter(PARAMETERS.MUSIC_SERVICE);
		Parameter p2 = new Parameter(PARAMETERS.MUSIC_GENRE);
		Parameter p3 = new Parameter(PARAMETERS.MUSIC_ALBUM);
		Parameter p4 = new Parameter(PARAMETERS.MUSIC_ARTIST);
		Parameter p5 = new Parameter(PARAMETERS.SONG);
		Parameter p6 = new Parameter(PARAMETERS.PLAYLIST_NAME);
		
		info.addParameter(p1).addParameter(p2).addParameter(p3).addParameter(p4).addParameter(p5).addParameter(p6);
		
		//Default answers
		info.addSuccessAnswer("music_1c")
			.addFailAnswer("error_0a")
			.addOkayAnswer("default_not_possible_0a")
			.addCustomAnswer("music_type", "music_ask_0b")
			.addCustomAnswer("what_song", "music_ask_1a")
			.addCustomAnswer("what_artist", "music_ask_1b")
			.addCustomAnswer("what_playlist", "music_ask_1c")
			.addCustomAnswer("no_music_match", "music_0b")
			;
		
		return info;
	}
	
	@Override
	public ServiceResult getResult(NluResult nluResult) {
		//initialize result
		ServiceBuilder api = new ServiceBuilder(nluResult, 
				getInfoFreshOrCache(nluResult.input, this.getClass().getCanonicalName()));
		
		//get parameters
		Parameter serviceP = nluResult.getOptionalParameter(PARAMETERS.MUSIC_SERVICE, "");
		String service = serviceP.getValueAsString().replaceAll("^<|>$", "").trim();
		String serviceLocal = (String) serviceP.getDataFieldOrDefault(InterviewData.VALUE_LOCAL);
		
		boolean isSpotifyService = service.equals(MusicService.Service.spotify.name()) || service.equals(MusicService.Service.spotify_link.name());
		
		Parameter genreP = nluResult.getOptionalParameter(PARAMETERS.MUSIC_GENRE, "");
		String genre = genreP.getValueAsString();
		
		Parameter albumP = nluResult.getOptionalParameter(PARAMETERS.MUSIC_ALBUM, "");
		String album = albumP.getValueAsString();
		
		Parameter artistP = nluResult.getOptionalParameter(PARAMETERS.MUSIC_ARTIST, "");
		String artist = artistP.getValueAsString();
		
		Parameter songP = nluResult.getOptionalParameter(PARAMETERS.SONG, "");
		String song = songP.getValueAsString();
		
		Parameter playlistP = nluResult.getOptionalParameter(PARAMETERS.PLAYLIST_NAME, "");
		String playlistName = playlistP.getValueAsString();
		
		boolean hasMinimalInfo = Is.notNullOrEmpty(song) || Is.notNullOrEmpty(artist) 
				|| Is.notNullOrEmpty(album) || Is.notNullOrEmpty(playlistName) || Is.notNullOrEmpty(genre);
		
		if (!hasMinimalInfo){
			String customTypeSelectionParameter = "music_type";
			JSONObject typeSelection = api.getSelectedOptionOf(customTypeSelectionParameter);
			if (Is.notNullOrEmpty(typeSelection)){
				//System.out.println(typeSelection.toJSONString()); 		//DEBUG
				int selection = JSON.getIntegerOrDefault(typeSelection, "selection", 0);
				if (selection == 1){
					api.setIncompleteAndAsk(PARAMETERS.SONG, "music_ask_1a");
				}else if (selection == 2){
					api.setIncompleteAndAsk(PARAMETERS.MUSIC_ARTIST, "music_ask_1b");
				}else if (selection == 3){
					api.setIncompleteAndAsk(PARAMETERS.PLAYLIST_NAME, "music_ask_1c");
				}else{
					//This should never happen
					Debugger.println("MusicSearch had invalid selection result for 'music_type'", 1);
					api.setStatusFail();
				}
				ServiceResult result = api.buildResult();
				return result;
			}else{
				api.askUserToSelectOption(customTypeSelectionParameter, JSON.make(
						"1", "song|lied", 
						"2", "artist|kuenstler", 
						"3", "playlist"
				), "music_ask_0b");
				ServiceResult result = api.buildResult();
				//System.out.println(result.getResultJSON().toString()); 	//DEBUG
				return result;
			}
		}
		
		//Check platform
		Platform platform = CLIENTS.getPlatform(nluResult.input.clientInfo);
		/*
		if (platform.equals(Platform.browser)){
			
		}else if (platform.equals(Platform.android)){
			
		}else if (platform.equals(Platform.ios)){
			
		}else if (platform.equals(Platform.windows)){
			
		}
		*/
		
		//Basically this service cannot fail here ... only inside client ... but we'll also try to get some more data:
		
		String foundTrack = "";
		String foundArtist = "";
		String foundAlbum = "";
		String foundPlaylist = "";
		String foundUri = "";
		String foundType = "";
		String cardTitle = "";
		String cardSubtitle = "";
		String cardIconUrl = Config.urlWebImages + "cards/music_default.png";
		String cardBrand = "default"; 
		
		//Use YouTube for now
		if (service.equals(MusicService.Service.youtube.name()) || (service.isEmpty() && Config.spotifyApi == null)){
			//Icon
			cardIconUrl = Config.urlWebImages + "brands/youtube-logo.png";
			cardBrand = "YouTube";
			cardSubtitle = "YouTube Search";
			//Search
			String q = "";
			try{
				if (Is.notNullOrEmpty(playlistName)){
					q = URLEncoder.encode(playlistName + " playlist", "UTF-8");
					cardTitle = "Playlist: " + playlistName;
				
				}else if (Is.notNullOrEmpty(song) && Is.notNullOrEmpty(album)){
					q = URLEncoder.encode(song + ", " + album + " album", "UTF-8");
					cardTitle = "Song: " + song + ", Album: " + album;
				
				}else if (Is.notNullOrEmpty(song) && Is.notNullOrEmpty(artist)){
					q = URLEncoder.encode(song + ", " + artist, "UTF-8");
					cardTitle = "Song: " + song + ", Artist: " + artist;
				
				}else if (Is.notNullOrEmpty(album)){
					if (Is.notNullOrEmpty(artist)){
						q = URLEncoder.encode(artist + ", " + album + " album", "UTF-8");
						cardTitle = "Artist: " + artist + ", Album: " + album;
					}else{
						q = URLEncoder.encode(album + " album", "UTF-8");
						cardTitle = "Album: " + album;
					}
					
				}else if (Is.notNullOrEmpty(artist)){
					q = URLEncoder.encode(artist + " playlist", "UTF-8");
					cardTitle = "Playlist: " + artist;
					
				}else if (Is.notNullOrEmpty(genre)){
					q = URLEncoder.encode(genre + " playlist", "UTF-8");
					cardTitle = "Playlist: " + genre;
				
				}else if (Is.notNullOrEmpty(song)){
					q = URLEncoder.encode(song, "UTF-8");
					cardTitle = "Q: " + song;
				}
			}catch (Exception e){
				//ignore
			}
			if (!q.isEmpty()){
				foundUri = "https://www.youtube.com/results?search_query=" + q;
			}

		//Spotify API (currently used when service is Spotify or platform can only handle URIs)
		}else if (isSpotifyService || (service.isEmpty() && Config.spotifyApi != null)){
			//we need the API (in early version it was possible to call it without registration)
			if (Config.spotifyApi != null){
				//Icon
				cardIconUrl = Config.urlWebImages + "brands/spotify-logo.png";
				cardBrand = "Spotify";
				//Search
				JSONObject spotifyBestItem = Config.spotifyApi.searchBestItem(song, artist, album, playlistName, genre);
				foundUri = JSON.getString(spotifyBestItem, "uri");
				foundType = JSON.getString(spotifyBestItem, "type");
				//get URI and build Card data
				if (Is.notNullOrEmpty(foundUri)){
					//get info by type
					if (foundType.equals(SpotifyApi.TYPE_TRACK)){
						foundTrack = JSON.getString(spotifyBestItem, "name");
						foundArtist = JSON.getString(spotifyBestItem, "primary_artist");
						foundAlbum =  JSON.getString(spotifyBestItem, "album");
						cardTitle = "Song: " + foundTrack;
						cardSubtitle = (foundAlbum.isEmpty())? foundArtist : (foundArtist + ", " + foundAlbum);
						//add play tag to URI
						foundUri = foundUri + ":play";
						
					}else if (foundType.equals(SpotifyApi.TYPE_ALBUM)){
						foundArtist = JSON.getString(spotifyBestItem, "primary_artist");
						foundAlbum =  JSON.getString(spotifyBestItem, "name");
						cardTitle = "Album: " + foundAlbum;
						cardSubtitle = foundArtist;
						//add play tag to URI
						foundUri = foundUri + ":play";
						
					}else if (foundType.equals(SpotifyApi.TYPE_ARTIST)){
						foundArtist = JSON.getString(spotifyBestItem, "name");
						JSONArray genres = JSON.getJArray(spotifyBestItem, "genres");
						String genresString = "";
						if (Is.notNullOrEmpty(genres)){
							for (int i=0; i<Math.min(genres.size(),3); i++){
								genresString += (genres.get(i).toString() + ", ");
							}
							genresString = genresString.replaceFirst(", $", "").trim();
						}
						cardTitle = "Artist: " + foundArtist;
						cardSubtitle = (Is.notNullOrEmpty(genresString))? genresString : "";
						//add play tag to URI
						foundUri = foundUri + ":play";
						
					}else if (foundType.equals(SpotifyApi.TYPE_PLAYLIST)){
						foundPlaylist = JSON.getString(spotifyBestItem, "name");
						String owner = JSON.getString(spotifyBestItem, "owner_display_name");
						long tracks = JSON.getLongOrDefault(spotifyBestItem, "total_tracks", 0);
						cardTitle = "Playlist: " + foundPlaylist;
						if (!owner.isEmpty() && tracks > 0){
							cardSubtitle = "By: " + owner + ", Tracks: " + tracks;
						}else if (owner.isEmpty() && tracks > 0){
							cardSubtitle = "Tracks: " + tracks;
						}else if (!owner.isEmpty()){
							cardSubtitle = "By: " + owner;
						}
						//add play tag to URI
						//foundUri = foundUri + ":play";		//not supported? breaks link?
					}
				}
			}
		}
		
		//If we have only a song we should declare the 'search' field and not rely on song-name
		boolean hasOnlySong = Is.notNullOrEmpty(song) && 
				Is.nullOrEmpty(artist) && Is.nullOrEmpty(album) && Is.nullOrEmpty(playlistName) && Is.nullOrEmpty(genre);
		String search = (hasOnlySong)? song : "";
		
		String controlFun = ClientFunction.Type.searchForMusic.name();
		JSONObject controlData = JSON.make(
				/*
				"artist", artist,
				"song", song,
				"album", album,
				"playlist", playlistName,
				*/
				"artist", (foundArtist.isEmpty())? artist : foundArtist,
				"song", (foundTrack.isEmpty())? song : foundTrack,
				"album", (foundAlbum.isEmpty())? album : foundAlbum,
				"playlist", (foundPlaylist.isEmpty())? playlistName : foundPlaylist,
				"service", service
		);
		JSON.put(controlData, "genre", genre);
		JSON.put(controlData, "search", search);
		if (Is.notNullOrEmpty(foundUri)){
			JSON.put(controlData, "uri", foundUri);
		}
		
		api.addAction(ACTIONS.CLIENT_CONTROL_FUN);
		api.putActionInfo("fun", controlFun);
		api.putActionInfo("controlData", controlData);
		
		//some buttons - we use the custom function button but the client needs to parse the string itself!
		
		//simple action button
		api.addAction(ACTIONS.BUTTON_CUSTOM_FUN);
		api.putActionInfo("fun", "controlFun;;" + controlFun + ";;" + controlData.toJSONString());
		api.putActionInfo("title", Is.notNullOrEmpty(serviceLocal)? serviceLocal : "Button");
		
		//Cards (or web-search?)
		if (Is.notNullOrEmpty(foundUri)){
			Card card = new Card(Card.TYPE_SINGLE);
			//JSONObject linkCard = 
			card.addElement(ElementType.link, 
					JSON.make(
							"title", cardTitle, 
							"desc", cardSubtitle,
							"type", "musicSearch",
							"brand", cardBrand
					),
					null, null, "", 
					foundUri, 
					cardIconUrl, 
					null, null);
			//JSON.put(linkCard, "imageBackground", "#f0f0f0");	//use any CSS background option you wish
			api.addCard(card.getJSON());
		}else{
			//web-search action
			api.addAction(ACTIONS.BUTTON_CMD);
			api.putActionInfo("title", "Web Search");
			api.putActionInfo("info", "direct_cmd");
			api.putActionInfo("cmd", CmdBuilder.getWebSearch(nluResult.input.textRaw));
			api.putActionInfo("options", JSON.make(ACTIONS.SKIP_TTS, true));
			
			//can we still search via Android Intent?
			if (!platform.equals(Platform.android)){
				api.setCustomAnswer("music_0b");
			}
		}

		//all good
		/*if (!hasMinimalInfo){
			api.setStatusOkay();
		}else{
			api.setStatusSuccess();
		}*/
		api.setStatusSuccess();
		
		//build the API_Result
		ServiceResult result = api.buildResult();
		return result;
	}
}