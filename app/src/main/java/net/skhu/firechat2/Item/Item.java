package net.skhu.firechat2.Item;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Item implements Serializable {
    String message;
    String userName;
    Date createTime;
    boolean checked;

    boolean havePhoto;
    String photoFileName;

    boolean haveVideo;
    String videoFileName;

    boolean haveMusic;
    String musicFileName;

    boolean haveBinaryFile;
    String BinaryFileName;
    String realBinaryFileName;

    public boolean getHaveBinaryFile() {
        return haveBinaryFile;
    }

    public void setHaveBinaryFile(boolean haveBinaryFile) {
        this.haveBinaryFile = haveBinaryFile;
    }

    public String getBinaryFileName() {
        return BinaryFileName;
    }

    public void setBinaryFileName(String binaryFileName) {
        BinaryFileName = binaryFileName;
    }

    public String getRealBinaryFileName() {
        return realBinaryFileName;
    }

    public void setRealBinaryFileName(String realBinaryFileName) {
        this.realBinaryFileName = realBinaryFileName;
    }

    public boolean getHaveMusic() {
        return haveMusic;
    }

    public void setHaveMusic(boolean haveMusic) {
        this.haveMusic = haveMusic;
    }

    public String getMusicFileName() {
        return musicFileName;
    }

    public void setMusicFileName(String musicFileName) {
        this.musicFileName = musicFileName;
    }

   static SimpleDateFormat format = new SimpleDateFormat("HH시 mm분");

    public Item() {
    }

    public Item(String message) {
        this.message = message;
        this.createTime = new Date();
        havePhoto = false;
        haveVideo = false;
        haveMusic = false;
        haveBinaryFile = false;
    }

    public String getUserName(){
        return userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date date) {
        this.createTime = date;
    }

    public String getCreateTimeFormatted() {
        return format.format(createTime);
    }

    public void setCreateTimeFormatted(String s) {
        try {
            this.createTime = format.parse(s);
        } catch (Exception e) {
        }
    }

    public String getPhotoFileName(){
        return photoFileName;
    }

    public void setPhotoFileName(String photoFileName){
        this.photoFileName = photoFileName;
    }

    public boolean getHavePhoto(){
        return havePhoto;
    }


    public boolean getHaveVideo(){
        return haveVideo;
    }

    public void setHaveVideo(boolean haveVideo){
        this.haveVideo = haveVideo;
    }

    public String getVideoFileName(){
        return videoFileName;
    }

    public void setVideoFileName(String videoFileName){
        this.videoFileName = videoFileName;
    }


    public void setHavePhoto(boolean havePhoto){
        this.havePhoto = havePhoto;
    }


    @Override
    public String toString() {
        if(haveVideo == true){
            return String.format("(%s), (%s)", message, videoFileName);
        }
        else if (havePhoto == true){
            return String.format("(%s), (%s)", message, photoFileName);
        }
        else {
            return String.format("(%s)", message);
        }
    }
}
