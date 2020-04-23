package net.skhu.firechat2.FirebaseDBService;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.skhu.firechat2.Item.RoomMemberLocationItem;
import net.skhu.firechat2.Item.RoomMemberLocationItemList;
import net.skhu.firechat2.Room.MemberLocation.GpsTracker;
import net.skhu.firechat2.Room.MemberLocation.RoomMemberLocationRecyclerViewAdapter;

import java.io.File;

public class FirebaseDbServiceForRoomMemberLocationList implements ChildEventListener {

    //MyRecyclerViewAdapter myRecyclerViewAdapter;
    RoomMemberLocationRecyclerViewAdapter roomMemberLocationRecyclerViewAdapter;
    //ItemList itemList; // RecyclerView에 표시할 데이터 목록
    RoomMemberLocationItemList roomMemberLocationItemList;
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

    private GpsTracker gpsTracker;

    public FirebaseDbServiceForRoomMemberLocationList(Context context, RoomMemberLocationRecyclerViewAdapter roomMemberLocationRecyclerViewAdapter, RoomMemberLocationItemList roomMemberLocationItemList, RecyclerView recyclerView, String roomKey) {
        this.roomMemberLocationRecyclerViewAdapter = roomMemberLocationRecyclerViewAdapter;
        this.roomMemberLocationItemList = roomMemberLocationItemList; // RecyclerView에 표시할 데이터 목록
        //this.userId = userId;
        this.recyclerView = recyclerView;
        databaseReference = FirebaseDatabase.getInstance().getReference("myServerData04");
        databaseReference.child(roomKey).child("RoomMemberLocationList").addChildEventListener(this);
        this.context = context;
        this.roomKey = roomKey;
        //this.roomName = roomName;

    }

    //데이터 베이스에 추가할 때
    public void addIntoServer(RoomMemberLocationItem roomMemberLocationItem) {
        // 새 기본 키(primary key)를 생성한다.
        String key = databaseReference.child(roomKey).child("RoomMemberLocationList").push().getKey();
        userKey = key;
        // 새 기본 키로 데이터를 등록한다.
        // 서버에서 key 값으로 dataItem 값이 새로 등록된다.
        databaseReference.child(roomKey).child("RoomMemberLocationList").child(key).setValue(roomMemberLocationItem);
    }

    public void removeFromServer(String key) {
        // 서버에서 데이터를 delete 한다.
        // 서버에서 key 값으로 등록된 데이터가 제거된다.
        databaseReference.child(roomKey).child("RoomMemberLocationList").child(key).removeValue();
    }

    public void removeAllFromServer(){
        for (int i = 0; i < roomMemberLocationItemList.size(); i++){
            String key = roomMemberLocationItemList.getKey(i);
            databaseReference.child(roomKey).child("RoomMemberLocationList").child(key).removeValue();
        }
    }

    public void updateInServer(int index) {
        // 서버에서 데이터를 update 한다.
        String key = roomMemberLocationItemList.getKey(index);
        RoomMemberLocationItem roomMemberLocationItem = roomMemberLocationItemList.get(index);

        if (userKey == key){//해당 사용자에게 위치 upodate요청이 오면 현제 위치를 roomMemberLocationItem에 저장해서 upodate시키도록 했습니다.
            gpsTracker = new GpsTracker(context);

            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            if (roomMemberLocationItem.getLatitude() != latitude &&
                    roomMemberLocationItem.getLongitude() != longitude) {
                roomMemberLocationItem.setLatitude(latitude);
                roomMemberLocationItem.setLongitude(longitude);
            }
        }

        databaseReference.child(roomKey).child("RoomMemberLocationList").child(key).setValue(roomMemberLocationItem);
    }

    public void updateInServerAll() {
        // 서버에서 데이터를 update 한다.
        for (int i = 0; i < roomMemberLocationItemList.size(); i++) {
            String key = roomMemberLocationItemList.getKey(i);
            RoomMemberLocationItem roomMemberLocationItem = roomMemberLocationItemList.get(i);

            if (userKey == key) {//해당 사용자에게 위치 upodate요청이 오면 현제 위치를 roomMemberLocationItem에 저장해서 upodate시키도록 했습니다.
                gpsTracker = new GpsTracker(context);

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();
                if (roomMemberLocationItem.getLatitude() != latitude &&
                        roomMemberLocationItem.getLongitude() != longitude) {
                    roomMemberLocationItem.setLatitude(latitude);
                    roomMemberLocationItem.setLongitude(longitude);
                }
            }

            databaseReference.child(roomKey).child("RoomMemberLocationList").child(key).setValue(roomMemberLocationItem);
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        // DB에 새 데이터 항목이 등록되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 등록된 새 데이터 항목이다.
        String key = dataSnapshot.getKey(); // 새 데이터 항목의 키 값을 꺼낸다.
        RoomMemberLocationItem RoomMemberLocationItem = dataSnapshot.getValue(net.skhu.firechat2.Item.RoomMemberLocationItem.class);  // 새 데이터 항목을 꺼낸다.
        int index = roomMemberLocationItemList.add(key, RoomMemberLocationItem); // 새 데이터를 itemList에 등록한다.
        // key 값으로 등록된 데이터 항목이 없었기 때문에 새 데이터 항목이 등록된다.

        selectIndex = index;

        if (roomMemberLocationRecyclerViewAdapter != null) {
            roomMemberLocationRecyclerViewAdapter.notifyItemInserted(index); // RecyclerView를 다시 그린다.
            //roomMemberRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        // DB의 어떤 데이터 항목이 수정되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 수정된 데이터 항목이다.
        String key = dataSnapshot.getKey();  // 수정된 데이터 항목의 키 값을 꺼낸다.
        RoomMemberLocationItem roomMemberLocationItem = dataSnapshot.getValue(net.skhu.firechat2.Item.RoomMemberLocationItem.class); // 수정된 데이터 항목을 꺼낸다.
        int index = roomMemberLocationItemList.update(key, roomMemberLocationItem);  // 수정된 데이터를 itemList에 대입한다.
        // 전에 key 값으로 등록되었던 데이터가  덮어써진다. (overwrite)

        if (userKey == key){//해당 사용자에게 위치 upodate요청이 오면 현제 위치를 upodate시키도록 했습니다.

            gpsTracker = new GpsTracker(context);

            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();

            //현제 위치가 이미 update 되어 있으면, 더 이상 update하지 않도록 해주었습니다.
            if (roomMemberLocationItemList.get(roomMemberLocationItemList.findIndex(userKey)).getLatitude() != latitude &&
                    roomMemberLocationItemList.get(roomMemberLocationItemList.findIndex(userKey)).getLongitude() != longitude) {
                updateInServer(roomMemberLocationItemList.findIndex(userKey));
            }
        }

        if (roomMemberLocationRecyclerViewAdapter != null) {
            roomMemberLocationRecyclerViewAdapter.notifyItemChanged(index); // RecyclerView를 다시 그린다.
            //roomMemberRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        // DB의 어떤 데이터 항목이 삭제 되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 삭제된 데이터 항목이다.
        String key = dataSnapshot.getKey(); // 삭제된 데이터 항목의 키 값을 꺼낸다.
        int index = roomMemberLocationItemList.remove(key); // itemList에서 그 데이터 항목을 삭제한다.
        if (roomMemberLocationRecyclerViewAdapter != null) {
            roomMemberLocationRecyclerViewAdapter.notifyItemRemoved(index); // RecyclerView를 다시 그린다.
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