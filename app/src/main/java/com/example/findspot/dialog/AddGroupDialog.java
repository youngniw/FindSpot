package com.example.findspot.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
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
    private final String userNickName;
    private final AddGroupDialogListener addGroupDialogListener;
    private final ArrayList<String> friendList;
    private final ArrayList<GroupInfo> groupList;

    private EditText etGroupName;
    private TextView tvNameAlertMsg, tvUsersAlertMsg;
    private ListView lvFriendList;

    public AddGroupDialog(@NonNull Context context, AddGroupDialogListener addGroupDialogListener, String userNickName, ArrayList<String> friendList, ArrayList<GroupInfo> groupList) {
        super(context);
        this.context = context;
        this.addGroupDialogListener = addGroupDialogListener;
        this.userNickName = userNickName;
        this.friendList = friendList;
        this.groupList = groupList;
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

        //그룹 이름 값이 변경될 때,
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

        //친구 목록 클릭 시,
        lvFriendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {     //친구 클릭 시!
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tvUsersAlertMsg.setVisibility(View.INVISIBLE);
            }
        });

        //"완료"버튼 눌렀을 때,
        btComplete.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();     //TODO: 8명 초과하면 안된다고 표시 (빨간색으로 흔드는 효과..?)
            ArrayList<String> groupUsers = new ArrayList<>();
            groupUsers.add(userNickName);       //방장명이 제일 먼저 포함됨

            SparseBooleanArray checkedItemPositions = lvFriendList.getCheckedItemPositions();   //리스트뷰에서 선택된 아이템 목록
            for (int i=0; i<checkedItemPositions.size(); i++ ) {
                int pos = checkedItemPositions.keyAt(i);

                if (checkedItemPositions.valueAt(i)) {
                    groupUsers.add(lvFriendList.getItemAtPosition(pos).toString());
                }
            }

            if (groupName.length()==0) {                //그룹 이름이 없을 시
                tvNameAlertMsg.setVisibility(View.VISIBLE);
                tvNameAlertMsg.setText("* 그룹 이름을 입력해주세요.");
            } else if (groupUsers.size() == 1) {        //포함된 사용자를 0명 선택 시
                tvUsersAlertMsg.setVisibility(View.VISIBLE);
            } else {
                boolean canAdd = true;
                String sameGroupName = "";
                for (GroupInfo gi : groupList) {
                    if (gi.getGroupUsers().containsAll(groupUsers) && groupUsers.containsAll(gi.getGroupUsers())) {
                        canAdd = false;
                        sameGroupName = gi.getGroupName();

                        break;
                    }
                }

                if (!canAdd) {     //선택한 사용자들이 동일하게 포함된 그룹이 있는 경우
                    tvUsersAlertMsg.setVisibility(View.VISIBLE);
                    tvUsersAlertMsg.setText("* 지정하신 사용자들이 속한 그룹이 이미 '"+sameGroupName+"'이름으로 존재합니다.");
                }
                else {
                    final GroupInfo tmpGroup = new GroupInfo(groupName, userNickName, groupUsers);
                    Response.Listener<String> responseListener = response -> {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean canAddGroup = jsonObject.getBoolean("canAdd");
                            if (canAddGroup) {
                                addGroupDialogListener.clickCompleteBt(tmpGroup);
                                dismiss();
                            }
                            else {
                                tvNameAlertMsg.setVisibility(View.VISIBLE);
                                tvNameAlertMsg.setText("* 해당 이름을 가진 그룹이 이미 존재합니다.");
                            }
                        } catch (JSONException e) { e.printStackTrace(); }
                    };
                    AddGroupRequest addGroupRequest = new AddGroupRequest(tmpGroup, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(getContext());
                    queue.add(addGroupRequest);
                }
            }
        });

        btCancel.setOnClickListener(v -> dismiss());
    }

    public interface AddGroupDialogListener {
        void clickCompleteBt(GroupInfo group);
    }
}
