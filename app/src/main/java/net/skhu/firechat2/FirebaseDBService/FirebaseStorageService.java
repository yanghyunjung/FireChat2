package net.skhu.firechat2.FirebaseDBService;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import net.skhu.firechat2.ListenerInterface.RoomChatListener.OnFileDownloadCompleteListener;

import java.io.File;
import java.io.IOException;

public class FirebaseStorageService {
    public static void videoDownload(Context context, File path, String downloadFileName, OnFileDownloadCompleteListener OnFileDownloadCompleted){
        //downloadFileName = itemList.get(selectIndex).getVideoFileName();

        com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("videos/" + downloadFileName);

        try{

            //로컬에 저장할 폴더의 위치
            //path = getFilesDir();

            //저장하는 파일의 이름
            final File file = new File(path, downloadFileName);
            try {
                if (!path.exists()) {
                    //저장할 폴더가 없으면 생성
                    path.mkdirs();
                }

                if (!file.exists()) {
                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("다운로드중...");
                    progressDialog.show();

                    file.createNewFile();
                    //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                    final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                    fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        OnFileDownloadCompleteListener onFileDownloadCompleteListener = OnFileDownloadCompleted;
                        //int VideoIndex = index;
                        //String videoFileName = item.getVideoFileName();
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            //다운로드 성공 후 할 일
                            onFileDownloadCompleteListener.onFileDownloadCompleteListener();

                            Log.v("pjw", file.getPath() + "다운로드 성공");
                            //roomChatRecyclerViewAdapter.notifyItemChanged(VideoIndex);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //다운로드 실패 후 할 일
                            Log.v("pjw", file.getPath() + "다운로드 실패");
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

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void audioDownload(Context context, File path, String downloadFileName, OnFileDownloadCompleteListener OnFileDownloadCompleted){
        //downloadFileName = itemList.get(selectIndex).getVideoFileName();

        com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("audio/" + downloadFileName);

        try{

            //로컬에 저장할 폴더의 위치
            //path = getFilesDir();

            //저장하는 파일의 이름
            final File file = new File(path, downloadFileName);
            try {
                if (!path.exists()) {
                    //저장할 폴더가 없으면 생성
                    path.mkdirs();
                }

                if (!file.exists()) {
                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("다운로드중...");
                    progressDialog.show();

                    file.createNewFile();
                    //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                    final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                    fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        OnFileDownloadCompleteListener onFileDownloadCompleteListener = OnFileDownloadCompleted;
                        //int VideoIndex = index;
                        //String videoFileName = item.getVideoFileName();
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            //다운로드 성공 후 할 일
                            onFileDownloadCompleteListener.onFileDownloadCompleteListener();

                            Log.v("pjw", file.getPath() + "다운로드 성공");
                            //roomChatRecyclerViewAdapter.notifyItemChanged(VideoIndex);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //다운로드 실패 후 할 일
                            Log.v("pjw", file.getPath() + "다운로드 실패");
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

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void imagesDownload(Context context, File path, String downloadFileName, OnFileDownloadCompleteListener OnFileDownloadCompleted){
        //downloadFileName = itemList.get(selectIndex).getVideoFileName();

        com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("images/" + downloadFileName);

        try{

            //로컬에 저장할 폴더의 위치
            //path = getFilesDir();

            //저장하는 파일의 이름
            final File file = new File(path, downloadFileName);
            try {
                if (!path.exists()) {
                    //저장할 폴더가 없으면 생성
                    path.mkdirs();
                }

                if (!file.exists()) {
                   // final ProgressDialog progressDialog = new ProgressDialog(context);
                    //progressDialog.setTitle("다운로드중...");
                   // progressDialog.show();

                    file.createNewFile();
                    //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                    final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                    fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        OnFileDownloadCompleteListener onFileDownloadCompleteListener = OnFileDownloadCompleted;
                        //int VideoIndex = index;
                        //String videoFileName = item.getVideoFileName();
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            //다운로드 성공 후 할 일
                            onFileDownloadCompleteListener.onFileDownloadCompleteListener();

                            Log.v("pjw", file.getPath() + "다운로드 성공");
                            //roomChatRecyclerViewAdapter.notifyItemChanged(VideoIndex);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //다운로드 실패 후 할 일
                            Log.v("pjw", file.getPath() + "다운로드 실패");
                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        //진행상태 표시
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                                    double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            //progressDialog.setMessage("download " + ((int) progress) + "% ...");
                        }
                    });
                }
                else{

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
