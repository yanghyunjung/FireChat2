package net.skhu.firechat2.Room;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import net.skhu.firechat2.FirebaseDBService.FirebaseDbService;
import net.skhu.firechat2.FirebaseDBService.FirebaseDbServiceForRoomMemberLocationList;
import net.skhu.firechat2.FirebaseDBService.FirebaseStorageService;
import net.skhu.firechat2.FirebaseDBService.MusicUploadActivity;
import net.skhu.firechat2.FirebaseDBService.PhotoUploadActivity;
import net.skhu.firechat2.FirebaseDBService.RemoveFileThread;
import net.skhu.firechat2.FirebaseDBService.VideoUploadActivity;
import net.skhu.firechat2.InitInformDialog;
import net.skhu.firechat2.Item.Item;
import net.skhu.firechat2.Item.RoomMemberLocationItem;
import net.skhu.firechat2.Item.RoomMemberLocationItemList;
import net.skhu.firechat2.R;
import net.skhu.firechat2.Room.MemberLocation.GpsTracker;
import net.skhu.firechat2.Room.MemberLocation.RoomMemberLocationListActivity;
import net.skhu.firechat2.Room.MemberLocation.RoomMemberLocationRecyclerViewAdapter;
import net.skhu.firechat2.UnCatchTaskService;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class RoomActivity extends AppCompatActivity {

    static final int RC_LOGIN = 1; //  로그인 액티비티 호출을 구별하기 위한 식별 번호이다.
    FirebaseUser currentUser = null; // 현재 사용자

    String userName;
    String userEmail;
    Uri photoUrl;
    //ItemList itemList;
    int selectedIndex;
    //MyRecyclerViewAdapter myRecyclerViewAdapter;
    RoomChatRecyclerViewAdapter roomChatRecyclerViewAdapter;
    RecyclerView recyclerView;

    FirebaseDbService firebaseDbService;
    ItemEditDialogFragment itemEditDialogFragment; // 수정 대화상자 관리자
    RenameDiolog renameDiolog;//이름 변경 대화상자 관리자
    ScrollDiolog scrollDiolog;
    InitInformDialog initInformDialog;
    MusicPreviewDialog musicPreviewDialog;


    boolean checkedFreeScroll;
    CheckBox checkBoxFreeScroll;

    static final int DOWNLOAD_PHOTO = 2;
    String downloadFileName;
    File path;

    ImageView imageViewPhoto;

    static final int DOWNLOAD_VIDEO = 3;
    String downloadVideoFileName;

    String roomKey;
    String roomName;
    String roomMemberLocationKey;

    static final int DOWNLOAD_MUSIC = 4;
    String downloadMusicFileName;

    static final int DOWNLOAD_BINARY_FILE = 5;
    String downloadBinaryFileName;

    public static Context mContext;

    static final int SHOW_ROOM_MEMBER = 6;

    static final int SHOW_ROOM_MEMBER_LOCATION = 7;

    static final int PHOTO_PREVIEW = 8;

    static final int VIDEO_PREVIEW = 9;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    RoomMemberLocationItemList roomMemberLocationItemList;
    FirebaseDbServiceForRoomMemberLocationList firebaseDbServiceForRoomMemberLocationList;
    RoomMemberLocationRecyclerViewAdapter roomMemberLocationRecyclerViewAdapter;
    private GpsTracker gpsTracker;

    // 로그인 액티비티를 호출할 때, 사용할 요청 식별 번호(request code) 이다.
    static final int RC_SIGN_IN = 337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room); // 레이아웃 인플레이션

        startService(new Intent(this, UnCatchTaskService.class));

        userName = "anonymous";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roomKey = extras.getString("roomKey");
            userName = extras.getString("userName");
            roomName = extras.getString("roomName");
            userEmail = extras.getString("userEmail");
            roomMemberLocationKey = extras.getString("roomMemberLocationKey");
        }

        mContext = this;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //세로모드 고정

        initCheckBoxScroll();

        initRecyclerView(); // 리사이클러뷰 초기화

        initRecyclerViewRoomMemberLocationList();// 리사이클러뷰 초기화

        gpsTracker = new GpsTracker(RoomActivity.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String address = getCurrentAddress(latitude, longitude);

        RoomMemberLocationItem roomMemberLocationItem = new RoomMemberLocationItem();
        roomMemberLocationItem.setUserName(userName);
        roomMemberLocationItem.setUserEmail(userEmail);
        roomMemberLocationItem.setLatitude(latitude);
        roomMemberLocationItem.setLongitude(longitude);
        firebaseDbServiceForRoomMemberLocationList.addIntoServer(roomMemberLocationItem);

        //Toast.makeText(RoomActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();

        LocationUpdateThread locationUpdateThread = new LocationUpdateThread(this,
                ()->firebaseDbServiceForRoomMemberLocationList.updateUserSelf());
        Thread t = new Thread(locationUpdateThread,"locationUpdateThread");

        t.start();
    }

    // 리사이클러뷰 초기화 작업
    private void initRecyclerView() {
        //itemList = new ItemList(); // 데이터 목록 객체 생성

        // 리사이클러 뷰 설정
        //myRecyclerViewAdapter = new MyRecyclerViewAdapter(this, itemList, userEmail);
        roomChatRecyclerViewAdapter = new RoomChatRecyclerViewAdapter(this, userEmail,
                (index)->showItemEditDialog(index),
                (index)->intentPhotoPreview(index),
                (index)->intentVideoPreview(index),
                (index)->showMusicPreviewDialog(index));
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(roomChatRecyclerViewAdapter);

        // firebase DB 서비스 생성
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "anonymous";
        firebaseDbService = new FirebaseDbService(this, userId, roomKey, roomName,
                (key, item)->onAddedChatListener(key, item),
                (key, item)->onChangedChatListener(key, item),
                (key)->onRemovedChatListener(key));

        Button b = (Button) findViewById(R.id.btnSend);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText editText = (EditText) findViewById(R.id.editText);
                String message = editText.getText().toString();

                //에딧텍스트의 내용이 비어있으면 보내지 않도록 해주었습니다.
                if (!editText.getText().toString().equals("")) {
                    editText.setText("");
                    Item item = new Item(message);
                    item.setUserName(userName);
                    item.setUserEmail(userEmail);
                    firebaseDbService.addIntoServer(item);
                }
            }
        });

        //EditText editText = (EditText) findViewById(R.id.editText);
        //editText.setHorizontallyScrolling(false);

    }

    // 리사이클러뷰 초기화 작업
    private void initRecyclerViewRoomMemberLocationList() {
        roomMemberLocationItemList = new RoomMemberLocationItemList(); // 데이터 목록 객체 생성

        /*// 리사이클러 뷰 설정
        roomMemberLocationRecyclerViewAdapter = new RoomMemberLocationRecyclerViewAdapter(this, roomMemberLocationItemList);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerViewRoomMemberLocationList);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(roomMemberLocationRecyclerViewAdapter);*/

        // firebase DB 서비스 생성
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //String userId = (user != null) ? user.getUid() : "anonymous";
        firebaseDbServiceForRoomMemberLocationList = new FirebaseDbServiceForRoomMemberLocationList(this,
                null, roomMemberLocationItemList, null, roomKey, roomName, roomMemberLocationKey,
                null);
    }

    public void initCheckBoxScroll(){
        checkBoxFreeScroll = (CheckBox) findViewById(R.id.checkBoxFreeScroll);
        checkBoxFreeScroll.setChecked(false);
        checkedFreeScroll = false;//스크롤을 자유 해제를 default로 해주었습니다.

        checkBoxFreeScroll.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedFreeScroll) {
                    checkedFreeScroll=false;
                    Toast.makeText(getApplicationContext(), "자유 스크롤이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    checkedFreeScroll=true;
                    Toast.makeText(getApplicationContext(), "자유 스크롤이 설정되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showItemEditDialog(int position) {
        if (itemEditDialogFragment == null) // 대화상자 관리자 객체를 아직 만들지 않았다면
            itemEditDialogFragment = new ItemEditDialogFragment(); // 대화상자 관리자 객체를 만든다
        selectedIndex = position; // 수정할 항목의 index를 대입한다.
        itemEditDialogFragment.show(getSupportFragmentManager(), "EditDialog"); // 화면에 대화상자 보이기
    }

    public void showRenameDialog() {
        if (renameDiolog == null) {// 대화상자 관리자 객체를 아직 만들지 않았다면
            renameDiolog = new RenameDiolog(); // 대화상자 관리자 객체를 만든다
        }
        renameDiolog.show(getSupportFragmentManager(), "EditDialog"); // 화면에 대화상자 보이기
    }

    public void showScrollDialog() {
        if (scrollDiolog == null) {// 대화상자 관리자 객체를 아직 만들지 않았다면
            scrollDiolog = new ScrollDiolog(); // 대화상자 관리자 객체를 만든다
        }
        scrollDiolog.show(getSupportFragmentManager(), "EditDialog"); // 화면에 대화상자 보이기
    }

    public void showMusicPreviewDialog() {
        if (musicPreviewDialog == null) {// 대화상자 관리자 객체를 아직 만들지 않았다면
            musicPreviewDialog = new MusicPreviewDialog(); // 대화상자 관리자 객체를 만든다
        }
        musicPreviewDialog.show(getSupportFragmentManager(), "EditDialog"); // 화면에 대화상자 보이기
    }

    public static String locationDataStr(double latitude, double longitude){
        return "geo:"+latitude+", "+longitude;
    }

    public void intentPhotoPreview(int selectIndex){
        Item item = roomChatRecyclerViewAdapter.get(selectIndex);

        if (item.getHavePhoto()) {
            Intent intent = new Intent(this, PhotoPreview.class);
            intent.putExtra("photoFileName", item.getPhotoFileName());
            intent.putExtra("selectIndex", selectIndex);
            //startActivity(intent);
            startActivityForResult(intent, PHOTO_PREVIEW);
        }
    }

    public void intentVideoPreview(int selectIndex){
        /*Item item = itemList.get(selectIndex);
        int index = selectIndex;

        //final RoomActivity activity = (RoomActivity)view.getContext();
        //activity.showPhotoDialog(super.getAdapterPosition());

        downloadFileName = itemList.get(selectIndex).getVideoFileName();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("videos/" + downloadFileName);

        path = getFilesDir();

        try{

            //로컬에 저장할 폴더의 위치
            path = getFilesDir();

            //저장하는 파일의 이름
            final File file = new File(path, downloadFileName);
            try {
                if (!path.exists()) {
                    //저장할 폴더가 없으면 생성
                    path.mkdirs();
                }

                if (!file.exists()) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("다운로드중...");
                    progressDialog.show();

                    file.createNewFile();
                    //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                    final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                    fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        int VideoIndex = index;
                        String videoFileName = item.getVideoFileName();
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            //다운로드 성공 후 할 일
                            Toast.makeText(RoomActivity.this, file.getPath() + "다운로드 성공", Toast.LENGTH_LONG).show();
                            roomChatRecyclerViewAdapter.notifyItemChanged(VideoIndex);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //다운로드 실패 후 할 일
                            Toast.makeText(RoomActivity.this, file.getPath() + "다운로드 실패", Toast.LENGTH_LONG).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        //진행상태 표시
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            progressDialog.setMessage("download " + ((int) progress) + "% ...");
                        }
                    });
                }
                else{
                    Intent intent = new Intent(RoomActivity.this, VideoPreview.class);
                    intent.putExtra("videoFileName", item.getVideoFileName());
                    intent.putExtra("selectIndex", index);
                    startActivity(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(Exception e){
            e.printStackTrace();
        }*/
        if (roomChatRecyclerViewAdapter.get(selectIndex).getHaveVideo()) {
            Item item = roomChatRecyclerViewAdapter.get(selectIndex);
            int index = selectIndex;

            path = getFilesDir();

            downloadFileName = roomChatRecyclerViewAdapter.get(selectIndex).getVideoFileName();

            //저장하는 파일의 이름
            final File file = new File(path, downloadFileName);

            if (!path.exists()) {
                //저장할 폴더가 없으면 생성
                path.mkdirs();
            }

            if (!file.exists()) {
                FirebaseStorageService.videoDownload(this, getFilesDir(), downloadFileName,
                        () -> roomChatRecyclerViewAdapter.notifyDataSetChanged());
            } else {
                Intent intent = new Intent(RoomActivity.this, VideoPreview.class);
                intent.putExtra("videoFileName", item.getVideoFileName());
                intent.putExtra("selectIndex", index);
                startActivityForResult(intent, VIDEO_PREVIEW);
            }
        }
    }

    public void showMusicPreviewDialog(int selectIndex) {
        /*final Item item = itemList.get(selectIndex);
        int index = selectIndex;

        downloadFileName = itemList.get(index).getMusicFileName();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("audio/" + downloadFileName);

        try{

            //로컬에 저장할 폴더의 위치
            path = getFilesDir();

            //저장하는 파일의 이름
            final File file = new File(path, downloadFileName);
            try {
                if (!path.exists()) {
                    //저장할 폴더가 없으면 생성
                    path.mkdirs();
                }

                if (!file.exists()) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("다운로드중...");
                    progressDialog.show();

                    file.createNewFile();
                    //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                    final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                    fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        int MusicIndex = index;
                        String musicFileName = item.getMusicFileName();
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            //다운로드 성공 후 할 일
                            Toast.makeText(RoomActivity.this, file.getPath() + "다운로드 성공", Toast.LENGTH_LONG).show();

                            roomChatRecyclerViewAdapter.notifyItemChanged(MusicIndex);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //다운로드 실패 후 할 일
                            Toast.makeText(RoomActivity.this, file.getPath() + "다운로드 실패", Toast.LENGTH_LONG).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        //진행상태 표시
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            progressDialog.setMessage("download " + ((int) progress) + "% ...");
                        }
                    });
                }
                else{
                    selectedIndex = index;
                    showMusicPreviewDialog();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(Exception e){
            e.printStackTrace();
        }*/

        if (roomChatRecyclerViewAdapter.get(selectIndex).getHaveMusic()) {

            path = getFilesDir();

            downloadFileName = roomChatRecyclerViewAdapter.get(selectIndex).getMusicFileName();

            //저장하는 파일의 이름
            final File file = new File(path, downloadFileName);

            if (!path.exists()) {
                //저장할 폴더가 없으면 생성
                path.mkdirs();
            }

            if (!file.exists()) {
                FirebaseStorageService.audioDownload(this, getFilesDir(), downloadFileName,
                        () -> roomChatRecyclerViewAdapter.notifyDataSetChanged());
            } else {
                this.selectedIndex = selectIndex;
                showMusicPreviewDialog();
            }
        }
    }

    public void removeAll(){

        Iterator<String> iterator = roomChatRecyclerViewAdapter.getIteratorKeys();
        while (iterator.hasNext()) {
            //String key = iterator.next();
            //Item item = roomChatRecyclerViewAdapter.get(roomChatRecyclerViewAdapter.findIndex(key));
            //removeFile(item);
            //databaseReference.child(roomKey).child(roomName).child(key).removeValue();

            firebaseDbService.removeFromServer(iterator.next());
        }
    }

    public void onAddedChatListener(String key, Item item) {
        int index = roomChatRecyclerViewAdapter.add(key, item); // 새 데이터를 itemList에 등록한다.

        downloadPhoto(index);

        roomChatRecyclerViewAdapter.notifyItemInserted(index); // RecyclerView를 다시 그린다.

        if (!checkedFreeScroll) {
            recyclerView.scrollToPosition(index);
        }
    }

    public void downloadPhoto(int selectIndex){
        if(roomChatRecyclerViewAdapter.get(selectIndex).getHavePhoto()) {
            FirebaseStorageService.imagesDownload(this, getFilesDir(), roomChatRecyclerViewAdapter.get(selectIndex).getPhotoFileName(),
                    () -> roomChatRecyclerViewAdapter.notifyDataSetChanged());
        }
    }

    public void onChangedChatListener(String key, Item item){
        int index = roomChatRecyclerViewAdapter.update(key, item);  // 수정된 데이터를 itemList에 대입한다.
        // 전에 key 값으로 등록되었던 데이터가  덮어써진다. (overwrite)
        roomChatRecyclerViewAdapter.notifyItemChanged(index); // RecyclerView를 다시 그린다.
    }

    public void onRemovedChatListener(String key){
        RemoveFileThread removeFileThread = new RemoveFileThread(roomChatRecyclerViewAdapter.get(roomChatRecyclerViewAdapter.findIndex(key)), getFilesDir());
        Thread t = new Thread(removeFileThread,"RemoveFileThread");
        t.start();

        int index = roomChatRecyclerViewAdapter.remove(key); // itemList에서 그 데이터 항목을 삭제한다.
        roomChatRecyclerViewAdapter.notifyItemRemoved(index); // RecyclerView를 다시 그린다.
    }

    //////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_room, menu); // 메뉴 생성

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_rename) {
            this.showRenameDialog();
            return true;
        } else if (id == R.id.action_removeAll) {
            //firebaseDbService.removeAllFromServer();
            removeAll();
            return true;
        } else if (id == R.id.scroll) {
            this.showScrollDialog();
            return true;
        } else if (id == R.id.action_photoUpload) {
            Intent intent = new Intent(this, PhotoUploadActivity.class);
            startActivityForResult(intent, DOWNLOAD_PHOTO);
            return true;
        } else if (id == R.id.action_videoUpload) {
            Intent intent = new Intent(this, VideoUploadActivity.class);
            startActivityForResult(intent, DOWNLOAD_VIDEO);
            return true;
        }
        else if (id == R.id.action_closeRoom) {
            //firebaseDbService.removeAllFromServer();
            removeAll();

            Intent intent = new Intent();
            intent.putExtra("roomKey", roomKey);
            setResult(Activity.RESULT_OK, intent);
            finish();

            return true;
        }
        else if (id == R.id.action_musicUpload) {
            Intent intent = new Intent(this, MusicUploadActivity.class);
            startActivityForResult(intent, DOWNLOAD_MUSIC);

            return true;
        }
        else if (id == R.id.action_showMemberLocation) {

            Intent intent = new Intent(this, RoomMemberLocationListActivity.class);
            intent.putExtra("roomKey", roomKey);
            intent.putExtra("userName", userName);
            intent.putExtra("userEmail", userEmail);
            intent.putExtra("roomName", roomName);
            intent.putExtra("roomMemberLocationKey", roomMemberLocationKey);
            startActivityForResult(intent, SHOW_ROOM_MEMBER_LOCATION);

            return true;
        }
        else if (id == R.id.action_showMidpoint) {

            firebaseDbServiceForRoomMemberLocationList.updateInServerAll();

            double midpointLatitude = 0;
            double midpointLongitude = 0;

            for (int i = 0; i < roomMemberLocationItemList.size(); i++) {
                midpointLatitude += roomMemberLocationItemList.get(i).getLatitude();
                midpointLongitude += roomMemberLocationItemList.get(i).getLongitude();
            }
            midpointLatitude = midpointLatitude/roomMemberLocationItemList.size();
            midpointLongitude = midpointLongitude/roomMemberLocationItemList.size();

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setPackage("com.google.android.apps.maps");
            //String data = "geo:"+midpointLatitude+", "+midpointLongitude;
            intent.setData(Uri.parse(locationDataStr(midpointLatitude, midpointLongitude)));
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }


    // 화면에 현재 사용자 이름을 표시한다.
    void setUserName() {
        // 현재 사용자 객체를 구한다.
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) { // 로그인 성공했다면
            //textView.setText(currentUser.getDisplayName());
            userName = currentUser.getDisplayName();
        } else { // 로그인 실패했다면
            //textView.setText("Anonymous");
            userName = "Anonymous";
        }
        invalidateOptionsMenu(); // 메뉴를 상태를 변경해야 함
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(RoomActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(RoomActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(RoomActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(RoomActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(RoomActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(RoomActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    // startActivityForResult 메소드로 호출된 액티비티로부터 전달된 결과를 받기위한 메소드이다.
    // 파라미터 변수:
    //   requestCode: startActivityForResult 메소드를 호출할 때 전달한 호출 식별 번호이다.
    //   resultCode:  호출된 액티비티의 실행 결과 값이다. (RESULT_OK, RESULT_CANCELED)
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle extras;

        if (requestCode == DOWNLOAD_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                extras = data.getExtras();
                //Toast.makeText(this, "onActivityResult 됨", Toast.LENGTH_SHORT).show();
                if (extras != null) {
                    downloadVideoFileName = extras.getString("downloadVideoFileName");
                    Uri filePath = data.getParcelableExtra("originalPath");

                    Toast.makeText(this, downloadVideoFileName, Toast.LENGTH_LONG).show();

                    //final FirebaseStorage storage = FirebaseStorage.getInstance();
                    //StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("videos/" + downloadVideoFileName);

                    //Toast.makeText(this, downloadVideoFileName, Toast.LENGTH_SHORT).show();

                    try {
                        //로컬에 저장할 폴더의 위치


                        Item item = new Item("동영상");
                        item.setUserName(userName);
                        item.setUserEmail(userEmail);
                        item.setVideoFileName(downloadVideoFileName);
                        item.setHaveVideo(true);
                        firebaseDbService.addIntoServer(item);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (requestCode == DOWNLOAD_MUSIC) {
            if (resultCode == Activity.RESULT_OK) {
                extras = data.getExtras();
                //Toast.makeText(this, "onActivityResult 됨", Toast.LENGTH_SHORT).show();
                if (extras != null) {
                    downloadMusicFileName = extras.getString("downloadMusicFileName");
                    Uri filePath = data.getParcelableExtra("originalPath");

                    Toast.makeText(this, downloadMusicFileName, Toast.LENGTH_LONG).show();

                    //final FirebaseStorage storage = FirebaseStorage.getInstance();
                    //StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("videos/" + downloadVideoFileName);

                    //Toast.makeText(this, downloadVideoFileName, Toast.LENGTH_SHORT).show();

                    try {
                        //로컬에 저장할 폴더의 위치


                        Item item = new Item("음악");
                        item.setUserName(userName);
                        item.setUserEmail(userEmail);
                        item.setMusicFileName(downloadMusicFileName);
                        item.setHaveMusic(true);
                        firebaseDbService.addIntoServer(item);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (requestCode == DOWNLOAD_BINARY_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                extras = data.getExtras();
                //Toast.makeText(this, "onActivityResult 됨", Toast.LENGTH_SHORT).show();
                if (extras != null) {
                    downloadBinaryFileName = extras.getString("downloadBinaryFileName");
                    String realBinaryFileName = extras.getString("realBinaryFileName");
                    Uri filePath = data.getParcelableExtra("originalPath");

                    Toast.makeText(this, downloadBinaryFileName, Toast.LENGTH_LONG).show();

                    //final FirebaseStorage storage = FirebaseStorage.getInstance();
                    //StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("videos/" + downloadVideoFileName);

                    //Toast.makeText(this, downloadVideoFileName, Toast.LENGTH_SHORT).show();

                    try {
                        //로컬에 저장할 폴더의 위치


                        Item item = new Item("파일");
                        item.setUserName(userName);
                        item.setUserEmail(userEmail);
                        item.setBinaryFileName(downloadBinaryFileName);
                        item.setRealBinaryFileName(realBinaryFileName);
                        item.setHaveBinaryFile(true);
                        firebaseDbService.addIntoServer(item);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (requestCode == DOWNLOAD_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                extras = data.getExtras();
                if (extras != null) {
                    downloadFileName = extras.getString("downloadFileName");


                    Toast.makeText(this, downloadFileName, Toast.LENGTH_LONG).show();

                    final FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("images/" + downloadFileName);

                    try {
                        //로컬에 저장할 폴더의 위치
                        path = getFilesDir();

                        //저장하는 파일의 이름
                        final File file = new File(path, downloadFileName);
                        try {
                            if (!path.exists()) {
                                //저장할 폴더가 없으면 생성
                                path.mkdirs();
                            }
                            file.createNewFile();

                            //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                            final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                            final ProgressDialog progressDialog = new ProgressDialog(this);
                            fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                String downloadPhotoName = downloadFileName;

                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    //다운로드 성공 후 할 일
                                    //Toast.makeText(MainActivity.this, file.getPath()+"다운로드 성공", Toast.LENGTH_LONG).show();
                                    Item item = new Item("사진");
                                    item.setUserName(userName);
                                    item.setUserEmail(userEmail);
                                    item.setPhotoFileName(downloadPhotoName);
                                    item.setHavePhoto(true);
                                    firebaseDbService.addIntoServer(item);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //다운로드 실패 후 할 일
                                    //Toast.makeText(MainActivity.this, file.getPath()+"다운로드 실패", Toast.LENGTH_LONG).show();
                                }
                            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                //진행상태 표시
                                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (requestCode == PHOTO_PREVIEW) {
            if (resultCode == Activity.RESULT_OK) {
                extras = data.getExtras();
                if (extras != null) {
                    int index = extras.getInt("selectIndex");

                    String key = roomChatRecyclerViewAdapter.getKey(index);
                    firebaseDbService.removeFromServer(key);

                }
            }
        }

        if (requestCode == VIDEO_PREVIEW) {
            if (resultCode == Activity.RESULT_OK) {
                extras = data.getExtras();
                if (extras != null) {
                    int index = extras.getInt("selectIndex");

                    String key = roomChatRecyclerViewAdapter.getKey(index);
                    firebaseDbService.removeFromServer(key);

                }
            }
        }

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("pjw", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //세로모드 고정
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        int index = -1;
        for (int i = 0; i < roomMemberLocationItemList.size(); i++){
            if (roomMemberLocationItemList.get(i).getUserEmail().equals(userEmail)){
                index = i;
                break;
            }
        }

        if (index >=0 ) {
            firebaseDbServiceForRoomMemberLocationList.removeFromServer(roomMemberLocationItemList.getKey(index));
        }
    }
}
