package com.adadapted.androidadapted.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.adadapted.androidadapted.databinding.FragmentListBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListFragment : Fragment(),ListRecyclerAdapter.ItemClickListener {

    private lateinit var listViewModel: ListViewModel
    private var _binding: FragmentListBinding? = null
    private var adapter: ListRecyclerAdapter? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        listViewModel =
            ViewModelProvider(this).get(ListViewModel::class.java)

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sampleGroceries: ArrayList<String> = ArrayList()
        sampleGroceries.add("Milk")
        sampleGroceries.add("Eggs")
        sampleGroceries.add("Cheese")
        sampleGroceries.add("Bread")
        sampleGroceries.add("Coffee")

        val recyclerView: RecyclerView = binding.listRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        adapter = ListRecyclerAdapter(this.context, sampleGroceries)
        adapter?.setClickListener(this)
        recyclerView.adapter = adapter

//        val textView: TextView = binding.textList
//        listViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(view: View?, position: Int) {
        Toast.makeText(this.context, "You clicked " + adapter?.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show()
    }
}