package net.skhu.firechat2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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


    BooleanCommunication checkedFreeScroll;
    CheckBox checkBoxFreeScroll;

    final int DOWNLOAD_PHOTO = 2;
    String downloadFileName;
    File path;

    ImageView imageViewPhoto;

    final int DOWNLOAD_VIDEO = 3;
    String downloadVideoFileName;

    public static Context mContext;

    // 로그인 액티비티를 호출할 때, 사용할 요청 식별 번호(request code) 이다.
    static final int RC_SIGN_IN = 337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 레이아웃 인플레이션

        userName = "anonymous";

        mContext = this;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //세로모드 고정

        initRecyclerView(); // 리사이클러뷰 초기화
    }

    /*
    // startActivityForResult 메소드로 호출된 액티비티로부터 전달된 결과를 받기위한 메소드이다.
    // 파라미터 변수:
    //   requestCode: startActivityForResult 메소드를 호출할 때 전달한 호출 식별 번호이다.
    //   resultCode:  호출된 액티비티의 실행 결과 값이다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) { // 로그인 액티비티 호출에 대한 결과이면
            IdpResponse response = IdpResponse.fromResultIntent(data);
            String msg = null;
            if (resultCode == RESULT_OK) {
                // 로그인 작업이 성공인 경우
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                msg = "Authentication success. " + user.getDisplayName();
            } else {
                // 로그인 작업이 실패한 경우
                msg = "Authentication failure. " + response.getError().getErrorCode()
                        + " " + response.getError().getMessage();
            }
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            invalidateOptionsMenu(); // 메뉴 다시 그리기
            initRecyclerView();
        }


    }*/

    // 리사이클러뷰 초기화 작업
    private void initRecyclerView() {
        itemList = new ItemList(); // 데이터 목록 객체 생성

        checkedFreeScroll = new BooleanCommunication(false);//스크롤을 자유 해제를 default로 해주었습니다.

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
                myRecyclerViewAdapter, itemList, userId, recyclerView, checkedFreeScroll);

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

        startLoginInActivity();
        //this.showRenameDialog();

        this.showInitInformDialog();
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

    //정보 알리는 다이얼 로그
    public void showInitInformDialog() {
        if (initInformDialog == null) {// 대화상자 관리자 객체를 아직 만들지 않았다면
            initInformDialog = new InitInformDialog(); // 대화상자 관리자 객체를 만든다
        }
        initInformDialog.show(getSupportFragmentManager(), "EditDialog"); // 화면에 대화상자 보이기
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return true;
    }*/

    /*@Override public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_rename){
            this.showRenameDialog();
        }
        else if (id == R.id.action_removeAll){
            firebaseDbService.removeAllFromServer();
        }
        else if (id == R.id.scroll){
            this.showScrollDialog();
        }
        return super.onOptionsItemSelected(menuItem);
    }*/

    //////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // 메뉴 생성
        MenuItem menuItem_login = menu.findItem(R.id.action_login); // 로그인 메뉴
        MenuItem menuItem_logout = menu.findItem(R.id.action_logout); // 로그아웃 메뉴
        menuItem_login.setVisible(currentUser == null); // 로그아웃 상태이면 로그인 메뉴가 보임
        menuItem_logout.setVisible(currentUser != null); // 로그인 상태라면 로그아웃 메뉴가 보임

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_login) { // 로그인 메뉴 클릭
            startLoginInActivity();
            return true;
        } else if (id == R.id.action_logout) { // 로그아웃 메뉴 클릭
            logout();
            return true;
        } else if (id == R.id.action_rename) {
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


        return super.onOptionsItemSelected(menuItem);
    }

    // 로그인 액티비티를 호출하는 메소드이다.
    void startLoginInActivity() {
        // 이메일 인증과 구글 계정 인증을 사용하여 로그인 가능하도록 설정한다.
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                //new AuthUI.IdpConfig.EmailBuilder().build(), // 이메일 로그인 기능을 사용하려면 이 줄이 필요함
                new AuthUI.IdpConfig.GoogleBuilder().build()); // google 계정 로그인 기능을 사용하려면 이 줄이 필요함

        // 로그인(sign in) 액티비티를 호출한다.
        // startActivityForResult 메소드는, 다른 액티비티를 호출할 때 사용하는 메소드이다.
        // startActivityForResult = 결과를 받기 위해서 액티비티를 호출한다는 뜻이다.
        // 어떤 액티비티를 호출하고, 그 액티비티 실행 결과를 전달받겠다는 뜻이다.
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_LOGIN); // 로그인 액티비티 호출을 구별하기 위한 호출 식별 번호이다.
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

    // 로그아웃한다
    void logout() {
        FirebaseAuth.getInstance().signOut();
        setUserName(); // 화면에 표시된 사용자 이름을 "Anonymouse"로 바꾼다.
        moveTaskToBack(true);

        finish();

        android.os.Process.killProcess(android.os.Process.myPid());
    }

    // startActivityForResult 메소드로 호출된 액티비티로부터 전달된 결과를 받기위한 메소드이다.
    // 파라미터 변수:
    //   requestCode: startActivityForResult 메소드를 호출할 때 전달한 호출 식별 번호이다.
    //   resultCode:  호출된 액티비티의 실행 결과 값이다. (RESULT_OK, RESULT_CANCELED)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle extras;

        if (requestCode == RC_LOGIN) { // 로그인 액티비티 호출 결과
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {  // 로그인 작업이 성공인 경우
                Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_LONG).show();
            } else {
                // 로그인 작업이 실패한 경우
                String message = "Authentication failure. " + response.getError().getErrorCode()
                        + " " + response.getError().getMessage();
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
            setUserName(); // 화면에 표시된 사용자 이름을 표시한다.
        }

        if (requestCode == DOWNLOAD_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                extras = data.getExtras();
                //Toast.makeText(this, "onActivityResult 됨", Toast.LENGTH_SHORT).show();
                if (extras != null) {
                    downloadVideoFileName = extras.getString("downloadVideoFileName");

                    Toast.makeText(MainActivity.this, downloadVideoFileName, Toast.LENGTH_LONG).show();

                    final FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("videos/" + downloadVideoFileName);

                    Toast.makeText(this, downloadVideoFileName, Toast.LENGTH_SHORT).show();

                    try {
                        //로컬에 저장할 폴더의 위치
                        path = getFilesDir();

                        //저장하는 파일의 이름
                        final File file = new File(path, downloadVideoFileName);
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
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    //다운로드 성공 후 할 일
                                    Toast.makeText(MainActivity.this, file.getPath() + "다운로드 성공", Toast.LENGTH_LONG).show();

                                    Item item = new Item("동영상");
                                    item.setUserName(userName);
                                    item.setVideoFileName(downloadVideoFileName);
                                    item.setHaveVideo(true);
                                    firebaseDbService.addIntoServer(item);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //다운로드 실패 후 할 일
                                    Toast.makeText(MainActivity.this, file.getPath() + "다운로드 실패", Toast.LENGTH_LONG).show();
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

        if (requestCode == DOWNLOAD_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                extras = data.getExtras();
                if (extras != null) {
                    downloadFileName = extras.getString("downloadFileName");

                    Toast.makeText(MainActivity.this, downloadFileName, Toast.LENGTH_LONG).show();

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
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    //다운로드 성공 후 할 일
                                    //Toast.makeText(MainActivity.this, file.getPath()+"다운로드 성공", Toast.LENGTH_LONG).show();
                                    Item item = new Item("사진");
                                    item.setUserName(userName);
                                    item.setPhotoFileName(downloadFileName);
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