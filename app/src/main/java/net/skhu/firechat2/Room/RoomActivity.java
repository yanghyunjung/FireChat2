package net.skhu.firechat2.Room;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
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

import net.skhu.firechat2.FirebaseDBService.FileUploadActivity;
import net.skhu.firechat2.FirebaseDBService.FirebaseDbService;
import net.skhu.firechat2.FirebaseDBService.MusicUploadActivity;
import net.skhu.firechat2.FirebaseDBService.VideoUploadActivity;
import net.skhu.firechat2.InitInformDialog;
import net.skhu.firechat2.Item.Item;
import net.skhu.firechat2.Item.ItemList;
import net.skhu.firechat2.R;

import java.io.File;
import java.io.IOException;

public class RoomActivity extends AppCompatActivity {

    static final int RC_LOGIN = 1; //  로그인 액티비티 호출을 구별하기 위한 식별 번호이다.
    FirebaseUser currentUser = null; // 현재 사용자

    String userName;
    ItemList itemList;
    int selectedIndex;
    MyRecyclerViewAdapter myRecyclerViewAdapter;

    FirebaseDbService firebaseDbService;
    ItemEditDialogFragment itemEditDialogFragment; // 수정 대화상자 관리자
    RenameDiolog renameDiolog;//이름 변경 대화상자 관리자
    ScrollDiolog scrollDiolog;
    InitInformDialog initInformDialog;
    MusicPreviewDialog musicPreviewDialog;


    BooleanCommunication checkedFreeScroll;
    CheckBox checkBoxFreeScroll;

    final int DOWNLOAD_PHOTO = 2;
    String downloadFileName;
    File path;

    ImageView imageViewPhoto;

    final int DOWNLOAD_VIDEO = 3;
    String downloadVideoFileName;

    String roomKey;
    String roomName;

    final int DOWNLOAD_MUSIC = 4;
    String downloadMusicFileName;

    final int DOWNLOAD_BINARY_FILE = 5;
    String downloadBinaryFileName;

    public static Context mContext;

    // 로그인 액티비티를 호출할 때, 사용할 요청 식별 번호(request code) 이다.
    static final int RC_SIGN_IN = 337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room); // 레이아웃 인플레이션

        userName = "anonymous";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roomKey = extras.getString("RoomKey");
            userName = extras.getString("userName");
            roomName = extras.getString("RoomName");
        }

        mContext = this;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //세로모드 고정

        initCheckBoxScroll();

        initRecyclerView(); // 리사이클러뷰 초기화
    }

    // 리사이클러뷰 초기화 작업
    private void initRecyclerView() {
        itemList = new ItemList(); // 데이터 목록 객체 생성

        // 리사이클러 뷰 설정
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(this, itemList);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(myRecyclerViewAdapter);

        // firebase DB 서비스 생성
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "anonymous";
        firebaseDbService = new FirebaseDbService(this,
                myRecyclerViewAdapter, itemList, userId, recyclerView, checkedFreeScroll, roomKey, roomName);

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
                    firebaseDbService.addIntoServer(item);
                }
            }
        });

        //EditText editText = (EditText) findViewById(R.id.editText);
        //editText.setHorizontallyScrolling(false);

    }

    public void initCheckBoxScroll(){
        checkedFreeScroll = new BooleanCommunication(false);//스크롤을 자유 해제를 default로 해주었습니다.

        checkBoxFreeScroll = (CheckBox) findViewById(R.id.checkBoxFreeScroll);
        checkBoxFreeScroll.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedFreeScroll.getBoolean()) {
                    checkedFreeScroll.setBoolean(false);
                    Toast.makeText(getApplicationContext(), "자유 스크롤이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    checkedFreeScroll.setBoolean(true);
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
        } else if (id == R.id.action_removeAll) {
            firebaseDbService.removeAllFromServer();
        } else if (id == R.id.scroll) {
            this.showScrollDialog();
        } else if (id == R.id.action_photoUpload) {
            Intent intent = new Intent(this, FileUploadActivity.class);
            startActivityForResult(intent, DOWNLOAD_PHOTO);
            return true;
        } else if (id == R.id.action_videoUpload) {
            Intent intent = new Intent(this, VideoUploadActivity.class);
            startActivityForResult(intent, DOWNLOAD_VIDEO);
        }
        else if (id == R.id.action_closeRoom) {
            firebaseDbService.removeAllFromServer();

            Intent intent = new Intent();
            intent.putExtra("roomKey", roomKey);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        else if (id == R.id.action_musicUpload) {
            Intent intent = new Intent(this, MusicUploadActivity.class);
            startActivityForResult(intent, DOWNLOAD_MUSIC);
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

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //세로모드 고정
    }
}
