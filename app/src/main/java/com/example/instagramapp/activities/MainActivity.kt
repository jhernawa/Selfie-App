package com.example.instagramapp.activities

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagramapp.R
import com.example.instagramapp.adapters.RecyclerViewImageAdapter
import com.example.instagramapp.app.Config
import com.example.instagramapp.models.Image
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import android.provider.MediaStore.Images
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.content.Context
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity(), View.OnClickListener {

    //Request Code Constant
    private val REQUEST_CODE_CAMERA = 101
    private val REQUEST_CODE_GALLERY = 102

    //Firebase
    lateinit var auth: FirebaseAuth
    lateinit var databaseReference : DatabaseReference

    //Adapters
    lateinit var recyclerViewImageAdapter : RecyclerViewImageAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //firebase init
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference(Config.FIREBASE_DATABASE_NAME_REAL_TIME)

        //adapters init
        recyclerViewImageAdapter = RecyclerViewImageAdapter(this)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = recyclerViewImageAdapter

        /* Main activity is the launcher activity.
       *  If the user is not signed in, we direct them
       *  to the login page first
       * */

        if(auth.currentUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
        }

        updateRecyclerView() //display the images that are already inside the database

        init()
    }

    private fun init(){
        button_logout.setOnClickListener(this)
        button_floating_camera.setOnClickListener(this)
        button_floating_gallery.setOnClickListener(this)
    }

    override fun onClick(view: View) {
         when(view.id){
             R.id.button_logout -> handleButtonLogout()
             R.id.button_floating_camera -> handleButtonFloatingCamera()
             R.id.button_floating_gallery -> handleButtonFloatingGallery()
         }
    }

    private fun handleButtonLogout(){
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun handleButtonFloatingCamera(){
        //request camera permission
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    //check if all permissions granted
                    if(report!!.areAllPermissionsGranted()){
                        takePhotoFromCamera()
                        Toast.makeText(applicationContext, "Permissions Granted", Toast.LENGTH_SHORT).show()
                    }

                    //check for permanent denial of any permission
                    if(report.isAnyPermissionPermanentlyDenied){
                        // permission is denied permanently then navigate user to setting
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }

            })
            .onSameThread()
            .check()

    }

    private fun handleButtonFloatingGallery(){
        //request camera permission
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    //check if all permissions granted
                    if(report!!.areAllPermissionsGranted()){
                        choosePhotoFromGallery()
                        Toast.makeText(applicationContext, "Permissions Granted", Toast.LENGTH_SHORT).show()
                    }

                    //check for permanent denial of any permission
                    if(report.isAnyPermissionPermanentlyDenied){
                        // permission is denied permanently then navigate user to setting
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }

            })
            .onSameThread()
            .check()
    }


    private fun takePhotoFromCamera(){
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
    }

    private fun choosePhotoFromGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY)
    }


    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE_GALLERY){
            if(data != null){
                var contentUri = data.data

                try{
                    //save the image in the database
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                    var imagePath = getImageString(bitmap)

                    saveToDatabase(imagePath)

                    //get all the images from the firebase
                    updateRecyclerView()


                    Toast.makeText(applicationContext, "Gallery", Toast.LENGTH_SHORT).show()

                }catch (e : IOException){
                    e.printStackTrace()
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()

                }
            }
        }
        else if(requestCode == REQUEST_CODE_CAMERA){
            val bitmap = data!!.extras!!.get("data") as Bitmap
            var imagePath = getImageString(bitmap)

            saveToDatabase(imagePath)
            updateRecyclerView()


            Toast.makeText(applicationContext, "Camera", Toast.LENGTH_SHORT).show()

        }
    }

    fun getImageString(imgBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

        //insert the image to the gallery and create a thumbnail for it
        val path = MediaStore.Images.Media.insertImage(this.getContentResolver(), imgBitmap, "title", null)
        return path
    }

    fun saveToDatabase(imagePath : String){

        //save the image in the database
        var imageKey =  databaseReference.push().key
        var image = Image(imagePath, imageKey)

        databaseReference.child(imageKey!!).setValue(image)

    }

    fun updateRecyclerView(){

        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            //dataSnapshot is the images node
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var imageList : ArrayList<Image> = ArrayList<Image>()
                for(child in dataSnapshot.children){
                    var image: Image? = child.getValue(Image::class.java)
                    imageList.add(image!!)

                }

                recyclerViewImageAdapter.setData(imageList)

            }
        })
    }




}
