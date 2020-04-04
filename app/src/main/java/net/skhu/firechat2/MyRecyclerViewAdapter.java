package net.skhu.firechat2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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
            MainActivity activity = (MainActivity)view.getContext();
            activity.showItemEditDialog(super.getAdapterPosition());
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
            Item item = itemList.get(super.getAdapterPosition());

            MainActivity activity = (MainActivity)view.getContext();
            //activity.showPhotoDialog(super.getAdapterPosition());

            Intent intent = new Intent(activity, PhotoPreview.class);
            intent.putExtra("photoFileName", item.getPhotoFileName());
            intent.putExtra("selectIndex", super.getAdapterPosition());
            activity.startActivity(intent);
        }
    }

    class ViewHolderVideo extends RecyclerView.ViewHolder  implements View.OnClickListener {
        TextView textViewNameInVideo, textViewTimeInVideo;
        //CheckBox checkBox;
        VideoView videoView;


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
                Toast.makeText(context, "비디오 파일 있음", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "비디오 파일 없음", Toast.LENGTH_SHORT).show();
            }

            this.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.seekTo(0);
                }
            });
        }

        @Override
        public void onClick(View view) {
            Item item = itemList.get(super.getAdapterPosition());

            MainActivity activity = (MainActivity)view.getContext();
            //activity.showPhotoDialog(super.getAdapterPosition());

            Intent intent = new Intent(activity, VideoPreview.class);
            intent.putExtra("videoFileName", item.getVideoFileName());
            intent.putExtra("selectIndex", super.getAdapterPosition());
            activity.startActivity(intent);
        }
    }

    LayoutInflater layoutInflater;
    ItemList itemList;
    Context context;

    final int MESSAGE=0;
    final int PHOTO=1;
    final int VIDEO=2;

    public MyRecyclerViewAdapter(Context context, ItemList itemList) {
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.itemList = itemList;
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
        else{
            ((ViewHolder)viewHolder).setData();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position).getHavePhoto()){
            return PHOTO;
        }
        else if(itemList.get(position).getHaveVideo()){
            return VIDEO;
        }
        else {
            return MESSAGE;
        }
    }
}

