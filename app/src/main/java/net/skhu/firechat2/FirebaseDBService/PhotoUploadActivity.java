package net.skhu.firechat2.FirebaseDBService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoUploadActivity extends AppCompatActivity {

    private static final String TAG = "pjw";

    private Button btChoose;
    private Button btUpload;
    private ImageView ivPreview;

    private Uri filePath;

    File path;

    String filename;
    String mediaType;

    boolean uploaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_upload);

        uploaded = false;

        btChoose = (Button) findViewById(R.id.bt_choose);
        btUpload = (Button) findViewById(R.id.bt_upload);
        ivPreview = (ImageView) findViewById(R.id.iv_preview);

        //버튼 클릭 이벤트
        btChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이미지를 선택
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);
            }
        });

        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uploaded == false) {
                    //업로드
                    uploadFile();

                    //finish();
                }
            }
        });
    }

    //결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if(requestCode == 0 && resultCode == RESULT_OK){
            filePath = data.getData();
            Log.d(TAG, "uri:" + String.valueOf(filePath));
            try {
                //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivPreview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //upload the file
    private void uploadFile() {
        //업로드할 파일이 있으면 수행
        if (filePath != null) {
            uploaded=true;

            //업로드 진행 Dialog 보이기
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //Unique한 파일명을 만들자.
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss.SSS");
            Date now = new Date();
            filename = formatter.format(now) + ".png";
            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("images/" + filename);

            //올라가거라...
            storageRef.putFile(filePath)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();

                            //Uri externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            String[] projection = new String[]{
                                    MediaStore.Images.Media._ID,
                                    MediaStore.Images.Media.DISPLAY_NAME,
                                    MediaStore.Images.Media.MIME_TYPE
                            };

                            Cursor cursor = getContentResolver().query(filePath, projection, null, null, null);

                            if (cursor == null || !cursor.moveToFirst()) {
                                Log.e("pjw", "cursor null or cursor is empty");
                            }
                            else {
                                do {
                                    String contentUrl = filePath.toString() + "/" + cursor.getString(0);

                                    Log.v("pjw", "contentUrl" + contentUrl);

                                    path = getFilesDir();

                                    //저장하는 파일의 이름
                                    final File file = new File(path, filename);

                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                /*Cursor cursor = getContentResolver().query(filePath, null, null, null);
                                cursor.moveToNext();
                                String pathStr = cursor.getString(cursor.getColumnIndex("_data"));
                                cursor.close();

                                final File originalFile = new File(pathStr);

                                String downloadVideoName = filename;

                                Log.v("pjw", "\noriginalFile Path " + originalFile.toString());
                                Log.v("pjw", "\nfile Path " + file.toString());*/

                                    try {

                                        FileInputStream inputStream = new FileInputStream(contentUrl);

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

                                    Log.v("pjw", "\nfile Path " + file.toString());

                                /*try {
                                    InputStream is = getContentResolver().openInputStream(Uri.parse(contentUrl));
                                    OutputStream os;


                                    int data = 0;
                                    StringBuilder sb = new StringBuilder();

                                    while ((data = is.read()) != -1) {
                                        sb.append((char) data);
                                    }

                                    is.close();

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/

                                } while (cursor.moveToNext());
                            }

                            Intent intent = new Intent();
                            intent.putExtra("downloadFileName", filename);
                            //intent.putExtra("originalFilePath", pathStr);
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
}
