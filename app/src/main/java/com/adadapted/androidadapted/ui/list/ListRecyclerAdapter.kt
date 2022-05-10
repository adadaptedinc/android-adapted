package com.adadapted.androidadapted.ui.list

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import com.adadapted.androidadapted.R
import com.adadapted.library.AdAdaptedListManager

class ListRecyclerAdapter(context: Context?, data: MutableList<String>?) : RecyclerView.Adapter<ListRecyclerAdapter.ViewHolder>() {
    private var mData: MutableList<String>? = data
    private var mInflater: LayoutInflater? = null
    private var mClickListener: ItemClickListener? = null

    init {
        mInflater = LayoutInflater.from(context)
    }

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater!!.inflate(R.layout.recycler_row, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData!![position]
        holder.itemTextView.text = item
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData!!.size
    }

    fun addItem(itemName: String) {
        mData?.add(itemName)
        notifyDataSetChanged()
    }

    fun removeItem(index: Int) {
        mData?.removeAt(index)
        notifyDataSetChanged()
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val itemTextView: TextView = itemView.findViewById(R.id.itemName)
        override fun onClick(view: View?) {
            mClickListener?.onItemClick(view, adapterPosition)
        }
        init {
            itemView.setOnClickListener(this)
            itemView.findViewById<ImageButton>(R.id.delete_button).setOnClickListener {
                removeItem(adapterPosition)
                AdAdaptedListManager.itemDeletedFromList(itemTextView.text.toString(), "Main Grocery List")
            }
        }
    }

    // convenience method for getting data at click position
    fun getItem(id: Int): String {
        return mData!![id]
    }

    // allows clicks events to be caught
    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}