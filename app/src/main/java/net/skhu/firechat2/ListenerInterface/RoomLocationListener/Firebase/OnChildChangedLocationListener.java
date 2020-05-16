package net.skhu.firechat2.ListenerInterface.RoomLocationListener.Firebase;

import net.skhu.firechat2.Item.RoomMemberLocationItem;

public interface OnChildChangedLocationListener {
    void onChildChangedLocation(String key, RoomMemberLocationItem roomMemberLocationItem);
}
