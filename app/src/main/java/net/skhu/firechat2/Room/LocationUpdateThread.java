package net.skhu.firechat2.Room;

import android.content.Context;

import net.skhu.firechat2.ListenerInterface.RoomLocationListener.OnUpdateUserSelfListener;

public class LocationUpdateThread implements Runnable {
    //View view;
    Context context;
    OnUpdateUserSelfListener onUpdateUserSelfListener;
    public LocationUpdateThread(Context context, OnUpdateUserSelfListener onUpdateUserSelfListener) {
        //this.view = view;
        this.context = context;
        this.onUpdateUserSelfListener = onUpdateUserSelfListener;
    }

    public void run() {
        //RoomActivity activity = (RoomActivity)context;

        try {
            Thread.sleep(2000); //0.5초 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onUpdateUserSelfListener.onUpdateUserSelfListener();

        //activity.firebaseDbServiceForRoomMemberLocationList.updateUserSelf();
    }
}