package com.salihutimothy.deepworktimer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.widget.TextView
import com.salihutimothy.deepworktimer.*
import com.salihutimothy.deepworktimer.adapter.CursorRvAdapter
import com.salihutimothy.deepworktimer.dialogs.AppDialog
import com.salihutimothy.deepworktimer.dialogs.DIALOG_ID
import com.salihutimothy.deepworktimer.dialogs.DIALOG_MESSAGE
import com.salihutimothy.deepworktimer.dialogs.DIALOG_POSITIVE_RID
import com.salihutimothy.deepworktimer.entities.Task
import com.salihutimothy.deepworktimer.models.DeepWorkViewModel

private const val TAG = "TaskFragment"

private const val DIALOG_ID_DELETE = 1
private const val DIALOG_TASK_ID = "task_id"

class TaskFragment : Fragment(), CursorRvAdapter.OnTaskClickListener, AppDialog.DialogEvents {

    private val viewModel by lazy { ViewModelProviders.of(requireActivity()).get(DeepWorkViewModel::class.java)}
    private val mAdapter = CursorRvAdapter(null, this)
    private lateinit var taskList : RecyclerView
    lateinit var currentTask : TextView

    interface OnEditTask{
        fun onEditTask (task: Task)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: called")

        val view = inflater.inflate(R.layout.fragment_task, container, false)
        currentTask = view.findViewById(R.id.current_task)

        return view
    }
    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: called")
        super.onAttach(context)

        if (context !is OnEditTask) {
            throw RuntimeException("${context.toString()} must implement OnEditTask")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        viewModel.cursor.observe(this, Observer { cursor -> mAdapter.swapCursor(cursor)?.close() })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        super.onViewCreated(view, savedInstanceState)

        taskList = view.findViewById(R.id.task_list)
//        currentTask = view.findViewById(R.id.current_task)

        taskList.layoutManager =
            LinearLayoutManager(context)
        taskList.adapter = mAdapter
    }



    override fun onEditClick(task: Task) {
        Log.d(TAG, "onEditClick: called")

        (activity as OnEditTask?)?.onEditTask(task)
    }

    override fun onDeleteClick(task: Task) {
        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_ID_DELETE)
            putString(DIALOG_MESSAGE, getString(R.string.deldiag_message, task.id, task.name))
            putInt(DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption)
            putLong(DIALOG_TASK_ID, task.id)   // pass the id in the arguments, so we can retrieve it when we get called back.
        }
        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(childFragmentManager, null)
    }

    override fun onTaskLongClick(task: Task) {
        Log.d(TAG, "onTaskLongClick: called")
        viewModel.timeTask(task)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: called with id $dialogId")

        if (dialogId == DIALOG_ID_DELETE) {
            val taskId = args.getLong(DIALOG_TASK_ID)
            if (BuildConfig.DEBUG && taskId == 0L) throw AssertionError("Task ID is zero")
            viewModel.deleteTask(taskId)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task The task to be edited, or null to add a new task.
         * @return A new instance of fragment AddEditFragment.
         */
        @JvmStatic
        fun newInstance() =
            TaskFragment.apply {

            }
    }
}
