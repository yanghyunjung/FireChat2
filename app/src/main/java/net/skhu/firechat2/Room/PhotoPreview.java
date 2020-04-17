package net.skhu.firechat2.Room;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import net.skhu.firechat2.R;

import java.io.File;

public class PhotoPreview extends AppCompatActivity {
    String photoFileName;
    ImageView imageViewPhoto;
    int selectIndex;
    //ItemEditDialogFragment itemEditDialogFragment; // 수정 대화상자 관리자
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);

        imageViewPhoto = findViewById(R.id.imageViewTimePhotoPre);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            photoFileName = extras.getString("photoFileName");
            selectIndex = extras.getInt("selectIndex");
        }

        File path = getFilesDir();
        File imgFile = new File(path, photoFileName);

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            this.imageViewPhoto.setImageBitmap(myBitmap);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo_preview, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            String key = ((RoomActivity) RoomActivity.mContext).itemList.getKey(selectIndex);
            ((RoomActivity) RoomActivity.mContext).firebaseDbService.removeFromServer(key);
            finish();
            return true;
        }
        else if (id == R.id.action_exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
