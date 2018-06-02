package nyc.c4q.translator.chat_rv;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import nyc.c4q.translator.R;

/**
 * Created by jervon.arnoldd on 5/16/18.
 */

class ChatViewHolder extends RecyclerView.ViewHolder {

    TextView message;
    public ChatViewHolder(View itemView) {
        super(itemView);
        message =  itemView.findViewById(R.id.message);
    }
}
