package com.example.notesappusingfirebase.Adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notesappusingfirebase.Activities.ImagePreviewActivity;
import com.example.notesappusingfirebase.Helper.ReactionPopup;
import com.example.notesappusingfirebase.Model.MembersModel;
import com.example.notesappusingfirebase.Model.RoomChatMemberModel;
import com.example.notesappusingfirebase.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * ChatAdapter - supports:
 * - TEXT / IMAGE / FILE
 * - reply preview
 * - reactions (saves to DB)
 * - deleteForMe (writes to DeletedFor/<roomId>/<messageId>/<uid>)
 * - deleteForEveryone (updates message text + deleted flag)
 * - open files via Google Docs viewer or chooser (no download)
 * <p>
 * Usage:
 * new ChatAdapter(context, messagesList, roomId, callback)
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_SENDER = 1;
    private static final int VIEW_RECEIVER = 2;

    private final Context ctx;
    private final List<RoomChatMemberModel> items;
    private final String roomId;
    private final String myUid;
    private final Callback callback;
    private final Set<String> selectedIds = new HashSet<>();
    public boolean selectionMode = false;
    String currentUid;
    TextView seenCountTv;
    LinearLayout avatarContainer;
    View seenLayout;
    private int roomMemberCount;
    private Map<String, String> memberAvatars;
    private List<MembersModel> roomMembers;
    private Callback.SelectionListener selectionListener;

    public ChatAdapter(Context ctx, List<RoomChatMemberModel> items, String roomId, int roomMemberCount, Map<String, String> memberAvatars, List<MembersModel> roomMembers, String currentUid, Callback callback) {
        this.ctx = ctx;
        this.items = items;
        this.roomId = roomId;
        this.callback = callback;
        this.myUid = FirebaseAuth.getInstance().getUid();
        this.roomMemberCount = roomMemberCount;
        this.roomMembers = roomMembers;
        this.memberAvatars = memberAvatars;
        this.currentUid = currentUid;

        Log.e("COUNT_MEMBER", String.valueOf(roomMemberCount));
    }

    @Override
    public int getItemViewType(int position) {
        RoomChatMemberModel m = items.get(position);
        if (m.getSenderId() != null && m.getSenderId().equals(myUid)) return VIEW_SENDER;
        return VIEW_RECEIVER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(ctx);
        if (viewType == VIEW_SENDER) {
            View v = li.inflate(R.layout.item_sender, parent, false);
            return new SenderVH(v);
        } else {
            View v = li.inflate(R.layout.item_receiver, parent, false);
            return new ReceiverVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holderRaw, int position) {
        RoomChatMemberModel m = items.get(position);
        if (holderRaw instanceof SenderVH) {

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                holderRaw.itemView.setBackgroundColor(Color.TRANSPARENT);
            }, 500);

            ((SenderVH) holderRaw).bind(m);

        } else {

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                holderRaw.itemView.setBackgroundColor(Color.TRANSPARENT);
            }, 500);

            ((ReceiverVH) holderRaw).bind(m);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void setFileIcon(String fileName, ImageView iv) {
        if (fileName == null) {
            iv.setImageResource(R.drawable.baseline_insert_drive_file_24);
            return;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) iv.setImageResource(R.drawable.pdf);
        else if (lower.endsWith(".ppt") || lower.endsWith(".pptx"))
            iv.setImageResource(R.drawable.ppt);
        else if (lower.endsWith(".doc") || lower.endsWith(".docx"))
            iv.setImageResource(R.drawable.doc);
        else iv.setImageResource(R.drawable.image);
    }


    private void showMessageActions(RoomChatMemberModel message, View anchor) {

        View sheetView = LayoutInflater.from(ctx).inflate(R.layout.bs_message_actions, null);

        BottomSheetDialog dialog = new BottomSheetDialog(ctx);
        dialog.setContentView(sheetView);

        setupAction(sheetView.findViewById(R.id.action_copy), R.drawable.outline_file_copy_24, "Copy");

        setupAction(sheetView.findViewById(R.id.action_reply), R.drawable.outline_swipe_right_24, "Reply");

        setupAction(sheetView.findViewById(R.id.action_react), R.drawable.outline_add_reaction_24, "React");

        setupAction(sheetView.findViewById(R.id.action_download), R.drawable.outline_download_24, "Download");

        setupDangerAction(sheetView.findViewById(R.id.action_delete_for_me), R.drawable.outline_delete_24, "Delete for me");

        if (message.getSenderId().equals(currentUid)){
            setupDangerAction(sheetView.findViewById(R.id.action_delete_for_everyone), R.drawable.baseline_delete_24, "Delete for everyone");
        }else {
           sheetView.findViewById(R.id.action_delete_for_everyone).setVisibility(View.GONE);
        }

        setupDangerAction(sheetView.findViewById(R.id.action_delete_for_everyone), R.drawable.baseline_delete_24, "Delete for everyone");

        setupAction(sheetView.findViewById(R.id.action_save_message), R.drawable.outline_data_saver_on_24, "Save");

        dialog.findViewById(R.id.action_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(message.getNote());
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.action_reply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) callback.onReplyRequested(message);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.action_react).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show ReactionPopup
                ReactionPopup.show(ctx, anchor, reaction -> {
                    saveReaction(message, reaction);
                });
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.action_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // try to open with viewer (no-download) or fallback to browser/chooser
                openFile(message.getFileUrl(), message.getFileName());
                dialog.dismiss();

            }
        });

        dialog.findViewById(R.id.action_delete_for_me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // markDeletedForMe(message);
                callback.onDeleteForMe(message);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.action_delete_for_everyone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // confirm
                new AlertDialog.Builder(ctx)
                        .setTitle("Delete for everyone?")
                        .setMessage("This will delete the message for all participants.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            callback.onDeleteForEveryone(message);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                dialog.dismiss();

            }
        });

        dialog.findViewById(R.id.action_save_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ctx, "Save", Toast.LENGTH_SHORT).show();

                callback.onSaveRequested(message);
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void setupAction(View v, int icon, String title) {
        ImageView iv = v.findViewById(R.id.icon);
        TextView tv = v.findViewById(R.id.title);
        iv.setImageResource(icon);
        tv.setText(title);
    }

    private void setupDangerAction(View v, int icon, String title) {
        ImageView iv = v.findViewById(R.id.icon);
        TextView tv = v.findViewById(R.id.title);
        iv.setImageResource(icon);
        iv.setColorFilter(Color.RED);
        tv.setText(title);
        tv.setTextColor(Color.RED);
    }


    private void copyToClipboard(String text) {
        if (TextUtils.isEmpty(text)) return;
        ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData c = ClipData.newPlainText("copied", text);
        if (cm != null) cm.setPrimaryClip(c);
        Toast.makeText(ctx, "Copied", Toast.LENGTH_SHORT).show();
    }

    /* ----------------------- HELPERS ----------------------- */

    private void saveReaction(RoomChatMemberModel message, String reaction) {
        if (message.getMessageId() == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("NoteList").child(roomId).child(message.getMessageId()).child("reaction");
        ref.setValue(reaction);
    }

    public void markDeletedForMe(RoomChatMemberModel message) {
        if (message.getMessageId() == null) return;
        String uid = myUid;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DeletedFor").child(roomId).child(message.getMessageId()).child(uid);

        ref.setValue(true).addOnSuccessListener(aVoid -> {
            // optionally remove locally
            for (int i = 0; i < items.size(); i++) {
                if (message.getMessageId().equals(items.get(i).getMessageId())) {
                    items.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
            Toast.makeText(ctx, "Deleted for you", Toast.LENGTH_SHORT).show();
        });
    }

    public void markDeletedForEveryone(RoomChatMemberModel message) {
        if (message.getMessageId() == null) return;
        DatabaseReference msgRef = FirebaseDatabase.getInstance().getReference("NoteList").child(roomId).child(message.getMessageId());

        msgRef.child("note").setValue("This message was deleted");
        msgRef.child("type").setValue("TEXT");
        msgRef.child("deletedForEveryone").setValue(true);
        Toast.makeText(ctx, "Deleted for everyone", Toast.LENGTH_SHORT).show();
    }

    /**
     * Open file without forcing a download:
     * - For PDF/DOC/DOCX/PPT/PPTX -> open with Google Docs viewer in browser (no download)
     * - For image -> open ImagePreviewActivity (in-app)
     * - Otherwise -> try ACTION_VIEW with the URL (opens in browser/compatible handlers)
     * <p>
     * NOTE: Many installed office apps require a local file (content://) to open.
     * If you want to open inside installed Office apps, you must download the file and pass a FileProvider URI.
     */
    private void openFile(String fileUrl, String fileName) {
        if (TextUtils.isEmpty(fileUrl)) {
            Toast.makeText(ctx, "No file URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String lower = (fileName != null ? fileName.toLowerCase(Locale.ROOT) : fileUrl.toLowerCase(Locale.ROOT));
        try {
            if (lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".ppt") || lower.endsWith(".pptx")) {

                // Use Google Docs viewer (works without downloading; opens in browser)
                String openUrl = "https://docs.google.com/gview?embedded=true&url=" + Uri.encode(fileUrl);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(openUrl));
                ctx.startActivity(Intent.createChooser(i, "Open with"));
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")) {
                // image -> open internal preview (preferred)
                Intent pi = new Intent(ctx, ImagePreviewActivity.class);
                pi.putExtra("imageUrl", fileUrl);
                ctx.startActivity(pi);
            } else {
                // fallback: open URL in browser/chooser
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                ctx.startActivity(Intent.createChooser(i, "Open with"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ctx, "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }

    public void addOrUpdate(RoomChatMemberModel message) {

        if (message == null || message.getMessageId() == null) return;

        for (int i = 0; i < items.size(); i++) {
            if (message.getMessageId().equals(items.get(i).getMessageId())) {
                // update existing message
                items.set(i, message);
                notifyItemChanged(i);
                return;
            }
        }

        // add new message
        items.add(message);
        notifyItemInserted(items.size() - 1);


    }

    private void bindSeen(RoomChatMemberModel m) {

        if (!m.getSenderId().equals(currentUid)) {
            seenLayout.setVisibility(View.GONE);
            return;
        }

        Map<String, Boolean> seenMap = m.getSeenBy();

        if (seenMap == null || seenMap.isEmpty()) {
            seenLayout.setVisibility(View.GONE);
            return;
        }

        int seenCount = seenMap.size();
        int total = roomMemberCount - 1;

        seenLayout.setVisibility(View.VISIBLE);
        seenCountTv.setVisibility(View.VISIBLE);
        seenCountTv.setText("Seen by " + seenCount + "/" + total + " members");

        avatarContainer.removeAllViews();

        int maxAvatars = 3;
        int added = 0;

        for (String uid : seenMap.keySet()) {
            if (added >= maxAvatars) break;

            ImageView iv = new ImageView(ctx);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(40, 40);
            lp.setMarginEnd(-12); // overlap
            iv.setLayoutParams(lp);

            iv.setBackgroundResource(R.drawable.circle_bg);
            iv.setClipToOutline(true);

            String avatarUrl = memberAvatars.get(uid);
            Glide.with(ctx)
                    .load(avatarUrl)
                    .placeholder(R.drawable.baseline_person_24)
                    .into(iv);

            avatarContainer.addView(iv);
            added++;
        }
    }

    public void toggleSelection(RoomChatMemberModel m) {

        if (selectedIds.contains(m.getMessageId())) {
            selectedIds.remove(m.getMessageId());
        } else {
            selectedIds.add(m.getMessageId());
        }

        selectionMode = !selectedIds.isEmpty();
        notifySelectionChanged();
        notifyDataSetChanged();
    }


    public boolean isSelected(String messageId) {
        return selectedIds.contains(messageId);
    }

    public List<RoomChatMemberModel> getSelectedMessages() {
        List<RoomChatMemberModel> list = new ArrayList<>();
        for (RoomChatMemberModel m : items) {
            if (selectedIds.contains(m.getMessageId())) {
                list.add(m);
            }
        }
        return list;
    }

    public void clearSelection() {
        selectedIds.clear();
        selectionMode = false;
        notifySelectionChanged();
        notifyDataSetChanged();
    }

    public void setSelectionListener(Callback.SelectionListener listener) {
        this.selectionListener = listener;
    }

    private void notifySelectionChanged() {
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(selectedIds.size());
        }
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }


    public interface Callback {
        void onDeleteRequested(RoomChatMemberModel message); // optional activity handling

        void onReplyRequested(RoomChatMemberModel message);

        void onImageClicked(RoomChatMemberModel message, ImageView imageView);

        void onScrollToMessage(String messageId); // ✅ add

        void onDeleteForEveryone(RoomChatMemberModel message);

        void onDeleteForMe(RoomChatMemberModel message);


        void onSaveRequested(RoomChatMemberModel message);

        void onPinRequested(RoomChatMemberModel message);

        public interface SelectionListener {
            void onSelectionChanged(int count);
        }

    }

    private class SenderVH extends RecyclerView.ViewHolder {
        TextView senderMessageTv, senderTimeTv, senderFileName, senderReactionTv, senderReplyUser, senderReplyText, forwardedLabelTv, noteTitleTv;
        ImageView senderImageIv, senderFileIcon, senderSeenTick;
        View senderFileLayout, senderReplyLayout;
        LinearLayout senderMainContainer;


        SenderVH(@NonNull View v) {
            super(v);
            senderMessageTv = v.findViewById(R.id.senderMessageTv);
            senderTimeTv = v.findViewById(R.id.senderTimeTv);
            senderImageIv = v.findViewById(R.id.senderImageIv);
            senderFileLayout = v.findViewById(R.id.senderFileLayout);
            senderFileName = v.findViewById(R.id.senderFileName);
            senderFileIcon = v.findViewById(R.id.senderFileIcon);
            senderReactionTv = v.findViewById(R.id.senderReactionTv);
            senderSeenTick = v.findViewById(R.id.senderSeenTick);
            senderReplyLayout = v.findViewById(R.id.senderReplyLayout);
            senderReplyUser = v.findViewById(R.id.senderReplyUser);
            senderReplyText = v.findViewById(R.id.senderReplyText);
            senderMainContainer = v.findViewById(R.id.senderMainContainer);
            forwardedLabelTv = v.findViewById(R.id.senderForwardedLabelTv);
            noteTitleTv = v.findViewById(R.id.noteTitleTv);
            seenLayout = v.findViewById(R.id.seenLayout);
            seenCountTv = v.findViewById(R.id.seenCountTv);
            avatarContainer = v.findViewById(R.id.avatarContainer);

        }


        @SuppressLint("UseCompatLoadingForDrawables")
        void bind(RoomChatMemberModel m) {
            // reset visibility
            senderMessageTv.setVisibility(View.GONE);
            senderImageIv.setVisibility(View.GONE);
            senderFileLayout.setVisibility(View.GONE);
            senderReactionTv.setVisibility(View.GONE);
            senderReplyLayout.setVisibility(View.GONE);

            if (m.getType().equals("TEXT")) {
                noteTitleTv.setVisibility(View.GONE);
                senderMessageTv.setVisibility(View.VISIBLE);
            } else if (m.getType().equals("NOTETEXT")) {
                noteTitleTv.setVisibility(View.VISIBLE);
                noteTitleTv.setText(m.getTitle());

            } else {
                noteTitleTv.setVisibility(View.GONE);
            }


            if (m.isForwarded()) {
                forwardedLabelTv.setVisibility(View.VISIBLE);

                if (!TextUtils.isEmpty(m.getForwardedFrom())) {
                    if (m.getSenderId().equals(currentUid)) {
                        forwardedLabelTv.setText("Forward You");
                    } else {
                        forwardedLabelTv.setText("Forwarded from " + m.getForwardedFrom());
                    }
                } else {
                    forwardedLabelTv.setText("Forwarded");
                }

            } else {
                forwardedLabelTv.setVisibility(View.GONE);
            }

            itemView.setOnLongClickListener(v -> {
                toggleSelection(m);
                return true;
            });

            itemView.setOnClickListener(v -> {
                if (selectionMode) {
                    toggleSelection(m);
                } else {
                    // normal click (open image/file)
                    showMessageActions(m, itemView);
                }
            });


            if (isSelected(m.getMessageId())) {
//                senderMainContainer.setBackgroundColor(ContextCompat.getColor(ctx, R.color.blue));
                senderMainContainer.setBackground(ctx.getDrawable(R.drawable.preview_bg));
            } else {
                senderMainContainer.setBackground(ctx.getDrawable(R.drawable.bubble_sender));
            }

            //senderMainContainer.setBackgroundColor(isSelected(m.getMessageId()) ? ContextCompat.getColor(ctx, R.color.blue) : Color.GRAY);

            // seen tick
            senderSeenTick.setVisibility(View.VISIBLE);
            int seenCount = m.getSeenBy() != null ? m.getSeenBy().size() : 0;

            Log.e("SEEN_BY", String.valueOf(seenCount));

            if (seenCount <= 0) {
                senderSeenTick.setImageResource(R.drawable.single_tick);
            } else if (seenCount > 0) {
                senderSeenTick.setImageResource(R.drawable.blue_tick);
            } else {
                senderSeenTick.setVisibility(View.VISIBLE);
            }

            itemView.setBackground(null); // ALWAYS reset first

            senderReplyLayout.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onScrollToMessage(m.getReplyToId());
                }
            });

            // time
            if (m.getTimestamp() > 0) {
                senderTimeTv.setText(android.text.format.DateFormat.format("dd-MMM hh:mm a", m.getTimestamp()));
            } else {
                senderTimeTv.setText(m.getTimestamp() + "");
            }

            // reply preview
            if (!TextUtils.isEmpty(m.getReplyToText())) {
                senderReplyLayout.setVisibility(View.VISIBLE);
                //  senderReplyUser.setText(m.getReplyToUser() != null ? m.getReplyToUser() : "");
                senderReplyUser.setText(m.getReplyTitle());
                senderReplyText.setText(m.getReplyToText());
            }

            // reaction
            if (!TextUtils.isEmpty(m.getReaction())) {
                senderReactionTv.setVisibility(View.VISIBLE);
                senderReactionTv.setText(m.getReaction());
            }

            if (m.isDeletedForEveryone() || "DELETED".equals(m.getType())) {

                String text = m.getSenderId().equals(currentUid) ? "You deleted this message" : "This message was deleted";

                senderMessageTv.setText(text);

                senderMessageTv.setVisibility(View.VISIBLE);
                senderMessageTv.setText(text);

                senderImageIv.setVisibility(View.GONE);
                senderFileLayout.setVisibility(View.GONE);
                senderReplyLayout.setVisibility(View.GONE);
                senderReactionTv.setVisibility(View.GONE);

                senderMessageTv.setTypeface(null, Typeface.ITALIC);
                senderMessageTv.setTextColor(
                        ContextCompat.getColor(ctx, R.color.black)
                );
                return; // 🔥 STOP HERE
            }


            // handle types
            String type = m.getType() == null ? "TEXT" : m.getType().toUpperCase(Locale.ROOT);

            switch (type) {
                case "IMAGE":
                    senderImageIv.setVisibility(View.VISIBLE);
                    String img = m.getImageUrl() != null ? m.getImageUrl() : m.getFileUrl();
                    if (!TextUtils.isEmpty(img)) {
                        Glide.with(ctx).load(img).placeholder(R.drawable.outline_imagesmode_24).into(senderImageIv);
                    }
                    senderImageIv.setOnClickListener(v -> {
                        if (callback != null) callback.onImageClicked(m, senderImageIv);
                        else {
                            // default: open ImagePreviewActivity
                            Intent i = new Intent(ctx, ImagePreviewActivity.class);
                            i.putExtra("imageUrl", img);
                            ctx.startActivity(i);
                        }
                    });
                    if (!TextUtils.isEmpty(m.getNote())) {
                        senderMessageTv.setVisibility(View.VISIBLE);
                        senderMessageTv.setText(m.getNote());
                    }
                    break;

                case "CANVAS":
                    senderImageIv.setVisibility(View.VISIBLE);
                    String img1 = m.getImageUrl() != null ? m.getImageUrl() : m.getFileUrl();
                    if (!TextUtils.isEmpty(img1)) {
                        Glide.with(ctx).load(img1).placeholder(R.drawable.outline_imagesmode_24).into(senderImageIv);
                    }
                    senderImageIv.setOnClickListener(v -> {
                        if (callback != null) callback.onImageClicked(m, senderImageIv);
                        else {
                            // default: open ImagePreviewActivity
                            Intent i = new Intent(ctx, ImagePreviewActivity.class);
                            i.putExtra("imageUrl", img1);
                            ctx.startActivity(i);
                        }
                    });
                    if (!TextUtils.isEmpty(m.getNote())) {
                        senderMessageTv.setVisibility(View.VISIBLE);
                        senderMessageTv.setText(m.getNote());
                    }
                    break;

                case "FILE":
                    senderFileLayout.setVisibility(View.VISIBLE);
                    senderFileName.setText(m.getFileName() != null ? m.getFileName() : "File");
                    setFileIcon(m.getFileName(), senderFileIcon);
                    senderFileLayout.setOnClickListener(v -> openFile(m.getFileUrl(), m.getFileName()));
                    if (!TextUtils.isEmpty(m.getNote())) {
                        senderMessageTv.setVisibility(View.VISIBLE);
                        senderMessageTv.setText(m.getNote());
                    }
                    break;

                case "PDF":
                    senderFileLayout.setVisibility(View.VISIBLE);
                    senderFileName.setText(m.getFileName() != null ? m.getFileName() : "File");
                    setFileIcon(m.getFileName(), senderFileIcon);
                    senderFileLayout.setOnClickListener(v -> openFile(m.getFileUrl(), m.getFileName()));
                    if (!TextUtils.isEmpty(m.getNote())) {
                        senderMessageTv.setVisibility(View.VISIBLE);
                        senderMessageTv.setText(m.getNote());
                    }
                    break;

                case "DOC":
                    senderFileLayout.setVisibility(View.VISIBLE);
                    senderFileName.setText(m.getFileName() != null ? m.getFileName() : "File");
                    setFileIcon(m.getFileName(), senderFileIcon);
                    senderFileLayout.setOnClickListener(v -> openFile(m.getFileUrl(), m.getFileName()));
                    if (!TextUtils.isEmpty(m.getNote())) {
                        senderMessageTv.setVisibility(View.VISIBLE);
                        senderMessageTv.setText(m.getNote());
                    }
                    break;

                case "PPT":
                    senderFileLayout.setVisibility(View.VISIBLE);
                    senderFileName.setText(m.getFileName() != null ? m.getFileName() : "File");
                    setFileIcon(m.getFileName(), senderFileIcon);
                    senderFileLayout.setOnClickListener(v -> openFile(m.getFileUrl(), m.getFileName()));
                    if (!TextUtils.isEmpty(m.getNote())) {
                        senderMessageTv.setVisibility(View.VISIBLE);
                        senderMessageTv.setText(m.getNote());
                    }
                    break;

                case "TEXT":
                default:
                    senderMessageTv.setVisibility(View.VISIBLE);
                    senderMessageTv.setText(m.getNote() != null ? m.getNote() : "");
                    break;
            }
            bindSeen(m);
        }

    }

    private class ReceiverVH extends RecyclerView.ViewHolder {
        TextView recvText, recvTime, recvFileName, recvReaction, recvReplyUser, recvReplyText, senderName, forwardedLabelTv, noteTitleTv;
        ImageView recvImage, recvFileIcon;
        View recvFileLayout, recvReplyLayout;
        LinearLayout receiverLinear;

        ReceiverVH(@NonNull View v) {
            super(v);
            recvText = v.findViewById(R.id.textMessage);
            noteTitleTv = v.findViewById(R.id.noteTitleTv);
            senderName = v.findViewById(R.id.snNameTv);
            recvTime = v.findViewById(R.id.timeStamp);
            recvImage = v.findViewById(R.id.imageMessage);
            recvFileLayout = v.findViewById(R.id.fileLayout);
            recvFileName = v.findViewById(R.id.fileName);
            recvFileIcon = v.findViewById(R.id.fileIcon);
            recvReaction = v.findViewById(R.id.reactionView);
            recvReplyLayout = v.findViewById(R.id.replyLayout);
            recvReplyUser = v.findViewById(R.id.replyUser);
            recvReplyText = v.findViewById(R.id.replyText);
            receiverLinear = v.findViewById(R.id.receiverLinear);
            forwardedLabelTv = v.findViewById(R.id.receiverForwardedLabelTv);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        void bind(RoomChatMemberModel m) {
            // reset
            recvText.setVisibility(View.GONE);
            recvImage.setVisibility(View.GONE);
            recvFileLayout.setVisibility(View.GONE);
            recvReaction.setVisibility(View.GONE);
            recvReplyLayout.setVisibility(View.GONE);

            if (m.getType().equals("TEXT")) {
                recvText.setVisibility(View.GONE);
                recvText.setVisibility(View.VISIBLE);
            } else if (m.getType().equals("NOTETEXT")) {
                noteTitleTv.setVisibility(View.VISIBLE);
                noteTitleTv.setText(m.getTitle());
            } else {
                noteTitleTv.setVisibility(View.GONE);
            }

            if (m.isForwarded()) {
                forwardedLabelTv.setVisibility(View.VISIBLE);
                senderName.setVisibility(View.GONE);

                if (!TextUtils.isEmpty(m.getForwardedFrom())) {
                    if (m.getSenderId().equals(currentUid)) {
                        forwardedLabelTv.setText("Forward You");
                    } else {
                        forwardedLabelTv.setText("Forwarded from " + m.getForwardedFrom());
                    }
                } else {
                    forwardedLabelTv.setText("Forwarded");
                }

            } else {
                forwardedLabelTv.setVisibility(View.GONE);
            }


            itemView.setOnLongClickListener(v -> {
                toggleSelection(m);
                return true;
            });

            itemView.setOnClickListener(v -> {
                if (selectionMode) {
                    toggleSelection(m);
                } else {
                    // normal click (open image/file)
                    showMessageActions(m, itemView);
                }
            });


            if (isSelected(m.getMessageId())) {
                // receiverMainContainer.setBackgroundColor(ContextCompat.getColor(ctx, R.color.blue));
                receiverLinear.setBackground(ctx.getDrawable(R.drawable.preview_bg));
            } else {
                receiverLinear.setBackground(ctx.getDrawable(R.drawable.bubble_sender));
            }

            //senderMainContainer.setBackgroundColor(isSelected(m.getMessageId()) ? ContextCompat.getColor(ctx, R.color.blue) : Color.GRAY);

            itemView.setBackground(null); // ALWAYS reset first

            recvReplyLayout.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onScrollToMessage(m.getReplyToId());
                }
            });

            if (m.getTimestamp() > 0) {
                recvTime.setText(android.text.format.DateFormat.format("dd-MMM hh:mm a", m.getTimestamp()));
            }

            // reply preview
            if (!TextUtils.isEmpty(m.getReplyToText())) {
                recvReplyLayout.setVisibility(View.VISIBLE);
                recvReplyUser.setText(m.getReplyToUser() != null ? m.getReplyToUser() : "");
                recvReplyText.setText(m.getReplyToText());
            }

            // reaction
            if (!TextUtils.isEmpty(m.getReaction())) {
                recvReaction.setVisibility(View.VISIBLE);
                recvReaction.setText(m.getReaction());
            }

            if (m.isDeletedForEveryone() || "DELETED".equals(m.getType())) {

                String text = m.getSenderId().equals(currentUid) ? "You deleted this message" : "This message was deleted";

                recvText.setText(text);


                recvText.setVisibility(View.VISIBLE);
                recvText.setText(text);

                recvImage.setVisibility(View.GONE);
                recvFileLayout.setVisibility(View.GONE);
                recvReplyLayout.setVisibility(View.GONE);
                recvReaction.setVisibility(View.GONE);

                recvText.setTypeface(null, Typeface.ITALIC);
                recvText.setTextColor(
                        ContextCompat.getColor(ctx, R.color.black)
                );

                return; // 🔥 STOP HERE
            }

            String type = m.getType() == null ? "TEXT" : m.getType().toUpperCase(Locale.ROOT);

            switch (type) {
                case "IMAGE":
                    recvImage.setVisibility(View.VISIBLE);
                    String img = m.getImageUrl() != null ? m.getImageUrl() : m.getFileUrl();
                    if (!TextUtils.isEmpty(img)) {
                        Glide.with(ctx).load(img).placeholder(R.drawable.outline_imagesmode_24).into(recvImage);
                    }
                    recvImage.setOnClickListener(v -> {
                        if (callback != null) callback.onImageClicked(m, recvImage);
                        else {
                            Intent i = new Intent(ctx, ImagePreviewActivity.class);
                            i.putExtra("imageUrl", img);
                            ctx.startActivity(i);
                        }
                    });
                    if (!TextUtils.isEmpty(m.getNote())) {
                        recvText.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.VISIBLE);
                        recvText.setText(m.getNote());
                        senderName.setText(m.getSenderName());
                    }

                    if (m.isForwarded()) {
                        forwardedLabelTv.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(m.getForwardedFrom())) {
                            if (m.getSenderId().equals(currentUid)) {
                                forwardedLabelTv.setText("Forward You");
                            } else {
                                forwardedLabelTv.setText("Forwarded from " + m.getForwardedFrom());
                            }
                        } else {
                            forwardedLabelTv.setText("Forwarded");
                        }

                    } else {
                        forwardedLabelTv.setVisibility(View.GONE);
                    }
                    break;

                case "CANVAS":
                    recvImage.setVisibility(View.VISIBLE);
                    String img1 = m.getImageUrl() != null ? m.getImageUrl() : m.getFileUrl();
                    if (!TextUtils.isEmpty(img1)) {
                        Glide.with(ctx).load(img1).placeholder(R.drawable.outline_imagesmode_24).into(recvImage);
                    }
                    recvImage.setOnClickListener(v -> {
                        if (callback != null) callback.onImageClicked(m, recvImage);
                        else {
                            Intent i = new Intent(ctx, ImagePreviewActivity.class);
                            i.putExtra("imageUrl", img1);
                            ctx.startActivity(i);
                        }
                    });
                    if (!TextUtils.isEmpty(m.getNote())) {
                        recvText.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.VISIBLE);
                        recvText.setText(m.getNote());
                        senderName.setText(m.getSenderName());
                    }

                    if (m.isForwarded()) {
                        forwardedLabelTv.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(m.getForwardedFrom())) {
                            if (m.getSenderId().equals(currentUid)) {
                                forwardedLabelTv.setText("Forward You");
                            } else {
                                forwardedLabelTv.setText("Forwarded from " + m.getForwardedFrom());
                            }
                        } else {
                            forwardedLabelTv.setText("Forwarded");
                        }

                    } else {
                        forwardedLabelTv.setVisibility(View.GONE);
                    }
                    break;

                case "OTHER":
                    recvFileLayout.setVisibility(View.VISIBLE);
                    recvFileName.setText(m.getFileName() != null ? m.getFileName() : "File");
                    setFileIcon(m.getFileName(), recvFileIcon);
                    recvFileLayout.setOnClickListener(v -> openFile(m.getFileUrl(), m.getFileName()));
                    if (!TextUtils.isEmpty(m.getNote())) {
                        recvText.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.VISIBLE);
                        recvText.setText(m.getNote());
                        senderName.setText(m.getSenderName());
                    }

                    if (m.isForwarded()) {
                        forwardedLabelTv.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(m.getForwardedFrom())) {
                            if (m.getSenderId().equals(currentUid)) {
                                forwardedLabelTv.setText("Forward You");
                            } else {
                                forwardedLabelTv.setText("Forwarded from " + m.getForwardedFrom());
                            }
                        } else {
                            forwardedLabelTv.setText("Forwarded");
                        }

                    } else {
                        forwardedLabelTv.setVisibility(View.GONE);
                    }
                    break;

                case "PDF":
                    recvFileLayout.setVisibility(View.VISIBLE);
                    recvFileName.setText(m.getFileName() != null ? m.getFileName() : "File");
                    setFileIcon(m.getFileName(), recvFileIcon);
                    recvFileLayout.setOnClickListener(v -> openFile(m.getFileUrl(), m.getFileName()));
                    if (!TextUtils.isEmpty(m.getNote())) {
                        recvText.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.VISIBLE);
                        recvText.setText(m.getNote());
                        senderName.setText(m.getSenderName());
                    }

                    if (m.isForwarded()) {
                        forwardedLabelTv.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(m.getForwardedFrom())) {
                            if (m.getSenderId().equals(currentUid)) {
                                forwardedLabelTv.setText("Forward You");
                            } else {
                                forwardedLabelTv.setText("Forwarded from " + m.getForwardedFrom());
                            }
                        } else {
                            forwardedLabelTv.setText("Forwarded");
                        }

                    } else {
                        forwardedLabelTv.setVisibility(View.GONE);
                    }
                    break;

                case "DOC":
                    recvFileLayout.setVisibility(View.VISIBLE);
                    recvFileName.setText(m.getFileName() != null ? m.getFileName() : "File");
                    setFileIcon(m.getFileName(), recvFileIcon);
                    recvFileLayout.setOnClickListener(v -> openFile(m.getFileUrl(), m.getFileName()));
                    if (!TextUtils.isEmpty(m.getNote())) {
                        recvText.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.VISIBLE);
                        recvText.setText(m.getNote());
                        senderName.setText(m.getSenderName());
                    }

                    if (m.isForwarded()) {
                        forwardedLabelTv.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(m.getForwardedFrom())) {
                            if (m.getSenderId().equals(currentUid)) {
                                forwardedLabelTv.setText("Forward You");
                            } else {
                                forwardedLabelTv.setText("Forwarded from " + m.getForwardedFrom());
                            }
                        } else {
                            forwardedLabelTv.setText("Forwarded");
                        }

                    } else {
                        forwardedLabelTv.setVisibility(View.GONE);
                    }
                    break;

                case "PPT":
                    recvFileLayout.setVisibility(View.VISIBLE);
                    recvFileName.setText(m.getFileName() != null ? m.getFileName() : "File");
                    setFileIcon(m.getFileName(), recvFileIcon);
                    recvFileLayout.setOnClickListener(v -> openFile(m.getFileUrl(), m.getFileName()));
                    if (!TextUtils.isEmpty(m.getNote())) {
                        recvText.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.VISIBLE);
                        recvText.setText(m.getNote());
                        senderName.setText(m.getSenderName());
                    }

                    if (m.isForwarded()) {
                        forwardedLabelTv.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(m.getForwardedFrom())) {
                            if (m.getSenderId().equals(currentUid)) {
                                forwardedLabelTv.setText("Forward You");
                            } else {
                                forwardedLabelTv.setText("Forwarded from " + m.getForwardedFrom());
                            }
                        } else {
                            forwardedLabelTv.setText("Forwarded");
                        }

                    } else {
                        forwardedLabelTv.setVisibility(View.GONE);
                    }

                    break;

                case "TEXT":
                default:
                    recvText.setVisibility(View.VISIBLE);

                    recvText.setText(m.getNote() != null ? m.getNote() : "");
                    senderName.setVisibility(View.VISIBLE);
                    senderName.setText(m.getSenderName());

                    Log.e("SENDER_NAME", m.getSenderName());

                    if (m.isForwarded()) {
                        forwardedLabelTv.setVisibility(View.VISIBLE);
                        senderName.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(m.getForwardedFrom())) {
                            if (m.getSenderId().equals(currentUid)) {
                                forwardedLabelTv.setText("Forward You");
                            } else {
                                forwardedLabelTv.setText("Forwarded from " + m.getForwardedFrom());
                            }
                        } else {
                            forwardedLabelTv.setText("Forwarded");
                        }

                    } else {
                        forwardedLabelTv.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }
}

