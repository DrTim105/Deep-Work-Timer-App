package com.salihutimothy.deepworktimer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.salihutimothy.deepworktimer.debug.TestData
import com.salihutimothy.deepworktimer.dialogs.AppDialog
import com.salihutimothy.deepworktimer.dialogs.SettingsDialog
import com.salihutimothy.deepworktimer.entities.Task
import com.salihutimothy.deepworktimer.fragments.AddEditFragment
import com.salihutimothy.deepworktimer.fragments.TaskFragment
import com.salihutimothy.deepworktimer.models.DeepWorkViewModel


private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked, TaskFragment.OnEditTask,
    AppDialog.DialogEvents {

    // Whether or the activity is in 2-pane mode
    // i.e. running in landscape, or on a tablet.
    private var mTwoPane = false

    // module scope because we need to dismiss it in onStop (e.g. when orientation changes) to avoid memory leaks.
    private var aboutDialog: AlertDialog? = null

    private val viewModel by lazy { ViewModelProvider(this).get(DeepWorkViewModel::class.java) }


    private lateinit var toolbar: Toolbar
    private lateinit var taskContainer: FrameLayout
    private lateinit var mainFragment: FragmentContainerView
    private lateinit var taskFragment: TaskFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        taskContainer = findViewById(R.id.task_details_container)
        mainFragment = findViewById<FragmentContainerView>(R.id.mainFragment)

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.d(TAG, "onCreate: twoPane is $mTwoPane")

        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment != null) {
            // There was an existing fragment to edit a task, make sure the panes are set correctly
            showEditPane()
        } else {
            taskContainer.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.visibility = View.VISIBLE
        }

//        val currentTask = findViewById<TextView>(R.id.current_task)

//        taskFragment = TaskFragment.newInstance()

        val mFragment = findFragmentById(R.id.mainFragment)
        viewModel.timing.observe(this, Observer<String> { timing ->
            Log.d(TAG, "onCreate: viewModel is observing ${mFragment.toString()}")
            if (mFragment is TaskFragment) {
                val taskFragment = mFragment as TaskFragment
                taskFragment.currentTask.text = if (timing != null) {
                    getString(R.string.timing_message, timing)
                } else {
                    getString(R.string.no_task_message)
                }
            }

        })

        Log.d(TAG, "onCreate: finished")
    }

    private fun showEditPane() {
        taskContainer.visibility = View.VISIBLE
        // hide the left hand pane, if in single pane view
        mainFragment.visibility = if (mTwoPane) View.VISIBLE else View.GONE
    }

    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane called")
        if (fragment != null) {
            removeFragment(fragment)
        }

        // Set the visibility of the right hand pane
        taskContainer.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
        // and show the left hand pane
        mainFragment.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

    }

    override fun onSaveClicked() {
        Log.d(TAG, "onSaveClicked: called")
        removeEditPane(findFragmentById(R.id.task_details_container))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        if (BuildConfig.DEBUG) {
            val generate = menu.findItem(R.id.menumain_generate)
            generate.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menumain_addTask -> taskEditRequest(null)
            R.id.menumain_settings -> {
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager, null)
            }
            R.id.menumain_showAbout -> showAboutDialog()
            R.id.menumain_generate -> TestData.generateTestData(contentResolver)
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: home button pressed")
                val fragment = findFragmentById(R.id.task_details_container)
//                removeEditPane(fragment)
                if ((fragment is AddEditFragment) && fragment.isDirty()) {
                    Log.d("BUG", "About to call showConfirmationDialog func")

                    showConfirmationDialog(
                        DIALOG_ID_CANCEL_EDIT,
                        getString(R.string.cancelEditDiag_message),
                        R.string.cancelEditDiag_positive_caption,
                        R.string.cancelEditDiag_negative_caption
                    )
                } else {
                    removeEditPane(fragment)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAboutDialog() {
        val messageView = layoutInflater.inflate(R.layout.about, null, false)
        val builder = AlertDialog.Builder(this)

        builder.setTitle(R.string.app_name)
        builder.setIcon(R.drawable.ic_app)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            if (aboutDialog != null && aboutDialog?.isShowing == true) {
                aboutDialog?.dismiss()
            }
        }

        aboutDialog = builder.setView(messageView).create()
        aboutDialog?.setCanceledOnTouchOutside(true)

        val aboutVersion = messageView.findViewById(R.id.about_version) as TextView
        aboutVersion.text = BuildConfig.VERSION_NAME
        aboutDialog?.show()
    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")

        // Create a new fragment to edit the task
        val newFragment = AddEditFragment.newInstance(task)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            .replace(R.id.task_details_container, newFragment)
            .commit()

        showEditPane()

        Log.d(TAG, "Exiting taskEditRequest")
    }

    override fun onBackPressed() {
        val fragment = findFragmentById(R.id.task_details_container)
        if (fragment == null || mTwoPane) {
            super.onBackPressed()
        } else {
//                removeEditPane(fragment)
            if ((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(
                    DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancelEditDiag_message),
                    R.string.cancelEditDiag_positive_caption,
                    R.string.cancelEditDiag_negative_caption
                )
            } else {
                removeEditPane(fragment)
            }
        }
    }

    override fun onEditTask(task: Task) {
        taskEditRequest(task)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: called with dialogId $dialogId")
        if (dialogId == DIALOG_ID_CANCEL_EDIT) {
            removeEditPane(findFragmentById(R.id.task_details_container))
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop: called")
        super.onStop()
        if (aboutDialog?.isShowing == true) {
            aboutDialog?.dismiss()
        }
    }
}


