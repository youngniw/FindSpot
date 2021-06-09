package com.example.findspot;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InquiryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquiry);

        ImageView ivBack = findViewById(R.id.inquiry_back);
        ivBack.setOnClickListener(v -> finish());

        TextView tvContent = findViewById(R.id.inquiry_content);
        Spannable span = (Spannable) tvContent.getText();
        span.setSpan(new UnderlineSpan(), 4, 25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#2F9D27")), 4, 25, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
