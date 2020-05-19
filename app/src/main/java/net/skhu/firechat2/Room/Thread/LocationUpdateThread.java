package net.skhu.firechat2.Room.Thread;

import android.content.Context;

import net.skhu.firechat2.ListenerInterface.RoomLocationListener.OnUpdateUserSelfListener;

public class LocationUpdateThread implements Runnable {
    //View view;
    Context context;
    long sleepMillis;
    OnUpdateUserSelfListener onUpdateUserSelfListener;
    private volatile boolean shutdown = false;

    public LocationUpdateThread(Context context, long sleepMillis, OnUpdateUserSelfListener onUpdateUserSelfListener) {
        //this.view = view;
        this.context = context;
        this.sleepMillis = sleepMillis;
        this.onUpdateUserSelfListener = onUpdateUserSelfListener;
    }

    @Override
    public void run() {
        //RoomActivity activity = (RoomActivity)context;

        while(true) {
            try {
                Thread.sleep(sleepMillis); //0.5초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized(this) {
            if(shutdown) {
                break;
            }
            }

            onUpdateUserSelfListener.onUpdateUserSelfListener();
        }

        //activity.firebaseDbServiceForRoomMemberLocationList.updateUserSelf();
    }

    public void cancel() {

        synchronized (this) {
            this.shutdown = true;
        }
    }
}