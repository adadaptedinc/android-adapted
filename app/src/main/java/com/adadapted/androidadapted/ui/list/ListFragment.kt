package com.adadapted.androidadapted.ui.list

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.adadapted.androidadapted.databinding.FragmentListBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration

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
        val clearButton = binding.clearButton
        val addButton = binding.addButton

        addButton.isVisible = false
        clearButton.isVisible = false

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

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, 1)
        recyclerView.addItemDecoration(dividerItemDecoration)

        val addItemText = binding.addItemText
        addItemText.setOnEditorActionListener { editText, _, _ ->
            if (!addItemText.text.isNullOrEmpty()) {
                adapter?.addItem(editText?.text.toString())
                addItemText.text.clear()
            }
            true
        }

        addButton.setOnClickListener {
            adapter?.addItem(addItemText.text.toString())
            addItemText.text.clear()
        }

        clearButton.setOnClickListener {
            addItemText.text.clear()
        }

        addItemText.doOnTextChanged { text, start, before, count ->
            if (count > 0) {
                addButton.isVisible = true
                clearButton.isVisible = true
            } else {
                addButton.isVisible = false
                clearButton.isVisible = false
            }
        }

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