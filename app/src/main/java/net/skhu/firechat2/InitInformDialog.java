package net.skhu.firechat2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

public class InitInformDialog extends DialogFragment {
    @Override
    // 수정 대화상자를 만드는 메소드.  이 메소드는 대화상자를 새로 만들어야 할 때에만 호출된다.
    // 한 번 만들어진 대화상자는 계속 재사용 된다.
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MainActivity activity = (MainActivity)getActivity();  // 액티비티 객체에 대한 참조 얻기
        //final Item item = activity.itemList.get(activity.selectedIndex); // 선택된 항목 얻기
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("초기화 정보"); // 대화 상자의 제목 설정하기

        // 대화 상자에 표시될 뷰 객체들을 자동으로 생성함.
        final View rootView = activity.getLayoutInflater().inflate(R.layout.init_inform, null);

        // 자동으로 생성된 뷰 객체들을 대화상자에 추가한다
        builder.setView(rootView);

        // 대화상자에 '확인' 버튼 추가하는 코드의 시작
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            // 대화상자의 '확인' 버튼이 클릭되면 실행되는 메소드
            public void onClick(DialogInterface dialog, int which) {

            } // onClick 메소드의 끝
        });
        // 대화상자에 '확인' 버튼을 추가하는 코드의 끝

        builder.setNegativeButton("취소", null); // 대화상자에 '취소' 버튼을 추가하기
        AlertDialog dialog = builder.create(); // 대화상자 객체 생성하기
        return dialog; // 생성된 대화상자 객체 리턴
    }
}
