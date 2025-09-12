package com.example.notesappusingfirebase.Activities;
//
//import android.annotation.SuppressLint;
//import android.app.AlertDialog;
//import android.app.DownloadManager;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.database.Cursor;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.icu.text.SimpleDateFormat;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.OpenableColumns;
//import android.text.Editable;
//import android.text.SpannableString;
//import android.text.Spanned;
//import android.text.TextPaint;
//import android.text.TextWatcher;
//import android.text.method.LinkMovementMethod;
//import android.text.style.ClickableSpan;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.Window;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.LinearLayoutManager;
//
//import com.example.notesappusingfirebase.Chat.ChatAdapter;
//import com.example.notesappusingfirebase.Chat.ImagePreviewActivity;
//import com.example.notesappusingfirebase.Chat.SwipeReplyHelper;
//import com.example.notesappusingfirebase.Model.RoomNoteMemberModel;
//import com.example.notesappusingfirebase.R;
//import com.example.notesappusingfirebase.databinding.ActivityOpenRoomBinding;
//import com.github.barteksc.pdfviewer.PDFView;
//import com.google.android.gms.tasks.Continuation;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.material.bottomsheet.BottomSheetDialog;
//import com.google.android.material.textfield.TextInputLayout;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.OnProgressListener;
//import com.google.firebase.storage.StorageReference;
//import com.google.firebase.storage.UploadTask;
//import com.squareup.picasso.Picasso;
//
//import org.apache.poi.hslf.usermodel.HSLFShape;
//import org.apache.poi.hslf.usermodel.HSLFSlide;
//import org.apache.poi.hslf.usermodel.HSLFSlideShow;
//import org.apache.poi.hslf.usermodel.HSLFTextShape;
//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.hwpf.extractor.WordExtractor;
//import org.apache.poi.xslf.usermodel.XMLSlideShow;
//import org.apache.poi.xslf.usermodel.XSLFShape;
//import org.apache.poi.xslf.usermodel.XSLFSlide;
//import org.apache.poi.xslf.usermodel.XSLFTextShape;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//import org.apache.poi.xwpf.usermodel.XWPFParagraph;
//
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Objects;
//
//public class OpenRoomActivity extends AppCompatActivity {
//
//    ActivityOpenRoomBinding binding;
//
//    String roomName, address, adminId, name, email, currentUid, imageUrl, userId, status;
//    DatabaseReference userAccountRef, roomListRef, roomRef, membersRef, adminReqRef;
//    StorageReference storageReference;
//    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
//
//    //init/setup progressDialog to show, while adding/updating Ads
//    private ProgressDialog progressDialog;
//
//    ChatAdapter chatAdapter;
//    List<RoomNoteMemberModel> messageList = new ArrayList<>();
//
//    RoomNoteMemberModel replyingTo = null;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//
//        binding = ActivityOpenRoomBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        // If you used WindowCompat.setDecorFitsSystemWindows(...) elsewhere, ensure it's set to true here:
//        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            roomName = bundle.getString("roomName");
//            adminId = bundle.getString("adminId");
//            address = bundle.getString("address");
//
//            binding.roomNameTv.setText(roomName);
//
//            Log.e("VALUES", roomName + "\n" + adminId + "\n" + address);
//        } else {
//            Toast.makeText(this, "Room Value Missing", Toast.LENGTH_SHORT).show();
//        }
//
//        Window window = getWindow();
//        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
//
//        storageReference = firebaseStorage.getReference("NoteFiles");
//        roomListRef = firebaseDatabase.getReference("NoteList").child(address);
//
//        // if the RecyclerViewAdapter changes can't affect to the size of the RecyclerView
//        binding.recyclerView.setHasFixedSize(true);
//        //set layout to RecyclerView
//        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//
//        //init/setup progressDialog to show, while adding/updating Ads
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Please Wait..");
//        progressDialog.setCanceledOnTouchOutside(false);
//
//        String text = "You are no longer a member of this room.Why..?";
//        SpannableString spannableString = new SpannableString(text);
//
//// Define the range of "Click Here"
//        int start = text.indexOf("Why..?");
//        int end = start + "Why..?".length();
//
//// Make it clickable
//        ClickableSpan clickableSpan = new ClickableSpan() {
//            @Override
//            public void onClick(@NonNull View widget) {
//                // 👇 Action when user clicks "Click Here"
//                showRemovedPopup();
//            }
//
//            @Override
//            public void updateDrawState(@NonNull TextPaint ds) {
//                super.updateDrawState(ds);
//                ds.setColor(Color.parseColor("#6750A3")); // highlight color
//                ds.setUnderlineText(true); // show underline like a link
//                ds.setFakeBoldText(true);
//            }
//        };
//
//        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//// Apply to TextView
//        binding.blockedLayout.setText(spannableString);
//        binding.blockedLayout.setMovementMethod(LinkMovementMethod.getInstance());
//        binding.blockedLayout.setHighlightColor(Color.TRANSPARENT);
//
//
//        currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
//
//        //creating "UserName" reference in Firebase Database
//        userAccountRef = FirebaseDatabase.getInstance().getReference().child("NotesUserAccounts");
//        adminReqRef = FirebaseDatabase.getInstance().getReference("AdminRequests");
//        roomRef = firebaseDatabase.getReference("Rooms").child(currentUid);
//        membersRef = firebaseDatabase.getReference("RoomMembers");
//
//
//        if (adminId.equals(currentUid)) {
//            binding.addMemberBtn.setText("Sent");
//            status = "add";
//        } else {
//            binding.addMemberBtn.setText("Quit");
//            status = "leave";
//        }
//
//
//        userAccountRef.child(Objects.requireNonNull(firebaseAuth.getUid())).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                if (snapshot.exists()) {
//                    //get User Info, spelling should be as in Firebase realtime database
//                    email = "" + snapshot.child("email").getValue();
//                    name = "" + snapshot.child("name").getValue();
//                    imageUrl = (String) snapshot.child("profile").getValue();
//                    userId = (String) snapshot.child("userId").getValue();
//
////                    binding.nameTv.setText(name);
////                    binding.emailTv.setText(email);
////                    Glide.with(mContext).load(imageUrl).into(binding.userProfileIv);
//
////                    try {
////
////                        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
////
////                        Glide.with(mContext)
////                                .load(imageUrl)
////                                .placeholder(R.drawable.person)
////                                .apply(requestOptions)
////                                .listener(new RequestListener<Drawable>() {
////                                    @Override
////                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
////                                        Log.e("GlideError", "Load failed", e);
////                                        return false;
////                                    }
////
////                                    @Override
////                                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
////                                        return false;
////                                    }
////                                })
////                                .into(binding.userProfileIv);
////
////                    } catch (Exception e) {
////                        Log.e("TAG", Objects.requireNonNull(e.getMessage()));
////                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(OpenRoomActivity.this, "Fetching Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        binding.addMemberBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (status.equals("add")) {
//
//                    Intent intent = new Intent(OpenRoomActivity.this, AddMembersActivity.class);
//                    intent.putExtra("roomName", roomName);
//                    intent.putExtra("adminId", adminId);
//                    intent.putExtra("address", address);
//                    startActivity(intent);
//
//                } else if (status.equals("leave")) {
//
//                    roomRef.child(address).removeValue();
//                    membersRef.child(address).child(currentUid).removeValue();
//                    adminReqRef.child(adminId).child(address).child(currentUid).child("status").setValue("Self Quit");
//                    adminReqRef.child(adminId).child(address).child(currentUid).child("name").setValue(name);
//                    adminReqRef.child(adminId).child(address).child(currentUid).child("email").setValue(email);
//                    adminReqRef.child(adminId).child(address).child(currentUid).child("profile").setValue(imageUrl);
//                    adminReqRef.child(adminId).child(address).child(currentUid).child("userId").setValue(userId);
//                    adminReqRef.child(adminId).child(address).child(currentUid).child("timestamp").setValue("Leave On: " + new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
//
//                    getOnBackPressedDispatcher().onBackPressed();
//                }
//            }
//        });
//
//        binding.titleContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(OpenRoomActivity.this, ShowMembersActivity.class);
//                intent.putExtra("address", address);
//                intent.putExtra("adminId", adminId);
//                startActivity(intent);
//            }
//        });
//
//
//        binding.sendMessageBtn.setEnabled(false);
//        binding.sendMessageBtn.setVisibility(View.GONE);
//
//        // Inside onCreate or after binding views
//        binding.messageInput.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // Not needed
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // Enable/disable button based on text
//                if (s.toString().trim().isEmpty()) {
//                    binding.sendMessageBtn.setEnabled(false);
//                    binding.sendMessageBtn.setVisibility(View.GONE); // hide button if empty
//                } else {
//                    binding.sendMessageBtn.setEnabled(true);
//                    binding.sendMessageBtn.setVisibility(View.VISIBLE); // show button when typing
//
//
//                    binding.sendMessageBtn.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//
//                            String textMessage = binding.messageInput.getText().toString().trim();
////                            RoomNoteMemberModel roomNoteMemberModel = new RoomNoteMemberModel();
////
////                            roomNoteMemberModel.setSenderName(name);
////                            roomNoteMemberModel.setSenderUId(userId);
////                            roomNoteMemberModel.setNote(textMessage);
////                            roomNoteMemberModel.setSearch(textMessage.toLowerCase());
////                            roomNoteMemberModel.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
////                            roomNoteMemberModel.setType("TEXT");
////
////                            String key = roomListRef.push().getKey();
////                            roomListRef.child(key).setValue(roomNoteMemberModel).addOnCompleteListener(new OnCompleteListener<Void>() {
////                                @Override
////                                public void onComplete(@NonNull Task<Void> task) {
////
////                                    if (task.isSuccessful()) {
////                                        Toast.makeText(OpenRoomActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
////                                    }
////                                }
////                            });
////                            binding.messageInput.setText("");
//
//
//                            RoomNoteMemberModel msg = new RoomNoteMemberModel();
//                            msg.setSenderName(name);
//                            msg.setSenderUId(currentUid);
//                            msg.setNote(textMessage);
//                            msg.setType("TEXT");
//                            msg.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
//
//                            if (replyingTo != null && replyingTo.getMessageId() != null) {
//                                // add reply fields
//                                // store under: replyToId and replyToText (or nested object)
//                                // Because model has no dedicated fields, write them to DB as children manually:
//                                Map<String,Object> map = new HashMap<>();
//                                map.put("senderName", msg.getSenderName());
//                                map.put("senderUId", msg.getSenderUId());
//                                map.put("note", msg.getNote());
//                                map.put("type", msg.getType());
//                                map.put("time", msg.getTime());
//                                map.put("replyToId", replyingTo.getMessageId());
//                                map.put("replyToText", replyingTo.getNote()!=null?replyingTo.getNote():replyingTo.getFileName());
//                                String key = roomListRef.push().getKey();
//                                if (key!=null) roomListRef.child(key).setValue(map);
//                                // reset reply UI:
//                                replyingTo = null;
//                                binding.replyPreview.setVisibility(View.GONE);
//                            } else {
//                                String key = roomListRef.push().getKey();
//                                if (key!=null) {
//                                    msg.setMessageId(key);
//                                    roomListRef.child(key).setValue(msg);
//                                }
//                            }
//
//                            binding.messageInput.setText("");
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                // Not needed
//            }
//        });
//
//        binding.addAttachmentBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openBottomSheet();
//            }
//        });
//
//        binding.replyCloseIv.setOnClickListener(v -> {
//        replyingTo = null;
//            binding.replyPreview.setVisibility(View.GONE);
//        });
//
//        checkMembershipBeforeOpenChat();
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        ChatAdapter.Callback cb = new ChatAdapter.Callback() {
//            @Override
//            public void onReplyRequested(RoomNoteMemberModel message) {
//                // show reply UI in your message composer
//                replyingTo = message;
//                binding.replySenderTv.setText(message.getSenderName());
//                Picasso.get().load(message.getFileUrl()).into(binding.replyPreviewImg);
//                String preview = message.getNote();
//                if (preview == null) preview = message.getFileName() != null ? message.getFileName() : "";
//                binding.replyTextTv.setText(preview.length() > 80 ? preview.substring(0,80) + "..." : preview);
//                binding.replyPreview.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onDeleteRequested(RoomNoteMemberModel message) {
//                // delete from Firebase using message.getMessageId()
//                if (message.getMessageId()!=null) {
//                    roomListRef.child(message.getMessageId()).removeValue();
//                }
//            }
//
//            @Override
//            public void onReact(RoomNoteMemberModel message, String reaction) {
//                // save reaction to message node (e.g. roomListRef.child(id).child("reaction").setValue(reaction))
//                if (message.getMessageId()!=null) {
//                    roomListRef.child(message.getMessageId()).child("reaction").setValue(reaction);
//                }
//            }
//
//            @Override
//            public void onImageClicked(RoomNoteMemberModel message, ImageView imageView) {
//                // open full screen viewer
//                Intent i = new Intent(OpenRoomActivity.this, ImagePreviewActivity.class);
//                i.putExtra("imageUrl", message.getIamgeUrl());
//                startActivity(i);
//            }
//        };
//
//        chatAdapter = new ChatAdapter(this, messageList, currentUid,address,cb);
//        binding.recyclerView.setAdapter(chatAdapter);
//
//// Attach swipe-to-reply
//        SwipeReplyHelper.ReplyListener replyListener = pos -> {
//            RoomNoteMemberModel m = messageList.get(pos);
//            // open your reply composer with m.note preview
//        };
//        ItemTouchHelper helper = new ItemTouchHelper(new SwipeReplyHelper(chatAdapter, replyListener));
//        helper.attachToRecyclerView(binding.recyclerView);
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   roomListRef.addChildEventListener(new ChildEventListener() {
//       @Override
//       public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//           // check "DeletedFor/<address>/<messageId>/<currentUid>"
//           DatabaseReference delRef = FirebaseDatabase.getInstance()
//                   .getReference("DeletedFor").child(address).child(snapshot.getKey()).child(currentUid);
//
//           delRef.addListenerForSingleValueEvent(new ValueEventListener() {
//               @Override
//               public void onDataChange(@NonNull DataSnapshot delSnap) {
//
//                   if (delSnap.exists() && delSnap.getValue(Boolean.class) != null && delSnap.getValue(Boolean.class)) {
//                       // skip for this user
//                       return;
//                   }
//
//                   RoomNoteMemberModel model = snapshot.getValue(RoomNoteMemberModel.class);
//                   if (model == null) return;
//                   model.setMessageId(snapshot.getKey());
//
//                   // read optional replyToText and reaction
//                   if (snapshot.child("replyToText").exists()) model.setSearch(snapshot.child("replyToText").getValue(String.class)); // re-use 'search' for preview if you want
//                   // or add new getters/setters to model for replyToId/replyToText/reaction
//
//                   messageList.add(model);
//                   chatAdapter.notifyItemInserted(messageList.size()-1);
//                   binding.recyclerView.scrollToPosition(messageList.size()-1);
//
//
//
//               }
//
//               @Override
//               public void onCancelled(@NonNull DatabaseError error) {
//
//               }
//           });
//       }
//
//       @Override
//       public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//       }
//
//       @Override
//       public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//       }
//
//       @Override
//       public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//       }
//
//       @Override
//       public void onCancelled(@NonNull DatabaseError error) {
//
//       }
//   });
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        loadMessages();
//
//    }
//
//    private void openBottomSheet() {
//
//        // Inflate your custom layout
//        View sheetView = LayoutInflater.from(OpenRoomActivity.this).inflate(R.layout.add_file_bs, null);
//
//        LinearLayout optImage, optPPT, optDoc, optPDF;
//
//        optImage = sheetView.findViewById(R.id.optImage);
//        optPPT = sheetView.findViewById(R.id.optPPT);
//        optDoc = sheetView.findViewById(R.id.optDoc);
//        optPDF = sheetView.findViewById(R.id.optPDF);
//
//        // Create BottomSheetDialog
//        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(OpenRoomActivity.this, R.style.BottomSheetDialogTheme);
//        bottomSheetDialog.setContentView(sheetView);
//        bottomSheetDialog.show();
//
//        optImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(Intent.createChooser(intent, "Select Pictures"), 1);
//                bottomSheetDialog.dismiss();
//
//                Log.e("INTENT_VALUES", name + address + roomName);
//
//            }
//        });
//
//        optPDF.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent();
//                intent.setType("application/pdf");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(intent, 2);
//                bottomSheetDialog.dismiss();
//            }
//        });
//
//        optDoc.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*"); // accept all, then filter with MIME types
//                String[] mimeTypes = {
//                        "application/msword", // .doc
//                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
//                };
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(Intent.createChooser(intent, "Select Word Document"), 3);
//                bottomSheetDialog.dismiss();
//
//            }
//        });
//
//        optPPT.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
//                        "application/vnd.ms-powerpoint",                     // .ppt
//                        "application/vnd.openxmlformats-officedocument.presentationml.presentation" // .pptx
//                });
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(Intent.createChooser(intent, "Select PPT File"), 4);
//                bottomSheetDialog.dismiss();
//            }
//        });
//
//    }
//
//    private void checkMembershipBeforeOpenChat() {
//
//        DatabaseReference memberCheckRef = FirebaseDatabase.getInstance()
//                .getReference("RoomMembers")
//                .child(address)
//                .child(currentUid);
//
//        memberCheckRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                if (!snapshot.exists()) {
//                    // ❌ User not a member anymore
//                    blockChatAccess();
//                }
//            }
//
//            private void blockChatAccess() {
//
//                binding.messageBar.setVisibility(View.GONE);
//                binding.messageInput.setEnabled(false);
//                binding.sendMessageBtn.setEnabled(false);
//                // Optional: hide keyboard
//                binding.messageInput.clearFocus();
//
//                // Optional UI feedback
//                //   Toast.makeText(OpenRoomActivity.this, "You are no longer a member of this room", Toast.LENGTH_LONG).show();
//
//                // Disable RecyclerView
//                binding.recyclerView.setVisibility(View.VISIBLE);
//                // Show blocked message UI
//                binding.blockedLayout.setVisibility(View.VISIBLE);
//            }
//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });
//    }
//
//    private void showRemovedPopup() {
//
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_removed_by_admin, null);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.PopupAnimation);
//        builder.setView(dialogView);
//        builder.setCancelable(true);   // user MUST acknowledge
//
//        AlertDialog dialog = builder.create();
//        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        dialog.show();
//
//        Button okBtn = dialogView.findViewById(R.id.okBtn);
//        okBtn.setOnClickListener(v -> {
//            dialog.dismiss();
//            //  finish();  // close chat screen
//        });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK && data != null) {
//            if (requestCode == 2) {
//                pdfPreview(data.getData());
//            } else if (requestCode == 3) {
//                docPreview(data.getData());
//                Log.e("DOC_URI ", String.valueOf(data.getData()));
//            } else if (requestCode == 4) {
//                pptPreview(data.getData());
//            } else if (requestCode == 1) {
//                ArrayList<Uri> imageUris = new ArrayList<>();
//
//                if (data.getClipData() != null) {
//                    int count = data.getClipData().getItemCount();
//                    for (int i = 0; i < count; i++) {
//                        Uri uri = data.getClipData().getItemAt(i).getUri();
//                        imageUris.add(uri);
//                    }
//                } else if (data.getData() != null) {
//                    Uri uri = data.getData();
//                    imageUris.add(uri);
//                }
//
//                if (!imageUris.isEmpty()) {
//                    Intent intent = new Intent(OpenRoomActivity.this, MultipImageUploadActivity.class);
//                    intent.putExtra("address", address);
//                    intent.putExtra("name", name);
//                    intent.putExtra("roomName", roomName);
//                    intent.putParcelableArrayListExtra("imageUris", imageUris); // ✅ send all URIs
//                    startActivity(intent);
//                }
//            }
//        } else {
//            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void pdfPreview(Uri uri) {
//
//        String actualPdfName = getFileName(uri);
//
//        LayoutInflater inflater = LayoutInflater.from(OpenRoomActivity.this);
//        View view = inflater.inflate(R.layout.pdf_preview_dialog, null);
//
//        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(OpenRoomActivity.this);
//        builder.setCancelable(true);
//        builder.setView(view);
//
//        TextView pdfFileNameTv = view.findViewById(R.id.pdfFileNameTv);
//        PDFView pdfView = view.findViewById(R.id.pdfView);
//        TextInputLayout pdfCaptionTil = view.findViewById(R.id.pdfCaptionTil);
//        EditText pdfCaptionEt = view.findViewById(R.id.pdfCaptionEt);
//
//        androidx.appcompat.app.AlertDialog dialog = builder.create();
//
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().setBackgroundDrawableResource(R.drawable.preview_bgg);
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
//        dialog.show();
//
//        //changing start icon of TextInputLayout, when password recovery email sent successfully
//        pdfCaptionTil.setEndIconDrawable(R.drawable.baseline_send_24);
//        pdfCaptionTil.setEndIconCheckable(true);
//        //now visible TextInputLayout Icon
//        pdfCaptionTil.setEndIconVisible(true);
//
//        pdfCaptionTil.setEndIconOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                StorageReference storageRef = storageReference.child(System.currentTimeMillis() + "." + "pdf");
//                UploadTask uploadTask = storageRef.putFile(uri);
//
//                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                        //calculate the current progress of the image being uploaded
//                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
//
//                        //setup progress dialog message on basis of current progress. e.g Uploading 1 of the 10 images.. progress 95%
//                        String message = "Uploading PDF...\nProgress " + (int) progress + "%";
//
//                        //show progress
//                        progressDialog.setMessage(message);
//                        progressDialog.show();
//                    }
//                });
//
//                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                            @Override
//                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//
//                                if (!task.isSuccessful()) {
//                                    throw task.getException();
//                                }
//                                //Continue with the task to get the download url
//                                return storageRef.getDownloadUrl();
//                            }
//                        })
//                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Uri> task) {
//
//                                if (task.isSuccessful()) {
//
//                                    String pdfName = pdfCaptionEt.getText().toString().trim();
//
//                                    // If user entered nothing, set default name
//                                    if (pdfName.isEmpty()) {
//                                        pdfName = "PDF"; // actual name
//                                    }
//
//                                    Uri downloadUri = task.getResult();
//
//                                    RoomNoteMemberModel roomNoteMemberModel = new RoomNoteMemberModel();
//
//                                    roomNoteMemberModel.setSenderName(name);
//                                    roomNoteMemberModel.setSenderUId(userId);
//                                    roomNoteMemberModel.setNote(pdfName);
//                                    roomNoteMemberModel.setFileName(actualPdfName);
//                                    roomNoteMemberModel.setSearch(pdfName.toLowerCase());
//                                    roomNoteMemberModel.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
//                                    roomNoteMemberModel.setFileUrl(String.valueOf(downloadUri));
//                                    roomNoteMemberModel.setType("PDF");
//
//                                    String key = roomListRef.push().getKey();
//                                    roomListRef.child(key).setValue(roomNoteMemberModel).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//
//                                            if (task.isSuccessful()) {
//                                                Toast.makeText(OpenRoomActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
//                                                progressDialog.dismiss();
//                                            }
//                                        }
//                                    });
//                                } else {
//                                    //handle failures
//                                    //....
//                                }
//                            }
//                        });
//                dialog.dismiss();
//            }
//        });
//
//        pdfView.fromUri(uri)
//                .enableSwipe(true)
//                .showPageWithAnimation(true)
//                .showPageWithAnimation(true)
//                .enableSwipe(true)
//                .enableAnnotationRendering(true)
//                .enableDoubletap(true)
//                .showMinimap(true)
//                .swipeVertical(false)
//                .load();
//
//
//        pdfFileNameTv.setText("File Name: " + actualPdfName);
//        Toast.makeText(this, "PDF Name: " + actualPdfName, Toast.LENGTH_SHORT).show();
//    }
//
//    @SuppressLint("Range")
//    private String getFileName(Uri uri) {
//        String result = null;
//
//        // If the URI scheme is "content"
//        if (uri.getScheme().equals("content")) {
//            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//            try {
//                if (cursor != null && cursor.moveToFirst()) {
//                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
//                }
//            } finally {
//                if (cursor != null) {
//                    cursor.close();
//                }
//            }
//        }
//        // If the URI scheme is "file"
//        if (result == null) {
//            result = uri.getLastPathSegment();
//        }
//        return result;
//    }
//
//
//    private String readDocx(Uri uri) {
//        StringBuilder text = new StringBuilder();
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(uri);
//            XWPFDocument document = new XWPFDocument(inputStream);
//
//            for (XWPFParagraph para : document.getParagraphs()) {
//                text.append(para.getText()).append("\n");
//            }
//
//            inputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return text.toString();
//    }
//
//    private String readDoc(Uri uri) {
//        StringBuilder text = new StringBuilder();
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(uri);
//            HWPFDocument doc = new HWPFDocument(inputStream);
//            WordExtractor extractor = new WordExtractor(doc);
//
//            String[] paragraphs = extractor.getParagraphText();
//            for (String para : paragraphs) {
//                text.append(para).append("\n");
//            }
//
//            inputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return text.toString();
//    }
//
//    private void docPreview(Uri uri) {
//
//        String fileName = "";
//
//        LayoutInflater inflater = LayoutInflater.from(OpenRoomActivity.this);
//        View view = inflater.inflate(R.layout.doc_preview_dialog, null);
//
//        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(OpenRoomActivity.this);
//        builder.setCancelable(true);
//        builder.setView(view);
//
//        TextView docContent, docName, docSize;
//        docContent = view.findViewById(R.id.docContentTv);
//        docName = view.findViewById(R.id.docName);
//        docSize = view.findViewById(R.id.docSize);
//        TextInputLayout docCaptionTil = view.findViewById(R.id.docCaptionTil);
//        EditText docCaptionEt = view.findViewById(R.id.docCaptionEt);
//
//        //changing start icon of TextInputLayout, when password recovery email sent successfully
//        docCaptionTil.setEndIconDrawable(R.drawable.baseline_send_24);
//        docCaptionTil.setEndIconCheckable(true);
//        //now visible TextInputLayout Icon
//        docCaptionTil.setEndIconVisible(true);
//
//        androidx.appcompat.app.AlertDialog dialog = builder.create();
//
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().setBackgroundDrawableResource(R.drawable.preview_bgg);
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
//
//
//        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//        if (cursor != null && cursor.moveToFirst()) {
//            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
//
//            fileName = cursor.getString(nameIndex);
//            long fileSize = cursor.getLong(sizeIndex);
//
//            // Convert bytes to MB
//            double fileSizeInMB = (double) fileSize / (1024 * 1024);
//
//            if (fileSize < 1024 * 1024) {
//                docSize.setText((fileSize / 1024) + " KB");
//            } else {
//                docName.setText(String.format("%.2f MB", fileSizeInMB));
//            }
//
//            docName.setText(fileName);
//            docSize.setText(String.format(Locale.getDefault(), "%.2f MB", fileSizeInMB));
//
//            cursor.close();
//        } else {
//            docName.setText("Unknown Document");
//            docSize.setText("Unknown Size");
//        }
//
//
//        String finalFileName = fileName;
//        docCaptionTil.setEndIconOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                StorageReference storageRef = storageReference.child(System.currentTimeMillis() + "." + "doc");
//                UploadTask uploadTask = storageRef.putFile(uri);
//
//                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                        //calculate the current progress of the image being uploaded
//                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
//
//                        //setup progress dialog message on basis of current progress. e.g Uploading 1 of the 10 images.. progress 95%
//                        String message = "Uploading Document...\nProgress " + (int) progress + "%";
//
//                        //show progress
//                        progressDialog.setMessage(message);
//                        progressDialog.show();
//                    }
//                });
//
//                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                            @Override
//                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//
//                                if (!task.isSuccessful()) {
//                                    throw task.getException();
//                                }
//                                //Continue with the task to get the download url
//                                return storageRef.getDownloadUrl();
//                            }
//                        })
//                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Uri> task) {
//
//                                if (task.isSuccessful()) {
//
//                                    String docName = docCaptionEt.getText().toString().trim();
//
//                                    // If user entered nothing, set default name
//                                    if (docName.isEmpty()) {
//                                        docName = "DOC"; // actual name
//                                    }
//
//                                    Uri downloadUri = task.getResult();
//
//                                    RoomNoteMemberModel roomNoteMemberModel = new RoomNoteMemberModel();
//
//                                    roomNoteMemberModel.setSenderName(name);
//                                    roomNoteMemberModel.setSenderUId(userId);
//                                    roomNoteMemberModel.setNote(docName);
//                                    roomNoteMemberModel.setFileName(finalFileName);
//                                    roomNoteMemberModel.setSearch(docName.toLowerCase());
//                                    roomNoteMemberModel.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
//                                    roomNoteMemberModel.setFileUrl(String.valueOf(downloadUri));
//                                    roomNoteMemberModel.setType("DOC");
//
//                                    String key = roomListRef.push().getKey();
//                                    roomListRef.child(key).setValue(roomNoteMemberModel).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//
//                                            if (task.isSuccessful()) {
//                                                Toast.makeText(OpenRoomActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
//                                                progressDialog.dismiss();
//                                            }
//                                        }
//                                    });
//                                } else {
//                                    //handle failures
//                                    //....
//                                }
//                            }
//                        });
//                dialog.dismiss();
//            }
//        });
//
//        Log.e("DOC_URI", uri.toString());
//
//        String text;
//        if (uri.toString().endsWith(".docx")) {
//            text = readDocx(uri);
//        } else {
//            text = readDoc(uri);
//        }
//        docContent.setText(text);
//        dialog.show();
//    }
//
//    private String readPpt(Uri uri) {
//        StringBuilder text = new StringBuilder();
//        try {
//            // Open input stream from content:// URI
//            InputStream inputStream = getContentResolver().openInputStream(uri);
//
//            // Use HSLF for .ppt files
//            HSLFSlideShow ppt = new HSLFSlideShow(inputStream);
//            List<HSLFSlide> slides = ppt.getSlides();
//
//            for (HSLFSlide slide : slides) {
//                List<HSLFShape> shapes = slide.getShapes();
//                for (HSLFShape shape : shapes) {
//                    if (shape instanceof HSLFTextShape) {
//                        HSLFTextShape textShape = (HSLFTextShape) shape;
//                        text.append(textShape.getText()).append("\n");
//                    }
//                }
//            }
//
//            inputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return text.toString();
//    }
//
//    private String readPptx(Uri uri) {
//        StringBuilder text = new StringBuilder();
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(uri);
//
//            XMLSlideShow pptx = new XMLSlideShow(inputStream);
//            List<XSLFSlide> slides = pptx.getSlides();
//
//            for (XSLFSlide slide : slides) {
//                for (XSLFShape shape : slide.getShapes()) {
//                    if (shape instanceof XSLFTextShape) {
//                        XSLFTextShape textShape = (XSLFTextShape) shape;
//                        text.append(textShape.getText()).append("\n");
//                    }
//                }
//            }
//
//            inputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return text.toString();
//    }
//
//    private void pptPreview(Uri uri) {
//
//        String fileName = "";
//
//        LayoutInflater inflater = LayoutInflater.from(OpenRoomActivity.this);
//        View view = inflater.inflate(R.layout.ppt_preview_dialog, null);
//
//        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(OpenRoomActivity.this);
//        builder.setCancelable(true);
//        builder.setView(view);
//
//
//        TextView pptContent, pptName, pptSize;
////        pptContent = view.findViewById(R.id.pptContentTv);
//        pptName = view.findViewById(R.id.pptName);
//        pptSize = view.findViewById(R.id.pptSize);
//        TextInputLayout pptCaptionTil = view.findViewById(R.id.pptCaptionTil);
//        EditText pptCaptionEt = view.findViewById(R.id.pptCaptionEt);
//
//        //changing start icon of TextInputLayout, when password recovery email sent successfully
//        pptCaptionTil.setEndIconDrawable(R.drawable.baseline_send_24);
//        pptCaptionTil.setEndIconCheckable(true);
//        //now visible TextInputLayout Icon
//        pptCaptionTil.setEndIconVisible(true);
//
//
//        androidx.appcompat.app.AlertDialog dialog = builder.create();
//
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().setBackgroundDrawableResource(R.drawable.preview_bgg);
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
//
//
//        // File name & size
//        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//        if (cursor != null && cursor.moveToFirst()) {
//            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
//
//            fileName = cursor.getString(nameIndex);
//            long fileSize = cursor.getLong(sizeIndex);
//            double fileSizeInMB = (double) fileSize / (1024 * 1024);
//
//            pptName.setText(fileName);
//            pptSize.setText(String.format(Locale.getDefault(), "%.2f MB", fileSizeInMB));
//            cursor.close();
//
//            // Preview text depending on extension
//            String previewText;
//            if (fileName.endsWith(".pptx")) {
//                previewText = readPptx(uri);
//            } else {
//                previewText = readPpt(uri);
//            }
//            //content.setText(previewText);
//        }
//
//        String finalFileName = fileName;
//        pptCaptionTil.setEndIconOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                StorageReference storageRef = storageReference.child(System.currentTimeMillis() + "." + "ppt");
//                UploadTask uploadTask = storageRef.putFile(uri);
//
//                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
//                        //calculate the current progress of the image being uploaded
//                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
//
//                        //setup progress dialog message on basis of current progress. e.g Uploading 1 of the 10 images.. progress 95%
//                        String message = "Uploading PPT...\nProgress " + (int) progress + "%";
//
//                        //show progress
//                        progressDialog.setMessage(message);
//                        progressDialog.show();
//                    }
//                });
//
//                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                            @Override
//                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//
//                                if (!task.isSuccessful()) {
//                                    throw task.getException();
//                                }
//                                //Continue with the task to get the download url
//                                return storageRef.getDownloadUrl();
//                            }
//                        })
//                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Uri> task) {
//
//                                if (task.isSuccessful()) {
//
//                                    String pdfName = pptCaptionEt.getText().toString().trim();
//
//                                    // If user entered nothing, set default name
//                                    if (pdfName.isEmpty()) {
//                                        pdfName = "PPT"; // actual name
//                                    }
//
//                                    Uri downloadUri = task.getResult();
//
//                                    RoomNoteMemberModel roomNoteMemberModel = new RoomNoteMemberModel();
//
//                                    roomNoteMemberModel.setSenderName(name);
//                                    roomNoteMemberModel.setSenderUId(userId);
//                                    roomNoteMemberModel.setNote(pdfName);
//                                    roomNoteMemberModel.setFileName(finalFileName);
//                                    roomNoteMemberModel.setSearch(pdfName.toLowerCase());
//                                    roomNoteMemberModel.setTime(new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));
//                                    roomNoteMemberModel.setFileUrl(String.valueOf(downloadUri));
//                                    roomNoteMemberModel.setType("PPT");
//
//                                    String key = roomListRef.push().getKey();
//                                    roomListRef.child(key).setValue(roomNoteMemberModel).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//
//                                            if (task.isSuccessful()) {
//                                                Toast.makeText(OpenRoomActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
//                                                progressDialog.dismiss();
//                                            }
//                                        }
//                                    });
//                                } else {
//                                    //handle failures
//                                    //....
//                                }
//                            }
//                        });
//
//                dialog.dismiss();
//                Log.e("DOC_URI", uri.toString());
//            }
//        });
//        dialog.show();
//    }
//
//
//
//    private void loadMessages() {
//
//        roomListRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String prev) {
//
//                RoomNoteMemberModel model = snapshot.getValue(RoomNoteMemberModel.class);
//
//                if (model != null) {
//                    model.setMessageId(snapshot.getKey());
//                    messageList.add(model);
////                    chatAdapter.notifyItemInserted(messageList.size() - 1);
//
//                    binding.recyclerView.scrollToPosition(messageList.size() - 1);
//
//                    // Mark messages seen
//                    if (!currentUid.equals(model.getSenderUId())) {
//                        roomListRef.child(model.getMessageId()).child("seen").setValue("true");
//                    }
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String prev) {
//                RoomNoteMemberModel updated = snapshot.getValue(RoomNoteMemberModel.class);
//                if (updated == null) return;
//
//                String key = snapshot.getKey();
//
//                for (int i = 0; i < messageList.size(); i++) {
//                    if (messageList.get(i).getMessageId().equals(key)) {
//                        updated.setMessageId(key);
//                        messageList.set(i, updated);
////                        chatAdapter.notifyItemChanged(i);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                String key = snapshot.getKey();
//                for (int i = 0; i < messageList.size(); i++) {
//                    if (messageList.get(i).getMessageId().equals(key)) {
//                        messageList.remove(i);
//                        chatAdapter.notifyItemRemoved(i);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String prev) {
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//        });
//
//        binding.recyclerView.setAdapter(chatAdapter);
//
//    }
//
//
//
//}


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesappusingfirebase.Chat.ChatAdapter;
import com.example.notesappusingfirebase.Chat.ImagePreviewActivity;
import com.example.notesappusingfirebase.Model.MembersModel;
import com.example.notesappusingfirebase.Model.RoomNoteMemberModel;
import com.example.notesappusingfirebase.R;
import com.example.notesappusingfirebase.databinding.ActivityOpenRoomBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * OpenRoomActivity
 * <p>
 * - Loads messages from NoteList/<address>
 * - Skips messages marked under DeletedFor/<address>/<messageId>/<currentUid>
 * - Shows reply preview and sends messages with reply metadata
 * - Uploads single file to Firebase Storage and posts message with type = "FILE"
 * <p>
 * Make sure layout ids match those used below.
 */
public class OpenRoomActivity extends AppCompatActivity {

    private final Set<String> deletedForMe = new HashSet<>();
    ActivityOpenRoomBinding binding;
    List<RoomNoteMemberModel> messages = new ArrayList<>();
    ChatAdapter adapter;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference roomListRef, deletedForRef, roomRef, membersRef, adminReqRef, userAccountRef;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference("NoteFiles");
    String address, roomName, currentUid, senderName, status, adminId, name, email, imageUrl, userId;
    // ActivityResultLauncher for picking a file
    ActivityResultLauncher<String> pickFileLauncher;
    // message user is replying to (if any)
    RoomNoteMemberModel replyingTo = null;
    private boolean isInRoom = false;
    private ChildEventListener messageListener;
    private int roomMemberCount = 0;
    private Map<String, String> memberAvatars = new HashMap<>();
    private List<MembersModel> roomMembers = new ArrayList<>();
    private ChildEventListener deletedForListener;

    //    //init/setup progressDialog to show, while adding/updating Ads
    private ProgressDialog progressDialog;

    ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOpenRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot()); // ensure this layout exists

        // If you used WindowCompat.setDecorFitsSystemWindows(...) elsewhere, ensure it's set to true here:
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));

        //        //init/setup progressDialog to show, while adding/updating Ads
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        // hide reply preview initially
        binding.replyPreview.setVisibility(View.GONE);

        String text = "You are no longer a member of this room.Why..?";
        SpannableString spannableString = new SpannableString(text);

