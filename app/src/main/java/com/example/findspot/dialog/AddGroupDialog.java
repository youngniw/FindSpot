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

        //?????? ?????? ?????? ????????? ???,
        etGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1)     //?????? ?????? ??????
                    tvNameAlertMsg.setVisibility(View.INVISIBLE);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        //?????? ?????? ?????? ???,
        lvFriendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {     //?????? ?????? ???!
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tvUsersAlertMsg.setVisibility(View.INVISIBLE);
            }
        });

        //"??????"?????? ????????? ???,
        btComplete.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();     //TODO: 8??? ???????????? ???????????? ?????? (??????????????? ????????? ??????..?)
            ArrayList<String> groupUsers = new ArrayList<>();
            groupUsers.add(userNickName);       //???????????? ?????? ?????? ?????????

            SparseBooleanArray checkedItemPositions = lvFriendList.getCheckedItemPositions();   //?????????????????? ????????? ????????? ??????
            for (int i=0; i<checkedItemPositions.size(); i++ ) {
                int pos = checkedItemPositions.keyAt(i);

                if (checkedItemPositions.valueAt(i)) {
                    groupUsers.add(lvFriendList.getItemAtPosition(pos).toString());
                }
            }

            if (groupName.length()==0) {                //?????? ????????? ?????? ???
                tvNameAlertMsg.setVisibility(View.VISIBLE);
                tvNameAlertMsg.setText("* ?????? ????????? ??????????????????.");
            } else if (groupUsers.size() == 1) {        //????????? ???????????? 0??? ?????? ???
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

                if (!canAdd) {     //????????? ??????????????? ???????????? ????????? ????????? ?????? ??????
                    tvUsersAlertMsg.setVisibility(View.VISIBLE);
                    tvUsersAlertMsg.setText("* ???????????? ??????????????? ?????? ????????? ?????? '"+sameGroupName+"'???????????? ???????????????.");
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
                                tvNameAlertMsg.setText("* ?????? ????????? ?????? ????????? ?????? ???????????????.");
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
