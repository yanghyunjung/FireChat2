package net.skhu.firechat2.Item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Item 객체의 목록과, firebase DB의 키(string) 목록을 관리하는 클래스
public class ItemList implements Serializable {//Serializable마킹 인터페이스, 이게 있어야, Activity끼리 객체를 전달할 수 있습니다.
    List<String> keys = new ArrayList<String>();
    List<Item> items = new ArrayList<Item>();

    // index 위치의 Item 객체를 리턴
    public Item get(int index) {
        return items.get(index);
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
        items.remove(index);
        return index;
    }

    // key 값과 Item 객체를 목록에 추가
    public int add(String key, Item item) {
        keys.add(key);
        items.add(item);
        return items.size() - 1;
    }

    // key 값에 해당하는 Item 객체 변경
    public int update(String key, Item item) {
        int index = findIndex(key);
        items.set(index, item);
        return index;
    }
}
