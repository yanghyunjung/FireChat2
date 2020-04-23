package net.skhu.firechat2.FirebaseDBService;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.skhu.firechat2.Item.RoomMemberItem;
import net.skhu.firechat2.Item.RoomMemberItemList;
import net.skhu.firechat2.Room.Member.RoomMemberRecyclerViewAdapter;

import java.io.File;

public class FirebaseDbServiceForRoomMemberList implements ChildEventListener {

        //MyRecyclerViewAdapter myRecyclerViewAdapter;
        RoomMemberRecyclerViewAdapter roomMemberRecyclerViewAdapter;
        //ItemList itemList; // RecyclerView에 표시할 데이터 목록
        RoomMemberItemList roomMemberItemList;
        DatabaseReference databaseReference;
        //String userId;
        RecyclerView recyclerView;
        Context context;

        String downloadFileName;
        File path;

        String roomKey;
        //String roomName;

        int selectIndex;

        int selectVideoIndex;

        int selectPhotoIndex;

        String userKey;

    public FirebaseDbServiceForRoomMemberList(Context context, RoomMemberRecyclerViewAdapter roomMemberRecyclerViewAdapter, RoomMemberItemList roomMemberItemList, RecyclerView recyclerView, String roomKey) {
            this.roomMemberRecyclerViewAdapter = roomMemberRecyclerViewAdapter;
            this.roomMemberItemList = roomMemberItemList; // RecyclerView에 표시할 데이터 목록
            //this.userId = userId;
            this.recyclerView = recyclerView;
            databaseReference = FirebaseDatabase.getInstance().getReference("myServerData04");
            databaseReference.child(roomKey).child("RoomMemberList").addChildEventListener(this);
            this.context = context;
            this.roomKey = roomKey;
            //this.roomName = roomName;

        }

        //데이터 베이스에 추가할 때
        public void addIntoServer(RoomMemberItem roomMemberItem) {
            // 새 기본 키(primary key)를 생성한다.
            String key = databaseReference.child(roomKey).child("RoomMemberList").push().getKey();
            userKey = key;
            // 새 기본 키로 데이터를 등록한다.
            // 서버에서 key 값으로 dataItem 값이 새로 등록된다.
            databaseReference.child(roomKey).child("RoomMemberList").child(key).setValue(roomMemberItem);
        }

        public void removeFromServer(String key) {
            // 서버에서 데이터를 delete 한다.
            // 서버에서 key 값으로 등록된 데이터가 제거된다.
            databaseReference.child(roomKey).child("RoomMemberList").child(key).removeValue();
        }

        public void removeAllFromServer(){
            for (int i = 0; i < roomMemberItemList.size(); i++){
                String key = roomMemberItemList.getKey(i);
                databaseReference.child(roomKey).child("RoomMemberList").child(key).removeValue();
            }
        }

        public void updateInServer(int index) {
            // 서버에서 데이터를 update 한다.
            String key = roomMemberItemList.getKey(index);
            RoomMemberItem roomMemberItem = roomMemberItemList.get(index);
            databaseReference.child(roomKey).child("RoomMemberList").child(key).setValue(roomMemberItem);
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            // DB에 새 데이터 항목이 등록되었을 때, 이 메소드가 자동으로 호출된다.
            // dataSnapshot은 서버에서 등록된 새 데이터 항목이다.
            String key = dataSnapshot.getKey(); // 새 데이터 항목의 키 값을 꺼낸다.
            RoomMemberItem RoomMemberItem = dataSnapshot.getValue(net.skhu.firechat2.Item.RoomMemberItem.class);  // 새 데이터 항목을 꺼낸다.
            int index = roomMemberItemList.add(key, RoomMemberItem); // 새 데이터를 itemList에 등록한다.
            // key 값으로 등록된 데이터 항목이 없었기 때문에 새 데이터 항목이 등록된다.

            selectIndex = index;

            if (roomMemberRecyclerViewAdapter != null) {
                roomMemberRecyclerViewAdapter.notifyItemInserted(index); // RecyclerView를 다시 그린다.
                //roomMemberRecyclerViewAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            // DB의 어떤 데이터 항목이 수정되었을 때, 이 메소드가 자동으로 호출된다.
            // dataSnapshot은 서버에서 수정된 데이터 항목이다.
            String key = dataSnapshot.getKey();  // 수정된 데이터 항목의 키 값을 꺼낸다.
            RoomMemberItem roomMemberItem = dataSnapshot.getValue(net.skhu.firechat2.Item.RoomMemberItem.class); // 수정된 데이터 항목을 꺼낸다.
            int index = roomMemberItemList.update(key, roomMemberItem);  // 수정된 데이터를 itemList에 대입한다.
            // 전에 key 값으로 등록되었던 데이터가  덮어써진다. (overwrite)
            if (roomMemberRecyclerViewAdapter != null) {
                roomMemberRecyclerViewAdapter.notifyItemChanged(index); // RecyclerView를 다시 그린다.
                //roomMemberRecyclerViewAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            // DB의 어떤 데이터 항목이 삭제 되었을 때, 이 메소드가 자동으로 호출된다.
            // dataSnapshot은 서버에서 삭제된 데이터 항목이다.
            String key = dataSnapshot.getKey(); // 삭제된 데이터 항목의 키 값을 꺼낸다.
            int index = roomMemberItemList.remove(key); // itemList에서 그 데이터 항목을 삭제한다.
            if (roomMemberRecyclerViewAdapter != null) {
                roomMemberRecyclerViewAdapter.notifyItemRemoved(index); // RecyclerView를 다시 그린다.
                //roomMemberRecyclerViewAdapter.notifyDataSetChanged();
            }
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
