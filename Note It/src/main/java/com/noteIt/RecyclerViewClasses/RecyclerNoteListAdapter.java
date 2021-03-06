package com.noteIt.RecyclerViewClasses;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.noteIt.archivedNotes.ArchivedNoteFragment;
import com.noteIt.R;
import com.noteIt.data.Note;
import com.noteIt.data.Task;
import com.noteIt.noteDetail.NoteDetailActivity;
import com.noteIt.notes.NoteFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

public class RecyclerNoteListAdapter extends RecyclerView.Adapter<RecyclerNoteListAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private static final String ALERT_DELETION_TITLE = "Alert";
    private static final String ALERT_DELETION_STRING = "Are you sure to delete notes";
    private static final String ALERT_DELETION_CONFIRM_STRING = "YES";
    private static final String ALERT_DELETION_DENY_STRING = "NO";

    private ArrayList<Note> mArrayList;
    private Context mContext;
    private ActionMode mActionMode;

    private ArrayList<CardView> mSelectedCardViews;
    private ArrayList<Note> mSelectedNotes;

    private boolean isArchived;

    private NoteFragment mNoteFragment;
    private ArchivedNoteFragment mNoteArchivedFragment;

    private boolean isSwiped = false;

    class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder
    {
        private CardView mCardView;
        private TextView mTitle;
        private TextView mDescription;
        private LinearLayout mTaskLayout;

        ViewHolder(View v) {
            super(v);
            mTitle = v.findViewById(R.id.note_title);
            mDescription = v.findViewById(R.id.note_description);
            mCardView = v.findViewById(R.id.noteCardView);
            mTaskLayout = v.findViewById(R.id.note_task_layout);

        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {
            if(mActionMode != null  && isSwiped )
            {
                mActionMode.finish();
                isSwiped = false;
            }

        }

        void update(Note note)
        {
            mTitle.setText(note.getTitle());
            mDescription.setText(note.getDescription());
            setOnClickIntent(note);
            updateTasks(note.getId());
            setOnLongClickActionMode(note);

        }

        void setOnClickIntent(final Note note)
        {
            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mActionMode != null)
                    {
                        mCardView.setSelected(!mCardView.isSelected());
                        if(mCardView.isSelected())
                        {
                            mSelectedCardViews.add(mCardView);
                            mSelectedNotes.add(note);
                        }
                        else
                        {
                            mSelectedCardViews.remove(mCardView);
                            mSelectedNotes.remove(note);
                        }

                        setActionModeTitle(mSelectedCardViews.size());
                    }
                    else
                    {
                        Intent intent = new Intent(mContext, NoteDetailActivity.class);
                        intent.putExtra(NoteDetailActivity.GET_NOTE_DETAIL, note.getId());
                        mContext.startActivity(intent);
                    }

                }
            });
        }

        void setOnLongClickActionMode(final Note note)
        {
            mCardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(mActionMode == null)
                    {
                        mActionMode = ((AppCompatActivity)mContext).startSupportActionMode(actionModeCallbacks);
                        mCardView.setSelected(true);
                        mSelectedCardViews.add(mCardView);
                        mSelectedNotes.add(note);
                        setActionModeTitle(1);
                    }
                    else
                    {
                        mActionMode.finish();
                    }
                    return true;
                }
            });
        }

        void updateTasks(String noteId)
        {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ArrayList<Task> tasks;
            tasks = getNoteTasks(noteId);

            mTaskLayout.removeAllViews();
            for (Task task: tasks) {
                try
                {
                    View view = inflater.inflate(R.layout.task_fragment_layout, mTaskLayout, false);
                    TextView textView = view.findViewById(R.id.task_layout_text);
                    CheckBox checkBox = view.findViewById(R.id.task_layout_checkbox);
                    textView.setText(task.getDescription());
                    checkBox.setChecked(task.isDone());
                    mTaskLayout.addView(view);
                } catch (NullPointerException e) {
                    Log.e(TAG, "updateTasks: NPE " + e.getMessage());
                }


            }
        }
    }

    private void setActionModeTitle(int ArraySize)
    {
        if(ArraySize < 1)
        {
            mActionMode.finish();
        }
        else
        {
            mActionMode.setTitle("Selected: " + mSelectedCardViews.size());
        }

    }

    public RecyclerNoteListAdapter(Context context, ArrayList<Note> items, boolean isArchived, NoteFragment fragment) {
        this.mArrayList = items;
        this.mContext = context;
        this.isArchived = isArchived;

        mSelectedCardViews = new ArrayList<>(0);
        mSelectedNotes = new ArrayList<>(0);

        mNoteFragment = fragment;
    }

    public RecyclerNoteListAdapter(Context context, ArrayList<Note> items, boolean isArchived, ArchivedNoteFragment fragment) {
        this.mArrayList = items;
        this.mContext = context;
        this.isArchived = isArchived;

        mSelectedCardViews = new ArrayList<>(0);
        mSelectedNotes = new ArrayList<>(0);

        mNoteArchivedFragment = fragment;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View root = layoutInflater.inflate(R.layout.note, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(root);
        root.setTag(viewHolder);

        return (ViewHolder)root.getTag();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.update(mArrayList.get(position));

    }


    @Override
    public int getItemCount() {
        return mArrayList.size();
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        //Swap Notes Position first
        Note fromNote = mArrayList.get(fromPosition);
        Note toNote = mArrayList.get(toPosition);
        int tempPosition = fromNote.getPosition();
        fromNote.setPosition(toNote.getPosition());
        toNote.setPosition(tempPosition);

        Collections.swap(mArrayList, fromPosition, toPosition);

        updateNotes(new ArrayList<>(Arrays.asList(fromNote,toNote)));
        notifyItemMoved(fromPosition, toPosition);
        isSwiped = true;
        return true;
    }


    @Override
    public void onItemDismiss(int position) {

        mArrayList.remove(position);

        notifyItemRemoved(position);
    }

    public void replaceNoteList(List<Note> noteList) {
        setList(noteList);
    }


    private void setList(List<Note> noteList) {
        mArrayList.clear();
        mArrayList.addAll(noteList);

        if(noteList.size() > 0)
            showEmptyView(false);
        else
            showEmptyView(true);

        notifyDataSetChanged();
    }

    private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.contextual_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.item_delete:

                    AlertDialog.Builder alert = new AlertDialog.Builder(
                            mContext);
                    alert.setTitle(ALERT_DELETION_TITLE);
                    alert.setMessage(ALERT_DELETION_STRING);
                    alert.setPositiveButton(ALERT_DELETION_CONFIRM_STRING, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteNotes(mSelectedNotes);
                            mArrayList.removeAll(mSelectedNotes);

                            actionMode.finish();

                            dialog.dismiss();

                        }
                    });
                    alert.setNegativeButton(ALERT_DELETION_DENY_STRING, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    });
                    alert.show();

                    break;
                case R.id.item_archive:
                    mArrayList.removeAll(mSelectedNotes);
                    mSelectedNotes = archiveNotes(mSelectedNotes);
                    updateNotes(mSelectedNotes);

                    actionMode.finish();
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for (CardView cardview: mSelectedCardViews) {
                cardview.setSelected(false);
            }
            //setList(getNotes());
            notifyDataSetChanged();
            mSelectedCardViews.clear();
            mSelectedNotes.clear();

            mActionMode = null;
        }

        ArrayList<Note> archiveNotes(ArrayList<Note> noteList)
        {
            for (Note note: noteList) {
                note.setArchived(!note.isArchived());

            }
            return noteList;
        }
    };



    private void deleteNotes(ArrayList<Note> noteList)
    {
        if(mNoteFragment != null)
        {
            mNoteFragment.deleteNotes(noteList);
        } else {
            mNoteArchivedFragment.deleteNotes(noteList);
        }

    }

    private void updateNotes(ArrayList<Note> noteList)
    {
        if(mNoteFragment != null)
        {
            mNoteFragment.updateNotes( noteList );
        }
        else
        {
            mNoteArchivedFragment.updateNotes( noteList );
        }
    }

    private ArrayList<Note> getNotes()
    {
        if(isArchived)
            return mNoteArchivedFragment.getArchivedNotes();
        else
            return mNoteFragment.getNotes();
    }

    private ArrayList<Task> getNoteTasks(String noteId)
    {
        if(mNoteFragment != null)
        {
            return mNoteFragment.getNoteTasks(noteId);
        }
        else
        {
            return mNoteArchivedFragment.getNoteTasks(noteId);
        }
    }

    private void showEmptyView(boolean show)
    {
        if(mNoteFragment != null)
        {
            mNoteFragment.showEmptyView(show);
        }
        else
        {
            mNoteArchivedFragment.showEmptyView(show);
        }
    }
}