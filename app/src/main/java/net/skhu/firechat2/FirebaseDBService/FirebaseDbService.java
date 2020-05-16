package net.skhu.firechat2.FirebaseDBService;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.skhu.firechat2.Item.Item;
import net.skhu.firechat2.ListenerInterface.RoomChatListener.Firebase.OnChildAddedRoomChatListener;
import net.skhu.firechat2.ListenerInterface.RoomChatListener.Firebase.OnChildChangedRoomChatListener;
import net.skhu.firechat2.ListenerInterface.RoomChatListener.Firebase.OnChildRemovedRoomChatListener;

import java.io.File;

public class FirebaseDbService implements ChildEventListener {

    //MyRecyclerViewAdapter myRecyclerViewAdapter;
    //RoomChatRecyclerViewAdapter roomChatRecyclerViewAdapter;
    //ItemList itemList; // RecyclerView에 표시할 데이터 목록
    DatabaseReference databaseReference;
    String userId;
    //RecyclerView recyclerView;
    //BooleanCommunication checkedFreeScroll;
    Context context;

    String downloadFileName;
    File path;

    String roomKey;
    String roomName;

    int selectIndex;

    int selectVideoIndex;

    int selectPhotoIndex;

    OnChildAddedRoomChatListener onChildAddedRoomChatListener;
    OnChildChangedRoomChatListener onChildChangedRoomChatListener;
    OnChildRemovedRoomChatListener onChildRemovedRoomChatListener;

    public FirebaseDbService(Context context, String userId, String roomKey, String roomName, OnChildAddedRoomChatListener onChildAddedRoomChatListener,
                             OnChildChangedRoomChatListener onChildChangedRoomChatListener, OnChildRemovedRoomChatListener onChildRemovedRoomChatListener) {
        //this.roomChatRecyclerViewAdapter = roomChatRecyclerViewAdapter;
        //this.itemList = itemList; // RecyclerView에 표시할 데이터 목록
        this.userId = userId;
        //this.recyclerView = recyclerView;
        //this.checkedFreeScroll = checkedFreeScroll;
        databaseReference = FirebaseDatabase.getInstance().getReference("myServerData04");
        databaseReference.child(roomKey).child(roomName).addChildEventListener(this);
        this.context = context;
        this.roomKey = roomKey;
        this.roomName = roomName;

        this.onChildAddedRoomChatListener = onChildAddedRoomChatListener;
        this.onChildChangedRoomChatListener = onChildChangedRoomChatListener;
        this.onChildRemovedRoomChatListener = onChildRemovedRoomChatListener;
    }

    //데이터 베이스에 추가할 때
    public void addIntoServer(Item item) {
        // 새 기본 키(primary key)를 생성한다.
        String key = databaseReference.child(roomKey).child(roomName).push().getKey();
        // 새 기본 키로 데이터를 등록한다.
        // 서버에서 key 값으로 dataItem 값이 새로 등록된다.
        databaseReference.child(roomKey).child(roomName).child(key).setValue(item);
    }

    public void removeFromServer(String key) {
        // 서버에서 데이터를 delete 한다.
        // 서버에서 key 값으로 등록된 데이터가 제거된다.
        databaseReference.child(roomKey).child(roomName).child(key).removeValue();

        //Item item = roomChatRecyclerViewAdapter.get(roomChatRecyclerViewAdapter.findIndex(key));

        //removeFile(item);
    }

//    public void removeAllFromServer(){
//
//        Iterator<String> iterator = roomChatRecyclerViewAdapter.getIteratorKeys();
//        while (iterator.hasNext()) {
//            //String key = iterator.next();
            //Item item = roomChatRecyclerViewAdapter.get(roomChatRecyclerViewAdapter.findIndex(key));
            //removeFile(item);
            //databaseReference.child(roomKey).child(roomName).child(key).removeValue();

//            removeFromServer(iterator.next());
//        }
//    }

    public void updateInServer(String key, Item item) {
        // 서버에서 데이터를 update 한다.
        //String key = roomChatRecyclerViewAdapter.getKey(index);
       // Item item = roomChatRecyclerViewAdapter.get(index);
        databaseReference.child(roomKey).child(roomName).child(key).setValue(item);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        // DB에 새 데이터 항목이 등록되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 등록된 새 데이터 항목이다.
        String key = dataSnapshot.getKey(); // 새 데이터 항목의 키 값을 꺼낸다.
        Item item = dataSnapshot.getValue(net.skhu.firechat2.Item.Item.class);  // 새 데이터 항목을 꺼낸다.

//        int index = roomChatRecyclerViewAdapter.add(key, item); // 새 데이터를 itemList에 등록한다.
        // key 값으로 등록된 데이터 항목이 없었기 때문에 새 데이터 항목이 등록된다.

//        roomChatRecyclerViewAdapter.notifyItemInserted(index); // RecyclerView를 다시 그린다.

        //       if (checkedFreeScroll != null) {
//            if (!checkedFreeScroll.getBoolean()) {
//                recyclerView.scrollToPosition(index);
//            }
//        }

        onChildAddedRoomChatListener.onChildAddedRoomChatListener(key, item);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        // DB의 어떤 데이터 항목이 수정되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 수정된 데이터 항목이다.
        String key = dataSnapshot.getKey();  // 수정된 데이터 항목의 키 값을 꺼낸다.
        Item item = dataSnapshot.getValue(net.skhu.firechat2.Item.Item.class); // 수정된 데이터 항목을 꺼낸다.

        //int index = roomChatRecyclerViewAdapter.update(key, item);  // 수정된 데이터를 itemList에 대입한다.
        // 전에 key 값으로 등록되었던 데이터가  덮어써진다. (overwrite)
        //roomChatRecyclerViewAdapter.notifyItemChanged(index); // RecyclerView를 다시 그린다.

        onChildChangedRoomChatListener.onChildChangedRoomChatListener(key, item);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        // DB의 어떤 데이터 항목이 삭제 되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 삭제된 데이터 항목이다.
        String key = dataSnapshot.getKey(); // 삭제된 데이터 항목의 키 값을 꺼낸다.

        //removeFile(roomChatRecyclerViewAdapter.get(roomChatRecyclerViewAdapter.findIndex(key)));

//        RemoveFileThread removeFileThread = new RemoveFileThread(roomChatRecyclerViewAdapter.get(roomChatRecyclerViewAdapter.findIndex(key)), context.getFilesDir());
//        Thread t = new Thread(removeFileThread,"RemoveFileThread");
//        t.start();

//        int index = roomChatRecyclerViewAdapter.remove(key); // itemList에서 그 데이터 항목을 삭제한다.
//        roomChatRecyclerViewAdapter.notifyItemRemoved(index); // RecyclerView를 다시 그린다.

        onChildRemovedRoomChatListener.onChildRemovedRoomChatListener(key);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
        // DB의 어떤 데이터 항목의 위치가 변경되었을 때, 이 메소드가 자동으로 호출된다.
        // 데이터 이동 기능을 구현하지 않을 것이기 때문에, 이 메소드를 구현하지 않는다.
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // Firebase DB에서 에러가 발생했을 때, 이 메소드가 자동으로 호출된다.
        Log.e("Firebase Error", databaseError.getMessage());
    }
}
