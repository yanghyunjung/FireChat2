package net.skhu.firechat2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class RoomCreateDialog  extends DialogFragment {

    //스크롤 설정 다이얼로그
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity)getActivity();  // 액티비티 객체에 대한 참조 얻기
        //final Item item = activity.itemList.get(activity.selectedIndex); // 선택된 항목 얻기
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("방 이름"); // 대화 상자의 제목 설정하기

        // 대화 상자에 표시될 뷰 객체들을 자동으로 생성함.
        final View rootView = activity.getLayoutInflater().inflate(R.layout.room_create, null);


        // 자동으로 생성된 뷰 객체들을 대화상자에 추가한다
        builder.setView(rootView);

        final EditText editTextRoomTitle = rootView.findViewById(R.id.editTextRoomTitle);

        final RoomItem roomItem = new RoomItem("");

        // 대화상자에 '확인' 버튼 추가하는 코드의 시작
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            // 대화상자의 '확인' 버튼이 클릭되면 실행되는 메소드
            public void onClick(DialogInterface dialog, int which) {
                CharSequence s1 = editTextRoomTitle.getText();

                if (!s1.toString().isEmpty()) {
                    roomItem.setRoomName(s1.toString());
                    activity.firebaseDbServiceForRoom.addIntoServer(roomItem);
                }
            } // onClick 메소드의 끝
        });


        builder.setNegativeButton("취소", null); // 대화상자에 '취소' 버튼을 추가하기
        AlertDialog dialog = builder.create(); // 대화상자 객체 생성하기
        return dialog; // 생성된 대화상자 객체 리턴
    }
}
