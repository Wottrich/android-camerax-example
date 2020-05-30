package wottrich.github.io.api_camerax_example

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import wottrich.github.io.api_camerax_example.extensions.FLAGS_FULLSCREEN
import java.io.File

//tempo de Dalay para entrar no full screen
private const val FULLSCREEN_FLAG_TIMEOUT = 500L

class MainActivity : AppCompatActivity() {
    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.nav_fragment_container)
    }

    override fun onResume() {
        super.onResume()

        //Setando o "IMMERSIVE_MODE"
        container.postDelayed({
            container.systemUiVisibility = FLAGS_FULLSCREEN
        }, FULLSCREEN_FLAG_TIMEOUT)
    }

    companion object {

        /** Pegando as midias externas ou o diretorio de arquivos do aplicativo */
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply {
                    mkdirs()
                }
            }
            return if (mediaDir != null && mediaDir.exists()) mediaDir else appContext.filesDir
        }

    }

}
