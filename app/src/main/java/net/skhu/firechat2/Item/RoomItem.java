package net.skhu.firechat2.Item;

public class RoomItem {
    String roomName;
    //String password;

    public RoomItem(){

    }

    public RoomItem(String roomName){
        this.roomName = roomName;
    }

    public String getRoomName(){
        return roomName;
    }

    public void setRoomName(String roomName){
        this.roomName = roomName;
    }

    @Override
    public String toString() {
        return String.format("(%s)", roomName);
    }
}
