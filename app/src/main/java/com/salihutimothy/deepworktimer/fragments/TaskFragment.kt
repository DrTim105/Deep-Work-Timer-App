package com.salihutimothy.deepworktimer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.salihutimothy.deepworktimer.R
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import com.salihutimothy.deepworktimer.adapter.CursorRvAdapter
import com.salihutimothy.deepworktimer.entities.Task
import com.salihutimothy.deepworktimer.models.DeepWorkViewModel

private const val TAG = "TaskFragment"

class TaskFragment : Fragment(), CursorRvAdapter.OnTaskClickListener {

    private val viewModel by lazy { ViewModelProviders.of(requireActivity()).get(DeepWorkViewModel::class.java)}
    private val mAdapter = CursorRvAdapter(null, this)
    private lateinit var taskList : RecyclerView

    interface OnEditTask{
        fun onEditTask (task: Task)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: called")
        return inflater.inflate(R.layout.fragment_task, container, false)
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
        taskList.layoutManager =
            LinearLayoutManager(context)
        taskList.adapter = mAdapter
    }



    override fun onEditClick(task: Task) {
        (activity as OnEditTask?)?.onEditTask(task)
    }

    override fun onDeleteClick(task: Task) {
        viewModel.deleteTask(task.id)
    }

    override fun onTaskLongClick(task: Task) {
        Log.d(TAG, "onViewCreated: called")

    }
}
