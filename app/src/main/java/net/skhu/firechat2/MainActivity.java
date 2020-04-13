package net.skhu.firechat2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int RC_LOGIN = 1; //  로그인 액티비티 호출을 구별하기 위한 식별 번호이다.
    FirebaseUser currentUser = null; // 현재 사용자

    String userName;
    //ItemList itemList;
    int selectedIndex;
    //MyRecyclerViewAdapter myRecyclerViewAdapter;
    RoomRecyclerViewAdapter roomRecyclerViewAdapter;

    RoomItemList roomItemList;

    FirebaseDbServiceForRoom firebaseDbServiceForRoom;
    ItemEditDialogFragment itemEditDialogFragment; // 수정 대화상자 관리자
    InitInformDialog initInformDialog;
    RoomCreateDialog roomCreateDialog;

    public static Context mContext;

    // 로그인 액티비티를 호출할 때, 사용할 요청 식별 번호(request code) 이다.
    static final int RC_SIGN_IN = 337;

    static final int ROOM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 레이아웃 인플레이션

        userName = "anonymous";

        mContext = this;

        startLoginInActivity();

        initRecyclerView();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //세로모드 고정
    }

    // 리사이클러뷰 초기화 작업
    private void initRecyclerView() {
        //itemList = new ItemList(); // 데이터 목록 객체 생성
        roomItemList = new RoomItemList();

        // 리사이클러 뷰 설정
        roomRecyclerViewAdapter = new RoomRecyclerViewAdapter(this, roomItemList);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerViewRoom);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(roomRecyclerViewAdapter);

        // firebase DB 서비스 생성
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "anonymous";
        firebaseDbServiceForRoom = new FirebaseDbServiceForRoom(this,
                roomRecyclerViewAdapter, roomItemList, userId, recyclerView);

        //this.showRenameDialog();

        this.showInitInformDialog();
    }


    //정보 알리는 다이얼 로그
    public void showInitInformDialog() {
        if (initInformDialog == null) {// 대화상자 관리자 객체를 아직 만들지 않았다면
            initInformDialog = new InitInformDialog(); // 대화상자 관리자 객체를 만든다
        }
        initInformDialog.show(getSupportFragmentManager(), "EditDialog"); // 화면에 대화상자 보이기
    }

    //정보 알리는 다이얼 로그
    public void showRoomCreateDialog() {
        if (roomCreateDialog == null) {// 대화상자 관리자 객체를 아직 만들지 않았다면
            roomCreateDialog = new RoomCreateDialog(); // 대화상자 관리자 객체를 만든다
        }
        roomCreateDialog.show(getSupportFragmentManager(), "EditDialog"); // 화면에 대화상자 보이기
    }


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
        }
        else if (id == R.id.action_create_room){
            this.showRoomCreateDialog();
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
    @RequiresApi(api = Build.VERSION_CODES.O)
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

        if (requestCode == ROOM){
            if (resultCode == RESULT_OK) {
                extras = data.getExtras();
                if (extras != null) {
                    String roomKey = extras.getString("roomKey");
                    firebaseDbServiceForRoom.removeFromServer(roomKey);
                }
            }

            roomRecyclerViewAdapter.notifyDataSetChanged();//여기서 화면 갱신 안 해주면, recyclerView크기 갑자기 변함
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //세로모드 고정
    }
}