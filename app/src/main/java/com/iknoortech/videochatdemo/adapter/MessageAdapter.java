package com.iknoortech.videochatdemo.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iknoortech.videochatdemo.R;
import com.iknoortech.videochatdemo.helper.EditMessageDialog;
import com.iknoortech.videochatdemo.model.Chat;
import io.fabric.sdk.android.services.common.SafeToast;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.iknoortech.videochatdemo.helper.AppConstant.chatTableName;
import static com.iknoortech.videochatdemo.helper.AppUtils.getMyPrettyOnlyDate;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private ArrayList<Chat> mChat;
    private FirebaseUser fuser;

    public MessageAdapter(Context context, ArrayList<Chat> chats) {
        this.context = context;
        this.mChat = chats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, viewGroup, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, viewGroup, false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Chat chat = mChat.get(position);
        holder.message.setText(chat.getMessage());
        holder.time.setText(chat.getTime());

        if (chat.getDeleteForEveryone() != null) {
            if (!chat.getDeleteForEveryone().isEmpty()) {
                if (chat.getDeleteForEveryone().equals(fuser.getUid())) {
                    holder.message.setText("You delete this message");
                } else {
                    holder.message.setText("This message is deleted");
                }
            }
        }

        holder.tv_chatHeader.setText(getMyPrettyOnlyDate(Long.parseLong(chat.getMessageDate())));
        if (position > 0) {
            if (chat.getMessageDate().equalsIgnoreCase(mChat.get(position - 1).getMessageDate())) {
                holder.tv_chatHeader.setVisibility(View.GONE);
            } else {
                holder.tv_chatHeader.setVisibility(View.VISIBLE);
            }
        } else {
            holder.tv_chatHeader.setVisibility(View.VISIBLE);
        }

        if (chat.isIsseen()) {
            holder.seen.setBackgroundResource(R.drawable.ic_done_theme_24dp);
        } else {
            holder.seen.setBackgroundResource(R.drawable.ic_done_24dp);
        }

        if (chat.getDeleteForMe() != null) {
            if (!chat.getDeleteForMe().isEmpty()) {
                if (chat.getDeleteForMe().equals(fuser.getUid())) {
                    holder.message.setVisibility(View.GONE);
                    holder.time.setVisibility(View.GONE);
                    holder.seen.setVisibility(View.GONE);
                }
            }
        }

        holder.message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showDialog(chat, holder.itemView);
                return false;
            }
        });
    }

    private void showDialog(final Chat chat, View itemView) {
        final PopupMenu menu = new PopupMenu(context, itemView);
        menu.getMenu().add("Copy");
        if (chat.isIsseen()) {
            menu.getMenu().add("Delete for me");
        } else {
            menu.getMenu().add("Delete for everyone");
            if (!chat.isEdited()) {
                menu.getMenu().add("Edit Message");
            }
        }

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (menuItem.getTitle().equals("Copy")) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setText(chat.getMessage());
                    Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show();
                } else if (menuItem.getTitle().equals("Edit Message")) {
                    EditMessageDialog editMessageDialog = new EditMessageDialog(context, chat);
                    editMessageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    editMessageDialog.setCancelable(false);
                    editMessageDialog.show();
                } else if (menuItem.getTitle().equals("Delete for everyone")) {
                    deleteMessage("deleteForEveryone", chat);
                } else if (menuItem.getTitle().equals("Delete for me")) {
                    deleteMessage("deleteForMe", chat);
                }
                return false;
            }
        });

        menu.show();

    }

    private void deleteMessage(final String key, final Chat chat) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Message ?");
        if(key.equals("deleteForEveryone")){
            builder.setMessage("This message will be deleted for everyone.");
        }else{
            builder.setMessage("This message will be deleted for you only.");
        }
        builder.setCancelable(false);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(chatTableName).child(chat.getMessageId());
                try {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(key, fuser.getUid());
                    reference.updateChildren(hashMap);
                    dialogInterface.dismiss();
                } catch (Exception e) {
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView message, time, tv_chatHeader;
        private ImageView seen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.tv_chatMsg);
            seen = itemView.findViewById(R.id.img_seenMsg);
            time = itemView.findViewById(R.id.tv_chatTime);
            tv_chatHeader = itemView.findViewById(R.id.tv_chatHeader);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
