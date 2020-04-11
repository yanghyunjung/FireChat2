package net.skhu.firechat2;

import java.util.ArrayList;
import java.util.List;

public class RoomItemList {
    List<String> keys = new ArrayList<String>();
    List<RoomItem> RoomItems = new ArrayList<RoomItem>();

    // index 위치의 Item 객체를 리턴
    public RoomItem get(int index) {
        return RoomItems.get(index);
    }

    // index 위치의 키 값을 리턴
    public String getKey(int index) {
        return keys.get(index);
    }

    // Item 객체의 수를 리턴
    public int size() {
        return keys.size();
    }

    // key 값의 index를 리턴
    public int findIndex(String key) {
        for (int i = 0; i < keys.size(); ++i)
            if (keys.get(i).equals(key))
                return i;
        return -1;
    }

    // key 값에 해당하는 Item 객체를 목록에서 제거
    public int remove(String key) {
        int index = findIndex(key);
        keys.remove(index);
        RoomItems.remove(index);
        return index;
    }

    // key 값과 Item 객체를 목록에 추가
    public int add(String key, RoomItem roomItem) {
        keys.add(key);
        RoomItems.add(roomItem);
        return RoomItems.size() - 1;
    }

    // key 값에 해당하는 Item 객체 변경
    public int update(String key, RoomItem roomItem) {
        int index = findIndex(key);
        RoomItems.set(index, roomItem);
        return index;
    }
}
