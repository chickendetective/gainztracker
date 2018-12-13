package hu.ait.android.gainztracker

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import hu.ait.android.gainztracker.adapter.WorkoutAdapter
import hu.ait.android.gainztracker.data.Workout
import kotlinx.android.synthetic.main.activity_date.*
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*

class DateActivity : AppCompatActivity(), WorkoutDialog.WorkoutHandler {

    private lateinit var workoutAdapter: WorkoutAdapter
    private lateinit var workoutListener: ListenerRegistration

    var uploadBitmap : Bitmap? = null

    val db = FirebaseFirestore.getInstance()

    private var curUser = FirebaseAuth.getInstance().currentUser

    private var curDate = Calendar.getInstance().time

    companion object {
        val WORKOUT_ID = "WORKOUT_ID"
        val KEY_WORKOUT_TO_EDIT = "KEY_WORKOUT_TO_EDIT"
        val KEY_VIEW_GAINZ = "KEY_VIEW_GAINZ"
        private const val CAMERA_REQUEST_CODE = 102
    }
    private var editIndex: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)

        if (intent.hasExtra(MainActivity.KEY_DATE)) {
            Log.d("INTENT CHECKING", intent.getStringExtra(MainActivity.KEY_DATE))
            tvDay.text = intent.getStringExtra(MainActivity.KEY_DATE)
            curDate = Date(intent.getStringExtra(MainActivity.KEY_DATE).toLong())
        }

        initWorkoutRecyclerView()
        fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }

        btnViewGainz.setOnClickListener{
            /*if firebase has an imgUrl saved, do below
             */
            if() {
                val imgUrl: String =
                val gainzIntent = Intent(this@DateActivity, GainzViewActivity::class.java)
                gainzIntent.putExtra(KEY_VIEW_GAINZ, imgUrl)
                startActivity(gainzIntent)
            }else{
                Toast.makeText(this,
                        "No Gainz Saved, Please Record Gainz", Toast.LENGTH_SHORT).show()
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
                .document(curDate.toString()).set(data, SetOptions.merge())
        val workoutsCollection = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout") //create a subcollection for all the workouts

        workoutListener = workoutsCollection.addSnapshotListener(object: EventListener<QuerySnapshot> {
            override fun onEvent(querySnapshot: QuerySnapshot?, p1: FirebaseFirestoreException?) {

                if(p1 != null){
                    Toast.makeText(this@DateActivity,"Error: ${p1.message}",
                            Toast.LENGTH_LONG).show()
                    return
                }

                for (docChange in querySnapshot!!.documentChanges) {
                    when (docChange.type) {
                        DocumentChange.Type.ADDED -> {
                            val workout = docChange.document.toObject(Workout::class.java)
                            workoutAdapter.addWorkout(workout, docChange.document.id)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val workout = docChange.document.toObject(Workout::class.java)
                            workoutAdapter.editWorkout(workout, docChange.document.id)
                        }
                        DocumentChange.Type.REMOVED -> {
                            workoutAdapter.removeWorkoutByKey(docChange.document.id)
                        }
                    }
                }

            }
        })
    }

    private fun showAddWorkoutDialog() {
        WorkoutDialog().show(supportFragmentManager,
                "TAG_CREATE")
    }

    fun showEditWorkoutDialog(workoutToEdit: Workout, idx: Int) {
        editIndex = idx
        val editWorkoutDialog = WorkoutDialog()

        val bundle = Bundle()
        bundle.put(KEY_WORKOUT_TO_EDIT, workoutToEdit)
        editWorkoutDialog.arguments = bundle

        editWorkoutDialog.show(supportFragmentManager,
                "EDITITEMDIALOG")
    }

    override fun workoutCreated(workout: Workout) {
        //add workout to firebase
        val data = HashMap<String, Any>()
        data.put("name", workout.name)
        data.put("workoutType", workout.type)
        data.put("numExercise", 0)
        db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout").add(data)
                .addOnSuccessListener { documentReference ->
                    Log.d("TAG", "DocumentSnapshot written with ID: " + documentReference.id)
                    workout.id = documentReference.id
                    Thread {
                        runOnUiThread {
                            workoutAdapter.addWorkout(workout, documentReference.id)
                        }
                    }.start()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
    }

    override fun workoutUpdated(workout: Workout) {
        //update workout in firebase - either using id or index through the keylist in adapter to find it
        val workoutRef = db.collection("users").document(curUser!!.uid)
                .collection("DayData").document(curDate.toString())
                .collection("workout").document(workout.id!!)

        workoutRef
                .update("name", workout.name, "type", workout.type, "numExercise", workout.exercises.size)
                .addOnSuccessListener {
                    Log.d("TAG", "DocumentSnapshot successfully updated!")
                    Thread {
                        runOnUiThread {
                            workoutAdapter.editWorkout(workout, workout.id!!)
                        }
                    }.start()
                }
                .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e) }
    }

    fun getDate(): Any {
        return curDate
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
                            /*task.result.toString()
                            *
                            * ADD THIS^^^ TO DATE FOR FIREBASE
                            *
                            * */
                            db.collection("users").document(curUser!!.uid)
                                    .collection("DayData").
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