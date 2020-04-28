package net.skhu.firechat2.Item;

public class RoomItem {
    String roomName;
    String roomMemberLocationKey;//roomName과 이름이 같으면 안되기에, key로 두었습니다.
    //String password;

    public RoomItem(){

    }

    public String getRoomMemberLocationKey() {
        return roomMemberLocationKey;
    }

    public void setRoomMemberLocationKey(String roomMemberLocationKey) {
        this.roomMemberLocationKey = roomMemberLocationKey;
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
