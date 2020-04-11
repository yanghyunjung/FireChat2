package net.skhu.firechat2;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class FirebaseDbService implements ChildEventListener {

    MyRecyclerViewAdapter myRecyclerViewAdapter;
    ItemList itemList; // RecyclerView에 표시할 데이터 목록
    DatabaseReference databaseReference;
    String userId;
    RecyclerView recyclerView;
    BooleanCommunication checkedFreeScroll;
    Context context;

    String downloadFileName;
    File path;

    String roomKey;
    String roomName;

    int selectIndex;

    int selectVideoIndex;

    int selectPhotoIndex;

    public FirebaseDbService(Context context, MyRecyclerViewAdapter myRecyclerViewAdapter, ItemList itemList, String userId, RecyclerView recyclerView, BooleanCommunication checkedFreeScroll, String roomKey, String roomName) {
        this.myRecyclerViewAdapter = myRecyclerViewAdapter;
        this.itemList = itemList; // RecyclerView에 표시할 데이터 목록
        this.userId = userId;
        this.recyclerView = recyclerView;
        this.checkedFreeScroll = checkedFreeScroll;
        databaseReference = FirebaseDatabase.getInstance().getReference("myServerData04");
        databaseReference.child(roomKey).child(roomName).addChildEventListener(this);
        this.context = context;
        this.roomKey = roomKey;
        this.roomName = roomName;

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
        Item item = itemList.get(itemList.findIndex(key));

        if(item.getHavePhoto()) {
            File file = context.getFilesDir();
            File removeFile = new File(file, item.getPhotoFileName());
            if (removeFile.delete()) {
                Log.i("pjw", "file remove" + removeFile.getName() + "삭제성공");
            } else {
                Log.i("pjw", "file remove" + removeFile.getName() + "삭제실패");
            }
        }
        else if(item.getHaveVideo()) {
            File file = context.getFilesDir();
            File removeFile = new File(file, item.getVideoFileName());
            if (removeFile.delete()) {
                Log.i("pjw", "file remove" + removeFile.getName() + "삭제성공");
            } else {
                Log.i("pjw", "file remove" + removeFile.getName() + "삭제실패");
            }
        }
    }

    public void removeAllFromServer(){
        for (int i = 0; i < itemList.size(); i++){
            String key = itemList.getKey(i);
            databaseReference.child(roomKey).child(roomName).child(key).removeValue();

            Item item = itemList.get(itemList.findIndex(key));

            if(item.getHavePhoto()) {
                File file = context.getFilesDir();
                File removeFile = new File(file, item.getPhotoFileName());
                if (removeFile.delete()) {
                    Log.i("pjw", "file remove" + removeFile.getName() + "삭제성공");
                } else {
                    Log.i("pjw", "file remove" + removeFile.getName() + "삭제실패");
                }
            }
            else if(item.getHaveVideo()) {
                File file = context.getFilesDir();
                File removeFile = new File(file, item.getVideoFileName());
                if (removeFile.delete()) {
                    Log.i("pjw", "file remove" + removeFile.getName() + "삭제성공");
                } else {
                    Log.i("pjw", "file remove" + removeFile.getName() + "삭제실패");
                }
            }
        }
    }

    public void updateInServer(int index) {
        // 서버에서 데이터를 update 한다.
        String key = itemList.getKey(index);
        Item item = itemList.get(index);
        databaseReference.child(roomKey).child(roomName).child(key).setValue(item);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        // DB에 새 데이터 항목이 등록되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 등록된 새 데이터 항목이다.
        String key = dataSnapshot.getKey(); // 새 데이터 항목의 키 값을 꺼낸다.
        Item Item = dataSnapshot.getValue(net.skhu.firechat2.Item.class);  // 새 데이터 항목을 꺼낸다.
        int index = itemList.add(key, Item); // 새 데이터를 itemList에 등록한다.
        // key 값으로 등록된 데이터 항목이 없었기 때문에 새 데이터 항목이 등록된다.

        selectIndex = index;

        if(itemList.get(index).getHavePhoto()){
            downloadFileName = itemList.get(index).getPhotoFileName();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("images/" + downloadFileName);

            selectPhotoIndex = index;

            try{
                //로컬에 저장할 폴더의 위치
                path = context.getFilesDir();

                //저장하는 파일의 이름
                final File file = new File(path, downloadFileName);
                try {
                    if (!path.exists()) {
                        //저장할 폴더가 없으면 생성
                        path.mkdirs();
                    }
                    if (!file.exists()) {
                        file.createNewFile();
                        //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                        final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                        fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            int PhotoIndex = selectPhotoIndex;
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                //다운로드 성공 후 할 일
                                //Toast.makeText(context, file.getPath() + "다운로드 성공", Toast.LENGTH_LONG).show();
                                myRecyclerViewAdapter.notifyItemChanged(PhotoIndex);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //다운로드 실패 후 할 일
                                //Toast.makeText(context, file.getPath() + "다운로드 실패", Toast.LENGTH_LONG).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            //진행상태 표시
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }



        myRecyclerViewAdapter.notifyItemInserted(index); // RecyclerView를 다시 그린다.

        if (checkedFreeScroll != null) {
            if (!checkedFreeScroll.getBoolean()) {
                recyclerView.scrollToPosition(index);
            }
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        // DB의 어떤 데이터 항목이 수정되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 수정된 데이터 항목이다.
        String key = dataSnapshot.getKey();  // 수정된 데이터 항목의 키 값을 꺼낸다.
        Item Item = dataSnapshot.getValue(net.skhu.firechat2.Item.class); // 수정된 데이터 항목을 꺼낸다.
        int index = itemList.update(key, Item);  // 수정된 데이터를 itemList에 대입한다.
        // 전에 key 값으로 등록되었던 데이터가  덮어써진다. (overwrite)
        myRecyclerViewAdapter.notifyItemChanged(index); // RecyclerView를 다시 그린다.
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        // DB의 어떤 데이터 항목이 삭제 되었을 때, 이 메소드가 자동으로 호출된다.
        // dataSnapshot은 서버에서 삭제된 데이터 항목이다.
        String key = dataSnapshot.getKey(); // 삭제된 데이터 항목의 키 값을 꺼낸다.
        int index = itemList.remove(key); // itemList에서 그 데이터 항목을 삭제한다.
        myRecyclerViewAdapter.notifyItemRemoved(index); // RecyclerView를 다시 그린다.
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
