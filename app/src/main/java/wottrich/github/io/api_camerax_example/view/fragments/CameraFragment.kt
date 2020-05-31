package wottrich.github.io.api_camerax_example.view.fragments

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import wottrich.github.io.api_camerax_example.view.MainActivity
import wottrich.github.io.api_camerax_example.R
import java.io.File
import java.lang.Exception
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import androidx.camera.core.ImageCapture.Metadata
import androidx.core.net.toFile
import androidx.navigation.Navigation
import java.nio.ByteBuffer
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class CameraFragment : Fragment() {

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: PreviewView

    //Operação de bloqueio de camera
    private lateinit var cameraExecutor: ExecutorService

    //Diretorio que vamos salvar nossas fotos
    private lateinit var outputDirectory: File

    //Solicitar ProcessCameraProvider usando ListenableFuture para poder dar bind do lifecycle
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    //ProcessCameraProvider agrupa os useCase e os lifecycles
    private var cameraProvider: ProcessCameraProvider? = null

    //Preview onde vamos sertar o SurfaceProvider do nosso PreviewView
    private var preview: Preview? = null

    //Nossa instancia de camera
    private var camera: Camera? = null

    //Usado para capturar a imagem
    private var imageCapture: ImageCapture? = null

    //Responsavel por fechar o proxy da image anterior
    //para não haver problemas em outras images registradas futuramente
    private var imageAnalyzer: ImageAnalysis? = null

    override fun onResume() {
        super.onResume()

        if (!PermissionFragment.hasPermissions(
                requireContext()
            )
        ) {
            Navigation.findNavController(requireActivity(), R.id.nav_fragment_container)
                .navigate(CameraFragmentDirections.actionCameraToPermission())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        //Desligar o background executor
        cameraExecutor.shutdown()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
        viewFinder = view.findViewById(R.id.view_finder)

        //Iniciando background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        //Pegando o diretorio onde vamos salvar nossa foto
        outputDirectory = MainActivity.getOutputDirectory(requireContext())

        viewFinder.post {

            //Setando listeners
            listeners()

            //Iniciando processCameraProvider
            setupProcessCameraProvider()

        }
    }

    private fun listeners () {

        container.findViewById<Button>(R.id.btn_take_photo).setOnClickListener {
            //Tirando foto
            takePhoto()
        }

        container.findViewById<Button>(R.id.btn_gallery).setOnClickListener {
            if (true == outputDirectory.listFiles()?.isNotEmpty()) {
                Navigation.findNavController(requireActivity(), R.id.nav_fragment_container)
                    .navigate(CameraFragmentDirections.actionCameraToGallery(outputDirectory.absolutePath))
            }
        }

    }

    private fun setupProcessCameraProvider () {

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            //Inicializando o CameraProvider
            this.cameraProvider = cameraProviderFuture.get()

            // Selecionando lensFacing dependendo das cameras disponiveis
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            //Bind no preview e adicionando lifecycle
            bindPreview()
        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private fun bindPreview () {
        val cameraProvider = this.cameraProvider ?: throw IllegalStateException("Camera initialization failed")

        this.preview = Preview.Builder().build()

        //Para selecionar se vai iniciar o preview com a camera traseira ou camera frontal
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(cameraExecutor,
                    LuminosityAnalyzer { luma ->
                        Log.d(
                            TAG,
                            "Average luminosity: $luma"
                        )
                    })
            }

        try {
            //Deve remover os binds dos use-cases antes de fazer o rebinding deles
            cameraProvider.unbindAll()

            //Adicionar os casos de uso que vão no bindToLifecycle
            this.camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )

            //Setar o SurfaceProvider
            this.preview?.setSurfaceProvider(viewFinder.createSurfaceProvider(camera?.cameraInfo))
        } catch (e: Exception) {
            Log.e(TAG, "bindPreview: ${e.message}")
        }

    }

    private fun takePhoto () {

        imageCapture?.let { imageCapture ->

            val photoFile =
                createFile(
                    outputDirectory,
                    FILENAME,
                    PHOTO_EXTENSION
                )

            //Metadata da imagem capturada
            val metadata = Metadata().apply {
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }

            //Criando as opções de saida com o outputDirectory + metadata
            val outputOptions = ImageCapture.OutputFileOptions
                .Builder(photoFile)
                .setMetadata(metadata)
                .build()

            //Image capture listener
            imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Image Capture failed", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    Log.d(TAG, "Photo capture succeeded: $savedUri")

                    //Se o device rodar com um API Level < 24 você deve rodar esse broodcast
                    //para atualizar a galeria
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        requireActivity().sendBroadcast(
                            Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                        )
                    }

                    //Digitalizando image capturada para outros aplicativos poderem acessar a foto
                    val mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(savedUri.toFile().extension)

                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(savedUri.toFile().absolutePath),
                        arrayOf(mimeType)
                    ) { _, uri -> Log.d(TAG, "Image scanned into media store: $uri")}
                }
            })

            Toast.makeText(context, "The photo was taken...", Toast.LENGTH_SHORT).show()

        }

    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    /**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     */
    private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listeners.forEach { it(luma) }

            image.close()
        }
    }

    companion object {

        private const val TAG = "CameraXExample"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /** Função usada para ficar uma file temporaria */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension)
    }
}