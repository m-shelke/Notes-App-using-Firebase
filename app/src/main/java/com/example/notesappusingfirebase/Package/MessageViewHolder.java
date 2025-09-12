package com.example.notesappusingfirebase.Package;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    public TextView mgSenderNameTv, messageBodyTv, messageTimeTv, messageTypeTv, fileNameTv;
    public ImageView fileDownloadIv;
    LinearLayout messageLinearLayout;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void setMessageNote(Activity activity, String senderName, String senderUId, String note, String time, String type, String url, String search,String fileName) {

        mgSenderNameTv = itemView.findViewById(R.id.senderNameTv);
        messageBodyTv = itemView.findViewById(R.id.messageBodyTv);
        messageTimeTv = itemView.findViewById(R.id.messageTimeTv);
      //  messageTypeTv = itemView.findViewById(R.id.messageTypeTv);
        fileNameTv = itemView.findViewById(R.id.messageFileTv);
        fileDownloadIv = itemView.findViewById(R.id.fileDownloadIv);
        messageLinearLayout = itemView.findViewById(R.id.fileLinearLayout);

        switch (type) {
            case "TEXT":
                mgSenderNameTv.setText(senderName);
                messageBodyTv.setText(note);
                messageTimeTv.setText(time);
//                messageTypeTv.setText(type);
                messageLinearLayout.setVisibility(View.GONE);
                break;
            case "PPT":
                messageLinearLayout.setVisibility(View.VISIBLE);
                messageBodyTv.setVisibility(View.VISIBLE);
                mgSenderNameTv.setText(senderName);
                //  messageTv.setText(note);
                messageTimeTv.setText(time);
//                messageTypeTv.setText(type);
                fileNameTv.setText(fileName);

                fileNameTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ppt, 0, 0, 0);
                break;
            case "PDF":
                messageLinearLayout.setVisibility(View.VISIBLE);
                messageBodyTv.setVisibility(View.VISIBLE);
                mgSenderNameTv.setText(senderName);
                //  messageTv.setText(note);
                messageTimeTv.setText(time);
              //  messageTypeTv.setText(type);
                fileNameTv.setText(fileName);

                fileNameTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pdf, 0, 0, 0);
                break;
            case "DOC":
                messageLinearLayout.setVisibility(View.VISIBLE);
                messageBodyTv.setVisibility(View.VISIBLE);
                mgSenderNameTv.setText(senderName);
                //  messageTv.setText(note);
                messageTimeTv.setText(time);
          //      messageTypeTv.setText(type);
                fileNameTv.setText(fileName);

                fileNameTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.doc, 0, 0, 0);
                break;
            case "IMAGE":
                messageLinearLayout.setVisibility(View.VISIBLE);
                messageBodyTv.setVisibility(View.VISIBLE);
                mgSenderNameTv.setText(senderName);
                messageBodyTv.setText(note);
                messageTimeTv.setText(time);
            //    messageTypeTv.setText(type);
                fileNameTv.setText(fileName);

                fileNameTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.image, 0, 0, 0);
                break;
        }


    }
}
