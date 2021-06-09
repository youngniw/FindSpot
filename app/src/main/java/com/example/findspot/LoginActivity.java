package com.example.findspot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.findspot.data.GroupInfo;
import com.example.findspot.dialog.NickNameCheckDialog;
import com.example.findspot.request.JoinRequest;
import com.example.findspot.request.LoginRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static com.example.findspot.HomeActivity.friendList;
import static com.example.findspot.HomeActivity.groupList;

public class LoginActivity extends AppCompatActivity {
    static String nickName;
    @SuppressLint("StaticFieldLeak")
    static GoogleSignInClient mGoogleSignInClient;
    EditText et_loginID, et_loginPW;
    Button btn_login, btn_join;
    LinearLayout llBtn_login_google;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        friendList = new ArrayList<>();    //사용자의 친구목록 리스트 생성
        groupList = new ArrayList<>();     //사용자의 그룹목록 리스트 생성

        et_loginID = (EditText) findViewById(R.id.login_ID_input);      //id를 입력하는 editText
        et_loginPW = (EditText) findViewById(R.id.login_PW_input);      //비밀번호를 입력하는 editText

        btn_login = (Button) findViewById(R.id.login_login_btn);        //로그인 버튼
        btn_join = (Button) findViewById(R.id.login_join_btn);          //회원가입 버튼

        llBtn_login_google = (LinearLayout) findViewById(R.id.login_google);        //구글로 로그인하는 이미지버튼

