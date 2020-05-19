package net.skhu.firechat2.Room.MemberLocation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.skhu.firechat2.Item.RoomMemberLocationItem;
import net.skhu.firechat2.Item.RoomMemberLocationItemList;
import net.skhu.firechat2.ListenerInterface.RoomLocationListener.RecyclerView.OnClickRoomMemberLocationListener;
import net.skhu.firechat2.R;

import java.util.Iterator;

public class RoomMemberLocationRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textViewRoomMemberLocation;
        //CheckBox checkBox;


        public ViewHolder(View view) {
            super(view);
            this.textViewRoomMemberLocation = view.findViewById(R.id.textViewRoomMemberLocation);
            view.setOnClickListener(this);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            RoomMemberLocationItem roomMemberLocationItem = roomMemberLocationItemList.get(super.getAdapterPosition());
            //this.textViewRoomMember.setText(roomMemberItem.getUserName()+" : "+"현재위치 \n위도 " + roomMemberItem.getLatitude() + "\n경도 " + roomMemberItem.getLongitude());
            this.textViewRoomMemberLocation.setText(roomMemberLocationItem.getUserName());
        }

        @Override
        public void onClick(View view) {
            //RoomMemberLocationListActivity activity = (RoomMemberLocationListActivity)view.getContext();

            //activity.firebaseDbServiceForRoomMemberLocationList.updateInServer(super.getAdapterPosition());//상대 방에게 업데이트 요청

            if(onClickRoomMemberLocationListener != null) {
                onClickRoomMemberLocationListener.onClickRoomMemberLocation(super.getAdapterPosition());
            }

            //LocationIntentThread locationIntentThread = new LocationIntentThread(view, super.getAdapterPosition());
            //Thread thread = new Thread(locationIntentThread, "locationIntentThread");
            //thread.start();

            //추후에 firebase update할 때 listener 걸어 두어서 정확한 타이밍에 업데이트 받을 수 있게 구현

            //업데이트까지 지연시간.
            /*try {

                Thread.sleep(500); //0.5초 대기

            } catch (InterruptedException e) {

                e.printStackTrace();

            }


            RoomMemberLocationItem roomMemberLocationItem = activity.roomMemberLocationItemList.get(super.getAdapterPosition());//업데이트 받은 것 저장

            Toast.makeText(activity.getApplicationContext(), "현재위치 \n위도 " + roomMemberLocationItem.getLatitude() + "\n경도 " + roomMemberLocationItem.getLongitude(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setPackage("com.google.android.apps.maps");
            //String data = "geo:"+roomMemberLocationItem.getLatitude()+", "+roomMemberLocationItem.getLongitude();
            String data = locationDataStr(roomMemberLocationItem.getLatitude(), roomMemberLocationItem.getLongitude());
            intent.setData(Uri.parse(data));
            activity.startActivity(intent);*/
        }
    }

    LayoutInflater layoutInflater;
    RoomMemberLocationItemList roomMemberLocationItemList;
    Context context;
    OnClickRoomMemberLocationListener onClickRoomMemberLocationListener;

    final int ROOM_MEMBER_LOCATION=0;

    public RoomMemberLocationRecyclerViewAdapter(Context context, OnClickRoomMemberLocationListener onClickRoomMemberLocationListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.roomMemberLocationItemList = new RoomMemberLocationItemList();
        this.onClickRoomMemberLocationListener = onClickRoomMemberLocationListener;
    }

    // index 위치의 Item 객체를 리턴
    public RoomMemberLocationItem get(int index) {
        return roomMemberLocationItemList.get(index);
    }

    // index 위치의 키 값을 리턴
    public String getKey(int index) {
        return roomMemberLocationItemList.getKey(index);
    }

    // Item 객체의 수를 리턴
    public int size() {
        return roomMemberLocationItemList.size();
    }

    // key 값의 index를 리턴
    public int findIndex(String key) {
        return roomMemberLocationItemList.findIndex(key);
    }

    // key 값에 해당하는 Item 객체를 목록에서 제거
    public int remove(String key) {
        return roomMemberLocationItemList.remove(key);
    }

    // key 값과 Item 객체를 목록에 추가
    public int add(String key, RoomMemberLocationItem roomMemberLocationItem) {
        return roomMemberLocationItemList.add(key, roomMemberLocationItem);
    }

    // key 값에 해당하는 Item 객체 변경
    public int update(String key, RoomMemberLocationItem roomMemberLocationItem) {
        return roomMemberLocationItemList.update(key, roomMemberLocationItem);
    }

    public Iterator<String> getIteratorKeys(){
        return roomMemberLocationItemList.getIteratorKeys();
    }

    @Override
    public int getItemCount() {
        return roomMemberLocationItemList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType==ROOM_MEMBER_LOCATION) {
            View view = layoutInflater.inflate(R.layout.item_room_member_location_list, viewGroup, false);
            return new RoomMemberLocationRecyclerViewAdapter.ViewHolder(view);
        }

        View view = layoutInflater.inflate(R.layout.item_room_member_location_list, viewGroup, false);
        return new RoomMemberLocationRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof RoomMemberLocationRecyclerViewAdapter.ViewHolder) {
            ((RoomMemberLocationRecyclerViewAdapter.ViewHolder)viewHolder).setData();
        }
        else{
            ((RoomMemberLocationRecyclerViewAdapter.ViewHolder)viewHolder).setData();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ROOM_MEMBER_LOCATION;
    }

    public static String locationDataStr(double latitude, double longitude){
        return "geo:"+latitude+", "+longitude;
    }
}


