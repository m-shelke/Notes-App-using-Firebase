package com.example.notesappusingfirebase.Helper;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.notesappusingfirebase.R;

public class ReactionPopup {

    public interface ReactionListener {
        void onReact(String reaction);
    }

    public static void show(Context ctx, View anchor, ReactionListener listener) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View root = inflater.inflate(R.layout.reaction_popup_layout, null);

        PopupWindow popup = new PopupWindow(root,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);

        // simple emoji buttons
        root.findViewById(R.id.react_like).setOnClickListener(v -> {
            listener.onReact("❤️");
            popup.dismiss();
        });
        root.findViewById(R.id.react_laugh).setOnClickListener(v -> {
            listener.onReact("😂");
            popup.dismiss();
        });
        root.findViewById(R.id.react_thumb).setOnClickListener(v -> {
            listener.onReact("👍");
            popup.dismiss();
        });
        root.findViewById(R.id.react_wow).setOnClickListener(v -> {
            listener.onReact("😮");
            popup.dismiss();
        });
        root.findViewById(R.id.react_sad).setOnClickListener(v -> {
            listener.onReact("😢");
            popup.dismiss();
        });
        root.findViewById(R.id.react_angry).setOnClickListener(v -> {
            listener.onReact("😡");
            popup.dismiss();
        });

        popup.setElevation(8f);
        popup.showAsDropDown(anchor, 0, -anchor.getHeight() - 20, Gravity.TOP);
    }


}

