package com.adadapted.androidadapted.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.adadapted.androidadapted.databinding.FragmentListBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration
import com.adadapted.library.ad.AdContentListener
import com.adadapted.library.atl.AddToListContent
import com.adadapted.library.keyword.InterceptMatcher
import com.adadapted.library.view.AndroidZoneView

class ListFragment : Fragment(),ListRecyclerAdapter.ItemClickListener, AdContentListener {

    private lateinit var listViewModel: ListViewModel
    private var _binding: FragmentListBinding? = null
    private var adapter: ListRecyclerAdapter? = null
    private var listAdZoneView: AndroidZoneView? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        listViewModel = ViewModelProvider(this).get(ListViewModel::class.java)
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val clearButton = binding.clearButton
        val addButton = binding.addButton
        val addItemText = binding.addItemText

        listAdZoneView = binding.listAdZoneView
        listAdZoneView?.init("101930") //init list ZoneView 100804 101930
        addButton.isVisible = false
        clearButton.isVisible = false

        setupRecyclerView()

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
        setupAutoComplete(addItemText)
        return root
    }

    override fun onStart() {
        super.onStart()
        listAdZoneView?.onStart(contentListener = this)
    }

    override fun onStop() {
        super.onStop()
        listAdZoneView?.onStop(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onContentAvailable(zoneId: String, content: AddToListContent) {
        val items = content.getItems()
        for (item in items) {
            adapter?.addItem(item.title)
            // Acknowledge the item(s) added to the list
            content.itemAcknowledge(item)
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        Toast.makeText(this.context, "You clicked " + adapter?.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = binding.listRecyclerView
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, 1)
        val sampleGroceries: ArrayList<String> = ArrayList()

        sampleGroceries.add("Milk")
        sampleGroceries.add("Eggs")
        sampleGroceries.add("Cheese")
        sampleGroceries.add("Bread")
        sampleGroceries.add("Coffee")

        recyclerView.layoutManager = LinearLayoutManager(this.context)
        adapter = ListRecyclerAdapter(this.context, sampleGroceries)
        adapter?.setClickListener(this)
        recyclerView.adapter = adapter

        recyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun setupAutoComplete(addItemText: AutoCompleteTextView) {
        val autoCompleteAddedItems = mutableSetOf<String>()
        val autoCompleteItems = mutableListOf("Milk", "Eggs", "Cheese", "Bread")
        val arrayAdapter = this.context?.let { ArrayAdapter(it, android.R.layout.select_dialog_item, autoCompleteItems) }

        addItemText.threshold = 3
        addItemText.setAdapter(arrayAdapter)

        addItemText.setOnItemClickListener { _, _, position, _ ->
            arrayAdapter?.getItem(position)?.let { adapter?.addItem(it) }
            addItemText.text.clear()
        }

        addItemText.doOnTextChanged { text, _, _, _ ->
            val suggestions = text?.let { InterceptMatcher.match(it) }
            if (suggestions != null) {
                for (suggestion in suggestions) {
                    if (!autoCompleteAddedItems.contains(suggestion.name)) {
                        arrayAdapter?.add(suggestion.name)
                        autoCompleteAddedItems.add(suggestion.name)
                        arrayAdapter?.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}