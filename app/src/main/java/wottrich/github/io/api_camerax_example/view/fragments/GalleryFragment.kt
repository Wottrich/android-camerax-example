package wottrich.github.io.api_camerax_example.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import wottrich.github.io.api_camerax_example.R
import wottrich.github.io.api_camerax_example.view.list.MediaAdapter
import wottrich.github.io.api_camerax_example.view.list.MediaListener
import java.io.File
import java.util.*

/**
 * @author Wottrich
 * @author lucas.wottrich@operacao.rcadigital.com.br
 * @since 30/05/20
 *
 * Copyright © 2020 Api-camerax-example. All rights reserved.
 *
 */

val EXTENSION_WHITELIST = arrayOf("JPG")

class GalleryFragment : Fragment() {

    private val args: GalleryFragmentArgs by navArgs()
    private var rootDictionary: File? = null

    //Filtrando a lista para pegar todos os files que tem o extension certo
    //e invertendo a lista pois os mais recentes ficam nos ultimos items de uma lista
    private val mediaList: MutableList<File>
        get() {
            return rootDictionary?.listFiles { file ->
                EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
            }?.sortedDescending()?.toMutableList() ?: mutableListOf()
        }

    private lateinit var root: ConstraintLayout
    private lateinit var recycler: RecyclerView

    private val adapter: MediaAdapter by lazy {
        MediaAdapter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Para o lifecycle não ser reiniciado
        retainInstance = true

        //Pegando as args
        rootDictionary = File(args.rootDirectory)
    }

    override fun onResume() {
        super.onResume()
        setupAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gallery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root = view as ConstraintLayout
        recycler = view.findViewById(R.id.recycler)

        listeners()
    }

    private fun setupAdapter() {
        val numberOfColumns =  if (mediaList.size % 2 == 0) 2 else 3
        recycler.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)
        recycler.adapter = adapter
        adapter.addItems(mediaList)
    }

    private fun listeners() {

        adapter.onMediaListener = object : MediaListener {
            override fun onClickListener(file: File) {
                Navigation.findNavController(requireActivity(), R.id.nav_fragment_container)
                    .navigate(GalleryFragmentDirections.actionGalleryToPhoto(file.absolutePath))
            }
        }

        root.findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_fragment_container)
                .navigateUp()
        }
    }

}