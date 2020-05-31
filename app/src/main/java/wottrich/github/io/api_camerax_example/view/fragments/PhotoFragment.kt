package wottrich.github.io.api_camerax_example.view.fragments

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import wottrich.github.io.api_camerax_example.BuildConfig
import wottrich.github.io.api_camerax_example.R
import java.io.File

/**
 * @author Wottrich
 * @author lucas.wottrich@operacao.rcadigital.com.br
 * @since 30/05/20
 *
 * Copyright © 2020 Api-camerax-example. All rights reserved.
 *
 */

class PhotoFragment : Fragment() {

    private val args: PhotoFragmentArgs by navArgs()

    private lateinit var media: File
    private lateinit var root: ConstraintLayout
    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_photo, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        media = File(args.filePath)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root = view as ConstraintLayout
        imageView = view.findViewById(R.id.image_view)

        listeners()
        setImage()
    }

    private fun setImage() {
        Glide.with(requireActivity()).load(media).into(imageView)
    }

    private fun listeners() {

        val toolbar = root.findViewById<Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_fragment_container)
                .navigateUp()
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.itDelete -> {
                    deletePhoto()
                }
                R.id.itShare -> {
                    sharePhoto()
                }
                else -> return@setOnMenuItemClickListener false
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun deletePhoto() {

        //Deletando foto
        media.delete()

        //Notificando os outros apps que a foto foi deletada
        MediaScannerConnection.scanFile(
            root.context, arrayOf(media.absolutePath), null, null
        )

        //Voltando para o fragment anterior pois a foto foi deletada
        Navigation.findNavController(requireActivity(), R.id.nav_fragment_container).navigateUp()

    }

    private fun sharePhoto() {

        val intent = Intent().apply {

            //Media type da foto selecionada
            val mediaType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(media.extension)

            //Criando uri a partir do nosso FileProvider
            val uri = FileProvider.getUriForFile(
                root.context, BuildConfig.APPLICATION_ID + ".provider", media
            )

            //Setando actions e flags para compartilhamento
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mediaType
            action = Intent.ACTION_SEND
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        //Abrindo modal de opções de compartilhamento
        startActivity(Intent.createChooser(intent, "Share using"))
    }

}