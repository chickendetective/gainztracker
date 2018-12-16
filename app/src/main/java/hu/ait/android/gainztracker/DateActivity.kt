package hu.ait.android.gainztracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.widget.Toast
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import hu.ait.android.gainztracker.adapter.WorkoutAdapter
import hu.ait.android.gainztracker.data.Workout
import hu.ait.android.gainztracker.touch.ItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_date.*
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class DateActivity : AppCompatActivity(), WorkoutDialog.WorkoutHandler {

    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var workoutListener: ListenerRegistration

    private var context: Context = this@DateActivity

    var uploadBitmap : Bitmap? = null

    val db = FirebaseFirestore.getInstance()

    private var curUser = FirebaseAuth.getInstance().currentUser

    lateinit var curDate: String

    companion object {
        val WORKOUT_ID = "WORKOUT_ID"
        val WORKOUT_NAME = "WORKOUT_NAME"
        val WORKOUT_TYPE = "WORKOUT_TYPE"
        val WORKOUT_DATE = "WORKOUT_DATE"
        val KEY_WORKOUT_TO_EDIT = "KEY_WORKOUT_TO_EDIT_NAME"
        val KEY_WORKOUT_TO_EDIT_TYPE = "KEY_WORKOUT_TO_EDIT_TYPE"
        val KEY_WORKOUT_TO_EDIT_ID = "KEY_WORKOUT_TO_EDIT_ID"
        val KEY_VIEW_GAINZ = "KEY_VIEW_GAINZ"
        private const val CAMERA_REQUEST_CODE = 102
    }
    private var editIndex: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)

        if (intent.hasExtra(MainActivity.KEY_DATE)) {
            Log.d("INTENT CHECKING", intent.getStringExtra(MainActivity.KEY_DATE))
            //tvDay.text = intent.getStringExtra(MainActivity.KEY_DATE)
            curDate = intent.getStringExtra(MainActivity.KEY_DATE)
            tvDay.text = curDate
        }

        initWorkoutRecyclerView()
        fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }

        btnViewGainz.setOnClickListener{
            /*if firebase has an imgUrl saved, do below*/
            val dateRef  = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate).get()

            dateRef.addOnCompleteListener {
                task: Task<DocumentSnapshot> ->
                val data = task.result.data
                if (data.isNullOrEmpty() or !data!!.containsKey("url")){
                    Toast.makeText(this,
                        "No Gainz Saved, Please Record Gainz", Toast.LENGTH_SHORT).show()
                } else{
                    val imgUrl : String = data["url"].toString()
                    val gainzIntent = Intent(this@DateActivity, GainzViewActivity::class.java)
                    gainzIntent.putExtra(KEY_VIEW_GAINZ, imgUrl)
                    startActivity(gainzIntent)
                }
            }

        }

        btnRecordGainz.setOnClickListener {
            startActivityForResult(
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                    CAMERA_REQUEST_CODE)
        }
        btnRecordGainz.isEnabled = false
        requestNeededPermission()

    }

    private fun initWorkoutRecyclerView() {
        //create a document for current date if hasnt existed
        val data = HashMap<String, Any>()
        data.put("lastLogin", Calendar.getInstance().time)
        db.collection("users").document(curUser!!.uid).collection("DayData")
                .document(curDate).set(data, SetOptions.merge())
        val workoutsCollection = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate)
                .collection("workout") //create a subcollection for all the workouts if not yet there

        val options = FirestoreRecyclerOptions.Builder<Workout>()
            .setQuery(workoutsCollection, Workout::class.java)
            .build()


        workoutAdapter = WorkoutAdapter(options, context, workoutsCollection)

        val workoutList = workoutAdapter.getWorkoutList()

        recyclerWorkout.adapter = workoutAdapter
        workoutAdapter.notifyDataSetChanged()

        workoutListener = workoutsCollection.addSnapshotListener(EventListener { documentSnapshots, e ->
            if (e != null) {
                Log.e("TAG", "Listen failed!", e)
                return@EventListener
            }

            for (doc in documentSnapshots!!){
                val workout = doc.toObject(Workout::class.java)
                workoutList.add(workout)
            }

            Log.d("FILLED LIST", workoutList.toString())

            workoutAdapter.notifyDataSetChanged()
            recyclerWorkout.adapter = workoutAdapter
        }
        )

        workoutAdapter.startListening()

        runOnUiThread {

            val callback = ItemTouchHelperCallback(workoutAdapter)
            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(recyclerWorkout)
        }
    }

    override fun onResume(){
        super.onResume()
        initWorkoutRecyclerView()
    }

    public override fun onStart() {
        super.onStart()

        workoutAdapter.startListening()
    }

    public override fun onStop() {
        super.onStop()

        workoutAdapter.stopListening()
    }

    private fun showAddWorkoutDialog() {
        WorkoutDialog().show(supportFragmentManager,
                "TAG_CREATE")
    }

    fun showEditWorkoutDialog(workoutToEdit: Workout, idx: Int) {
        editIndex = idx
        val editWorkoutDialog = WorkoutDialog()

        val bundle = Bundle()
        bundle.putString(KEY_WORKOUT_TO_EDIT, workoutToEdit.name)
        bundle.putString(KEY_WORKOUT_TO_EDIT_TYPE, workoutToEdit.type)
        bundle.putString(KEY_WORKOUT_TO_EDIT_ID, workoutToEdit.id)
        editWorkoutDialog.arguments = bundle

        editWorkoutDialog.show(supportFragmentManager,
                "EDITITEMDIALOG")
    }

    override fun workoutCreated(workout: Workout) {
        //add workout to firebase
        val workoutCollection = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate)
                .collection("workout")

        val newDocRef = workoutCollection.document()

        workout.id = newDocRef.id
        val data = HashMap<String, Any>()
        data.put("id", workout.id)
        data.put("name", workout.name)
        data.put("type", workout.type)

        newDocRef.set(data)
            .addOnSuccessListener {
                Log.d("SUCCESS", "Updated Successfully")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

    override fun workoutUpdated(workout: Workout) {
        //update workout in firebase - either using id or index through the keylist in adapter to find it
        val workoutRef = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate)
                .collection("workout").document(workout.id)

        workoutRef
                .update("name", workout.name, "type", workout.type)
                .addOnSuccessListener {
                    Log.d("TAG", "DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }

    @Throws(Exception::class)
    private fun saveImage(){
        val baos = ByteArrayOutputStream()
        uploadBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageRef = FirebaseStorage.getInstance().getReference()
        val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImagesRef = storageRef.child("images/$newImage")

        newImagesRef.putBytes(imageInBytes)
                .addOnFailureListener { exception ->
                    Toast.makeText(this@DateActivity, exception.message, Toast.LENGTH_SHORT).show()
                }.addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.getMetadata()

                    newImagesRef.downloadUrl.addOnCompleteListener(object: OnCompleteListener<Uri> {
                        override fun onComplete(task: Task<Uri>) {
                            val data = HashMap<String, Any>()
                            data["url"] = task.result.toString()

                            db.collection("users").document(curUser!!.uid)
                                .collection("DayData").document(curDate).set(data, SetOptions.merge())
                        }
                    })
                }
    }

    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.CAMERA)) {
                Toast.makeText(this,
                        "Camera Access Required", Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    1001)
        } else {
            // we already have this permission
            btnRecordGainz.isEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "CAMERA permission granted", Toast.LENGTH_SHORT).show()
                    btnRecordGainz.isEnabled = true
                } else {
                    Toast.makeText(this, "CAMERA permission NOT granted", Toast.LENGTH_SHORT).show()
                    btnRecordGainz.isEnabled = false
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            data?.let {
                uploadBitmap = it.extras.get("data") as Bitmap
                saveImage()
            }
        }
    }
}