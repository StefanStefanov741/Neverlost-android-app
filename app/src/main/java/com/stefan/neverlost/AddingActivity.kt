package com.stefan.neverlost

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*


class AddingActivity : AppCompatActivity() {
    private lateinit var item_photo_b: ImageButton
    private lateinit var loc_photo_b: ImageButton
    private lateinit var backBtn: Button
    private lateinit var saveBtn: Button
    private var ItemNameBox: EditText?=null
    private var ItemLocationBox: EditText?=null
    private lateinit var ItemDescriptionBox: EditText
    private val IMAGE_CAPTURE_CODE: Int=1001
    private var lastBtnClicked = ""
    private var permissiongAskedFromBtn = ""
    var uri_string_item = ""
    var uri_string_loc = ""
    lateinit var item_pic: Bitmap
    lateinit var loc_pic: Bitmap
    var has_item_pic = false
    var has_loc_pic = false

    val SHARED_PREFS = "sharedPrefs"
    val MY_ITEMS = "my_items"
    val ITEMS_SAVED = "items_saved"

    val CAMERA_RQ = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding)

        item_photo_b=findViewById<ImageButton>(R.id.itempicbtn)
        loc_photo_b=findViewById<ImageButton>(R.id.locimagebtn)
        backBtn=findViewById<Button>(R.id.backBtn)
        saveBtn=findViewById<Button>(R.id.SaveBtn)
        ItemNameBox=findViewById<EditText>(R.id.NameField)
        ItemLocationBox=findViewById<EditText>(R.id.LocationField)
        ItemDescriptionBox=findViewById<EditText>(R.id.DescriptionField)

        backBtn.setOnClickListener{
            GoBack()
            BtnCd(backBtn,null)
        }

        saveBtn.setOnClickListener{
            Save()
            BtnCd(saveBtn,null)
        }

        item_photo_b.setOnClickListener(){
            if(ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                openCamera("item_photo_b")
            }else{
                AskForPermission(android.Manifest.permission.CAMERA,"camera",CAMERA_RQ)
                permissiongAskedFromBtn = "item_photo_b"
            }
            BtnCd(null,item_photo_b)

        }

        loc_photo_b.setOnClickListener(){
            if(ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                openCamera("loc_photo_b")
            }else{
                AskForPermission(android.Manifest.permission.CAMERA,"camera",CAMERA_RQ)
                permissiongAskedFromBtn = "loc_photo_b"
            }
            BtnCd(null,loc_photo_b)
        }
    }

    //handling camera and permissions
    private fun openCamera(btn: String){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE)
        lastBtnClicked = btn
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val w = item_photo_b.width
            val h = item_photo_b.height
            val resized = Bitmap.createScaledBitmap(imageBitmap, w, h, true)
            if(lastBtnClicked=="item_photo_b"){
                //set button image
                item_photo_b.setImageBitmap(resized)
                item_pic=imageBitmap
                has_item_pic=true
            }else if(lastBtnClicked=="loc_photo_b"){
                //set button image
                loc_photo_b.setImageBitmap(resized)
                loc_pic=imageBitmap
                has_loc_pic=true
            }
        }
    }

     fun AskForPermission(permission: String,name:String,requestCode:Int){
         if(ContextCompat.checkSelfPermission(applicationContext,permission)==PackageManager.PERMISSION_DENIED){
             ActivityCompat.requestPermissions(this,arrayOf(permission),requestCode)
         }
     }

    override fun onRequestPermissionsResult(requestCode: Int,permission: Array<out String>, grantResults: IntArray){
        if(requestCode==CAMERA_RQ){
            if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext,"Camera permission refused",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(applicationContext,"Camera permission granted",Toast.LENGTH_SHORT).show()
                //open camera for porper button
                if(permissiongAskedFromBtn!=""){
                    openCamera(permissiongAskedFromBtn)
                }
                permissiongAskedFromBtn=""
            }
        }
    }

    //button cooldown to prevent spamming button
    fun BtnCd(b: Button?, ib:ImageButton?){
        if(b!=null){
            b.setEnabled(false)

            val buttonTimer = Timer()
            buttonTimer.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread { b.setEnabled(true) }
                }
            }, 1000)
        }else if(ib!=null){
            ib.setEnabled(false)

            val buttonTimer = Timer()
            buttonTimer.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread { ib.setEnabled(true) }
                }
            }, 1000)
        }
    }

    //sends you back to the main activity if you change your mind and dont want to add anything
    fun GoBack(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    //saves the new item in shared prefs
    fun Save(){
        val NewItemSymbol = "!#~t^42@-="
        val BetweenParamsSymbol = "%&%$(r"
        if (ItemNameBox != null&&ItemLocationBox != null) {
            if (ItemNameBox!!.getText().toString().trim().length>0&&ItemLocationBox!!.getText().toString().trim().length>0) {
                if(ItemNameBox!!.getText().toString()==ItemDescriptionBox.getText().toString()){
                    InvalidDescription()
                }else{
                    //initialize sharedprefs and editor
                    val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    //extract the already stored set of items
                    var ExistingItems = sharedPreferences.getString(MY_ITEMS, "")
                    val NameString =ItemNameBox!!.getText().toString()
                    val LocString =ItemLocationBox!!.getText().toString()
                    val DescString =ItemDescriptionBox!!.getText().toString()

                    if (ExistingItems != null) {
                        if (ExistingItems.isEmpty()) {
                            ExistingItems=NewItemSymbol
                        }

                        if(!ExistingItems.contains(NameString.plus(BetweenParamsSymbol).plus(LocString))){
                            //add the new items to the list
                            val NewString = ExistingItems.plus(BetweenParamsSymbol).plus(NameString).plus(BetweenParamsSymbol)
                                    .plus(LocString).plus(BetweenParamsSymbol).plus(DescString).plus(BetweenParamsSymbol).plus(NewItemSymbol)

                            //extract the item counter
                            var num = sharedPreferences.getInt(ITEMS_SAVED,0)
                            num++
                            //add the new set to the shared prefs
                            editor.putString(MY_ITEMS, NewString)
                            editor.putInt(ITEMS_SAVED,num)

                            if(has_item_pic){
                                SaveItemImage(NameString,LocString)
                            }
                            if(has_loc_pic){
                                SaveLocImage(NameString,LocString)
                            }
                            editor.apply()
                            GoBack()
                        }else{
                            ItemAlreadyAddedError()
                        }
                    }
                }
            }else{
                //create popup if name and/or location boxes are empty
                if(ItemNameBox!!.getText().toString().trim().length<=0 && ItemLocationBox!!.getText().toString().trim().length>0){
                    NoNameError()
                }else if(ItemLocationBox!!.getText().toString().trim().length<=0 && ItemNameBox!!.getText().toString().trim().length>0){
                    NoLocError()
                }else{
                    NoNameAndLocError()
                }
            }
        }
    }

    //saves the images in internal storage
    fun SaveItemImage(name:String,loc:String){
        if(has_item_pic){
            var img_name = "ITEM"
            img_name=img_name.plus(name).plus(loc)
            val wrapper = ContextWrapper(applicationContext)
            var file = wrapper.getDir("images", Context.MODE_PRIVATE)
            file = File(file, "$img_name.jpg")
            try {
                // Get the file output stream
                val stream: OutputStream = FileOutputStream(file)

                // Compress bitmap
                val w = item_photo_b.width
                val h = item_photo_b.height
                val r = Bitmap.createScaledBitmap(item_pic, w, h, true)
                r.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                // Flush the stream
                stream.flush()

                // Close stream
                stream.close()
            } catch (e: IOException){ // Catch the exception
                e.printStackTrace()
            }
            uri_string_item = Uri.parse(file.absolutePath).toString()
        }
    }
    fun SaveLocImage(name:String,loc:String){
        if(has_loc_pic){
            var img_name = "LOC"
            img_name=img_name.plus(name).plus(loc)

            val wrapper = ContextWrapper(applicationContext)
            var file = wrapper.getDir("images", Context.MODE_PRIVATE)
            file = File(file, "$img_name.jpg")
            try {
                // Get the file output stream
                val stream: OutputStream = FileOutputStream(file)

                // Compress bitmap
                val w = loc_photo_b.width
                val h = loc_photo_b.height
                val r = Bitmap.createScaledBitmap(loc_pic, w, h, true)
                r.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                // Flush the stream
                stream.flush()

                // Close stream
                stream.close()
            } catch (e: IOException){ // Catch the exception
                e.printStackTrace()
            }
            uri_string_loc = Uri.parse(file.absolutePath).toString()
        }
    }

    fun ItemAlreadyAddedError(){
        val errorMsg = ErrorPopup("Item exists!","This item has already been added to your list!")
        errorMsg.show(supportFragmentManager, "error")
    }

    fun NoNameError(){
        val errorMsg = ErrorPopup("Missing information!","Please enter a name for your item.")
        errorMsg.show(supportFragmentManager, "error")
    }

    fun NoLocError(){
        val errorMsg = ErrorPopup("Missing information","Please enter a location for your item.")
        errorMsg.show(supportFragmentManager, "error")
    }

    fun InvalidDescription(){
        val errorMsg = ErrorPopup("Invalid description","Description can't be identical to the item name")
        errorMsg.show(supportFragmentManager, "error")
    }

    fun NoNameAndLocError(){
        val errorMsg = ErrorPopup("Missing information","Please enter a name and location for your item.")
        errorMsg.show(supportFragmentManager, "error")
    }

}