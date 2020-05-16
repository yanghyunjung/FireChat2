package net.skhu.firechat2.ListenerInterface.RoomLocationListener.Firebase;

import net.skhu.firechat2.Item.RoomMemberLocationItem;

public interface OnChildAddedLocationListener {
    void onChildAddedLocationListener(String key, RoomMemberLocationItem roomMemberLocationItem);
}
