package net.skhu.firechat2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import net.skhu.firechat2.FirebaseDBService.FirebaseDbServiceForRoom;
import net.skhu.firechat2.Item.RoomItemList;
import net.skhu.firechat2.Room.ItemEditDialogFragment;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int RC_LOGIN = 1; //  로그인 액티비티 호출을 구별하기 위한 식별 번호이다.
    FirebaseUser currentUser = null; // 현재 사용자

    String userName;
    String userEmail;
    //ItemList itemList;
    int selectedIndex;
    //MyRecyclerViewAdapter myRecyclerViewAdapter;
    RoomRecyclerViewAdapter roomRecyclerViewAdapter;

    RoomItemList roomItemList;

   // RoomMemberItemList roomMemberItemList;
   // FirebaseDbServiceForRoomMemberList firebaseDbServiceForRoomMemberList;

    FirebaseDbServiceForRoom firebaseDbServiceForRoom;
    ItemEditDialogFragment itemEditDialogFragment; // 수정 대화상자 관리자
    InitInformDialog initInformDialog;
    RoomCreateDialog roomCreateDialog;

    public static Context mContext;

    // 로그인 액티비티를 호출할 때, 사용할 요청 식별 번호(request code) 이다.
    static final int RC_SIGN_IN = 337;

    static final int ROOM = 2;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 레이아웃 인플레이션

        startService(new Intent(this, UnCatchTaskService.class));

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

        userName = "anonymous";

        mContext = this;

        startLoginInActivity();

        initRecyclerView();

        //roomMemberItemList = new RoomMemberItemList();

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
            userEmail = currentUser.getEmail();
        } else { // 로그인 실패했다면
            //textView.setText("Anonymous");
            userName = "Anonymous";
            userEmail= "Anonymous";
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

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

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

            /*int index = -1;
            for (int i = 0; i < roomMemberItemList.size(); i++){
                if (roomMemberItemList.get(i).getUserEmail().equals(userEmail)){
                    index = i;
                    break;
                }
            }

            if (index >=0 ) {
                firebaseDbServiceForRoomMemberList.removeFromServer(roomMemberItemList.getKey(index));
            }*/
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //세로모드 고정
    }
}