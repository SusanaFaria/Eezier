package susana.faria.eezier;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentUris;
import android.content.ContentValues;

import susana.faria.eezier.Data.EezierContract.EezierEntry;

public class GridCursorAdapter extends RecyclerView.Adapter<GridCursorAdapter.ViewHolder> {


    private Cursor mCursor;
    private Context mContext;

    // data is passed into the constructor
    public GridCursorAdapter(Context context, Cursor c) {

        mContext = context;
        mCursor = c;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int  position ) {
        // Passing the binding operation to cursor loader
        position =  holder.getAdapterPosition();
        if (!mCursor.moveToPosition(position)) {
            return;
        }
     ;
        final int finalPosition = position;
        holder.cardOptionsImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.cardOptionsImgBtn, finalPosition);
            }
        });


        String name = mCursor.getString(mCursor.getColumnIndex(EezierEntry.COLUMN_NAME));
        holder.nameTxt.setText(name);
        String photoUriString = mCursor.getString(mCursor.getColumnIndex(EezierEntry.COLUMN_IMAGE));
        Uri photo = Uri.parse(photoUriString);
        holder.imgView.setImageURI(photo);

    }

    private void showPopupMenu(View view, int position) {
        // inflate menu
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.card_options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(position));
        popup.show();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.main_grid_item_layout, parent, false);
        return new ViewHolder(view);
    }

    public void swapCursor(Cursor newCursor) {
        // Always close the previous mCursor first
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTxt;
        ImageView imgView;
        ImageButton cardOptionsImgBtn;


        public ViewHolder(View itemView) {
            super(itemView);
            nameTxt = itemView.findViewById(R.id.name);
            imgView = itemView.findViewById(R.id.photo);
            cardOptionsImgBtn = itemView.findViewById(R.id.options);
        }
    }

    public class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        private int position;



        public MyMenuItemClickListener(int position) {

            this.position = position;

        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            switch (menuItem.getItemId()) {

                case R.id.action_edit:
                    editPerson();

                case R.id.action_delete:
                    deletePerson();

                default:
            }
            return false;
        }

    }

    private void editPerson() {
        final int personId = mCursor.getInt(mCursor.getColumnIndexOrThrow(EezierEntry._ID));
        final Uri currentPersonUri = ContentUris.withAppendedId(EezierEntry.CONTENT_URI, personId);
        Intent personEdit = new Intent(mContext, EezierEditCard.class);
        personEdit.setData(currentPersonUri);
        mContext.startActivity(personEdit);

    }

    private void deletePerson() {

        final int personId = mCursor.getInt(mCursor.getColumnIndexOrThrow(EezierEntry._ID));
        final Uri currentPersonUri = ContentUris.withAppendedId(EezierEntry.CONTENT_URI, personId);
        // Only perform the delete if this is an existing person.
        if (currentPersonUri != null) {
            // Call the ContentResolver to delete the person at the given content URI.
            // Pass in null for the selection and selection args because the currentPersonUri
            // content URI already identifies the person that we want.
            int rowsDeleted = mContext.getContentResolver().delete(currentPersonUri, null, null);
            Log.v("GridCursorAdapter", rowsDeleted + " rows deleted from book database");
        }




    }


}