// Define the range of "Click Here"
        int start = text.indexOf("Why..?");
        int end = start + "Why..?".length();

// Make it clickable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // 👇 Action when user clicks "Click Here"
                showRemovedPopup();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#6750A3")); // highlight color
                ds.setUnderlineText(true); // show underline like a link
                ds.setFakeBoldText(true);
            }
        };

        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

// Apply to TextView
        binding.blockedLayout.setText(spannableString);
        binding.blockedLayout.setMovementMethod(LinkMovementMethod.getInstance());
        binding.blockedLayout.setHighlightColor(Color.TRANSPARENT);

        // get intent extras (address, roomName, senderName)
        if (getIntent() != null) {
            address = getIntent().getStringExtra("address");
            roomName = getIntent().getStringExtra("roomName");
            senderName = getIntent().getStringExtra("name");
            adminId = getIntent().getStringExtra("adminId");

            binding.roomNameTv.setText(roomName);
            Log.e("NAME", senderName + "\n" + adminId);
        }

        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Room not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        roomRef = firebaseDatabase.getReference("Rooms").child(currentUid);
        membersRef = firebaseDatabase.getReference("RoomMembers");
        adminReqRef = FirebaseDatabase.getInstance().getReference("AdminRequests");
        roomListRef = firebaseDatabase.getReference("NoteList").child(address);
        deletedForRef = firebaseDatabase.getReference("DeletedFor").child(address);
        userAccountRef = FirebaseDatabase.getInstance().getReference().child("NotesUserAccounts");

        if (adminId.equals(currentUid)) {
            binding.addMemberBtn.setText("Sent");
            status = "add";
        } else {
            binding.addMemberBtn.setText("Quit");
            status = "leave";
        }

        binding.titleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OpenRoomActivity.this, ShowMembersActivity.class);
                intent.putExtra("address", address);
                intent.putExtra("adminId", adminId);
                startActivity(intent);
            }
        });

        userAccountRef.child(Objects.requireNonNull(currentUid)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    //get User Info, spelling should be as in Firebase realtime database
                    email = "" + snapshot.child("email").getValue();
                    name = "" + snapshot.child("name").getValue();
                    imageUrl = (String) snapshot.child("profile").getValue();
                    userId = (String) snapshot.child("userId").getValue();

//                    binding.nameTv.setText(name);
//                    binding.emailTv.setText(email);
//                    Glide.with(mContext).load(imageUrl).into(binding.userProfileIv);

//                    try {
//
//                        RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);
//
//                        Glide.with(mContext)
//                                .load(imageUrl)
//                                .placeholder(R.drawable.person)
//                                .apply(requestOptions)
//                                .listener(new RequestListener<Drawable>() {
//                                    @Override
//                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
//                                        Log.e("GlideError", "Load failed", e);
//                                        return false;
//                                    }
//
//                                    @Override
//                                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
//                                        return false;
//                                    }
//                                })
//                                .into(binding.userProfileIv);
//
//                    } catch (Exception e) {
//                        Log.e("TAG", Objects.requireNonNull(e.getMessage()));
//                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OpenRoomActivity.this, "Fetching Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        loadRoomMembersCount();

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        lm.setSmoothScrollbarEnabled(true);
        lm.setOrientation(RecyclerView.VERTICAL);
        lm.setReverseLayout(false);
        binding.recyclerView.setLayoutManager(lm);

        // Send text message
        binding.sendMessageBtn.setOnClickListener(v -> {
            String txt = binding.messageInput.getText().toString().trim();
            if (txt.isEmpty()) {
                Snackbar.make(binding.recyclerView, "Enter message", Snackbar.LENGTH_SHORT).show();
                return;
            }
            sendTextMessage(txt);
            binding.messageInput.setText("");
        });

        // reply close button
        binding.replyCloseIv.setOnClickListener(v -> {
            replyingTo = null;
            binding.replyPreview.setVisibility(View.GONE);
        });

        // Attach file
        pickFileLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if (uri == null) return;
                // upload file and send message
                sendFileMessage(uri);
            }
        });

        binding.addAttachmentBtn.setOnClickListener(v -> {
            openBottomSheet();
        });


        binding.addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (status.equals("add")) {

                    Intent intent = new Intent(OpenRoomActivity.this, AddMembersActivity.class);
                    intent.putExtra("roomName", roomName);
                    intent.putExtra("adminId", adminId);
                    intent.putExtra("address", address);
                    startActivity(intent);

                } else if (status.equals("leave")) {

                    roomRef.child(address).removeValue();
                    membersRef.child(address).child(currentUid).removeValue();
                    adminReqRef.child(adminId).child(address).child(currentUid).child("status").setValue("Self Quit");
                    adminReqRef.child(adminId).child(address).child(currentUid).child("name").setValue(name);
                    adminReqRef.child(adminId).child(address).child(currentUid).child("email").setValue(email);
                    adminReqRef.child(adminId).child(address).child(currentUid).child("profile").setValue(imageUrl);
                    adminReqRef.child(adminId).child(address).child(currentUid).child("userId").setValue(userId);
                    adminReqRef.child(adminId).child(address).child(currentUid).child("timestamp").setValue("Leave On: " + new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(new Date()));

                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        ItemTouchHelper.SimpleCallback swipe = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                RoomNoteMemberModel message = messages.get(vh.getAdapterPosition());

                message.setReplyToUser(message.getSenderName());

                // show reply preview
                replyingTo = message;
                String user = message.getSenderId() != null && message.getSenderId().equals(currentUid) ? "You" : message.getReplyToUser(); // if you want to display name
                binding.replySenderTv.setText("Replied To: " + message.getReplyToUser() != null ? message.getReplyToUser() : (message.getReplyToId() != null && message.getSenderId().equals(currentUid) ? "You" : message.getReplyToUser()));
                String preview = message.getNote();
                if (TextUtils.isEmpty(preview))
                    preview = message.getFileName() != null ? message.getFileName() : "";
                binding.replyTextTv.setText(preview.length() > 120 ? preview.substring(0, 120) + "..." : preview);
                binding.replyPreview.setVisibility(View.VISIBLE);

                adapter.notifyItemChanged(vh.getAdapterPosition()); // reset swipe
            }
        };

        new ItemTouchHelper(swipe).attachToRecyclerView(binding.recyclerView);


        checkMembershipBeforeOpenChat();
        listenDeletedForMeRealtime();
        loadMessages();
        setupAdapter();
        loadRoomMembersCount();
    }

    private void showRemovedPopup() {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_removed_by_admin, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.PopupAnimation);
        builder.setView(dialogView);
        builder.setCancelable(true);   // user MUST acknowledge

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        Button okBtn = dialogView.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(v -> {
            dialog.dismiss();
            //  finish();  // close chat screen
        });
    }


    private void openBottomSheet() {

        // Inflate your custom layout
        View sheetView = LayoutInflater.from(OpenRoomActivity.this).inflate(R.layout.add_file_bs, null);

        LinearLayout optImage, optPPT, optDoc, optPDF, optOther;

        optImage = sheetView.findViewById(R.id.optImage);
        optPPT = sheetView.findViewById(R.id.optPPT);
        optDoc = sheetView.findViewById(R.id.optDoc);
        optPDF = sheetView.findViewById(R.id.optPDF);
        optOther = sheetView.findViewById(R.id.optOther);

        // Create BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(OpenRoomActivity.this, R.style.BottomSheetDialogTheme);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        optImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select Pictures"), 1);
                bottomSheetDialog.dismiss();

                Log.e("INTENT_VALUES", name + address + roomName);

            }
        });

        optPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 2);
                bottomSheetDialog.dismiss();
            }
        });

        optDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*"); // accept all, then filter with MIME types
                String[] mimeTypes = {
                        "application/msword", // .doc
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
                };
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select Word Document"), 3);
                bottomSheetDialog.dismiss();

            }
        });

        optPPT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "application/vnd.ms-powerpoint",                     // .ppt
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation" // .pptx
                });
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select PPT File"), 4);
                bottomSheetDialog.dismiss();
            }
        });

        optOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // allow any file type, user will pick single file
                pickFileLauncher.launch("*/*");
            }
        });

    }

    private void checkMembershipBeforeOpenChat() {

        DatabaseReference memberCheckRef = FirebaseDatabase.getInstance()
                .getReference("RoomMembers")
                .child(address)
                .child(currentUid);

        memberCheckRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    // ❌ User not a member anymore
                    blockChatAccess();
                }
            }

            private void blockChatAccess() {

                binding.messageBar.setVisibility(View.GONE);
                binding.messageInput.setEnabled(false);
                binding.sendMessageBtn.setEnabled(false);
                // Optional: hide keyboard
                binding.messageInput.clearFocus();

                // Optional UI feedback
                //   Toast.makeText(OpenRoomActivity.this, "You are no longer a member of this room", Toast.LENGTH_LONG).show();

                // Disable RecyclerView
                binding.recyclerView.setVisibility(View.VISIBLE);
                // Show blocked message UI
                binding.blockedLayout.setVisibility(View.VISIBLE);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void loadMessages() {

        if (messageListener != null) return;

        messageListener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String prevChildKey) {

//                RoomNoteMemberModel model = snapshot.getValue(RoomNoteMemberModel.class);
//                if (model == null) return;
//
//                model.setMessageId(snapshot.getKey());
//
//                // ❗ ONLY skip if deleted FOR ME
//                if (snapshot.child("DeletedFor")
//                        .child(currentUid)
//                        .getValue(Boolean.class) != null) {
//                    return;
//                }
//
//                adapter.addOrUpdate(model);
//                markSeenIfNeeded(model);

                String messageId = snapshot.getKey();
                if (messageId == null) return;

                // 🔥 SKIP IF DELETED FOR ME
                if (deletedForMe.contains(messageId)) return;

                RoomNoteMemberModel m = snapshot.getValue(RoomNoteMemberModel.class);
                if (m == null) return;

                m.setMessageId(messageId);

                adapter.addOrUpdate(m);
                markSeenIfNeeded(m);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String prevChildKey) {

                RoomNoteMemberModel model = snapshot.getValue(RoomNoteMemberModel.class);
                if (model == null) return;

                model.setMessageId(snapshot.getKey());

                adapter.addOrUpdate(model);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                RoomNoteMemberModel m = new RoomNoteMemberModel();

                adapter.markDeletedForMe(m);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        roomListRef.addChildEventListener(messageListener);
    }

    private void showUndoSnackbar(RoomNoteMemberModel m) {

        Snackbar.make(binding.recyclerView, "Message deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> {
                    deletedForRef.child(m.getMessageId())
                            .child(currentUid)
                            .removeValue();

                    adapter.addOrUpdate(m);
                    binding.recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                })
                .show();
    }

    private void markSeenIfNeeded(RoomNoteMemberModel m) {

        if (m.getSenderId().equals(currentUid)) return;

        if (m.getSeenBy() != null &&
                Boolean.TRUE.equals(m.getSeenBy().get(currentUid))) return;

        roomListRef.child(m.getMessageId())
                .child("seenBy")
                .child(currentUid)
                .setValue(true);
    }

    private void sendTextMessage(String text) {

        String key = roomListRef.push().getKey();
        if (key == null) {
            Toast.makeText(this, "Unable to create message key", Toast.LENGTH_SHORT).show();
            return;
        }

        long ts = new Date().getTime();

        Map<String, Object> map = new HashMap<>();
        map.put("messageId", key);
        map.put("senderId", currentUid);
        map.put("senderName", senderName);
        map.put("search", text.toLowerCase());
        map.put("type", "TEXT");
        map.put("note", text);
        map.put("timestamp", ts);

        if (replyingTo != null && replyingTo.getMessageId() != null) {
            map.put("replyToId", replyingTo.getMessageId());
            map.put("replyToText", replyingTo.getNote() != null ? replyingTo.getNote() : replyingTo.getFileName());
            map.put("replyToUser", replyingTo.getReplyToUser() != null ? replyingTo.getReplyToUser() : (replyingTo.getSenderId() != null ? replyingTo.getSenderId() : ""));
        }

        roomListRef.child(key).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // reset reply UI
                replyingTo = null;
                binding.replyPreview.setVisibility(View.GONE);
            } else {
                Toast.makeText(OpenRoomActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendFileMessage(Uri uri) {
        // determine filename from content resolver
        String fileName = queryName(uri);
        if (fileName == null) fileName = "file_" + System.currentTimeMillis();

        String storageName = System.currentTimeMillis() + "_" + fileName;
        StorageReference ref = storageReference.child(storageName);

        // upload file -> get download url -> create message node
        UploadTask uploadTask = ref.putFile(uri);
        uploadTask.addOnProgressListener(snapshot -> {
            long transferred = snapshot.getBytesTransferred();
            long total = snapshot.getTotalByteCount();
            int percent = (int) (100.0 * transferred / total);
            // optionally show progress in UI
        });

        String finalFileName = fileName;
        uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
            if (!task.isSuccessful()) throw task.getException();
            return ref.getDownloadUrl();
        }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                if (downloadUri == null) {
                    Toast.makeText(OpenRoomActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                String key = roomListRef.push().getKey();
                if (key == null) {
                    Toast.makeText(this, "Unable to create message key", Toast.LENGTH_SHORT).show();
                    return;
                }

                long ts = new Date().getTime();

                Map<String, Object> map = new HashMap<>();
                map.put("messageId", key);
                map.put("senderId", currentUid);
                map.put("senderName", senderName != null ? senderName : "");
                map.put("type", "OTHER");
                map.put("note", ""); // optional caption
                map.put("fileUrl", downloadUri.toString());
                map.put("fileName", finalFileName);
                map.put("timestamp", ts);

                if (replyingTo != null && replyingTo.getMessageId() != null) {
                    map.put("replyToId", replyingTo.getMessageId());
                    map.put("replyToText", replyingTo.getNote() != null ? replyingTo.getNote() : replyingTo.getFileName());
                    map.put("replyToUser", replyingTo.getReplyToUser() != null ? replyingTo.getReplyToUser() : (replyingTo.getSenderId() != null ? replyingTo.getSenderId() : ""));
                }

                roomListRef.child(key).setValue(map).addOnCompleteListener(t2 -> {
                    if (t2.isSuccessful()) {
                        replyingTo = null;
                        binding.replyPreview.setVisibility(View.GONE);
                        Toast.makeText(OpenRoomActivity.this, "File sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OpenRoomActivity.this, "Failed to send file", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(OpenRoomActivity.this, "Upload failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Helper to query display name (file name) from content Uri
     */
    private String queryName(Uri uri) {
        String result = null;
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        // Optionally remove listeners if you saved references to them
        detachMessageListener();

        if (messageListener != null) {
            roomListRef.removeEventListener(messageListener);
        }

        if (deletedForListener != null) {
            FirebaseDatabase.getInstance()
                    .getReference("DeletedFor")
                    .child(address)
                    .removeEventListener(deletedForListener);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        detachMessageListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInRoom = true;
        attachMessageListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInRoom = false;
    }

    private void detachMessageListener() {
        if (roomListRef != null && messageListener != null) {
            roomListRef.removeEventListener(messageListener);
            messageListener = null;
        }
    }

    private void attachMessageListener() {

        if (messageListener != null) return;

        messageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String prev) {
                RoomNoteMemberModel m = snapshot.getValue(RoomNoteMemberModel.class);
                handleMessage(snapshot, m);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String prev) {
                RoomNoteMemberModel m = snapshot.getValue(RoomNoteMemberModel.class);
                handleMessage(snapshot, m);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String prev) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        roomListRef.addChildEventListener(messageListener);
    }

    private void handleMessage(DataSnapshot snapshot, RoomNoteMemberModel m) {

        if (m == null) return;

        // sender should not mark own message seen
        if (currentUid.equals(m.getSenderId())) return;

        // already seen by this user
        if (m.getSeenBy() != null && m.getSeenBy().containsKey(currentUid)) return;

        if (!isInRoom) return;

        snapshot.getRef()
                .child("seenBy")
                .child(currentUid)
                .setValue(true);

    }

    private void loadRoomMembersCount() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("RoomMembers")
                .child(address);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomMemberCount = (int) snapshot.getChildrenCount();

                // ✅ Now adapter can be created safely
                setupAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupAdapter() {

        // RecyclerView setup
        adapter = new ChatAdapter(this, messages, address, roomMemberCount, memberAvatars, roomMembers, currentUid, new ChatAdapter.Callback() {
            @Override
            public void onDeleteRequested(RoomNoteMemberModel message) {
                // optional: show delete options (handled in adapter); here you could forward to activity-level logic
            }

            @Override
            public void onReplyRequested(RoomNoteMemberModel message) {

                message.setReplyToUser(message.getSenderName());

                // show reply preview
                replyingTo = message;
                String user = message.getSenderId() != null && message.getSenderId().equals(currentUid) ? "You" : message.getReplyToUser(); // if you want to display name
                binding.replySenderTv.setText("Replied To: " + message.getReplyToUser() != null ? message.getReplyToUser() : (message.getReplyToId() != null && message.getSenderId().equals(currentUid) ? "You" : message.getReplyToUser()));
                String preview = message.getNote();
                if (TextUtils.isEmpty(preview))
                    preview = message.getFileName() != null ? message.getFileName() : "";
                binding.replyTextTv.setText(preview.length() > 120 ? preview.substring(0, 120) + "..." : preview);
                binding.replyPreview.setVisibility(View.VISIBLE);
            }

            @Override
            public void onImageClicked(RoomNoteMemberModel message, ImageView imageView) {
                // default behaviour (or open ImagePreviewActivity). Adapter also tries to open preview
                Intent i = new Intent(OpenRoomActivity.this, ImagePreviewActivity.class);
                i.putExtra("imageUrl", message.getImageUrl() != null ? message.getImageUrl() : message.getFileUrl());
                startActivity(i);
            }


            @Override
            public void onScrollToMessage(String messageId) {
                if (TextUtils.isEmpty(messageId)) return;

                int i;
                for (i = 0; i < messages.size(); i++) {
                    if (messageId.equals(messages.get(i).getMessageId())) {
                        binding.recyclerView.scrollToPosition(i);
                        int finalI = i;
                        binding.recyclerView.post(() ->
                                Objects.requireNonNull(binding.recyclerView.findViewHolderForAdapterPosition(finalI))
                                        .itemView
                                        .setBackgroundResource(R.color.reply_highlight)
                        );
                        break;
                    }
                }

                RecyclerView.ViewHolder vh = binding.recyclerView.findViewHolderForAdapterPosition(i);

                if (vh != null) {
                    View item = vh.itemView;

                    item.setBackgroundResource(R.color.reply_highlight);

                    item.postDelayed(() -> {
                        item.setBackground(null); // ✅ THIS works
                    }, 700);
                }
            }

            @Override
            public void onDeleteForEveryone(RoomNoteMemberModel message) {
                //showUndoSnackbar(message);
                adapter.markDeletedForEveryone(message);
            }

            @Override
            public void onDeleteForMe(RoomNoteMemberModel message) {
                showUndoSnackbar(message);
                adapter.markDeletedForMe(message);
            }

            @Override
            public void onSaveRequested(RoomNoteMemberModel message) {
                saveMessage(message, false); // starred
            }

            @Override
            public void onPinRequested(RoomNoteMemberModel message) {
                saveMessage(message, true); // pinned
            }

        });
        binding.recyclerView.setAdapter(adapter);
    }

    private void saveMessage(RoomNoteMemberModel m, boolean pinned) {
        String uid = currentUid;
        String key = FirebaseDatabase.getInstance()
                .getReference("SavedItems")
                .child(uid)
                .push()
                .getKey();

        if (key == null) return;

        Map<String, Object> map = new HashMap<>();
        map.put("messageId", m.getMessageId());
        map.put("savedId", key);
        map.put("roomId", address);
        map.put("type", m.getType());
        map.put("note", m.getNote());
        map.put("fileUrl", m.getFileUrl());
        map.put("fileName", m.getFileName());
        map.put("senderName", m.getSenderName());
        map.put("timestamp", m.getTimestamp());
        map.put("pinned", pinned);
        map.put("roomName", roomName);

        FirebaseDatabase.getInstance()
                .getReference("SavedItems")
                .child(uid)
                .child(key)
                .setValue(map);

        Toast.makeText(this, pinned ? "Pinned" : "Saved", Toast.LENGTH_SHORT).show();
    }


    private void listenDeletedForMeRealtime() {

        RoomNoteMemberModel model = new RoomNoteMemberModel();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("DeletedFor")
                .child(address);

        deletedForListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String prev) {
                String messageId = snapshot.getKey();
                if (messageId == null) return;

                Boolean deleted = snapshot.child(currentUid).getValue(Boolean.class);
                if (Boolean.TRUE.equals(deleted)) {
                    deletedForMe.add(messageId);
                    adapter.markDeletedForMe(model); // 🔥 REALTIME REMOVE
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String prev) {
                onChildAdded(snapshot, prev);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String prev) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        ref.addChildEventListener(deletedForListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 2) {
                pdfPreview(data.getData());
            } else if (requestCode == 3) {
                docPreview(data.getData());
                Log.e("DOC_URI ", String.valueOf(data.getData()));
            } else if (requestCode == 4) {
                pptPreview(data.getData());
            } else if (requestCode == 1) {
                ArrayList<Uri> imageUris = new ArrayList<>();

                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        imageUris.add(uri);
                    }
                } else if (data.getData() != null) {
                    Uri uri = data.getData();
                    imageUris.add(uri);
                }

                if (!imageUris.isEmpty()) {
                    Intent intent = new Intent(OpenRoomActivity.this, MultipImageUploadActivity.class);
                    intent.putExtra("address", address);
                    intent.putExtra("name", name);
                    intent.putExtra("roomName", roomName);
                    intent.putParcelableArrayListExtra("imageUris", imageUris); // ✅ send all URIs
                    startActivity(intent);
                }
            }
        } else {
            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void pdfPreview(Uri uri) {

        // determine filename from content resolver
        String fileName = queryName(uri);

        if (fileName == null) fileName = "file_" + System.currentTimeMillis();

        LayoutInflater inflater = LayoutInflater.from(OpenRoomActivity.this);
        View view = inflater.inflate(R.layout.pdf_preview_dialog, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(OpenRoomActivity.this);
        builder.setCancelable(true);
        builder.setView(view);

        TextView pdfFileNameTv = view.findViewById(R.id.pdfFileNameTv);
        PDFView pdfView = view.findViewById(R.id.pdfView);
        TextInputLayout pdfCaptionTil = view.findViewById(R.id.pdfCaptionTil);
        EditText pdfCaptionEt = view.findViewById(R.id.pdfCaptionEt);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.preview_bgg);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();

        //changing start icon of TextInputLayout, when password recovery email sent successfully
        pdfCaptionTil.setEndIconDrawable(R.drawable.baseline_send_24);
        pdfCaptionTil.setEndIconCheckable(true);
        //now visible TextInputLayout Icon
        pdfCaptionTil.setEndIconVisible(true);

        String finalFileName = fileName;
        pdfCaptionTil.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StorageReference storageRef = storageReference.child(finalFileName + "." + "pdf");
                UploadTask uploadTask = storageRef.putFile(uri);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        //calculate the current progress of the image being uploaded
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                        //setup progress dialog message on basis of current progress. e.g Uploading 1 of the 10 images.. progress 95%
                        String message = "Uploading PDF...\nProgress " + (int) progress + "%";

                        //show progress
                        progressDialog.setMessage(message);
                        progressDialog.show();
                    }
                });

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                //Continue with the task to get the download url
                                return storageRef.getDownloadUrl();
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    String pdfName = pdfCaptionEt.getText().toString().trim();

                                    // If user entered nothing, set default name
                                    if (pdfName.isEmpty()) {
                                        pdfName = "PDF"; // actual name
                                    }

                                    Uri downloadUri = task.getResult();

                                    if (downloadUri == null) {
                                        Toast.makeText(OpenRoomActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                                        return;
                                    }


                                    String key = roomListRef.push().getKey();

                                    long ts = new Date().getTime();

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("messageId", key);
                                    map.put("senderId", currentUid);
                                    map.put("senderName", senderName != null ? senderName : "");
                                    map.put("type", "PDF");
                                    map.put("search", pdfName.toLowerCase());
                                    map.put("note", pdfName); // optional caption
                                    map.put("fileUrl", downloadUri.toString());
                                    map.put("fileName", finalFileName);
                                    map.put("timestamp", ts);

                                    if (replyingTo != null && replyingTo.getMessageId() != null) {
                                        map.put("replyToId", replyingTo.getMessageId());
                                        map.put("replyToText", replyingTo.getNote() != null ? replyingTo.getNote() : replyingTo.getFileName());
                                        map.put("replyToUser", replyingTo.getReplyToUser() != null ? replyingTo.getReplyToUser() : (replyingTo.getSenderId() != null ? replyingTo.getSenderId() : ""));
                                    }


                                    roomListRef.child(key).setValue(map).addOnCompleteListener(t2 -> {
                                        if (t2.isSuccessful()) {
                                            replyingTo = null;
                                            binding.replyPreview.setVisibility(View.GONE);
                                            Toast.makeText(OpenRoomActivity.this, "File sent", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        } else {
                                            Toast.makeText(OpenRoomActivity.this, "Failed to send file", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    });

                                } else {
                                    Toast.makeText(OpenRoomActivity.this, "Upload failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }

                            }
                        });
                dialog.dismiss();
            }
        });

        pdfView.fromUri(uri)
                .enableSwipe(true)
                .showPageWithAnimation(true)
                .showPageWithAnimation(true)
                .enableSwipe(true)
                .enableAnnotationRendering(true)
                .enableDoubletap(true)
                .showMinimap(true)
                .swipeVertical(false)
                .load();


        pdfFileNameTv.setText("File Name: " + fileName);
        Toast.makeText(this, "PDF Name: " + fileName, Toast.LENGTH_SHORT).show();
    }

    private String readDocx(Uri uri) {

        StringBuilder text = new StringBuilder();
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            XWPFDocument document = new XWPFDocument(inputStream);

            for (XWPFParagraph para : document.getParagraphs()) {
                text.append(para.getText()).append("\n");
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private String readDoc(Uri uri) {
        StringBuilder text = new StringBuilder();
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            HWPFDocument doc = new HWPFDocument(inputStream);
            WordExtractor extractor = new WordExtractor(doc);

            String[] paragraphs = extractor.getParagraphText();
            for (String para : paragraphs) {
                text.append(para).append("\n");
            }

            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private void docPreview(Uri uri) {

        String fileName = "";

        LayoutInflater inflater = LayoutInflater.from(OpenRoomActivity.this);
        View view = inflater.inflate(R.layout.doc_preview_dialog, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(OpenRoomActivity.this);
        builder.setCancelable(true);
        builder.setView(view);

        TextView docContent, docName, docSize;
        docContent = view.findViewById(R.id.docContentTv);
        docName = view.findViewById(R.id.docName);
        docSize = view.findViewById(R.id.docSize);
        TextInputLayout docCaptionTil = view.findViewById(R.id.docCaptionTil);
        EditText docCaptionEt = view.findViewById(R.id.docCaptionEt);

        //changing start icon of TextInputLayout, when password recovery email sent successfully
        docCaptionTil.setEndIconDrawable(R.drawable.baseline_send_24);
        docCaptionTil.setEndIconCheckable(true);
        //now visible TextInputLayout Icon
        docCaptionTil.setEndIconVisible(true);

        androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.preview_bgg);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;


        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

            fileName = cursor.getString(nameIndex);
            long fileSize = cursor.getLong(sizeIndex);

            // Convert bytes to MB
            double fileSizeInMB = (double) fileSize / (1024 * 1024);

            if (fileSize < 1024 * 1024) {
                docSize.setText((fileSize / 1024) + " KB");
            } else {
                docName.setText(String.format("%.2f MB", fileSizeInMB));
            }

            docName.setText(fileName);
            docSize.setText(String.format(Locale.getDefault(), "%.2f MB", fileSizeInMB));

            cursor.close();
        } else {
            docName.setText("Unknown Document");
            docSize.setText("Unknown Size");
        }


        String finalFileName = fileName;
        docCaptionTil.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StorageReference storageRef = storageReference.child(finalFileName + "." + "doc");
                UploadTask uploadTask = storageRef.putFile(uri);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        //calculate the current progress of the image being uploaded
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                        //setup progress dialog message on basis of current progress. e.g Uploading 1 of the 10 images.. progress 95%
                        String message = "Uploading Document...\nProgress " + (int) progress + "%";

                        //show progress
                        progressDialog.setMessage(message);
                        progressDialog.show();
                    }
                });

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                //Continue with the task to get the download url
                                return storageRef.getDownloadUrl();
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    String docName = docCaptionEt.getText().toString().trim();

                                    // If user entered nothing, set default name
                                    if (docName.isEmpty()) {
                                        docName = "DOC"; // actual name
                                    }

                                    Uri downloadUri = task.getResult();

                                    if (downloadUri == null) {
                                        Toast.makeText(OpenRoomActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String key = roomListRef.push().getKey();

                                    long ts = new Date().getTime();

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("messageId", key);
                                    map.put("senderId", currentUid);
                                    map.put("senderName", senderName != null ? senderName : "");
                                    map.put("type", "DOC");
                                    map.put("search", docName.toLowerCase());
                                    map.put("note", docName.toLowerCase()); // optional caption
                                    map.put("fileUrl", downloadUri.toString());
                                    map.put("fileName", finalFileName);
                                    map.put("timestamp", ts);

                                    if (replyingTo != null && replyingTo.getMessageId() != null) {
                                        map.put("replyToId", replyingTo.getMessageId());
                                        map.put("replyToText", replyingTo.getNote() != null ? replyingTo.getNote() : replyingTo.getFileName());
                                        map.put("replyToUser", replyingTo.getReplyToUser() != null ? replyingTo.getReplyToUser() : (replyingTo.getSenderId() != null ? replyingTo.getSenderId() : ""));
                                    }


                                    roomListRef.child(key).setValue(map).addOnCompleteListener(t2 -> {
                                        if (t2.isSuccessful()) {
                                            replyingTo = null;
                                            binding.replyPreview.setVisibility(View.GONE);
                                            Toast.makeText(OpenRoomActivity.this, "File sent", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        } else {
                                            Toast.makeText(OpenRoomActivity.this, "Failed to send file", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    });

                                } else {
                                    Toast.makeText(OpenRoomActivity.this, "Upload failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                dialog.dismiss();
            }
        });

        Log.e("DOC_URI", uri.toString());

        String text;
        if (uri.toString().endsWith(".docx")) {
            text = readDocx(uri);
        } else {
            text = readDoc(uri);
        }
        docContent.setText(text);

        Log.e("DOC_TEXT", text);

        dialog.show();
    }

    private String readPpt(Uri uri) {
        StringBuilder text = new StringBuilder();
        try {
            // Open input stream from content:// URI
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // Use HSLF for .ppt files
            HSLFSlideShow ppt = new HSLFSlideShow(inputStream);
            List<HSLFSlide> slides = ppt.getSlides();

            for (HSLFSlide slide : slides) {
                List<HSLFShape> shapes = slide.getShapes();
                for (HSLFShape shape : shapes) {
                    if (shape instanceof HSLFTextShape) {
                        HSLFTextShape textShape = (HSLFTextShape) shape;
                        text.append(textShape.getText()).append("\n");
                    }
                }
            }

            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private String readPptx(Uri uri) {
        StringBuilder text = new StringBuilder();
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            XMLSlideShow pptx = new XMLSlideShow(inputStream);
            List<XSLFSlide> slides = pptx.getSlides();

            for (XSLFSlide slide : slides) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        text.append(textShape.getText()).append("\n");
                    }
                }
            }

            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private void pptPreview(Uri uri) {

        String fileName = "";

        LayoutInflater inflater = LayoutInflater.from(OpenRoomActivity.this);
        View view = inflater.inflate(R.layout.ppt_preview_dialog, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(OpenRoomActivity.this);
        builder.setCancelable(true);
        builder.setView(view);


        TextView pptContent, pptName, pptSize;
//        pptContent = view.findViewById(R.id.pptContentTv);
        pptName = view.findViewById(R.id.pptName);
        pptSize = view.findViewById(R.id.pptSize);
        TextInputLayout pptCaptionTil = view.findViewById(R.id.pptCaptionTil);
        EditText pptCaptionEt = view.findViewById(R.id.pptCaptionEt);

        //changing start icon of TextInputLayout, when password recovery email sent successfully
        pptCaptionTil.setEndIconDrawable(R.drawable.baseline_send_24);
        pptCaptionTil.setEndIconCheckable(true);
        //now visible TextInputLayout Icon
        pptCaptionTil.setEndIconVisible(true);


        androidx.appcompat.app.AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.preview_bgg);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;


        // File name & size
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

            fileName = cursor.getString(nameIndex);
            long fileSize = cursor.getLong(sizeIndex);
            double fileSizeInMB = (double) fileSize / (1024 * 1024);

            pptName.setText(fileName);
            pptSize.setText(String.format(Locale.getDefault(), "%.2f MB", fileSizeInMB));
            cursor.close();

            // Preview text depending on extension
            String previewText;
            if (fileName.endsWith(".pptx")) {
                previewText = readPptx(uri);
            } else {
                previewText = readPpt(uri);
            }
            //content.setText(previewText);
        }

        String finalFileName = fileName;
        pptCaptionTil.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StorageReference storageRef = storageReference.child(finalFileName + "." + "ppt");
                UploadTask uploadTask = storageRef.putFile(uri);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        //calculate the current progress of the image being uploaded
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                        //setup progress dialog message on basis of current progress. e.g Uploading 1 of the 10 images.. progress 95%
                        String message = "Uploading PPT...\nProgress " + (int) progress + "%";

                        //show progress
                        progressDialog.setMessage(message);
                        progressDialog.show();
                    }
                });

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                //Continue with the task to get the download url
                                return storageRef.getDownloadUrl();
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    String pptName = pptCaptionEt.getText().toString().trim();

                                    // If user entered nothing, set default name
                                    if (pptName.isEmpty()) {
                                        pptName = "PPT"; // actual name
                                    }

                                    Uri downloadUri = task.getResult();


                                    String key = roomListRef.push().getKey();

                                    long ts = new Date().getTime();

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("messageId", key);
                                    map.put("senderId", currentUid);
                                    map.put("senderName", senderName != null ? senderName : "");
                                    map.put("type", "PPT");
                                    map.put("search", finalFileName.toLowerCase());
                                    map.put("note", pptName); // optional caption
                                    map.put("fileUrl", downloadUri.toString());
                                    map.put("fileName", finalFileName);
                                    map.put("timestamp", ts);

                                    if (replyingTo != null && replyingTo.getMessageId() != null) {
                                        map.put("replyToId", replyingTo.getMessageId());
                                        map.put("replyToText", replyingTo.getNote() != null ? replyingTo.getNote() : replyingTo.getFileName());
                                        map.put("replyToUser", replyingTo.getReplyToUser() != null ? replyingTo.getReplyToUser() : (replyingTo.getSenderId() != null ? replyingTo.getSenderId() : ""));
                                    }


                                    roomListRef.child(key).setValue(map).addOnCompleteListener(t2 -> {
                                        if (t2.isSuccessful()) {
                                            replyingTo = null;
                                            binding.replyPreview.setVisibility(View.GONE);
                                            Toast.makeText(OpenRoomActivity.this, "File sent", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        } else {
                                            Toast.makeText(OpenRoomActivity.this, "Failed to send file", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    });

                                } else {
                                    Toast.makeText(OpenRoomActivity.this, "Upload failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });

                dialog.dismiss();
                Log.e("DOC_URI", uri.toString());
            }
        });
        dialog.show();
    }

}
