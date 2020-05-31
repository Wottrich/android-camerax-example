package wottrich.github.io.api_camerax_example.view.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import wottrich.github.io.api_camerax_example.R

/**
 * @author Wottrich
 * @author lucas.wottrich@operacao.rcadigital.com.br
 * @since 30/05/20
 *
 * Copyright © 2020 Api-camerax-example. All rights reserved.
 *
 */

//Codigo da solicitação
private const val PERMISSION_REQUEST_CODE = 80

//Permissões que são requisitos para o aplicativo
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
 
class PermissionFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Verificando se o app tem todas as permissões que ele precisa
        if (!hasPermissions(
                requireContext()
            )
        ) {
            //Solicitando as permissões necessárias
            requestPermissions(
                PERMISSIONS_REQUIRED,
                PERMISSION_REQUEST_CODE
            )
        } else {
            //Se tivermos as permissões necessarias podemos prosseguir
            Navigation.findNavController(requireActivity(), R.id.nav_fragment_container)
                .navigate(PermissionFragmentDirections.actionPermissionToCamera())
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                //Se a permissão for concedida mandamos o usuario para a tela de camera
                Toast.makeText(context, "Permission request granted", Toast.LENGTH_SHORT).show()
                Navigation.findNavController(requireActivity(), R.id.nav_fragment_container)
                    .navigate(PermissionFragmentDirections.actionPermissionToCamera())
            } else {
                Toast.makeText(context, "Permission request denied", Toast.LENGTH_SHORT).show()
            }

        }

    }

    companion object {

        fun hasPermissions (context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    }

}