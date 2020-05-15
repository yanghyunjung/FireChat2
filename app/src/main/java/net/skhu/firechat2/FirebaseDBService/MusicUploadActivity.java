package net.skhu.firechat2.FirebaseDBService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.skhu.firechat2.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MusicUploadActivity extends AppCompatActivity {
    private static final String TAG = "pjw";

    private Button bt_chooseMusic;
    private Button bt_uploadMusic;

    ImageView imageViewPlay;
    ImageView imageViewStop;

    SeekBar seekBarMusic;

    MediaPlayer music;

    private Uri filePath;

    File path;

    String filename;
    String mediaType;

    final static int MUSIC = 1;

    boolean uploaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_upload);

        uploaded = false;

        bt_chooseMusic = (Button) findViewById(R.id.bt_chooseMusic);
        bt_uploadMusic = (Button) findViewById(R.id.bt_uploadMusic);

        imageViewPlay = (ImageView) findViewById(R.id.imageViewPlay);
        imageViewStop = (ImageView) findViewById(R.id.imageViewStop);
        seekBarMusic = (SeekBar) findViewById(R.id.seekBarMusic);

        //버튼 클릭 이벤트
        bt_chooseMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (music != null) {
                    music.stop();
                }

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*") ;
                startActivityForResult(Intent.createChooser(intent, "재생 파일 불러오기"), MUSIC);
            }
        });

        bt_uploadMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploaded==false) {
                    //업로드
                    uploadFile();
                }

            }
        });


        seekBarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override

            public void onStopTrackingTouch(SeekBar seekBar) {

                // TODO Auto-generated method stub

            }



            @Override

            public void onStartTrackingTouch(SeekBar seekBar) {

                // TODO Auto-generated method stub

            }



            @Override

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // TODO Auto-generated method stub

                if(fromUser)

                    music.seekTo(progress);

            }

        });

        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                //Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID, videoFile);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(filePath, "audio/*");
                startActivity(intent);

                //music.start();
                //Thread();
            }
        });

        imageViewStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //music.pause();
            }
        });


    }


    //결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면

        if(requestCode == MUSIC) {
            if (resultCode == RESULT_OK) {

                filePath = data.getData();

                String realName;
                String[] pathStrArr = filePath.getPath().split("/");
                realName = pathStrArr[pathStrArr.length-1];

                String mimeType = getContentResolver().getType(filePath);
                mediaType = mimeType.replaceAll("audio/", ".");
                Toast.makeText(this, mediaType, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //upload the file
    private void uploadFile() {
        //업로드할 파일이 있으면 수행
        if (filePath != null) {
            uploaded = true;

            //업로드 진행 Dialog 보이기
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //Unique한 파일명을 만들자.
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss.SSS");
            Date now = new Date();
            filename = formatter.format(now) + mediaType;
            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("audio/" + filename);

            //올라가거라...
            storageRef.putFile(filePath)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();


                            /*try {
                                path = getFilesDir();

                                //저장하는 파일의 이름
                                final File file = new File(path, filename);
                                if (!file.exists()) {

                                    file.createNewFile();

                                    Cursor cursor2 = getContentResolver().query(filePath, null, null, null);
                                    cursor2.moveToNext();
                                    String pathStr = cursor2.getString(cursor2.getColumnIndex("_data"));
                                    cursor2.close();

                                    final File originalFile = new File(pathStr);

                                    String downloadVideoName = filename;

                                    Log.v("pjw", "\noriginalFile Path " + originalFile.toString());
                                    Log.v("pjw", "\nfile Path " + file.toString());

                                    try {

                                        FileInputStream inputStream = new FileInputStream(originalFile);

                                        FileOutputStream outputStream = new FileOutputStream(file);

                                        int bytesRead = 0;

                                        byte[] buffer = new byte[1024];

                                        while ((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
                                            outputStream.write(buffer, 0, bytesRead);
                                        }

                                        outputStream.close();

                                        inputStream.close();

                                    } catch (FileNotFoundException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            } catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }*/


                            Intent intent = new Intent();
                            intent.putExtra("downloadMusicFileName", filename);
                            setResult(Activity.RESULT_OK, intent);

                            finish();
                        }
                    })
                    //실패시
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //진행중
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                        }
                    });


        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }
    }


    public void Thread(){

        Runnable task = new Runnable(){

            public void run(){

                /**

                 * while문을 돌려서 음악이 실행중일때 게속 돌아가게 합니다

                 */

                while(music.isPlaying()){

                    try {

                        Thread.sleep(1000);

                    } catch (InterruptedException e) {

                        // TODO Auto-generated catch block

                        e.printStackTrace();

                    }

                    /**

                     * music.getCurrentPosition()은 현재 음악 재생 위치를 가져오는 구문 입니다

                     */

                    seekBarMusic.setProgress(music.getCurrentPosition());

                }

            }

        };

        Thread thread = new Thread(task);

        thread.start();

    }

    public String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(music != null) {
            music.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

