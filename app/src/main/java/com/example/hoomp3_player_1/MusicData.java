package com.example.hoomp3_player_1;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicData implements Parcelable {
    private byte[] image;
    private String artistName;
    private String musicName;
    private String duration;
    private String genre;
    private String albumName;
    private String releaseDate;
    private int heart;
    private String playListName;
    private int countPlay;//현재 재생목록 여부
    private String fileName;

    //장르 및 가수별 검색 했을 때 결과값을 위한 생성자, 결과값으로 int, string 이렇게 2개가 넘어오는데 그냥 임의로 num, artistName 사용하여 정보 전달
    public MusicData(int num, String artistName) {
        this.heart = num;
        this.artistName = artistName;
    }

    public MusicData(byte[] image, String artistName, String musicName, String duration, String genre, String albumName, String releaseDate, int heart, String playListName, int countPlay, String fileName) {
        this.image = image;
        this.artistName = artistName;
        this.musicName = musicName;
        this.duration = duration;
        this.genre = genre;
        this.albumName = albumName;
        this.releaseDate = releaseDate;
        this.heart = heart;
        this.playListName = playListName;
        this.countPlay = countPlay;
        this.fileName = fileName;
    }

    protected MusicData(Parcel in) {
        image = in.createByteArray();
        artistName = in.readString();
        musicName = in.readString();
        duration = in.readString();
        genre = in.readString();
        albumName = in.readString();
        releaseDate = in.readString();
        heart = in.readInt();
        playListName = in.readString();
        countPlay = in.readInt();
        fileName = in.readString();
    }

    public static final Creator<MusicData> CREATOR = new Creator<MusicData>() {
        @Override
        public MusicData createFromParcel(Parcel in) {
            return new MusicData(in);
        }

        @Override
        public MusicData[] newArray(int size) {
            return new MusicData[size];
        }
    };

    public int getCountPlay() {
        return countPlay;
    }

    public void setCountPlay(int countPlay) {
        this.countPlay = countPlay;
    }

    public int getHeart() {
        return heart;
    }

    public void setHeart(int heart) {
        this.heart = heart;
    }

    public String getPlayListName() {
        return playListName;
    }

    public void setPlayListName(String playListName) {
        this.playListName = playListName;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByteArray(image);
        parcel.writeString(artistName);
        parcel.writeString(musicName);
        parcel.writeString(duration);
        parcel.writeString(genre);
        parcel.writeString(albumName);
        parcel.writeString(releaseDate);
        parcel.writeInt(heart);
        parcel.writeString(playListName);
        parcel.writeInt(countPlay);
        parcel.writeString(fileName);
    }
}
