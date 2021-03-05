package com.stefan.neverlost

import android.widget.Button
import android.widget.LinearLayout

data class Item(val n: String,val l: String,val d: String){
    var item_name=n
    var item_location=l
    var item_description=d
    lateinit var item_button: Button
    lateinit var item_layout: LinearLayout
}