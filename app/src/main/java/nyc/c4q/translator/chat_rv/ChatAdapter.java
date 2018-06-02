package nyc.c4q.translator.chat_rv;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import nyc.c4q.translator.R;
import nyc.c4q.translator.model.Message;
import nyc.c4q.translator.util.ChatAdapterDiffCallback;

/**
 * Created by jervon.arnoldd on 5/16/18.
 */

public class ChatAdapter  extends RecyclerView.Adapter<ChatViewHolder> {
    private int PRIMARY_USER = 100;
    private List<Message> chatList;


    public ChatAdapter(List <Message> chatList) {
        if (chatList == null) {
            this.chatList = new ArrayList<>();
        } else {
            this.chatList = chatList;
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;

        if (viewType == PRIMARY_USER) {
            // self message
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_self, parent, false);
        } else {
            // WatBot message
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_watson, parent, false);
        }
        return new ChatViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = chatList.get(position);
        if (message.getId()!=null && message.getId().equals("1")) {
            return PRIMARY_USER;
        }
        return position;
    }

    public void updateTicketListItems(List<Message> tickets) {
        final ChatAdapterDiffCallback diffCallback = new ChatAdapterDiffCallback(this.chatList, tickets);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.chatList.clear();
        this.chatList.addAll(tickets);
        diffResult.dispatchUpdatesTo(this);
    }



    @Override
    public void onBindViewHolder(final ChatViewHolder holder, int position) {
        Message message = chatList.get(position);
        message.setMessage(message.getMessage());
        holder.message.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}
