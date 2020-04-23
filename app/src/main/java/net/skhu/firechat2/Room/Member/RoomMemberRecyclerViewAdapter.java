package net.skhu.firechat2.Room.Member;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.skhu.firechat2.Item.RoomMemberItem;
import net.skhu.firechat2.Item.RoomMemberItemList;
import net.skhu.firechat2.R;

public class RoomMemberRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textViewRoomMember;
        //CheckBox checkBox;


        public ViewHolder(View view) {
            super(view);
            this.textViewRoomMember = view.findViewById(R.id.textViewRoomMember);
            view.setOnClickListener(this);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            RoomMemberItem roomMemberItem = roomMemberItemList.get(super.getAdapterPosition());
            //this.textViewRoomMember.setText(roomMemberItem.getUserName()+" : "+"현재위치 \n위도 " + roomMemberItem.getLatitude() + "\n경도 " + roomMemberItem.getLongitude());
            this.textViewRoomMember.setText(roomMemberItem.getUserName());
        }

        @Override
        public void onClick(View view) {
            RoomMemberListActivity activity = (RoomMemberListActivity)view.getContext();
            RoomMemberItem roomMemberItem = activity.roomMemberItemList.get(super.getAdapterPosition());

            Toast.makeText(activity.getApplicationContext(), "현재위치 \n위도 " + roomMemberItem.getLatitude() + "\n경도 " + roomMemberItem.getLongitude(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setPackage("com.google.android.apps.maps");
            String data = "geo:"+roomMemberItem.getLatitude()+", "+roomMemberItem.getLongitude();
            intent.setData(Uri.parse(data));
            activity.startActivity(intent);
        }
    }

    LayoutInflater layoutInflater;
    RoomMemberItemList roomMemberItemList;
    Context context;

    final int ROOM_MEMBER=0;

    public RoomMemberRecyclerViewAdapter(Context context, RoomMemberItemList roomMemberItemList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.roomMemberItemList = roomMemberItemList;
    }

    @Override
    public int getItemCount() {
        return roomMemberItemList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType==ROOM_MEMBER) {
            View view = layoutInflater.inflate(R.layout.item_room_member_list, viewGroup, false);
            return new RoomMemberRecyclerViewAdapter.ViewHolder(view);
        }

        View view = layoutInflater.inflate(R.layout.item_room_member_list, viewGroup, false);
        return new RoomMemberRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder) {
            ((ViewHolder)viewHolder).setData();
        }
        else{
            ((ViewHolder)viewHolder).setData();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ROOM_MEMBER;
    }
}