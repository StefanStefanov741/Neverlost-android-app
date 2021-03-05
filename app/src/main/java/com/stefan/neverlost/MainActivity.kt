package com.stefan.neverlost

import android.app.ActionBar
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.util.*
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private lateinit var addBtn: ImageButton
    private lateinit var searchBtn: ImageButton
    private lateinit var searchBox: EditText
    private lateinit var emptyTxt: TextView
    private lateinit var scrollBox: ScrollView
    private lateinit var itemLayout: LinearLayout

    private lateinit var popup_name: EditText
    private lateinit var popup_loc: EditText
    private lateinit var popup_desc: EditText
    private lateinit var popup_save: Button
    private lateinit var popup_delete: Button
    private lateinit var popup_close: ImageButton
    private lateinit var popup_item_img: ImageButton
    private lateinit var popup_loc_img: ImageButton

    var LoadedItemsList = mutableListOf<Item>()

    val SHARED_PREFS = "sharedPrefs"
    val MY_ITEMS = "my_items"
    val ITEMS_SAVED = "items_saved"

    var NewItemSymbol = "!#~t^42@-="
    var BetweenParamsSymbol = "%&%$(r"

    var ItemList = ""

    var has_itm_img = false
    var has_lc_img = false
    //stuff for item editing with new photos
    var edited_item_b: Button? = null
    var edited_itm_photo: Bitmap? = null
    var edited_lc_photo: Bitmap? = null
    var edited_name: String? = null
    var edited_loc: String? = null
    var edited_desc: String? = null
    var img_w by Delegates.notNull<Int>()
    var img_h by Delegates.notNull<Int>()

    val CAMERA_RQ = 102

    private val IMAGE_CAPTURE_CODE: Int=1001
    private var lastBtnClicked = ""
    private var permissiongAskedFromBtn = ""

    private var images_folder_path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        emptyTxt=findViewById(R.id.EmptyText)
        scrollBox=findViewById(R.id.ScrollBox)
        itemLayout=findViewById(R.id.ItemLayout)
        searchBtn=findViewById(R.id.searchbtn)
        searchBox=findViewById(R.id.SearchBox)
        images_folder_path = ContextWrapper(applicationContext).getDir("images", Context.MODE_PRIVATE).toString()

        //check for saved items and load them
        LoadAllItemButtons()

        addBtn=findViewById(R.id.addBtn)

        addBtn.setOnClickListener{
            OpenAdd()
            BtnCd(null,addBtn)
        }

        searchBtn.setOnClickListener{
            SearchItem(searchBox.getText().toString())
            BtnCd(null,searchBtn)
        }

        searchBox.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                SearchItem(searchBox.getText().toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }
        })

    }

    fun OpenAdd(){
        val intent = Intent(this,AddingActivity::class.java)
        startActivity(intent)
    }

    fun LoadAllItemButtons(){
        LoadedItemsList.clear()
        ItemList = LoadItems()
        if(ItemList!=""&&ItemList!=NewItemSymbol){
            val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
            emptyTxt.setVisibility(View.GONE)
            //create new layer for a new item
            val itemCount = sharedPreferences.getInt(ITEMS_SAVED,0)
            var i = 0
            while(i<itemCount){
                var item = ItemList.substringAfter(NewItemSymbol).substringBefore(NewItemSymbol)
                ItemList = ItemList.replace(NewItemSymbol.plus(item).plus(NewItemSymbol), NewItemSymbol)
                val Name =item.substringAfter(BetweenParamsSymbol).substringBefore(BetweenParamsSymbol)
                item = item.replace(BetweenParamsSymbol.plus(Name).plus(BetweenParamsSymbol), BetweenParamsSymbol)
                val Loc =item.substringAfter(BetweenParamsSymbol).substringBefore(BetweenParamsSymbol)
                item = item.replace(BetweenParamsSymbol.plus(Loc).plus(BetweenParamsSymbol), BetweenParamsSymbol)
                val Desc =item.substringAfter(BetweenParamsSymbol).substringBefore(BetweenParamsSymbol)
                //create a child layer
                val ChildLayer = LinearLayout(this)
                itemLayout.addView(ChildLayer)
                ChildLayer.setOrientation(LinearLayout.HORIZONTAL)
                //set layer width and height
                val p = ChildLayer.layoutParams
                p.width=ActionBar.LayoutParams.MATCH_PARENT
                p.height=ActionBar.LayoutParams.WRAP_CONTENT
                ChildLayer.layoutParams=p
                val p2  = ChildLayer.layoutParams as ViewGroup.MarginLayoutParams
                p2.setMargins(14,7,14,14)
                ChildLayer.layoutParams = p2
                ChildLayer.minimumHeight = 250
                //Add border to each item layout
                val border = GradientDrawable()
                border.setColor(Color.parseColor("#15DDB7"))
                border.setStroke(2, -0x1000000)
                ChildLayer.setBackground(border)
                //Create the button for the item
                val ItemBtn = Button(this)
                val PhotoBtn = ImageButton(this)
                ChildLayer.addView(PhotoBtn)
                ChildLayer.addView(ItemBtn)
                //load image
                val item_p = "ITEM".plus(Name).plus(Loc).plus(".jpg")
                try {
                    val f = File(images_folder_path, item_p)
                    val b = BitmapFactory.decodeStream(FileInputStream(f))
                    val resB = Bitmap.createScaledBitmap(b,(b.width/1.2).toInt(),(b.height/1.2).toInt(),false)
                    PhotoBtn.setImageBitmap(resB)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                //adjust img button
                PhotoBtn.setBackgroundColor(Color.TRANSPARENT)
                val PBparam = PhotoBtn.layoutParams as ViewGroup.MarginLayoutParams
                PBparam.setMargins(14,7,0,14)
                PhotoBtn.layoutParams=PBparam
                PhotoBtn.setPadding(14,10,10,14)
                //load info in text button
                val btn_txt="Name: $Name\n  Location: $Loc"
                ItemBtn.setText(btn_txt)
                ItemBtn.setBackgroundColor(Color.TRANSPARENT)
                val IBparam = ItemBtn.layoutParams as ViewGroup.MarginLayoutParams
                IBparam.setMargins(0,7,7,14)
                IBparam.width= ViewGroup.LayoutParams.MATCH_PARENT
                IBparam.height= ViewGroup.LayoutParams.MATCH_PARENT
                ItemBtn.layoutParams = IBparam
                ItemBtn.textSize= 16F
                ItemBtn.setTextColor(Color.WHITE)
                //add listener for new item button
                ItemBtn.setOnClickListener{
                    OpenItem(ItemBtn)
                    BtnCd(ItemBtn,null)
                    BtnCd(null,PhotoBtn)
                }
                PhotoBtn.setOnClickListener{
                    OpenItem(ItemBtn)
                    BtnCd(null,PhotoBtn)
                    BtnCd(ItemBtn,null)
                }
                //Add item to the LoadedItemsList
                val itm = Item(Name,Loc,Desc)
                itm.item_button=ItemBtn
                itm.item_layout=ChildLayer
                LoadedItemsList.add(itm)
                //prepare next loop
                i++
            }
        }else{
            emptyTxt.setVisibility(View.VISIBLE)
        }
    }

    //Funs for searching specific items and displaying them and only them
    fun ClearAllItemButtons(){
        itemLayout.removeAllViews()
    }

    fun SearchItem(text: String){
        ClearAllItemButtons()
        if(text==""){
            LoadAllItemButtons()
        }else{
            val itmList = LoadedItemsList.toMutableList()
            //search for text in name locationg or description
            var itemsCount = itmList.count()
            while(itemsCount>0){
                val childItem= itmList[itemsCount-1]
                if(!childItem.item_name.toLowerCase().contains(text.toLowerCase())&&!childItem.item_location.toLowerCase().contains(text.toLowerCase())&&!childItem.item_description.toLowerCase().contains(text.toLowerCase())){
                    itmList.removeAt(itemsCount-1)
                }
                itemsCount--
            }
            LoadSpecificButtons(itmList)
        }
    }

    fun ClearSearchBox(){
        searchBox=findViewById(R.id.SearchBox)
        searchBox.text.clear()
    }

    fun LoadSpecificButtons(list: MutableList<Item>){
        var i =0
        while(i<list.count()){
            //create a child layer
            val ChildLayer = LinearLayout(this)
            itemLayout.addView(ChildLayer)
            ChildLayer.setOrientation(LinearLayout.HORIZONTAL)
            //set layer width and height
            val p = ChildLayer.layoutParams
            p.width= ActionBar.LayoutParams.MATCH_PARENT
            p.height=ActionBar.LayoutParams.WRAP_CONTENT
            ChildLayer.layoutParams=p
            val p2  = ChildLayer.layoutParams as ViewGroup.MarginLayoutParams
            p2.setMargins(14,7,14,14)
            ChildLayer.layoutParams = p2
            ChildLayer.minimumHeight = 250
            //Add border to item each layout
            val border = GradientDrawable()
            border.setColor(Color.parseColor("#15DDB7"))
            border.setStroke(2, -0x1000000)
            ChildLayer.setBackground(border)
            //Add the button in the child layer
            val btn = Button(this)
            val Pbtn = ImageButton(this)
            ChildLayer.addView(Pbtn)
            ChildLayer.addView(btn)
            //load image
            val item_p = "ITEM".plus(list[i].item_name).plus(list[i].item_location).plus(".jpg")
            try {
                val f = File(images_folder_path, item_p)
                val b = BitmapFactory.decodeStream(FileInputStream(f))
                val resB = Bitmap.createScaledBitmap(b,(b.width/1.2).toInt(),(b.height/1.2).toInt(),false)
                Pbtn.setImageBitmap(resB)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            //adjust img button
            Pbtn.setBackgroundColor(Color.TRANSPARENT)
            val PBparam = Pbtn.layoutParams as ViewGroup.MarginLayoutParams
            PBparam.setMargins(14,7,0,14)
            Pbtn.layoutParams=PBparam
            Pbtn.setPadding(14,10,10,14)
            //load info in text btn
            btn.setText(list[i].item_button.text)
            btn.setBackgroundColor(Color.TRANSPARENT)
            val IBparam = btn.layoutParams as ViewGroup.MarginLayoutParams
            IBparam.setMargins(0,7,7,14)
            IBparam.width= ViewGroup.LayoutParams.MATCH_PARENT
            IBparam.height= ViewGroup.LayoutParams.MATCH_PARENT
            btn.layoutParams = IBparam
            btn.textSize= 16F
            btn.setTextColor(Color.WHITE)
            //add listener for new item button
            btn.setOnClickListener{
                OpenItem(btn)
                BtnCd(btn,null)
                BtnCd(null,Pbtn)
            }
            Pbtn.setOnClickListener{
                OpenItem(btn)
                BtnCd(null,Pbtn)
                BtnCd(btn,null)
            }
            //prepare next loop
            i++
        }
    }

    //returns a string with all the item data saved in shared prefs
    fun LoadItems(): String {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        var num = 0
        var CurrentString=""
        num = sharedPreferences.getInt(ITEMS_SAVED,0)
        if(num>0){
            if(sharedPreferences.getString(MY_ITEMS,"")!=null){
                val ExistingString = sharedPreferences.getString(MY_ITEMS, "")
                if (ExistingString != null) {
                    CurrentString = ExistingString
                }
            }
        }
        return CurrentString
    }

    //displaying a popup of an item with more information and the option to edit it
    fun OpenItem(b:Button){
        //Find the corresponding item in the list
        val RightItem = LoadedItemsList.find { itm -> b.text.equals(itm.item_button.text) }
        if(RightItem==null){
            return
        }
        //build the popup
        val builder = AlertDialog.Builder(this)
        val item_popup = layoutInflater.inflate(R.layout.popup,null)
        builder.setView(item_popup)
        val dialog = builder.create()
        dialog.show()

        //enter information
        popup_name=item_popup.findViewById(R.id.options_name)
        popup_loc=item_popup.findViewById(R.id.options_loc)
        popup_desc=item_popup.findViewById(R.id.options_desc)
        popup_save=item_popup.findViewById(R.id.options_save_btn)
        popup_delete=item_popup.findViewById(R.id.options_delete_btn)
        popup_close=item_popup.findViewById(R.id.options_close_btn)
        popup_loc_img=item_popup.findViewById(R.id.loc_pic_btn)
        popup_item_img=item_popup.findViewById(R.id.item_pic_btn)


        popup_name.setText(RightItem.item_name)
        popup_loc.setText(RightItem.item_location)
        popup_desc.setText(RightItem.item_description)

        popup_save.setOnClickListener{
            if(CheckForErrors(popup_name.text.toString(),popup_loc.text.toString(),popup_desc.text.toString(),RightItem.item_name.plus(BetweenParamsSymbol).plus(RightItem.item_location))){
                //save a variable with old item name and loc name for image naming
                val old_name = RightItem.item_name
                val old_loc = RightItem.item_location
                val unmodified_item_string =NewItemSymbol.plus(BetweenParamsSymbol).plus(RightItem.item_name).plus(BetweenParamsSymbol)
                        .plus(RightItem.item_location).plus(BetweenParamsSymbol).plus(RightItem.item_description).plus(BetweenParamsSymbol).plus(NewItemSymbol)
                //change the list
                RightItem.item_name=popup_name.text.toString()
                RightItem.item_location=popup_loc.text.toString()
                RightItem.item_description=popup_desc.text.toString()
                LoadedItemsList.find { it ==  RightItem}?.item_name = RightItem.item_name
                LoadedItemsList.find { it ==  RightItem}?.item_location = RightItem.item_location
                LoadedItemsList.find { it ==  RightItem}?.item_description = RightItem.item_description
                //open up shared prefs
                val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                val OldSharedString = sharedPreferences.getString(MY_ITEMS, "")
                val modified_item_string = NewItemSymbol.plus(BetweenParamsSymbol).plus(RightItem.item_name).plus(BetweenParamsSymbol)
                        .plus(RightItem.item_location).plus(BetweenParamsSymbol).plus(RightItem.item_description).plus(BetweenParamsSymbol).plus(NewItemSymbol)
                val NewSharedString = OldSharedString?.replace(unmodified_item_string,modified_item_string)
                //change shared string
                editor.putString(MY_ITEMS, NewSharedString)
                editor.apply()
                //change value of current button
                b.setText("Name: ${RightItem.item_name}\n  Location: ${RightItem.item_location}")
                //delete old photos
                val itm_path =  "/data/user/0/com.stefan.neverlost/app_images/".plus("ITEM").plus(old_name).plus(old_loc).plus(".jpg")
                val lc_path =  "/data/user/0/com.stefan.neverlost/app_images/".plus("LOC").plus(old_name).plus(old_loc).plus(".jpg")
                val itm_pic = File(itm_path)
                itm_pic.delete()
                val lc_pic = File(lc_path)
                lc_pic.delete()
                //save new photos
                val item_photo = (popup_item_img.getDrawable() as BitmapDrawable).bitmap
                val location_photo = (popup_loc_img.getDrawable() as BitmapDrawable).bitmap
                SaveItmImg(item_photo,has_itm_img,RightItem.item_name,RightItem.item_location)
                SaveLcImg(location_photo,has_lc_img,RightItem.item_name,RightItem.item_location)
                //close out
                ClearAllItemButtons()
                LoadAllItemButtons()
                ClearSearchBox()
                dialog.dismiss()
            }
        }
        popup_delete.setOnClickListener{
            val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val OldSharedString = sharedPreferences.getString(MY_ITEMS,"")
            //get the chosen item string
            val item_for_del =NewItemSymbol.plus(BetweenParamsSymbol).plus(RightItem.item_name).plus(BetweenParamsSymbol)
                    .plus(RightItem.item_location).plus(BetweenParamsSymbol).plus(RightItem.item_description).plus(BetweenParamsSymbol).plus(NewItemSymbol)
            //remove it from the sharedPrefs
            val NewSharedString = OldSharedString?.replace(item_for_del,NewItemSymbol)
            editor.putString(MY_ITEMS,NewSharedString)
            var num = sharedPreferences.getInt(ITEMS_SAVED,0)
            num--
            editor.putInt(ITEMS_SAVED,num)
            editor.apply()
            //update the list in the current view
            ItemList=NewSharedString.toString()
            //remove the button from the current open view
            itemLayout.removeView(RightItem.item_layout)
            //Check if empty text should be enabled
            if(ItemList==NewItemSymbol||ItemList==""){
                emptyTxt.setVisibility(View.VISIBLE)
            }
            //delete photos
            val itm_path =  "/data/user/0/com.stefan.neverlost/app_images/".plus("ITEM").plus(RightItem.item_name).plus(RightItem.item_location).plus(".jpg")
            val lc_path =  "/data/user/0/com.stefan.neverlost/app_images/".plus("LOC").plus(RightItem.item_name).plus(RightItem.item_location).plus(".jpg")
            val itm_pic = File(itm_path)
            itm_pic.delete()
            val lc_pic = File(lc_path)
            lc_pic.delete()
            //close out
            ClearAllItemButtons()
            LoadAllItemButtons()
            ClearSearchBox()
            has_itm_img=false
            has_lc_img=false
            dialog.dismiss()
        }
        popup_close.setOnClickListener {
            has_itm_img=false
            has_lc_img=false
            dialog.dismiss()
        }

        popup_item_img.setOnClickListener{
            if(ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                openCamera("item_photo_b")
            }else{
                AskForPermission(android.Manifest.permission.CAMERA,"camera",CAMERA_RQ)
                permissiongAskedFromBtn = "item_photo_b"
            }

            edited_name=popup_name.text.toString()
            edited_loc=popup_loc.text.toString()
            edited_desc=popup_desc.text.toString()
            edited_itm_photo=(popup_item_img.getDrawable() as BitmapDrawable).bitmap
            edited_lc_photo=(popup_loc_img.getDrawable() as BitmapDrawable).bitmap
            edited_item_b=b
            img_w=popup_item_img.width
            img_h=popup_item_img.height
            dialog.dismiss()
            BtnCd(null,popup_item_img)
        }

        popup_loc_img.setOnClickListener {
            if(ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                openCamera("loc_photo_b")
            }else{
                AskForPermission(android.Manifest.permission.CAMERA,"camera",CAMERA_RQ)
                permissiongAskedFromBtn = "loc_photo_b"
            }
            edited_name=popup_name.text.toString()
            edited_loc=popup_loc.text.toString()
            edited_desc=popup_desc.text.toString()
            edited_lc_photo=(popup_loc_img.getDrawable() as BitmapDrawable).bitmap
            edited_itm_photo=(popup_item_img.getDrawable() as BitmapDrawable).bitmap//here
            edited_item_b=b
            img_w=popup_loc_img.width
            img_h=popup_loc_img.height
            dialog.dismiss()
            BtnCd(null,popup_loc_img)
        }

        //load images
        val item_p = "ITEM".plus(RightItem.item_name).plus(RightItem.item_location).plus(".jpg")
        val loc_p = "LOC".plus(RightItem.item_name).plus(RightItem.item_location).plus(".jpg")
        //try for item pic
        try {
            val f = File(images_folder_path, item_p)
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            popup_item_img.setImageBitmap(b)
            has_itm_img=true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        //try for loc pic
        try {
            val fi = File(images_folder_path, loc_p)
            val bi = BitmapFactory.decodeStream(FileInputStream(fi))
            popup_loc_img.setImageBitmap(bi)
            has_lc_img=true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        //if image was changed in edit
        if(edited_itm_photo!=null){
            //replace everything with edited counterparts
            if(edited_name!=null){
                popup_name.setText(edited_name)
                edited_name=null
            }
            if(edited_loc!=null){
                popup_loc.setText(edited_loc)
                edited_loc=null
            }
            if(edited_desc!=null){
                popup_desc.setText(edited_desc)
                edited_desc=null
            }
            popup_item_img.setImageBitmap(edited_itm_photo)
            edited_itm_photo=null
            edited_item_b=null
        }
        if(edited_lc_photo!=null){
            //replace everything with edited counterparts
            if(edited_name!=null){
                popup_name.setText(edited_name)
                edited_name=null
            }
            if(edited_loc!=null){
                popup_loc.setText(edited_loc)
                edited_loc=null
            }
            if(edited_desc!=null){
                popup_desc.setText(edited_desc)
                edited_desc=null
            }
            popup_loc_img.setImageBitmap(edited_lc_photo)
            edited_lc_photo=null
            edited_item_b=null
        }
    }

    fun CheckForErrors(n:String,l:String,d:String,original:String) :Boolean{
        var goodTOgo = true
        if(n==""&&l==""){
            NoNameAndLocError()
            goodTOgo=false
        }else if(n==""&&l!=""){
            NoNameError()
            goodTOgo=false
        }else if(n!=""&&l==""){
            NoLocError()
            goodTOgo=false
        }
        if(n==d && goodTOgo){
            InvalidDescription()
            goodTOgo=false
        }
        val sp = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val itms = sp.getString(MY_ITEMS, "")
        val tempString = n.plus(BetweenParamsSymbol).plus(l)
        if(itms.toString().contains(tempString)&&original!=n.plus(BetweenParamsSymbol).plus(l)&&goodTOgo==true){
            ItemAlreadyAddedError()
            goodTOgo=false
        }
        return goodTOgo
    }

    //saving the images in shared prefs
    fun SaveItmImg(img: Bitmap,exec : Boolean,name:String,loc:String){
        if(exec){
            var img_name = "ITEM"
            img_name=img_name.plus(name).plus(loc)
            val wrapper = ContextWrapper(applicationContext)
            var file = wrapper.getDir("images", Context.MODE_PRIVATE)
            file = File(file, "$img_name.jpg")
            try {
                // Get the file output stream
                val stream: OutputStream = FileOutputStream(file)

                img.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                // Flush the stream
                stream.flush()

                // Close stream
                stream.close()
            } catch (e: IOException){ // Catch the exception
                e.printStackTrace()
            }
        }
    }
    fun SaveLcImg(img: Bitmap,exec : Boolean,name:String,loc:String){
        if(exec){
            var img_name = "LOC"
            img_name=img_name.plus(name).plus(loc)
            val wrapper = ContextWrapper(applicationContext)
            var file = wrapper.getDir("images", Context.MODE_PRIVATE)
            file = File(file, "$img_name.jpg")
            try {
                // Get the file output stream
                val stream: OutputStream = FileOutputStream(file)

                img.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                // Flush the stream
                stream.flush()

                // Close stream
                stream.close()
            } catch (e: IOException){ // Catch the exception
                e.printStackTrace()
            }
        }
    }
    //handaling camera and permissions for camera

    private fun openCamera(btn: String){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE)
        lastBtnClicked = btn
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            if(lastBtnClicked=="item_photo_b"){
                edited_itm_photo=Bitmap.createScaledBitmap(imageBitmap, img_w, img_h, true)
                has_itm_img=true
            }else if(lastBtnClicked=="loc_photo_b"){
                edited_lc_photo=Bitmap.createScaledBitmap(imageBitmap, img_w, img_h, true)
                has_lc_img=true
            }
        }
        if(edited_item_b!=null){
            OpenItem(edited_item_b!!)
        }
    }

    private fun AskForPermission(permission: String,name:String,requestCode:Int){
        if(ContextCompat.checkSelfPermission(applicationContext,permission)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,arrayOf(permission),requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,permission: Array<out String>, grantResults: IntArray){
        if(requestCode==CAMERA_RQ){
            if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext,"Camera permission refused",Toast.LENGTH_SHORT).show()
                //bring back the popup the way it was edited before clicking on image button
                if(edited_item_b!=null){
                    OpenItem(edited_item_b!!)
                }
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

    //button cooldown to prevent pressing a button multiple times before the first one even executes
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