        login_btn_clickListener();        //로그인 화면의 버튼에 해당하는 onClickListener를 정의한 함수를 호출함
        login_socialbtn_clickListener();  //로그인 화면의 소셜로그인 버튼(4개)에 해당하는 onClickListener를 정의한 함수를 호출함
    }

    void login_btn_clickListener() {
        //로그인버튼을 클릭했을 때
        btn_login.setOnClickListener(v -> {
            InputMethodManager inputMM = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);      //키보드 안보이게 하기 위한 InputMethodManager객체
            inputMM.hideSoftInputFromWindow(et_loginID.getWindowToken(), 0);
            inputMM.hideSoftInputFromWindow(et_loginPW.getWindowToken(), 0);

            //로그인을 위해 ID와 PW를 모두 입력했는지를 확인함
            if (et_loginID.getText().toString().equals("")) {    //비밀번호 입력했는지를 점검
                Toast.makeText(getApplicationContext(), "ID를 입력해주세요:)", Toast.LENGTH_SHORT).show();
                return;
            }
            if (et_loginPW.getText().toString().equals("")) {    //비밀번호 입력했는지를 점검
                Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요:)", Toast.LENGTH_SHORT).show();
                return;
            }

            //로그인을 위해 입력한 id와 비밀번호가 유효한 지를 체크함
            try {
                new LoginCheckTask().execute().get();
            } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
        });

        //회원가입버튼을 클릭했을 때
        btn_join.setOnClickListener(v -> {
            Intent it_join = new Intent(LoginActivity.this, JoinActivity.class);        //회원가입 창으로 화면이 전환됨
            startActivity(it_join);     //이때 finish하지 않아서 로그인 창으로 회원가입때 뒤로가기로 돌아올 수 있음
        });
    }

    void login_socialbtn_clickListener() {      //구글 소셜로그인 버튼을 클릭했을 때에 해당하는 onClickListener를 정의함
        GoogleSignInOptions gsio = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gsio);       //GoogleSignInClient 객체를 만듬

        llBtn_login_google.setOnClickListener(v -> {
            //기존에 로그인 했던 계정을 확인
            GoogleSignInAccount gsia = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
            if (gsia != null) {     //로그인이 이미 한 적이 있는 경우
                Log.d("Google Login", "로그인한 계정이 있어요!");
                String userId = gsia.getId();
                String userEmail = gsia.getEmail();

                Log.d("Google Login", "handleSignInResult:userId "+userId);
                Log.d("Google Login", "handleSignInResult:userEmail "+userEmail);
                
                //DB로부터 사용자 정보를 받아옴
                try {
                    new LoginCheckTask(userId, userEmail).execute().get();
                } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
            }
            else {      //회원가입과 같은 기능
                Log.d("Google Login", "로그인한 계정이 없어요!");

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 900);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 900) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                String userId = account.getId();
                String userEmail = account.getEmail();

                Log.d("Google Login", "handleSignInResult:userId "+userId);
                Log.d("Google Login", "handleSignInResult:userEmail "+userEmail);
                
                //이전에 로그인한 사람이었는 지 확인(앱 삭제를 한 이후에 로그인 시 닉네임 받는 것을 방지하기 위함)
                try {
                    new LoginCheckTask(userId, userEmail).execute().get();
                } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
            }
        } catch (ApiException e) {
            Log.e("Google Login", "signInResult:failed code=" + e.getStatusCode());
        }
    }


    //자체 시스템 로그인을 위한 작업 수행함
    @SuppressLint("StaticFieldLeak")
    public class LoginCheckTask extends AsyncTask<String, Void, String> {
        int isSocialLogin = 0;      //0일 시에는 소셜로그인이 아니라는 것, 1일 시에는 소셜로그인을 한다는 것
        String userId = "";
        String userEmail = "";

        LoginCheckTask() { super(); }

        LoginCheckTask(String userId, String userEmail) {
            super();
            this.userId = userId;
            this.userEmail = userEmail;
            this.isSocialLogin = 1;
        }

        @Override
        protected String doInBackground(String... strings) {
            String userID;
            String userPW;

            if (isSocialLogin == 1) {
                userID = userId;
                userPW = userEmail;
            }
            else {
                userID = et_loginID.getText().toString();     //EditText에 현재 입력된 id값을 가져옴
                userPW = et_loginPW.getText().toString();     //EditText에 현재 입력된 비밀번호값을 가져옴
            }

            //데이터베이스에 사용자의 ID와 PW를 입력한 ID와 PW를 비교했을 때 존재한다면 입력한 ID와 PW로 로그인함
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean canLogin = jsonObject.getBoolean("canLogin");   //입력한 ID와 PW로 로그인 가능한 지 여부
                    boolean existID = jsonObject.getBoolean("existID");     //입력한 ID값이 DB에 있는지 여부(로그인 안될 시 원인 파악)
                    nickName = jsonObject.getString("nickName");     //해당 ID의 닉네임을 받음

                    if (canLogin) {     //로그인 가능
                        friendList.clear();     //로그인 시 계속해서 add될 수 있으므로 친구목록 초기화
                        JSONArray jsonArrayFriends = jsonObject.getJSONArray("friends");    //친구목록을 받아옴
                        for (int i=0; i < jsonArrayFriends.length(); i++) {
                            JSONObject subJsonObject = jsonArrayFriends.getJSONObject(i);
                            String fName = subJsonObject.getString("fName");

                            friendList.add(fName);      //사용자의 친구목록인 friendlist에 추가함
                        }

                        groupList.clear();      //로그인 시 계속해서 add될 수 있으므로 그룹목록 초기화
                        JSONArray jsonArrayGroups = jsonObject.getJSONArray("groups");      //사용자가 속한 그룹목록을 받아옴
                        for (int i=0; i < jsonArrayGroups.length(); i++) {
                            JSONObject subJsonObject = jsonArrayGroups.getJSONObject(i);
                            String gName = subJsonObject.getString("gName");                //그룹 이름 받아옴
                            String gHostName = subJsonObject.getString("gHostName");        //그룹 호스트사용자 이름 받아옴(방장)
                            GroupInfo group = new GroupInfo(gName, gHostName);

                            JSONArray jsonArraygUsers = subJsonObject.getJSONArray("gUsers");  //그룹에 속한 사용자들의 목록을 받아옴
                            for (int j=0; j < jsonArraygUsers.length(); j++) {
                                JSONObject subJsonUser = jsonArraygUsers.getJSONObject(j);
                                String gUName = subJsonUser.getString("gUName");    //사용자 이름 받아옴
                                group.getGroupUsers().add(gUName);
                            }
                            groupList.add(group);      //사용자가 포함된 그룹목록인 grouplist에 추가함
                        }

                        Toast.makeText(getApplicationContext(), nickName+"님 환영합니다:)", Toast.LENGTH_SHORT).show();
                        Intent it_home = new Intent(LoginActivity.this, HomeActivity.class);        //홈화면으로 화면이 전환됨
                        Bundle bundle = new Bundle();
                        if (isSocialLogin == 0)
                            bundle.putBoolean("isSocialLogin", false);
                        else
                            bundle.putBoolean("isSocialLogin", true);
                        it_home.putExtras(bundle);
                        startActivity(it_home);     //로그인이 가능하므로 Home창으로 넘어감
                        finish();
                    } else {            //로그인 가능하지 않음
                        if (!jsonObject.getBoolean("isSocialLogin")) {      //자체 로그인 시스템 사용 시
                            if (existID)        //ID는 존재하나 비밀번호가 일치하지 않음
                                Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.\n다시 입력 바랍니다:)", Toast.LENGTH_SHORT).show();
                            else                //입력한 ID가 존재하지 않음
                                Toast.makeText(getApplicationContext(), "입력하신 ID가 존재하지 않습니다.\n확인 바랍니다:)", Toast.LENGTH_SHORT).show();
                        }
                        else {      //소셜 로그인으로 처음 회원가입하는 경우
                            //닉네임 입력받음
                            NickNameCheckDialog nickNameCheckDialog = new NickNameCheckDialog(LoginActivity.this, userName -> {    //닉네임 중복확인 완료
                                //DB에 사용자 계정 추가 요청
                                nickName = userName;
                                try {
                                    new FirstGJoinSubmitTask(userId, userEmail).execute().get();
                                } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
                            });
                            nickNameCheckDialog.show();
                        }
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            };
            //서버로 Volley를 이용해서 요청을 함.(로그인 가능 여부 확인을 위한 전달)
            LoginRequest loginRequest = new LoginRequest(userID, userPW, String.valueOf(isSocialLogin), responseListener);
            RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
            queue.add(loginRequest);

            return null;
        }
    }

    //구글 계정으로 첫 로그인 시
    @SuppressLint("StaticFieldLeak")
    public class FirstGJoinSubmitTask extends AsyncTask<String, Void, String> {
        String userId;
        String userEmail;

        FirstGJoinSubmitTask(String userId, String userEmail) {
            super();
            this.userId = userId;
            this.userEmail = userEmail;
        }

        @Override
        protected String doInBackground(String... strings) {
            String userNickName = nickName;

            //처음으로 구글 소셜 로그인 할 시
            Response.Listener<String> responseListener = response -> {
                friendList.clear();     //친구목록 초기화
                groupList.clear();      //그룹목록 초기화

                Toast.makeText(getApplicationContext(), nickName+"님 환영합니다:)", Toast.LENGTH_SHORT).show();
                Intent it_home = new Intent(LoginActivity.this, HomeActivity.class);        //홈화면으로 화면이 전환됨
                Bundle bundle = new Bundle();
                bundle.putBoolean("isSocialLogin", true);
                startActivity(it_home);     //로그인이 가능하므로 Home창으로 넘어감
                finish();
            };

            //서버로 Volley를 이용해서 요청을 함
            JoinRequest joinRequest = new JoinRequest(userId, userEmail, userNickName, "", "", "1", responseListener);
            RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
            queue.add(joinRequest);

            return null;
        }
    }
}
