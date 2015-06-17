package net.hklight.nanodegree.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Hashtable;


public class TrackAdapter extends ArrayAdapter<Hashtable<String, String>> {
    // Store dataset
    private ArrayList<Hashtable<String, String>> dataset;
    private Context context;

    public TrackAdapter(Context context, ArrayList<Hashtable<String, String>> dataset) {
        super(context, R.layout.item_toptentracks, dataset);
        this.context = context;
        this.dataset = dataset;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            // View first appear

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.item_toptentracks, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.albumImageView = (ImageView)convertView.findViewById(R.id.imageview_albumImg);
            viewHolder.trackNameTextView = (TextView)convertView.findViewById(R.id.textview_tracktName);
            viewHolder.albumNameTextView = (TextView)convertView.findViewById(R.id.textview_albumName);
            // save into tag
            convertView.setTag(viewHolder);
        } else {
            // get the view holder back
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // insert data
        viewHolder.trackNameTextView.setText(dataset.get(position).get("trackName"));
        viewHolder.albumNameTextView.setText(dataset.get(position).get("albumName"));

        // Load artist image
        String imageUrl = dataset.get(position).get("smallAlbumImage");
        if (imageUrl.length() > 0) {
            // cancel the previous request, if any...
            //Picasso.with(context).cancelRequest(viewHolder.artistImageView);
            // then load new one
            Picasso.with(this.context).load(imageUrl).placeholder(R.drawable.loading).fit().into(viewHolder.albumImageView);
        } else {
            // clear the image..
            //Picasso.with(context).cancelRequest(viewHolder.artistImageView);
            Picasso.with(this.context).load(R.drawable.no_image).fit().into(viewHolder.albumImageView);
        }

        return convertView;
    }


    // view reference
    static class ViewHolder{
        ImageView albumImageView;
        TextView albumNameTextView;
        TextView trackNameTextView;
    }
}