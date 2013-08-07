package com.eldridge.twitsync.adapter;

import android.app.Activity;
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

import twitter4j.Status;

/**
 * Created by ryaneldridge on 8/4/13.
 */
public class TweetsAdapter extends ArrayAdapter<Status> {

    private Context context;
    private int textViewId;
    private List<Status> items;

    public TweetsAdapter(Context context, int textViewId, List<Status> items) {
        super(context, textViewId, items);
        this.context = context;
        this.textViewId = textViewId;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Status status = (Status) this.items.get(position);
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
        Picasso.with(context).load(R.drawable.ic_launcher).resize(125, 125).into(viewHolder.profileImage);

        if (status.isRetweet()) {
            Picasso.with(context).load(status.getRetweetedStatus().getUser().getBiggerProfileImageURLHttps()).resize(125, 125).into(viewHolder.profileImage);
            viewHolder.authorFullName.setText(status.getRetweetedStatus().getUser().getName());
            viewHolder.authorTwitterHandle.setText("@" + status.getRetweetedStatus().getUser().getScreenName());
            LinkifyWithTwitter.addLinks(viewHolder.authorTwitterHandle, LinkifyWithTwitter.AT_MENTIONS);

            viewHolder.retweetAuthor.setText("Retweeted by @" + status.getUser().getScreenName());
            LinkifyWithTwitter.addLinks(viewHolder.retweetAuthor, LinkifyWithTwitter.AT_MENTIONS);
        } else {
            Picasso.with(context).load(status.getUser().getBiggerProfileImageURLHttps()).resize(125, 125).into(viewHolder.profileImage);
            viewHolder.authorFullName.setText(status.getUser().getName());
            viewHolder.authorTwitterHandle.setText("@" + status.getUser().getScreenName());
            LinkifyWithTwitter.addLinks(viewHolder.authorTwitterHandle, LinkifyWithTwitter.AT_MENTIONS);
            viewHolder.retweetAuthor.setText("");
        }
        viewHolder.tweetText.setText(status.getText());
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
