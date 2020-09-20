package com.example.hoomp3_player_1;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Mp3DBHelper extends SQLiteOpenHelper {
    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    private Activity activity;

    //select 문을 위한 상수
    public static final int ALIGN_TOTAL = 0;
    public static final int ALIGN_SINGER = 1;
    public static final int ALIGN_SINGER_SELECT = 2;
    public static final int ALIGN_GENRE = 3;
    public static final int ALIGN_GENRE_SELECT = 4;
    public static final int ALIGN_PLAYLIST = 5;
    public static final int ALIGN_HEART = 6;
    public static final int PLAYLIST_TITLE = 7;
    public static final int NON_PLAYLIST = 8;
    public static final int SELECTED_PLAYLIST = 9;
    public static final int SELECTED_NOW_PLAYLIST = 10;
    public static final int SELECTED_NON_NOW_PLAYLIST = 11;

    //update set을 위한 상수
    public static final int UPDATE_NO_HEART = 0;
    public static final int UPDATE_HEART = 1;
    public static final int UPDATE_PLAYLIST =2;
    public static final int UPDATE_SAVE_NOW_PLAYLIST =3;

    //DB생성, 자동으로, 있으면 다시 만들지 않는다.
    public Mp3DBHelper(Context context) {
        super(context, "myMp3ListDB", null, 1);
        this.context = context;
    }

    //table 생성 칼럼 11개
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table myMusicTBL( " +
                "image BLOB, " +
                "name char, " +
                "musicName char, " +
                "duration char, " +
                "genre char, " +
                "albumName char, " +
                "releaseDate char, " +
                "heart integer, " +
                "playListName char, " +
                "countPlay integer, " +
                "fileName char, " +
                "primary key(name, musicName, albumName)" +
                ");");
    }

    //테이블 삭제
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists myMusicTBL");
        onCreate(sqLiteDatabase); //테이블 생성을 위한 callback 함수 호출
    }

    //select 문
    public ArrayList<MusicData> selectMyMusicTBL(int number, @Nullable MusicData data) {
        ArrayList<MusicData> musicDBdataList = new ArrayList<MusicData>();

        sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = null;
        switch (number) {
            case ALIGN_TOTAL:
                cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL order by musicName asc;", null);
                break;
            case ALIGN_SINGER:
                cursor = sqLiteDatabase.rawQuery("select count(*),name from myMusicTBL group by name;", null);
                break;
            case ALIGN_SINGER_SELECT:
                String sName = data.getArtistName();
                cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL where name =" + "'" + sName + "';", null);
                break;
            case ALIGN_GENRE:
                cursor = sqLiteDatabase.rawQuery("select count(*),genre from myMusicTBL group by genre;", null);
                break;
            case ALIGN_GENRE_SELECT:
                String sGenre = data.getArtistName();
                cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL where genre =" + "'" + sGenre + "';", null);
                break;
            case ALIGN_PLAYLIST:
                String plyName = data.getPlayListName();
                cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL where playListName =" + "'" + plyName + "';", null);
                break;
            case ALIGN_HEART:
                cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL where heart = 1 order by musicName asc;", null);
                break;
            case PLAYLIST_TITLE:
                cursor = sqLiteDatabase.rawQuery("select heart, playListName from myMusicTBL where playListName is not null group by playListName;", null);
                break;
            case NON_PLAYLIST:
                cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL where playListName is null order by musicName asc;", null);
                break;
            case SELECTED_PLAYLIST:
                String str = data.getArtistName();
                cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL where playListName = '"+ str +"';", null);
                break;
            case SELECTED_NOW_PLAYLIST:
                cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL where countPlay = 1 order by musicName asc;", null);
                break;
            case SELECTED_NON_NOW_PLAYLIST:
                cursor = sqLiteDatabase.rawQuery("select * from myMusicTBL where countPlay = 0 order by name asc;", null);
                break;
            default:
                break;
        }
        try {
            while (cursor.moveToNext()) {
                if (number == ALIGN_SINGER || number == ALIGN_GENRE || number == PLAYLIST_TITLE) {
                    MusicData musicData2 = new MusicData(cursor.getInt(0), cursor.getString(1));
                    musicDBdataList.add(musicData2);
                } else {
                    MusicData musicData11 = new MusicData(cursor.getBlob(0), cursor.getString(1), cursor.getString(2),
                            cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6),
                            cursor.getInt(7), cursor.getString(8), cursor.getInt(9), cursor.getString(10));
                    musicDBdataList.add(musicData11);
                }
            }//end of while
        }catch (IllegalStateException e){
        }
        cursor.close();
        sqLiteDatabase.close();

        return musicDBdataList;
    }

    //insert 문
    public boolean insertMyMusicTBL(MusicData musicData) {
        boolean returnValue = false;
        try {
            sqLiteDatabase = this.getWritableDatabase();

            byte[] imageByte = musicData.getImage();
            String artistName = musicData.getArtistName();
            String musicName = musicData.getMusicName();
            String duration = musicData.getDuration();
            String genre = musicData.getGenre();
            String albumName = musicData.getAlbumName();
            String releaseDate = musicData.getReleaseDate();
            String file = musicData.getFileName();

            Log.d("Mp3DBHelper", imageByte + " 이미지 byte");

            if (imageByte != null) {
                SQLiteStatement sqLiteStatement = sqLiteDatabase.compileStatement("insert or ignore into myMusicTBL(image,name,musicName,duration,genre,albumName,releaseDate,heart,countPlay,fileName) " +
                        "values( ?, '" + artistName + "', '" + musicName + "', '" + duration + "', '"
                        + genre + "',  '" + albumName + "', '" + releaseDate + "',0,0,'" + file + "' );");

                sqLiteStatement.bindBlob(1, imageByte);
                sqLiteStatement.execute();
            } else {
                String queryString = "insert or ignore into myMusicTBL(image,name,musicName,duration,genre,albumName,releaseDate,heart,countPlay,fileName) " +
                        "values( null, '" + artistName + "', '" + musicName + "', '" + duration + "', '"
                        + genre + "',  '" + albumName + "', '" + releaseDate + "',0,0,'" + file + "');";
                sqLiteDatabase.execSQL(queryString);
            }

            returnValue = true;
        } catch (SQLException e) {
            Log.d("database", e.getMessage());
            returnValue = false;
        } finally {
            sqLiteDatabase.close();
        }
        return returnValue;
    }

    //update 문
    public boolean updateMyMusicTBL(int num, @Nullable MusicData musicData) {
        boolean returnValue;
        try {
            sqLiteDatabase = this.getWritableDatabase();

            String queryString = null;

            switch (num) {
                case UPDATE_NO_HEART:
                    String aName1 = musicData.getArtistName();
                    String mName1 = musicData.getMusicName();
                    String bumName1 = musicData.getAlbumName();
                    queryString = "update myMusicTBL set heart = " + UPDATE_NO_HEART +
                            " where name = '" + aName1 + "' and musicName = '" + mName1 + "' and albumName = '" + bumName1 + "';";
                    break;
                case UPDATE_HEART:
                    String mName2 = musicData.getMusicName();
                    String aName2 = musicData.getArtistName();
                    String bumName2 = musicData.getAlbumName();
                    queryString = "update myMusicTBL set heart = " + UPDATE_HEART +
                            " where name = '" + aName2 + "' and musicName = '" + mName2 + "' and albumName = '" + bumName2 + "';";
                    break;
                case UPDATE_PLAYLIST:
                    String plyName = musicData.getPlayListName();
                    String mName = musicData.getMusicName();
                    String aName = musicData.getArtistName();
                    String bumName = musicData.getAlbumName();
                    queryString = "update myMusicTBL set playListName = '" + plyName + "' " +
                            "where name = '" + aName + "' and musicName = '" + mName + "' and albumName = '" + bumName + "';";
                    break;
                case UPDATE_SAVE_NOW_PLAYLIST:
                    int i = musicData.getCountPlay();
                    String mName3 = musicData.getMusicName();
                    String aName3 = musicData.getArtistName();
                    String bumName3 = musicData.getAlbumName();
                    queryString = "update myMusicTBL set countPlay = " + i + " " +
                            "where name = '" + aName3 + "' and musicName = '" + mName3 + "' and albumName = '" + bumName3 + "';";
                    break;
                default:
                    break;
            }
            sqLiteDatabase.execSQL(queryString);
            returnValue = true;
        } catch (SQLException e) {
            Log.d("database", e.getMessage());
            returnValue = false;
        } finally {
            sqLiteDatabase.close();
        }
        return returnValue;
    }


}
