package net.skhu.firechat2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.skhu.firechat2.Item.RoomItem;
import net.skhu.firechat2.Item.RoomItemList;
import net.skhu.firechat2.ListenerInterface.OnClickRoomListener;

public class RoomRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textViewRoomTitle;
        //CheckBox checkBox;


        public ViewHolder(View view) {
            super(view);
            this.textViewRoomTitle = view.findViewById(R.id.textViewRoomTitle);
            view.setOnClickListener(this);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            RoomItem roomItem = roomItemList.get(super.getAdapterPosition());
            this.textViewRoomTitle.setText(roomItem.getRoomName());
        }

        @Override
        public void onClick(View view) {
           /* MainActivity activity = (MainActivity)view.getContext();

            //방으로 들어가는 Intent
            Intent intent = new Intent(activity, RoomActivity.class);
            intent.putExtra("roomKey", roomItemList.getKey(super.getAdapterPosition()));
            intent.putExtra("userName", activity.userName);
            intent.putExtra("roomName", roomItemList.get(super.getAdapterPosition()).getRoomName());
            intent.putExtra("userEmail", activity.userEmail);
            intent.putExtra("roomMemberLocationKey", roomItemList.get(super.getAdapterPosition()).getRoomMemberLocationKey());

            activity.startActivityForResult(intent, activity.ROOM);*/

            onClickRoomListener.onClickRoomListener(super.getAdapterPosition());
        }
    }

    LayoutInflater layoutInflater;
    RoomItemList roomItemList;
    Context context;
    OnClickRoomListener onClickRoomListener;

    final int ROOM=0;

    public RoomRecyclerViewAdapter(Context context, RoomItemList roomItemList, OnClickRoomListener onClickRoomListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.roomItemList = roomItemList;
        this.onClickRoomListener = onClickRoomListener;
    }

    @Override
    public int getItemCount() {
        return roomItemList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType==ROOM) {
            View view = layoutInflater.inflate(R.layout.item_room, viewGroup, false);
            return new RoomRecyclerViewAdapter.ViewHolder(view);
        }

        View view = layoutInflater.inflate(R.layout.item_room, viewGroup, false);
        return new RoomRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof RoomRecyclerViewAdapter.ViewHolder) {
            ((RoomRecyclerViewAdapter.ViewHolder)viewHolder).setData();
        }
        else{
            ((RoomRecyclerViewAdapter.ViewHolder)viewHolder).setData();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ROOM;
    }
}