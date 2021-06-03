package com.example.findspot.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.data.GroupInfo;
import com.example.findspot.R;
import com.example.findspot.request.AddGroupRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddGroupDialog extends Dialog {
    private final Context context;
    private final AddGroupDialogListener addGroupDialogListener;
    private final String userNickName;
    private final ArrayList<String> friendList;

    private EditText etGroupName;
    private TextView tvNameAlertMsg, tvUsersAlertMsg;
    private ListView lvFriendList;

    public AddGroupDialog(@NonNull Context context, AddGroupDialogListener addGroupDialogListener, String userNickName, ArrayList<String> friendList) {
        super(context);
        this.context = context;
        this.addGroupDialogListener = addGroupDialogListener;
        this.userNickName = userNickName;
        this.friendList = friendList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_group);

        etGroupName = findViewById(R.id.gDialog_etGroupName);
        tvNameAlertMsg = findViewById(R.id.gDialog_tvNameAlertMsg);
        tvNameAlertMsg.setVisibility(View.INVISIBLE);
        lvFriendList = findViewById(R.id.gDialog_lvGroupUsers);
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_multiple_choice, friendList);
        lvFriendList.setAdapter(adapter);
        tvUsersAlertMsg = findViewById(R.id.gDialog_tvUsersAlertMsg);
        tvUsersAlertMsg.setVisibility(View.INVISIBLE);
        Button btComplete = findViewById(R.id.gDialog_complete);
        Button btCancel = findViewById(R.id.gDialog_cancel);

        //값이 변경될 때,
        etGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1)     //경고 내용 지움
                    tvNameAlertMsg.setVisibility(View.INVISIBLE);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });



        //"완료"버튼 눌렀을 때,
        btComplete.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString();
            ArrayList<String> groupUsers = new ArrayList<>();
            groupUsers.add(userNickName);

            SparseBooleanArray checkedItemPositions = lvFriendList.getCheckedItemPositions();   //리스트뷰에서 선택된 아이템 목록
            for (int i=0; i<checkedItemPositions.size(); i++ ) {
                int pos = checkedItemPositions.keyAt(i);

                if (checkedItemPositions.valueAt(i)) {
                    groupUsers.add(lvFriendList.getItemAtPosition(pos).toString());
                }
            }

            if (groupName.length()==0) {
                tvNameAlertMsg.setVisibility(View.VISIBLE);
                tvNameAlertMsg.setText("* 그룹 이름을 입력해주세요.");
            } else if (groupUsers.size() == 1) {      //리스트뷰 0명 선택 시
                tvUsersAlertMsg.setVisibility(View.VISIBLE);
            }
            else {
                final GroupInfo tmpGroup = new GroupInfo(groupName, userNickName, groupUsers);
                Response.Listener<String> responseListener = response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean canAddGroup = jsonObject.getBoolean("canAdd");
                        if (canAddGroup) {
                            addGroupDialogListener.clickCompleteBt(tmpGroup);
                            dismiss();      //TODO: 안됨!!
                        } else {
                            tvNameAlertMsg.setVisibility(View.VISIBLE);
                            tvNameAlertMsg.setText("* 해당 그룹이 이미 존재합니다.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                };
                AddGroupRequest addGroupRequest = new AddGroupRequest(tmpGroup, responseListener);
                RequestQueue queue = Volley.newRequestQueue(getContext());
                queue.add(addGroupRequest);
            }
        });

        btCancel.setOnClickListener(v -> dismiss());
    }

    public interface AddGroupDialogListener {
        void clickCompleteBt(GroupInfo group);
    }
}
