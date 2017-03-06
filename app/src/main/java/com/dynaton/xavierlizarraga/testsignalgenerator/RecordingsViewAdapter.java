package com.dynaton.xavierlizarraga.testsignalgenerator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by xavierlizarraga on 04/03/17.
 */
public class RecordingsViewAdapter extends BaseAdapter{


    /**
     * ListView adapter for the main list view. Holds RecordingsInfo objects and uses them to read the wave files
     * from disc to display them. Uses the ViewHolder pattern.
     * @author androiders
     */
        //private view holder class for efficiency
        public static class ViewHolder {

            //ImageView selfie;
            TextView fileName;
        }


        //the ist that holds all uri to images
        private ArrayList<RecordingInfo> list = new ArrayList<RecordingInfo>();

        //inflater to inflate lsit views
        private static LayoutInflater inflater = null;

        //context....
        public Context mContext;

        private String ASK_DELETE = "Really delete this wave file?";
        private String NO = "No";
        private String OK = "Yes";

        /**
         * Creates an adapter from a context. Searches the disc for images with the selfie string in them
         * If any are found, they are added to the list.
         * @param context
         */
        public RecordingsViewAdapter(Context context) {
            mContext = context;
            inflater = LayoutInflater.from(mContext);

            //See if we have any audio files stored on disk already.
            //If so, read them in
            File dir = RecordingInfo.getMediaFileStorageDir();
            if(dir == null)
                return;

            //Read files on directory
            String [] pics = dir.list();

            for (String string : pics) {
                if (!string.equals("synth_test.wav")) { // Filtering testing signal audio file
                    Uri path = Uri.parse(dir.toString() + File.separator + string);
                    RecordingInfo recording = new RecordingInfo(path);
                    add(recording);
                }
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View newView = convertView;
            ViewHolder holder;
            final int pos = position;

            final RecordingInfo curr = list.get(position);

            if (null == convertView) {
                holder = new ViewHolder();
                newView = inflater.inflate(R.layout.list_item_layout, null);
                holder.fileName = (TextView) newView.findViewById(R.id.recording_file_name);
                newView.setTag(holder);

            } else {
                holder = (ViewHolder) newView.getTag();
            }

            holder.fileName.setText(curr.getFormatedTimeString());
            //add a click listener to listen to clicks to show image
            newView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, RecordingsViewActivity.class);
                    intent.putExtra(RecordingsViewActivity.EXTRA_URI_STRING,curr.getUri().toString());
                    mContext.startActivity(intent);
                }
            });

            newView.setOnLongClickListener(new View.OnLongClickListener() {
                int lPos = pos;
                @Override
                public boolean onLongClick(View v) {
                    //inflate(R.menu.menu, this);
                    PopupMenu menu = new PopupMenu(mContext,v);
                    menu.getMenuInflater().inflate(R.menu.menu2, menu.getMenu());
                    // Set a listener so we are notified if a menu item is clicked
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.analize:
                                    // Remove the item from the adapter
                                    //adapter.remove(item);
                                    // TODO: Call for Activity to compute DSP
                                    return true;
                                case R.id.delete:
                                    //On long click we display i dialog to the user asking if he or she wants
                                    //to delete the clicked image
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                    builder.setTitle("Confirm")
                                            .setMessage(ASK_DELETE)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(OK, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //Yes button clicked, do something
                                                    RecordingInfo si = (RecordingInfo) RecordingsViewAdapter.this.getItem(lPos);
                                                    if( si.deleteFile()){
                                                        RecordingsViewAdapter.this.remove(si);
                                                    }
                                                    else{
                                                        Toast.makeText(mContext, "Could not delete file",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            })
                                            .setNegativeButton(NO, null)//Do nothing on no
                                            .show();
                                    return true;
                            }
                            return false;
                        }
                    });
                    // Finally show the PopupMenu
                    menu.show();
                    return true;
                }
            });
            return newView;
        }

        protected void remove(RecordingInfo si) {
            list.remove(si);
            notifyDataSetChanged();

        }

        public void add(RecordingInfo rec) {
            list.add(rec);
            notifyDataSetChanged();
        }

    }

