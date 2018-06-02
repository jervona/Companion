package nyc.c4q.translator.util;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

import nyc.c4q.translator.model.Message;

/**
 * Created by jervon.arnoldd on 4/1/18.
 */

public class ChatAdapterDiffCallback extends DiffUtil.Callback {


    private final List<Message> oldMessageList;
    private final List<Message> newMessageList;

    public ChatAdapterDiffCallback(List<Message> oldEmployeeList, List<Message> newEmployeeList) {
        this.oldMessageList = oldEmployeeList;
        this.newMessageList = newEmployeeList;
    }

    @Override
    public int getOldListSize() {
        return oldMessageList.size();
    }

    @Override
    public int getNewListSize() {
        return newMessageList.size();
    }


    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldMessageList.get(oldItemPosition).getMessage().equals(newMessageList.get(
                newItemPosition).getMessage());
    }


    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Message oldPayment = oldMessageList.get(oldItemPosition);
        final Message newPayment = newMessageList.get(newItemPosition);
        return oldPayment.getMessage().equals(newPayment.getMessage());
    }


    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}

