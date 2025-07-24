package com.driver;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User();
        user.setName(name);
        user.setMobile(mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist=new Artist();
        artist.setName(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        // Step 1: Create new album
        Album album = new Album();
        album.setTitle(title);
        album.setReleaseDate(new Date());
        albums.add(album);

        // Step 2: Find the artist with the given name
        Artist artist = null;
        for (Artist a : artists) {
            if (a.getName().equals(artistName)) {
                artist = a;
                break;
            }
        }

        // Step 3: If artist not found, create new artist
        if (artist == null) {
            artist = new Artist();
            artist.setName(artistName);
            artists.add(artist);
        }

        // Step 4: Add album to artistAlbumMap
        artistAlbumMap.putIfAbsent(artist, new ArrayList<>());
        artistAlbumMap.get(artist).add(album);

        return album;


    }

    public Song createSong(String title, String albumName, int length) throws Exception{
//      step 1 create song
        Song song = new Song();
        song.setTitle(title);
        song.setLength(length);
        songs.add(song);

//        step 2: find album in list with given name
        Album album = null;
        for(Album a:albums){
            if(a.getTitle().equals(albumName)){
                album=a;
            }
        }

//        step3: if album not found throw error
        if(album==null){
            throw new Exception("Album doesnt found");
        }

        // Step 4: Add song to AlbumSongMap
        albumSongMap.putIfAbsent(album, new ArrayList<>());
        albumSongMap.get(album).add(song);

        return song;

    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        // Step 1: Create playlist
        Playlist playlist = new Playlist();
        playlist.setTitle(title);

        // Step 2: Find the user
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }

        // Step 3: If user doesn't exist, throw error
        if (user == null) {
            throw new Exception("User not found");
        }

        // Step 4: Find all songs of given length
        List<Song> matchingSongs = new ArrayList<>();
        for (Song s : songs) {
            if (s.getLength() == length) {
                matchingSongs.add(s);
            }
        }

        // Step 5: Update playlistSongMap
        playlistSongMap.put(playlist, matchingSongs);

        // Step 6: Set user as creator
        creatorPlaylistMap.put(user, playlist);

        // Step 7: Add user as listener
        List<User> listeners = new ArrayList<>();
        listeners.add(user);
        playlistListenerMap.put(playlist, listeners);

        // Step 8: Add playlist to user's list
        if (!userPlaylistMap.containsKey(user)) {
            userPlaylistMap.put(user, new ArrayList<>());
        }
        userPlaylistMap.get(user).add(playlist);

        // Step 9: Add playlist to global list
        playlists.add(playlist);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        // Step 1: Create playlist
        Playlist playlist = new Playlist();
        playlist.setTitle(title);

        // Step 2: Find the user
        User user = null;
        for (User u : users) {
            if (u.getMobile().equals(mobile)) {
                user = u;
                break;
            }
        }

        // Step 3: If user doesn't exist, throw error
        if (user == null) {
            throw new Exception("User not found");
        }

        // Step 4: Find songs with matching titles
        List<Song> matchingSongs = new ArrayList<>();
        for (String songTitle : songTitles) {
            for (Song s : songs) {
                if (s.getTitle().equals(songTitle)) {
                    matchingSongs.add(s);
                    break; // avoid adding duplicates if same title appears multiple times
                }
            }
        }

        // Step 5: Update playlistSongMap
        playlistSongMap.put(playlist, matchingSongs);

        // Step 6: Set creator of playlist
        creatorPlaylistMap.put(user, playlist);

        // Step 7: Add user as listener
        List<User> listeners = new ArrayList<>();
        listeners.add(user);
        playlistListenerMap.put(playlist, listeners);

        // Step 8: Update userPlaylistMap
        if (!userPlaylistMap.containsKey(user)) {
            userPlaylistMap.put(user, new ArrayList<>());
        }
        userPlaylistMap.get(user).add(playlist);

        // Step 9: Add to global playlists list
        playlists.add(playlist);

        return playlist;

    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
//        step1: find user
        User user =null;
        for(User u:users){
            if(u.getMobile().equals(mobile)){
                user=u;
                break;
            }
        }

        if(user==null){
            throw new Exception("User does not exist");
        }

//        step2:Find playlist
        Playlist playlist = null;
        for(Playlist p:playlists){
            if(p.getTitle().equals(playlistTitle)){
                playlist=p;
                break;
            }
        }

        if(playlist==null){
            throw new Exception("Playlist does not exist");
        }

        // step 3: if user is the creator , do nothing
        if(creatorPlaylistMap.containsKey(user) && creatorPlaylistMap.get(user).equals(playlist)){
            return playlist;
        }

        List<User> listners = playlistListenerMap.getOrDefault(playlist,new ArrayList<>());
        if(listners.contains(user)){
            return playlist;
        }

        // step 5 : add user as listner
        listners.add(user);
        playlistListenerMap.put(playlist,listners);

        //step 6: update userPlaylistmap
        if(!userPlaylistMap.containsKey(user)){
            userPlaylistMap.put(user,new ArrayList<>());
        }
        userPlaylistMap.get(user).add(playlist);

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //step 1: find if song exists
        Song song = null;
        for(Song s:songs){
            if(s.getTitle().equals(songTitle)){
                song=s;
                break;
            }
        }
        if(song==null){
            throw new Exception("song does not exist");
        }

        // find if user exist
        User user =null;
        for(User u:users){
            if(u.getMobile().equals(mobile)){
                user=u;
                break;
            }
        }
        if(user==null){
            throw new Exception("User does not exist");
        }

        // Step 3: Check if user already liked the song
        List<User> likedUsers = songLikeMap.getOrDefault(song, new ArrayList<>());
        if (likedUsers.contains(user)) {
            return song; // already liked, do nothing
        }

        // Step 4: Add user to songLikeMap
        likedUsers.add(user);
        songLikeMap.put(song, likedUsers);

        song.setLikes(song.getLikes() + 1);

        // Step 5: Increment the artist's like count
        Album album = null;
        for (Album a : albumSongMap.keySet()) {
            List<Song> albumSongs = albumSongMap.get(a);
            if (albumSongs.contains(song)) {
                album = a;
                break;
            }
        }

        if (album != null) {
            Artist artist = null;
            for (Artist a : artistAlbumMap.keySet()) {
                List<Album> artistAlbums = artistAlbumMap.get(a);
                if (artistAlbums.contains(album)) {
                    artist = a;
                    break;
                }
            }
            if (artist != null) {
                artist.setLikes(artist.getLikes() + 1);
            }
        }

        return song;
    }

    public String mostPopularArtist() {
        int maxLikes = -1;
        String mostPopular = null;

        for (Artist a : artists) {
            if (a.getLikes() > maxLikes) {
                maxLikes = a.getLikes();
                mostPopular = a.getName();
            }
        }

        return mostPopular != null ? mostPopular : "";
    }

    public String mostPopularSong() {
        int maxLikes = -1;
        String song = null;

        for(Song s : songs){
            if(s.getLikes()>maxLikes){
                maxLikes=s.getLikes();
                song = s.getTitle();
            }
        }

        return song!=null ? song:"";
    }
}
