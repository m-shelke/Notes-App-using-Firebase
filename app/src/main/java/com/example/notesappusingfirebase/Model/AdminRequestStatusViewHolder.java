package com.example.notesappusingfirebase.Model;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.R;
import com.squareup.picasso.Picasso;

public class AdminRequestStatusViewHolder extends RecyclerView.ViewHolder {

    public TextView tvUserName, tvEmail, tvStatus, reqSentOnTv;
    ImageView userProfileImg;

    public AdminRequestStatusViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void setAdminRequestStatus(Activity activity, String userName, String requestTime, String email, String profile, String requestStatus) {

        tvUserName = itemView.findViewById(R.id.userNameTv);
        reqSentOnTv = itemView.findViewById(R.id.reqSendOnTv);
        tvEmail = itemView.findViewById(R.id.userEmailTv);
        userProfileImg = itemView.findViewById(R.id.userProfileImg);
        tvStatus = itemView.findViewById(R.id.tvStatus);

        tvUserName.setText(userName);
        reqSentOnTv.setText(requestTime);
        tvEmail.setText(email);
        tvStatus.setText(requestStatus);
        Picasso.get().load(profile).into(userProfileImg);

    }
}
