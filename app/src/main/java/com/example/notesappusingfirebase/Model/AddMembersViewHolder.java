package com.example.notesappusingfirebase.Model;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.R;
import com.squareup.picasso.Picasso;

public class AddMembersViewHolder extends RecyclerView.ViewHolder {

    public TextView memberName, memberEmail, txtInvite,showMemberJoinDate;
    public CheckBox checkBox;
    public TextView adminTv;
    ImageView memberProfileImg,checkIcon;

    public AddMembersViewHolder(@NonNull View itemView) {
        super(itemView);

//        checkBox = itemView.findViewById(R.id.memberCheckBox);
        adminTv = itemView.findViewById(R.id.tvAdminBadge);
        txtInvite = itemView.findViewById(R.id.txtInvite);
        checkBox = itemView.findViewById(R.id.memberCheckBox);
        showMemberJoinDate = itemView.findViewById(R.id.showMemberTime);
        checkIcon = itemView.findViewById(R.id.checkIcon);
    }

    public void setAddMembersProfile(Activity activity, String name, String email, String profileUrl) {

        memberName = itemView.findViewById(R.id.memberName);
        memberEmail = itemView.findViewById(R.id.memberEmail);
        memberProfileImg = itemView.findViewById(R.id.memberProfileImg);

        memberName.setText(name);
        memberEmail.setText(email);
        Picasso.get().load(profileUrl).into(memberProfileImg);

    }

    public void setShowMembersProfile(Activity activity, String name, String joinDate, String profileUrl){

        memberName = itemView.findViewById(R.id.showMemberName);
        memberProfileImg = itemView.findViewById(R.id.showMemberProfile);

        memberName.setText(name);
        showMemberJoinDate.setText(joinDate);
        Picasso.get().load(profileUrl).into(memberProfileImg);
    }

    public void setSelection(boolean isSelected) {
        if (isSelected) {
            checkIcon.setVisibility(View.VISIBLE);
            itemView.setBackgroundColor(Color.parseColor("#E3F2FD")); // light blue highlight
        } else {
            checkIcon.setVisibility(View.GONE);
            itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

}
