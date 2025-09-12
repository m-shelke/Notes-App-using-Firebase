package com.example.notesappusingfirebase.Chat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Simple swipe right to reply helper.
 * Attach like:
 * ItemTouchHelper helper = new ItemTouchHelper(new SwipeReplyHelper(adapter, (pos)-> {...}));
 * helper.attachToRecyclerView(recyclerView);
 */
public class SwipeReplyHelper extends ItemTouchHelper.SimpleCallback {

    public interface ReplyListener {
        void onReply(int position);
    }

    private final ReplyListener listener;
    private final ChatAdapter adapter;

    public SwipeReplyHelper(ChatAdapter adapter, ReplyListener listener) {
        super(0, ItemTouchHelper.RIGHT);
        this.listener = listener;
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION) {
            listener.onReply(pos);
        }
        // restore item (we don't remove)
        adapter.notifyItemChanged(pos);
    }
}

