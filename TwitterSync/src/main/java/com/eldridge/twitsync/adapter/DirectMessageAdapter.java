package com.eldridge.twitsync.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eldridge.twitsync.R;
import com.eldridge.twitsync.util.LinkifyWithTwitter;
import com.squareup.picasso.Picasso;

import java.util.List;

import twitter4j.DirectMessage;

/**
 * Created by reldridge1 on 9/4/13.
 */
public class DirectMessageAdapter extends ArrayAdapter<DirectMessage> {

    private Context context;
    private int textViewId;
    private List<DirectMessage> items;

    public DirectMessageAdapter(Context context, int textViewId, List<DirectMessage> items) {
        super(context, textViewId, items);
        this.context = context;
        this.textViewId = textViewId;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DirectMessage message = items.get(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(textViewId, null, false);
            viewHolder = new ViewHolder();

            viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.profileImage);
            viewHolder.authorFullName = (TextView) convertView.findViewById(R.id.authorFullName);
            viewHolder.authorTwitterHandle = (TextView) convertView.findViewById(R.id.authorTwitterHandle);
            viewHolder.authorTwitterHandle.setAutoLinkMask(0);
            viewHolder.tweetText = (TextView) convertView.findViewById(R.id.tweetText);
            viewHolder.tweetText.setAutoLinkMask(0);
            viewHolder.retweetAuthor = (TextView) convertView.findViewById(R.id.retweetAuthor);
            viewHolder.retweetAuthor.setAutoLinkMask(0);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(context).load(message.getSender().getBiggerProfileImageURLHttps()).resize(125, 125).into(viewHolder.profileImage);
        viewHolder.authorFullName.setText(message.getSender().getName());
        viewHolder.authorTwitterHandle.setText("@" + message.getSenderScreenName());
        LinkifyWithTwitter.addLinks(viewHolder.authorTwitterHandle, LinkifyWithTwitter.AT_MENTIONS);
        viewHolder.retweetAuthor.setText("");

        viewHolder.tweetText.setText(message.getText());
        LinkifyWithTwitter.addLinks(viewHolder.tweetText, LinkifyWithTwitter.ALL);


        return convertView;
    }

    static class ViewHolder {
        ImageView profileImage;
        TextView tweetText;
        TextView authorFullName;
        TextView authorTwitterHandle;
        TextView retweetAuthor;
    }

}
