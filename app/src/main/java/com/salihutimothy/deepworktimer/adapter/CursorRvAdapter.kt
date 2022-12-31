package com.salihutimothy.deepworktimer.adapter


import android.annotation.SuppressLint
import android.database.Cursor
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.salihutimothy.deepworktimer.R
import com.salihutimothy.deepworktimer.entities.Task
import com.salihutimothy.deepworktimer.entities.TasksContract

class TaskViewHolder(private val containerView: View) :
    RecyclerView.ViewHolder(containerView) {
    var name: TextView = containerView.findViewById(R.id.tli_name)
    var description: TextView = containerView.findViewById(R.id.tli_description)
    var edit: ImageButton = containerView.findViewById(R.id.tli_edit)
    var delete: ImageButton = containerView.findViewById(R.id.tli_delete)

    fun bind(task: Task, listener: CursorRvAdapter.OnTaskClickListener) {
        name.text = task.name
        description.text = task.description
        edit.visibility = View.VISIBLE
        delete.visibility = View.VISIBLE

        edit.setOnClickListener {
            listener.onEditClick(task)
        }

        delete.setOnClickListener {
            listener.onDeleteClick(task)
        }

        containerView.setOnLongClickListener {
            listener.onTaskLongClick(task)
            true
        }
    }
}

private const val TAG = "CursorRecyclerViewAdapt"

class CursorRvAdapter(private var cursor: Cursor?, private val listener: OnTaskClickListener) :
    RecyclerView.Adapter<TaskViewHolder>() {

    interface OnTaskClickListener {
        fun onEditClick(task: Task)
        fun onDeleteClick(task:Task)
        fun onTaskLongClick(task:Task)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        Log.d(TAG, "onCreateViewHOlder: new view requested")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.task_list_items, parent, false)
        return TaskViewHolder(view)

    }

    @SuppressLint("Range")
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: starts")

        val cursor = cursor

        if (cursor == null || cursor.count == 0) {
            Log.d(TAG, "onBindViewHolder: providing instructions")
            holder.name.setText(R.string.instructions_heading)
            holder.description.setText(R.string.instructions)
            holder.edit.visibility = View.GONE
            holder.delete.visibility = View.GONE
        } else {
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("Couldn't move cursor to position $position")
            }

            // Create a TAsk object from the data in the cursor
            val task = Task(
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
                cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(TasksContract.Columns.TASK_SORT_ORDER))
            )

            // Remember that the id isn't set in the constructor
            task.id = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))
//                cursor.getColumnIndex(TasksContract.Columns.ID))

            holder.bind(task, listener)

            Log.d(TAG, "onBindViewHolder: name ${task.name}, desc ${task.description}")
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursor
        val count = if (cursor == null || cursor.count == 0) {
            1
        } else {
            cursor.count
        }

        return count
    }

    /*
    Swap in a new Cursor, returning the old cursor
    the returned old cursor is NOT closed.
    @param newCursor - the new cursor to be used.
    @return Returns the previously set Cursor, or null if there wasn't one.
    if the given new cursor is the same instance as the previously set cursor,
    null is also returned.
     */
    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursor) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            //notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }

}
