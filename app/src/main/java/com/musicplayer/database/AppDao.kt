package com.musicplayer.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Songs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE folderPath = :folderPath")
    suspend fun getSongsInFolder(folderPath: String): List<SongEntity>

    // Folders
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Query("SELECT * FROM folders")
    suspend fun getAllFolders(): List<FolderEntity>

    // Favorites
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addToFavorites(favorite: FavoriteEntity)

    @Delete
    suspend fun removeFromFavorites(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorites")
    fun getFavoriteSongsFlow(): Flow<List<FavoriteEntity>>

    @Query("SELECT s.* FROM songs s INNER JOIN favorites f ON s.path = f.songPath")
    suspend fun getFavoriteSongs(): List<SongEntity>

    // Recently Played
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToRecent(recent: RecentEntity)

    @Query("SELECT s.* FROM songs s INNER JOIN recently_played r ON s.path = r.songPath ORDER BY r.timestamp DESC LIMIT 20")
    suspend fun getRecentSongs(): List<SongEntity>

    // Playlists
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createPlaylist(playlist: PlaylistEntity)

    @Query("SELECT name FROM playlists")
    suspend fun getAllPlaylistNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(entry: PlaylistSongEntity)

    @Query("SELECT s.* FROM songs s INNER JOIN playlist_songs ps ON s.path = ps.songPath WHERE ps.playlistName = :playlistName")
    suspend fun getSongsInPlaylist(playlistName: String): List<SongEntity>
}
