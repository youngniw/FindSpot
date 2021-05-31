package com.example.findspot.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.R;
import com.example.findspot.request.AddFriendRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddFriendDialog extends Dialog {
    private Context context;
    private AddFriendDialogListener addFriendDialogListener;
    private String userNickName, friendNick;
    private ArrayList<String> friendList;

    private EditText etGetByUser;
    private Button btComplete, btCancel;
    private TextView tvAlertMsg;

    public AddFriendDialog(@NonNull Context context, AddFriendDialogListener addFriendDialogListener, String userNickName, ArrayList<String> friendList) {
        super(context);
        this.context = context;
        this.addFriendDialogListener = addFriendDialogListener;
        this.userNickName = userNickName;
        this.friendList = friendList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_friend);

        etGetByUser = findViewById(R.id.fDialog_etByUser);
        tvAlertMsg = findViewById(R.id.fDialog_tvAlertMsg);
        btComplete = findViewById(R.id.fDialog_complete);
        btCancel = findViewById(R.id.fDialog_cancel);

        //값이 변경될 때,
        etGetByUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1)     //경고 내용 지움
                    tvAlertMsg.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0));
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        //"완료"버튼 눌렀을 때,
        btComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendNick = etGetByUser.getText().toString().replaceAll("\\p{Z}","");

                boolean isAlreadyFriend = false;
                for (int i=0; i<friendList.size(); i++) {
                    if (friendList.get(i).equals(friendNick)) {
                        isAlreadyFriend = true;
                        break;
                    }
                }

                if (isAlreadyFriend) {      //사용자가 이미 친구인 경우
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.BELOW, R.id.fDialog_etByUser);
                    params.setMargins(40, 8, 40, 0);
                    tvAlertMsg.setLayoutParams(params);
                    tvAlertMsg.setText("* 이미 해당 닉네임의 사용자가 친구목록에 포함돼있습니다.");
                } else if (friendNick.length() == 0) {      //TODO: 확인요망!!!
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.BELOW, R.id.fDialog_etByUser);
                    params.setMargins(40, 8, 40, 0);
                    tvAlertMsg.setLayoutParams(params);
                    tvAlertMsg.setText("* 친구 닉네임을 입력해주세요.");
                } else {
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                boolean canAddFriend = jsonObject.getBoolean("canAdd");
                                if (canAddFriend) {
                                    addFriendDialogListener.clickCompleteBt(friendNick);
                                    dismiss();      //TODO: 안됨!!
                                } else {
                                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                    params.addRule(RelativeLayout.BELOW, R.id.fDialog_etByUser);
                                    params.setMargins(40, 8, 40, 0);
                                    tvAlertMsg.setLayoutParams(params);
                                    tvAlertMsg.setText("* 해당 닉네임의 유저는 존재하지 않습니다.");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    AddFriendRequest addFriendRequest = new AddFriendRequest(userNickName, friendNick, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(getContext());
                    queue.add(addFriendRequest);
                }
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public interface AddFriendDialogListener {
        void clickCompleteBt(String friendNick);
    }
}
