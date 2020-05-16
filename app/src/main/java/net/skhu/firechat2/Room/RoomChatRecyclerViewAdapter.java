package net.skhu.firechat2.Room;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.skhu.firechat2.Item.Item;
import net.skhu.firechat2.Item.ItemList;
import net.skhu.firechat2.ListenerInterface.RoomChatListener.RecyclerView.OnClickRoomMessageListener;
import net.skhu.firechat2.ListenerInterface.RoomChatListener.RecyclerView.OnClickRoomMusicListener;
import net.skhu.firechat2.ListenerInterface.RoomChatListener.RecyclerView.OnClickRoomPhotoListener;
import net.skhu.firechat2.ListenerInterface.RoomChatListener.RecyclerView.OnClickRoomVideoListener;
import net.skhu.firechat2.R;

import java.io.File;
import java.util.Iterator;

public class RoomChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textView1, textView2;
        //CheckBox checkBox;


        public ViewHolder(View view) {
            super(view);
            this.textView1 = view.findViewById(R.id.textView1);
            this.textView2 = view.findViewById(R.id.textView2);
            view.setOnClickListener(this);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            Item item = itemList.get(super.getAdapterPosition());
            this.textView1.setText(item.getUserName() + " : " + item.getMessage());
            this.textView2.setText(item.getCreateTimeFormatted());
        }

        @Override
        public void onClick(View view) {
            //RoomActivity activity = (RoomActivity)view.getContext();
            //activity.showItemEditDialog(super.getAdapterPosition());

            onClickRoomMessageListener.onClickRoomMessageListener(super.getAdapterPosition());
        }
    }

    class ViewHolderPhoto extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textViewInPhoto;
        TextView textViewNameInPhoto;
        ImageView imageViewPhoto;
        //CheckBox checkBox;


        public ViewHolderPhoto(View view) {
            super(view);
            this.textViewNameInPhoto = view.findViewById(R.id.textViewNameInPhoto);
            this.textViewInPhoto = view.findViewById(R.id.textViewTimeInPhoto);
            this.imageViewPhoto = view.findViewById(R.id.imageViewPhoto);
            view.setOnClickListener(this);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            Item item = itemList.get(super.getAdapterPosition());
            this.textViewInPhoto.setText(item.getCreateTimeFormatted());
            this.textViewNameInPhoto.setText(item.getUserName());

            File path = context.getFilesDir();
            File imgFile = new File(path, item.getPhotoFileName());

            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                this.imageViewPhoto.setImageBitmap(myBitmap);
                //Toast.makeText(context, "파일 있음", Toast.LENGTH_SHORT).show();
            }
            else {
                //Toast.makeText(context, "파일 없음", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onClick(View view) {
           /* Item item = itemList.get(super.getAdapterPosition());

            RoomActivity activity = (RoomActivity)view.getContext();
            //activity.showPhotoDialog(super.getAdapterPosition());

            Intent intent = new Intent(activity, PhotoPreview.class);
            intent.putExtra("photoFileName", item.getPhotoFileName());
            intent.putExtra("selectIndex", super.getAdapterPosition());
            activity.startActivity(intent);*/

            onClickRoomPhotoListener.onClickRoomPhotoListener(super.getAdapterPosition());
        }
    }

    class ViewHolderVideo extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textViewNameInVideo, textViewTimeInVideo;
        //CheckBox checkBox;
        VideoView videoView;

        String downloadFileName;
        int selectVideoIndex;
        File path;

        public ViewHolderVideo(View view) {
            super(view);
            this.textViewNameInVideo = view.findViewById(R.id.textViewNameInVideo);
            this.textViewTimeInVideo = view.findViewById(R.id.textViewTimeInVideo);
            this.videoView = view.findViewById(R.id.videoView);
            view.setOnClickListener(this);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            Item item = itemList.get(super.getAdapterPosition());
            this.textViewNameInVideo.setText(item.getUserName());
            this.textViewTimeInVideo.setText(item.getCreateTimeFormatted());

            File path = context.getFilesDir();
            File videoFile = new File(path, item.getVideoFileName());

            if(videoFile.exists()){
                videoView.setVideoPath(videoFile.toString());
                //Toast.makeText(context, "비디오 파일 있음", Toast.LENGTH_SHORT).show();
                Log.v("pjw", "비디오 파일 있음");
            }
            else {
                //Toast.makeText(context, "비디오 파일 없음", Toast.LENGTH_SHORT).show();
                Log.v("pjw", "비디오 파일 없음");
            }

            this.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.seekTo(0);
                }
            });

            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    return true;
                }
            });
        }

        @Override
        public void onClick(View view) {
           /* final Item item = itemList.get(super.getAdapterPosition());
            int index = super.getAdapterPosition();

            final RoomActivity activity = (RoomActivity)view.getContext();
            //activity.showPhotoDialog(super.getAdapterPosition());

            downloadFileName = itemList.get(index).getVideoFileName();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("videos/" + downloadFileName);

            selectVideoIndex = index;


            try{

                //로컬에 저장할 폴더의 위치
                path = context.getFilesDir();

                //저장하는 파일의 이름
                final File file = new File(path, downloadFileName);
                try {
                    if (!path.exists()) {
                        //저장할 폴더가 없으면 생성
                        path.mkdirs();
                    }

                    if (!file.exists()) {
                        final ProgressDialog progressDialog = new ProgressDialog(activity);
                        progressDialog.setTitle("다운로드중...");
                        progressDialog.show();

                        file.createNewFile();
                        //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                        final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                        fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            int VideoIndex = selectVideoIndex;
                            String videoFileName = item.getVideoFileName();
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                                //다운로드 성공 후 할 일
                                Toast.makeText(context, file.getPath() + "다운로드 성공", Toast.LENGTH_LONG).show();

                                notifyItemChanged(VideoIndex);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //다운로드 실패 후 할 일
                                Toast.makeText(context, file.getPath() + "다운로드 실패", Toast.LENGTH_LONG).show();
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
                        Intent intent = new Intent(activity, VideoPreview.class);
                        intent.putExtra("videoFileName", item.getVideoFileName());
                        intent.putExtra("selectIndex", selectVideoIndex);
                        activity.startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch(Exception e){
                e.printStackTrace();
            }*/

            onClickRoomVideoListener.onClickRoomVideoListener(super.getAdapterPosition());
        }
    }

    class ViewHolderMusic extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textViewNameInMusic, textViewTimeInMusic;
        //CheckBox checkBox;

        String downloadFileName;
        int selectVideoIndex;
        File path;

        public ViewHolderMusic(View view) {
            super(view);
            this.textViewNameInMusic = view.findViewById(R.id.textViewNameInMusic);
            this.textViewTimeInMusic = view.findViewById(R.id.textViewTimeInMusic);

            view.setOnClickListener(this);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            Item item = itemList.get(super.getAdapterPosition());
            this.textViewNameInMusic.setText(item.getUserName());
            this.textViewTimeInMusic.setText(item.getCreateTimeFormatted());

            File path = context.getFilesDir();
            File musicFile = new File(path, item.getMusicFileName());
        }

        @Override
        public void onClick(View view) {
            /*final Item item = itemList.get(super.getAdapterPosition());
            int index = super.getAdapterPosition();

            final RoomActivity activity = (RoomActivity)view.getContext();
            //activity.showPhotoDialog(super.getAdapterPosition());

            downloadFileName = itemList.get(index).getMusicFileName();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("audio/" + downloadFileName);

            selectVideoIndex = index;


            try{

                //로컬에 저장할 폴더의 위치
                path = context.getFilesDir();

                //저장하는 파일의 이름
                final File file = new File(path, downloadFileName);
                try {
                    if (!path.exists()) {
                        //저장할 폴더가 없으면 생성
                        path.mkdirs();
                    }

                    if (!file.exists()) {
                        final ProgressDialog progressDialog = new ProgressDialog(activity);
                        progressDialog.setTitle("다운로드중...");
                        progressDialog.show();

                        file.createNewFile();
                        //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                        final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                        fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            int MusicIndex = selectVideoIndex;
                            String musicFileName = item.getMusicFileName();
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                                //다운로드 성공 후 할 일
                                Toast.makeText(context, file.getPath() + "다운로드 성공", Toast.LENGTH_LONG).show();

                                notifyItemChanged(MusicIndex);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //다운로드 실패 후 할 일
                                Toast.makeText(context, file.getPath() + "다운로드 실패", Toast.LENGTH_LONG).show();
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
                        activity.selectedIndex = super.getAdapterPosition();
                        activity.showMusicPreviewDialog();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch(Exception e){
                e.printStackTrace();
            }*/

            onClickRoomMusicListener.onClickRoomMusicListener(super.getAdapterPosition());
        }
    }

    class ViewHolderRight extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textView1, textView2;
        //CheckBox checkBox;


        public ViewHolderRight(View view) {
            super(view);
            this.textView1 = view.findViewById(R.id.textView1);
            this.textView2 = view.findViewById(R.id.textView2);
            view.setOnClickListener(this);

            //textView1.setBackgroundResource(R.color.LeftChat);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            Item item = itemList.get(super.getAdapterPosition());
            this.textView1.setText(item.getUserName() + " : " + item.getMessage());
            this.textView2.setText(item.getCreateTimeFormatted());
        }

        @Override
        public void onClick(View view) {
            //RoomActivity activity = (RoomActivity)view.getContext();
            //activity.showItemEditDialog(super.getAdapterPosition());

            onClickRoomMessageListener.onClickRoomMessageListener(super.getAdapterPosition());
        }
    }

    class ViewHolderPhotoRight extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textViewInPhoto;
        TextView textViewNameInPhoto;
        ImageView imageViewPhoto;
        //CheckBox checkBox;


        public ViewHolderPhotoRight(View view) {
            super(view);
            this.textViewNameInPhoto = view.findViewById(R.id.textViewNameInPhoto);
            this.textViewInPhoto = view.findViewById(R.id.textViewTimeInPhoto);
            this.imageViewPhoto = view.findViewById(R.id.imageViewPhoto);
            view.setOnClickListener(this);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            Item item = itemList.get(super.getAdapterPosition());
            this.textViewInPhoto.setText(item.getCreateTimeFormatted());
            this.textViewNameInPhoto.setText(item.getUserName());

            File path = context.getFilesDir();
            File imgFile = new File(path, item.getPhotoFileName());

            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                this.imageViewPhoto.setImageBitmap(myBitmap);
                //Toast.makeText(context, "파일 있음", Toast.LENGTH_SHORT).show();
            }
            else {
                //Toast.makeText(context, "파일 없음", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onClick(View view) {
           /* Item item = itemList.get(super.getAdapterPosition());

            RoomActivity activity = (RoomActivity)view.getContext();
            //activity.showPhotoDialog(super.getAdapterPosition());

            Intent intent = new Intent(activity, PhotoPreview.class);
            intent.putExtra("photoFileName", item.getPhotoFileName());
            intent.putExtra("selectIndex", super.getAdapterPosition());
            activity.startActivity(intent);*/

            onClickRoomPhotoListener.onClickRoomPhotoListener(super.getAdapterPosition());
        }
    }

    class ViewHolderVideoRight extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textViewNameInVideo, textViewTimeInVideo;
        //CheckBox checkBox;
        VideoView videoView;

        String downloadFileName;
        int selectVideoIndex;
        File path;

        public ViewHolderVideoRight(View view) {
            super(view);
            this.textViewNameInVideo = view.findViewById(R.id.textViewNameInVideo);
            this.textViewTimeInVideo = view.findViewById(R.id.textViewTimeInVideo);
            this.videoView = view.findViewById(R.id.videoView);
            view.setOnClickListener(this);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            Item item = itemList.get(super.getAdapterPosition());
            this.textViewNameInVideo.setText(item.getUserName());
            this.textViewTimeInVideo.setText(item.getCreateTimeFormatted());

            File path = context.getFilesDir();
            File videoFile = new File(path, item.getVideoFileName());

            if(videoFile.exists()){
                videoView.setVideoPath(videoFile.toString());
                //Toast.makeText(context, "비디오 파일 있음", Toast.LENGTH_SHORT).show();
                Log.v("pjw", "비디오 파일 있음");
            }
            else {
                //Toast.makeText(context, "비디오 파일 없음", Toast.LENGTH_SHORT).show();
                Log.v("pjw", "비디오 파일 없음");
            }

            this.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.seekTo(0);
                }
            });

            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    return true;
                }
            });
        }

        @Override
        public void onClick(View view) {
            /*final Item item = itemList.get(super.getAdapterPosition());
            int index = super.getAdapterPosition();

            final RoomActivity activity = (RoomActivity)view.getContext();
            //activity.showPhotoDialog(super.getAdapterPosition());

            downloadFileName = itemList.get(index).getVideoFileName();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("videos/" + downloadFileName);

            selectVideoIndex = index;


            try{

                //로컬에 저장할 폴더의 위치
                path = context.getFilesDir();

                //저장하는 파일의 이름
                final File file = new File(path, downloadFileName);
                try {
                    if (!path.exists()) {
                        //저장할 폴더가 없으면 생성
                        path.mkdirs();
                    }

                    if (!file.exists()) {
                        final ProgressDialog progressDialog = new ProgressDialog(activity);
                        progressDialog.setTitle("다운로드중...");
                        progressDialog.show();

                        file.createNewFile();
                        //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                        final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                        fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            int VideoIndex = selectVideoIndex;
                            String videoFileName = item.getVideoFileName();
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                                //다운로드 성공 후 할 일
                                Toast.makeText(context, file.getPath() + "다운로드 성공", Toast.LENGTH_LONG).show();

                                notifyItemChanged(VideoIndex);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //다운로드 실패 후 할 일
                                Toast.makeText(context, file.getPath() + "다운로드 실패", Toast.LENGTH_LONG).show();
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
                        Intent intent = new Intent(activity, VideoPreview.class);
                        intent.putExtra("videoFileName", item.getVideoFileName());
                        intent.putExtra("selectIndex", selectVideoIndex);
                        activity.startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch(Exception e){
                e.printStackTrace();
            }*/

            onClickRoomVideoListener.onClickRoomVideoListener(super.getAdapterPosition());
        }
    }

    class ViewHolderMusicRight extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textViewNameInMusic, textViewTimeInMusic;
        //CheckBox checkBox;

        String downloadFileName;
        int selectVideoIndex;
        File path;

        public ViewHolderMusicRight(View view) {
            super(view);
            this.textViewNameInMusic = view.findViewById(R.id.textViewNameInMusic);
            this.textViewTimeInMusic = view.findViewById(R.id.textViewTimeInMusic);

            view.setOnClickListener(this);
        }

        //RecyclerView에 보이는 내용을 설정하는 함수입니다.
        public void setData() {
            Item item = itemList.get(super.getAdapterPosition());
            this.textViewNameInMusic.setText(item.getUserName());
            this.textViewTimeInMusic.setText(item.getCreateTimeFormatted());

            File path = context.getFilesDir();
            File musicFile = new File(path, item.getMusicFileName());
        }

        @Override
        public void onClick(View view) {
            /*final Item item = itemList.get(super.getAdapterPosition());
            int index = super.getAdapterPosition();

            final RoomActivity activity = (RoomActivity)view.getContext();
            //activity.showPhotoDialog(super.getAdapterPosition());

            downloadFileName = itemList.get(index).getMusicFileName();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://firechat-51553.appspot.com").child("audio/" + downloadFileName);

            selectVideoIndex = index;


            try{

                //로컬에 저장할 폴더의 위치
                path = context.getFilesDir();

                //저장하는 파일의 이름
                final File file = new File(path, downloadFileName);
                try {
                    if (!path.exists()) {
                        //저장할 폴더가 없으면 생성
                        path.mkdirs();
                    }

                    if (!file.exists()) {
                        final ProgressDialog progressDialog = new ProgressDialog(activity);
                        progressDialog.setTitle("다운로드중...");
                        progressDialog.show();

                        file.createNewFile();
                        //파일을 다운로드하는 Task 생성, 비동기식으로 진행
                        final FileDownloadTask fileDownloadTask = storageRef.getFile(file);
                        fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            int MusicIndex = selectVideoIndex;
                            String musicFileName = item.getMusicFileName();
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                                //다운로드 성공 후 할 일
                                Toast.makeText(context, file.getPath() + "다운로드 성공", Toast.LENGTH_LONG).show();

                                notifyItemChanged(MusicIndex);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //다운로드 실패 후 할 일
                                Toast.makeText(context, file.getPath() + "다운로드 실패", Toast.LENGTH_LONG).show();
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
                        activity.selectedIndex = super.getAdapterPosition();
                        activity.showMusicPreviewDialog();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch(Exception e){
                e.printStackTrace();
            }*/

            onClickRoomMusicListener.onClickRoomMusicListener(super.getAdapterPosition());
        }
    }

    LayoutInflater layoutInflater;
    Context context;
    String userEmail;
    OnClickRoomMessageListener onClickRoomMessageListener;
    OnClickRoomPhotoListener onClickRoomPhotoListener;
    OnClickRoomVideoListener onClickRoomVideoListener;
    OnClickRoomMusicListener onClickRoomMusicListener;

    static final int MESSAGE=0;
    static final int PHOTO=1;
    static final int VIDEO=2;
    static final int MUSIC=3;
    static final int MESSAGE_LEFT=4;
    static final int PHOTO_LEFT=5;
    static final int VIDEO_LEFT=6;
    static final int MUSIC_LEFT=7;

    ItemList itemList;

    public RoomChatRecyclerViewAdapter(Context context, String userEmail, OnClickRoomMessageListener onClickRoomMessageListener,
                                       OnClickRoomPhotoListener onClickRoomPhotoListener, OnClickRoomVideoListener onClickRoomVideoListener,
                                       OnClickRoomMusicListener onClickRoomMusicListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.userEmail=userEmail;
        this.onClickRoomMessageListener = onClickRoomMessageListener;
        this.onClickRoomPhotoListener = onClickRoomPhotoListener;
        this.onClickRoomVideoListener = onClickRoomVideoListener;
        this.onClickRoomMusicListener = onClickRoomMusicListener;

        this.itemList = new ItemList();
    }

    // index 위치의 Item 객체를 리턴
    public Item get(int index) {
        return itemList.get(index);
    }

    // index 위치의 키 값을 리턴
    public String getKey(int index) {
        return itemList.getKey(index);
    }

    // Item 객체의 수를 리턴
    public int size() {
        return itemList.size();
    }

    // key 값의 index를 리턴
    public int findIndex(String key) {
        return itemList.findIndex(key);
    }

    // key 값에 해당하는 Item 객체를 목록에서 제거
    public int remove(String key) {
        return itemList.remove(key);
    }

    // key 값과 Item 객체를 목록에 추가
    public int add(String key, Item item) {
       return itemList.add(key, item);
    }

    // key 값에 해당하는 Item 객체 변경
    public int update(String key, Item item) {
        return itemList.update(key, item);
    }

    public Iterator<String> getIteratorKeys(){
        return itemList.getIteratorKeys();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType==MESSAGE) {
            View view = layoutInflater.inflate(R.layout.item, viewGroup, false);
            return new ViewHolder(view);
        }
        else if (viewType == PHOTO){
            View view = layoutInflater.inflate(R.layout.item_photo, viewGroup, false);
            return new ViewHolderPhoto(view);
        }
        else if (viewType == VIDEO){
            View view = layoutInflater.inflate(R.layout.item_video, viewGroup, false);
            return new ViewHolderVideo(view);
        }
        else if (viewType == MUSIC){
            View view = layoutInflater.inflate(R.layout.item_music, viewGroup, false);
            return new ViewHolderMusic(view);
        }
        else if (viewType==MESSAGE_LEFT) {
            View view = layoutInflater.inflate(R.layout.item_right, viewGroup, false);
            return new ViewHolderRight(view);
        }
        else if (viewType == PHOTO_LEFT){
            View view = layoutInflater.inflate(R.layout.item_photo_right, viewGroup, false);
            return new ViewHolderPhotoRight(view);
        }
        else if (viewType == VIDEO_LEFT){
            View view = layoutInflater.inflate(R.layout.item_video_right, viewGroup, false);
            return new ViewHolderVideoRight(view);
        }
        else if (viewType == MUSIC_LEFT){
            View view = layoutInflater.inflate(R.layout.item_music_right, viewGroup, false);
            return new ViewHolderMusicRight(view);
        }

        View view = layoutInflater.inflate(R.layout.item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ViewHolder) {
            ((ViewHolder)viewHolder).setData();
        }
        else if (viewHolder instanceof ViewHolderPhoto) {
            ((ViewHolderPhoto)viewHolder).setData();
        }
        else if (viewHolder instanceof ViewHolderVideo) {
            ((ViewHolderVideo)viewHolder).setData();
        }
        else if (viewHolder instanceof ViewHolderMusic) {
            ((ViewHolderMusic)viewHolder).setData();
        }
        else if (viewHolder instanceof ViewHolderRight) {
            ((ViewHolderRight)viewHolder).setData();
        }
        else if (viewHolder instanceof ViewHolderPhotoRight) {
            ((ViewHolderPhotoRight)viewHolder).setData();
        }
        else if (viewHolder instanceof ViewHolderVideoRight) {
            ((ViewHolderVideoRight)viewHolder).setData();
        }
        else if (viewHolder instanceof ViewHolderMusicRight) {
            ((ViewHolderMusicRight)viewHolder).setData();
        }
        else{
            ((ViewHolder)viewHolder).setData();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position).getHavePhoto()){
            if(itemList.get(position).getUserEmail().equals(userEmail)){
                return PHOTO_LEFT;
            }
            return PHOTO;
        }
        else if(itemList.get(position).getHaveVideo()){
            if(itemList.get(position).getUserEmail().equals(userEmail)){
                return VIDEO_LEFT;
            }
            return VIDEO;
        }
        else if(itemList.get(position).getHaveMusic()){
            if(itemList.get(position).getUserEmail().equals(userEmail)){
                return MUSIC_LEFT;
            }
            return MUSIC;
        }
        else {
            if(itemList.get(position).getUserEmail().equals(userEmail)){
                return MESSAGE_LEFT;
            }
            return MESSAGE;
        }
    }
}

