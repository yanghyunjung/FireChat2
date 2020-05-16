package net.skhu.firechat2.FirebaseDBService;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.skhu.firechat2.Item.RoomItem;
import net.skhu.firechat2.RoomRecyclerViewAdapter;

import java.io.File;

public class FirebaseDbServiceForRoom implements ChildEventListener {

    RoomRecyclerViewAdapter roomRecyclerViewAdapter;
    //RoomItemList roomItemList; // RecyclerView에 표시할 데이터 목록
    DatabaseReference databaseReference;
    String userId;
    //RecyclerView recyclerView;
    //BooleanCommunication checkedFreeScroll;
    Context context;

    String downloadFileName;
    File path;

    int selectIndex;

    int selectVideoIndex;

    int selectPhotoIndex;

    public FirebaseDbServiceForRoom(Context context, RoomRecyclerViewAdapter roomRecyclerViewAdapter, String userId) {
        this.roomRecyclerViewAdapter = roomRecyclerViewAdapter;
       // this.roomItemList = roomItemList; // RecyclerView에 표시할 데이터 목록
        this.userId = userId;
        //this.checkedFreeScroll = checkedFreeScroll;
        databaseReference = FirebaseDatabase.getInstance().getReference("myServerData04");
        databaseReference.addChildEventListener(this);
        this.context = context;

    }

    //데이터 베이스에 추가할 때
    public void addIntoServer(RoomItem roomItem) {
        // 새 기본 키(primary key)를 생성한다.
        Log.v("pjw", "\n 데이터 베이스 추가");
        String key = databaseReference.push().getKey();
        // 새 기본 키로 데이터를 등록한다.

        roomItem.setRoomMemberLocationKey(receiveRoomMemberLocationKey(key));//roomItem 안에 RoomMemberLocationKey 값을 저장했습니다. 데이터 베이스에서 활용하기 client 모두에게 공유하는 용도로 사용할 것이기 때문입니다.

        // 서버에서 key 값으로 dataItem 값이 새로 등록된다.
        databaseReference.child(key).setValue(roomItem);
    }

    public String receiveRoomMemberLocationKey(String roomKey){
        return databaseReference.child(roomKey).push().getKey();
    }

    public void removeFromServer(String key) {
        // 서버에서 데이터를 delete 한다.
        // 서버에서 key 값으로 등록된 데이터가 제거된다.
        databaseReference.child(key).removeValue();
        //RoomItem roomItem = roomItemList.get(roomItemList.findIndex(key));
    }

    /*public void removeAllFromServer(){
//        for (int i = 0; i < roomItemList.size(); i++){
//            String key = roomItemList.getKey(i);
//            databaseReference.child(key).removeValue();

            //RoomItem roomItem = roomItemList.get(roomItemList.findIndex(key));
//        }

        Iterator<String> iterator = roomItemList.getIteratorKeys();
        while (iterator.hasNext()) {
            String key = iterator.next();

            databaseReference.child(key).removeValue();
        }
    }*/

    public void updateInServer(String key, RoomItem roomItem) {
        // 서버에서 데이터를 update 한다.
        //String key = roomRecyclerViewAdapter.getKey(index);
        //RoomItem roomItem = roomRecyclerViewAdapter.get(index);

        databaseReference.child(key).setValue(roomItem);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        // DB에 새 데이터 항목이 등록되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 등록된 새 데이터 항목이다.
        String key = dataSnapshot.getKey(); // 새 데이터 항목의 키 값을 꺼낸다.
        RoomItem roomItem = dataSnapshot.getValue(net.skhu.firechat2.Item.RoomItem.class);  // 새 데이터 항목을 꺼낸다.
        int index = roomRecyclerViewAdapter.add(key, roomItem); // 새 데이터를 itemList에 등록한다.
        // key 값으로 등록된 데이터 항목이 없었기 때문에 새 데이터 항목이 등록된다.

        selectIndex = index;


        roomRecyclerViewAdapter.notifyItemInserted(index); // RecyclerView를 다시 그린다.
        roomRecyclerViewAdapter.notifyDataSetChanged();

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        // DB의 어떤 데이터 항목이 수정되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 수정된 데이터 항목이다.
        String key = dataSnapshot.getKey();  // 수정된 데이터 항목의 키 값을 꺼낸다.
        RoomItem RoomItem = dataSnapshot.getValue(net.skhu.firechat2.Item.RoomItem.class); // 수정된 데이터 항목을 꺼낸다.
        int index = roomRecyclerViewAdapter.update(key, RoomItem);  // 수정된 데이터를 itemList에 대입한다.
        // 전에 key 값으로 등록되었던 데이터가  덮어써진다. (overwrite)
        roomRecyclerViewAdapter.notifyItemChanged(index); // RecyclerView를 다시 그린다.
        roomRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        // DB의 어떤 데이터 항목이 삭제 되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 삭제된 데이터 항목이다.
        String key = dataSnapshot.getKey(); // 삭제된 데이터 항목의 키 값을 꺼낸다.
        int index = roomRecyclerViewAdapter.remove(key); // itemList에서 그 데이터 항목을 삭제한다.
        roomRecyclerViewAdapter.notifyItemRemoved(index); // RecyclerView를 다시 그린다.
        roomRecyclerViewAdapter.notifyDataSetChanged();
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