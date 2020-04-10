package com.example.instagramapp.adapters

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.models.Image
import kotlinx.android.synthetic.main.grid_layout_image.view.*

class RecyclerViewImageAdapter(var mainActivity : Context) : RecyclerView.Adapter<RecyclerViewImageAdapter.MyViewHolder>(){

    private var imageList : ArrayList<Image> = ArrayList<Image>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewImageAdapter.MyViewHolder {

        var view = LayoutInflater.from(mainActivity).inflate(R.layout.grid_layout_image, parent, false)
        var viewHolder = MyViewHolder(view)

        return viewHolder
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewImageAdapter.MyViewHolder, position: Int) {
        holder.bind(imageList!!.get(position))
    }

    fun setData(imageList : ArrayList<Image>){
        this.imageList = imageList
        notifyDataSetChanged()
    }

    inner class MyViewHolder(var view : View) : RecyclerView.ViewHolder(view){

        fun bind(image : Image){

            var uri = Uri.parse(image.imagePath)
            view.image_view.setImageURI(uri)

        }

    }

}