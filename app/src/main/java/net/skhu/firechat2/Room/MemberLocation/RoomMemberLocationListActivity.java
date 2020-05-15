package net.skhu.firechat2.Room.MemberLocation;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.skhu.firechat2.FirebaseDBService.FirebaseDbServiceForRoomMemberLocationList;
import net.skhu.firechat2.Item.RoomMemberLocationItem;
import net.skhu.firechat2.Item.RoomMemberLocationItemList;
import net.skhu.firechat2.R;
import net.skhu.firechat2.UnCatchTaskService;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RoomMemberLocationListActivity extends AppCompatActivity {


    RoomMemberLocationItemList roomMemberLocationItemList;
    FirebaseDbServiceForRoomMemberLocationList firebaseDbServiceForRoomMemberLocationList;
    RoomMemberLocationRecyclerViewAdapter roomMemberLocationRecyclerViewAdapter;

    String roomKey;
    String userEmail;
    String userName;
    String roomName;
    String roomMemberLocationKey;

    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    int selectIndex;

    Thread locationUpdateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_member_location_list);

        startService(new Intent(this, UnCatchTaskService.class));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roomKey = extras.getString("roomKey");
            userEmail = extras.getString("userEmail");
            userName = extras.getString("userName");
            roomName = extras.getString("roomName");
            roomMemberLocationKey = extras.getString("roomMemberLocationKey");
        }

        this.selectIndex=-1;

        initRecyclerViewRoomMemberLocationList();

    }

    // 리사이클러뷰 초기화 작업
    private void initRecyclerViewRoomMemberLocationList() {
        roomMemberLocationItemList = new RoomMemberLocationItemList(); // 데이터 목록 객체 생성

        // 리사이클러 뷰 설정
        roomMemberLocationRecyclerViewAdapter = new RoomMemberLocationRecyclerViewAdapter(this, roomMemberLocationItemList,
                (index)->intentMapInit(index));

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerViewRoomMemberLocationList);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(roomMemberLocationRecyclerViewAdapter);

        // firebase DB 서비스 생성
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //String userId = (user != null) ? user.getUid() : "anonymous";
        firebaseDbServiceForRoomMemberLocationList = new FirebaseDbServiceForRoomMemberLocationList(this,
                roomMemberLocationRecyclerViewAdapter, roomMemberLocationItemList, recyclerView, roomKey, roomName, roomMemberLocationKey,
                (index)->intentMap(index));
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

                    Toast.makeText(RoomMemberLocationListActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(RoomMemberLocationListActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(RoomMemberLocationListActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(RoomMemberLocationListActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(RoomMemberLocationListActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(RoomMemberLocationListActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(RoomMemberLocationListActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(RoomMemberLocationListActivity.this, REQUIRED_PERMISSIONS,
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

        AlertDialog.Builder builder = new AlertDialog.Builder(RoomMemberLocationListActivity.this);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void intentMapInit(int selectIndex){
        this.selectIndex = selectIndex;

        firebaseDbServiceForRoomMemberLocationList.updateInServer(selectIndex);//상대 방에게 업데이트 요청

        LocationIntentThread locationIntentThread = new LocationIntentThread(this, selectIndex);
        Thread thread = new Thread(locationIntentThread, "locationIntentThread");
        thread.start();
    }

    public void intentMap(int selectIndex){
        if (this.selectIndex == selectIndex) {//사용자가 클릭한, index 일 때, 구글 지도로 넘겨주도록 했습니다.
            RoomMemberLocationItem roomMemberLocationItem = roomMemberLocationItemList.get(selectIndex);//업데이트 받은 것 저장

            Log.v("pjw", "현재위치 \n위도 " + roomMemberLocationItem.getLatitude() + "\n경도 " + roomMemberLocationItem.getLongitude());

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setPackage("com.google.android.apps.maps");
            //String data = "geo:"+roomMemberLocationItem.getLatitude()+", "+roomMemberLocationItem.getLongitude();
            String data = locationDataStr(roomMemberLocationItem.getLatitude(), roomMemberLocationItem.getLongitude());
            intent.setData(Uri.parse(data));
            startActivity(intent);
        }
    }

    public static String locationDataStr(double latitude, double longitude){
        return "geo:"+latitude+", "+longitude;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //t1.append( "일시정지\n");
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.selectIndex = -1;//다시 앱으로 돌아왔을 때, 갑자기 지도로 넘어가지 않도록 하기 위함입니다.
        //t1.append( "재개\n");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //if (firebaseDbServiceForRoomMemberList!=null&&roomMemberItemList!=null){
        /*int index = -1;
        for (int i = 0; i < roomMemberLocationItemList.size(); i++){
            if (roomMemberLocationItemList.get(i).getUserEmail().equals(userEmail)){
                index = i;
                break;
            }
        }

        if (index >=0 ) {
            firebaseDbServiceForRoomMemberLocationList.removeFromServer(roomMemberLocationItemList.getKey(index));
        }*/
        //}
    }
}
