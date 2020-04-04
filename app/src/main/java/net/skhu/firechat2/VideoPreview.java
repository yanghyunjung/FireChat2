package net.skhu.firechat2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

public class VideoPreview extends AppCompatActivity {

    //private static final int URL = ;
    String videoFileName;
    VideoView videoViewPreview;
    int selectIndex;
    int currentPos;

    boolean horizontalMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        currentPos=0;

        horizontalMode = false;

        videoViewPreview = findViewById(R.id.videoViewPreview);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            videoFileName = extras.getString("videoFileName");
            selectIndex = extras.getInt("selectIndex");
        }

        File path = getFilesDir();
        File videoFile = new File(path, videoFileName);

        if (videoFile.exists()) {
            videoViewPreview.setVideoPath(videoFile.toString());
        }

        /*Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri uri = Uri.parse(videoFile.toString());

        intent.setDataAndType(uri, "video/*");

        if (intent != null && intent.resolveActivity(getPackageManager()) != null) {

            startActivity(intent);

        }*/


        //미디어컨트롤러 추가하는 부분
        MediaController controller = new MediaController(this);
        videoViewPreview.setMediaController(controller);

        //비디오뷰 포커스를 요청함
        videoViewPreview.requestFocus();


        /*//동영상이 재생준비가 완료되었을 때를 알 수 있는 리스너 (실제 웹에서 영상을 다운받아 출력할 때 많이 사용됨)
        videoViewPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //Toast.makeText(getApplicationContext(), "동영상이 준비되었습니다. \n'시작' 버튼을 누르세요", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "동영상이 준비되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //동영상 재생이 완료된 걸 알 수 있는 리스너
        videoViewPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //동영상 재생이 완료된 후 호출되는 메소드
                //Toast.makeText(getApplicationContext(), "동영상 재생이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "동영상 재생이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });*/


        /*Button b = (Button) findViewById(R.id.btnExitVideo);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        b = (Button) findViewById(R.id.btnStartInPreview);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                playVideo();
            }
        });

        b = (Button) findViewById(R.id.btnStopInPreview);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                stopVideo();
            }
        });

        b = (Button) findViewById(R.id.btnVideoEdit);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String key = ((MainActivity) MainActivity.mContext).itemList.getKey(selectIndex);
                ((MainActivity) MainActivity.mContext).firebaseDbService.removeFromServer(key);
                finish();
            }
        });*/

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
                Toast.makeText(getApplicationContext(), "동영상이 준비되었습니다. \n'시작' 버튼을 누르세요", Toast.LENGTH_SHORT).show();
                videoViewPreview.seekTo(currentPos);
            }
        });

        videoViewPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            public void onCompletion(MediaPlayer arg0){
                videoViewPreview.seekTo(0);
                videoViewPreview.start();
                currentPos = videoViewPreview.getCurrentPosition();
            }
        });

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //가로모드 고정
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

    public Uri getUriFromPath(String path) {

        String fileName = path;

        Uri fileUri = Uri.parse(fileName);

        String filePath = fileUri.getPath();

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

                null, "_data = '" + filePath + "'", null, null);

        cursor.moveToNext();

        int id = cursor.getInt(cursor.getColumnIndex("_id"));

        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);


        return uri;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_video_preview, menu);

        MenuItem menuItem_horizontalMode = menu.findItem(R.id.action_horizontalMode); // 화면 가로 메뉴
        MenuItem menuItem_verticalMode = menu.findItem(R.id.action_verticalMode); // 화면 세로 메뉴
        menuItem_horizontalMode.setVisible(!horizontalMode);// 화면 가로 상태라면 화면 세로 메뉴가 보임
        menuItem_verticalMode.setVisible(horizontalMode);// 화면 가로 상태이면 화면 세로 메뉴가 보임

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
            String key = ((MainActivity) MainActivity.mContext).itemList.getKey(selectIndex);
            ((MainActivity) MainActivity.mContext).firebaseDbService.removeFromServer(key);
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
    }
}
