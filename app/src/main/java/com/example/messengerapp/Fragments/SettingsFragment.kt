package com.example.messengerapp.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.messengerapp.ModelClasses.Users
import com.example.messengerapp.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*


/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment()
{
    private var usersReference: DatabaseReference?= null
    private var firebaseUser: FirebaseUser? = null
    private val RequestCode = 438;
    private var imageUris: Uri? = null
    private var storageRef: StorageReference?= null
    private var coverChecker: String? = null
    private var socialChecker: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_settings, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        usersReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    val user: Users? = p0.getValue(Users::class.java)

                    if (context!=null){
                        view.username_settings.text = user!!.getUserName()
                        Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
                        Picasso.get().load(user.getCover()).into(view.cover_image_settings)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) = Unit
        })

        view.profile_image_settings.setOnClickListener { pickImage() }

        view.cover_image_settings.setOnClickListener{
            coverChecker = "cover"
            pickImage()
        }

        view.set_facebook.setOnClickListener{
            socialChecker = "facebook"
            setSocialLink()
        }
        view.set_instagram.setOnClickListener{
            socialChecker = "instagram"
            setSocialLink()
        }
        view.set_website.setOnClickListener{
            socialChecker = "website"
            setSocialLink()
        }

        return view
    }

    private fun setSocialLink()
    {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context, R.style.Theme_AppCompat_DayNight_Dialog_Alert)

        if(socialChecker == "website")
            builder.setTitle("Write URL:")
        else
            builder.setTitle("Write  username:")

        val editText = EditText(context)

        editText.hint =  if(socialChecker == "website") "e.g www.google.com" else "e.g asdf"
        builder.setView(editText)

        builder.setPositiveButton("Create") { _, _ ->
            val str = editText.text.toString()

            if (str == "")
                Toast.makeText(context, "Please write something...", Toast.LENGTH_LONG).show()
            else
                saveSocialLink(str)
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun saveSocialLink(str: String)
    {
        val mapSocial = HashMap<String?,Any>()

        mapSocial[socialChecker] = when(socialChecker)
        {
            "facebook" -> "https://m.facebook.com/$str"
            "instagram" -> "https://m.instagram.com/$str"
            "website" -> "https:/$str"
            else -> ""
        }

        usersReference!!.updateChildren(mapSocial).addOnCompleteListener{
            task ->
            if (task.isSuccessful)
                Toast.makeText(context, getString(R.string.successful), Toast.LENGTH_LONG).show()
        }
    }

    private fun pickImage()
    {
        val intent = Intent()
        intent.type ="image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null)
        {
            imageUris = data.data
            Toast.makeText(context,"uploading...", Toast.LENGTH_LONG).show()
            uploadImageToDatabase()

        }
    }

    private fun uploadImageToDatabase() {
        val progressBar=  ProgressDialog(context)
        progressBar.setMessage("Image is uploading, please wait...")
        progressBar.show()
        if (imageUris != null)
        {
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUris!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{task ->
                if (!task.isSuccessful)
                    task.exception?.let { throw it }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener{ task ->
                if(task.isSuccessful)
                {
                    val downloadUri = task.result
                    val url = downloadUri.toString()

                    if (coverChecker == "cover")
                    {
                        val mapCoverImg = HashMap<String,Any>()
                        mapCoverImg["cover"] = url
                        usersReference!!.updateChildren(mapCoverImg)
                        coverChecker = ""
                    }
                    else
                    {
                        val mapProfileImg = HashMap<String,Any>()
                        mapProfileImg["profile"] = url
                        usersReference!!.updateChildren(mapProfileImg)
                        coverChecker = ""
                    }
                    progressBar.dismiss()
                }
            }
        }

    }


}