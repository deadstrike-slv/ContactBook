package com.example.deadstrike.contactbook.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.deadstrike.contactbook.DBHeaders;
import com.example.deadstrike.contactbook.R;

public class ContactsCursorRecyclerAdapter extends CursorRecyclerAdapter<ContactsCursorRecyclerAdapter.ContactViewHolder> {

    private Context mContext;

    //region column index for contact object
    private final int COLUMN_FIRST_NAME_ID;
    private final int COLUMN_LAST_NAME_ID;
    private final int COLUMN_PHONE_NUMBER_ID;
    private final int COLUMN_EMAIL_ID;
    //endregion

    private IViewHolderClickListener mListener;

    public ContactsCursorRecyclerAdapter(Context ctx, @NonNull Cursor c) {
        super(c);
        mContext = ctx;

        COLUMN_FIRST_NAME_ID = c.getColumnIndexOrThrow(
                DBHeaders.ContactEntry.COLUMN_NAME_FIRST_NAME);
        COLUMN_LAST_NAME_ID = c.getColumnIndexOrThrow(
                DBHeaders.ContactEntry.COLUMN_NAME_LAST_NAME);
        COLUMN_PHONE_NUMBER_ID = c.getColumnIndexOrThrow(
                DBHeaders.ContactEntry.COLUMN_NAME_PHONE_NUMBER);
        COLUMN_EMAIL_ID = c.getColumnIndexOrThrow(
                DBHeaders.ContactEntry.COLUMN_NAME_EMAIL);
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_entry, parent, false);
        return new ContactViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder vh, Cursor c) {
        vh.tvFirstName.setText(c.getString(COLUMN_FIRST_NAME_ID));
        vh.tvLastName.setText(c.getString(COLUMN_LAST_NAME_ID));
        vh.tvPhoneNumber.setText(c.getString(COLUMN_PHONE_NUMBER_ID));
        vh.tvEmail.setText(c.getString(COLUMN_EMAIL_ID));

        final int pos = vh.getAdapterPosition();
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onViewHolderClick(pos);
            }
        });
    }

    public interface IViewHolderClickListener{
        void onViewHolderClick(int pos);
    }

    public void setOnViewHolderClickListener(IViewHolderClickListener listener){
        mListener = listener;
    }


    class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvFirstName;
        TextView tvLastName;
        TextView tvPhoneNumber;
        TextView tvEmail;

        ContactViewHolder(View itemView) {
            super(itemView);

            tvFirstName = (TextView) itemView.findViewById(R.id.contact_first_name);
            tvLastName = (TextView) itemView.findViewById(R.id.contact_last_name);
            tvPhoneNumber = (TextView) itemView.findViewById(R.id.contact_phone_number);
            tvEmail = (TextView) itemView.findViewById(R.id.contact_email);
        }
    }

}

