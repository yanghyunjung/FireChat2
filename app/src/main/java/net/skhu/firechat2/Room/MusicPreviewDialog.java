package net.skhu.firechat2.Room;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import net.skhu.firechat2.BuildConfig;
import net.skhu.firechat2.Item.Item;
import net.skhu.firechat2.R;

import java.io.File;

public class MusicPreviewDialog extends DialogFragment {

    @Override
    // 수정 대화상자를 만드는 메소드.  이 메소드는 대화상자를 새로 만들어야 할 때에만 호출된다.
    // 한 번 만들어진 대화상자는 계속 재사용 된다.
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final RoomActivity activity = (RoomActivity)getActivity();  // 액티비티 객체에 대한 참조 얻기
        final Item item = activity.itemList.get(activity.selectedIndex); // 선택된 항목 얻기
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("음악 재생"); // 대화 상자의 제목 설정하기

        // 대화 상자에 표시될 뷰 객체들을 자동으로 생성함.
        final View rootView = activity.getLayoutInflater().inflate(R.layout.item_music_preview, null);
        final ImageView imageViewMusicStart = (ImageView)rootView.findViewById(R.id.imageViewMusicStart);

        imageViewMusicStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                File path = activity.getFilesDir();
                File musicFile = new File(path, item.getMusicFileName());

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID, musicFile);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.setDataAndType(uri, "audio/*");
                startActivity(intent);
            }
        });

        // 자동으로 생성된 뷰 객체들을 대화상자에 추가한다
        builder.setView(rootView);

        builder.setNeutralButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            // 대화상자의 '삭제' 버튼이 클릭되면 실행되는 메소드
            public void onClick(DialogInterface dialog, int which) {
                String key = activity.itemList.getKey(activity.selectedIndex);
                activity.firebaseDbService.removeFromServer(key);
            } // onClick 메소드의 끝
        });

        // 대화상자에 '확인' 버튼 추가하는 코드의 시작
        builder.setPositiveButton("확인", null);
        // 대화상자에 '확인' 버튼을 추가하는 코드의 끝

        builder.setNegativeButton("취소", null); // 대화상자에 '취소' 버튼을 추가하기
        AlertDialog dialog = builder.create(); // 대화상자 객체 생성하기
        return dialog; // 생성된 대화상자 객체 리턴
    }
}