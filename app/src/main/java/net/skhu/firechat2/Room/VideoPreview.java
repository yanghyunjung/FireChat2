package net.skhu.firechat2.Room;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import net.skhu.firechat2.BuildConfig;
import net.skhu.firechat2.R;

import java.io.File;

public class VideoPreview extends AppCompatActivity {

    //private static final int URL = ;
    String videoFileName;
    VideoView videoViewPreview;
    int selectIndex;
    int currentPos;

    boolean horizontalMode;

    Menu mMenu;

    ActionBar actionBar;
    boolean actionShow;

    File videoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);


        currentPos=0;

        horizontalMode = false;

        videoViewPreview = findViewById(R.id.videoViewPreview);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            videoFileName = extras.getString("videoFileName");
            selectIndex = extras.getInt("selectIndex");
        }

        File path = getFilesDir();
        videoFile = new File(path, videoFileName);

        if (videoFile.exists()) {
            videoViewPreview.setVideoPath(videoFile.toString());
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID, videoFile);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "video/*");
        startActivity(intent);



        //미디어컨트롤러 추가하는 부분
        //MediaController controller = new MediaController(this);
        //videoViewPreview.setMediaController(controller);

        //비디오뷰 포커스를 요청함
        videoViewPreview.requestFocus();

        videoViewPreview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                finish();
                return true;
            }
        });


        //동영상이 재생준비가 완료되었을 때를 알 수 있는 리스너 (실제 웹에서 영상을 다운받아 출력할 때 많이 사용됨)
        videoViewPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoViewPreview.seekTo(currentPos);
                videoViewPreview.requestFocus();
                Toast.makeText(getApplicationContext(), "동영상이 준비되었습니다. \n'시작' 버튼을 누르세요", Toast.LENGTH_SHORT).show();
            }
        });

        videoViewPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            public void onCompletion(MediaPlayer arg0){
                videoViewPreview.seekTo(0);
                videoViewPreview.start();
                currentPos = videoViewPreview.getCurrentPosition();
            }
        });

        videoViewPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID, videoFile);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(uri, "video/*");
                startActivity(intent);
            }
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //가로모드 고정
        horizontalMode = true;
        invalidateOptionsMenu();
    }

    //동영상 재생 Method
    private void playVideo() {
        //비디오를 처음부터 재생할 때 0으로 시작(파라메터 sec)
        //videoViewPreview.seekTo(0);
        videoViewPreview.start();
    }

    //동영상 정지 Method
    private void stopVideo() {
        //비디오 재생 잠시 멈춤
        videoViewPreview.pause();
        //비디오 재생 완전 멈춤
//        videoView.stopPlayback();
        //videoView를 null로 반환 시 동영상의 반복 재생이 불가능
//        videoView = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_video_preview, menu);

        MenuItem menuItem_horizontalMode = menu.findItem(R.id.action_horizontalMode); // 화면 가로 메뉴
        MenuItem menuItem_verticalMode = menu.findItem(R.id.action_verticalMode); // 화면 세로 메뉴
        menuItem_horizontalMode.setVisible(!horizontalMode);// 화면 가로 상태라면 화면 세로 메뉴가 보임
        menuItem_verticalMode.setVisible(horizontalMode);// 화면 가로 상태이면 화면 세로 메뉴가 보임

        mMenu = menu;

        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_startVideo) {
            playVideo();
            return true;
        } else if (id == R.id.action_stopVideo) {
            stopVideo();
            return true;
        }
        else if (id == R.id.action_exit) {
            finish();
            return true;
        }
        else if (id == R.id.action_removeVideo) {
            String key = ((RoomActivity) RoomActivity.mContext).itemList.getKey(selectIndex);
            ((RoomActivity) RoomActivity.mContext).firebaseDbService.removeFromServer(key);
            this.finish();
            return true;
        }
        else if (id == R.id.action_horizontalMode){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //가로모드 고정
            horizontalMode = true;
            invalidateOptionsMenu(); // 메뉴를 상태를 변경해야 함
        }
        else if (id == R.id.action_verticalMode){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //세로모드 고정
            horizontalMode = false;
            invalidateOptionsMenu(); // 메뉴를 상태를 변경해야 함
        }
        else if (id == R.id.action_fullScreen){
            actionBar = getSupportActionBar();
            actionBar.hide();
            actionShow = false;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    protected void onPause() {
        super.onPause();
        currentPos = videoViewPreview.getCurrentPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoViewPreview.seekTo(currentPos);
        videoViewPreview.requestFocus();
    }

    private Uri getUriFromPath(String filePath) {
        long photoId;
        Uri photoUri = MediaStore.Video.Media.getContentUri("external");
        String[] projection = {MediaStore.Video.VideoColumns._ID};
        Cursor cursor = getContentResolver().query(photoUri, projection, MediaStore.Video.VideoColumns.DATA + " LIKE ?", new String[] { filePath }, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(projection[0]);
        photoId = cursor.getLong(columnIndex);

        cursor.close();
        return Uri.parse(photoUri.toString() + "/" + photoId);
    }
}
