package com.martinprice20.fileclientapp

import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.martinprice20.fileclientapp.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Byte
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

private const val MAIN_ACTIVITY = "MainActivity"

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fileContents: EditText
    private lateinit var getButton: Button
    private lateinit var requestFileIntent: Intent
    private lateinit var inputPFD: ParcelFileDescriptor
    private lateinit var contentResolver: ContentResolver
    private val sb = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contentResolver = this.getContentResolver()

        getButton = binding.getFilesButton
        fileContents = binding.fileContentsTextInput
        requestFileIntent = Intent(Intent.ACTION_PICK).apply {
            type = "text/plain"
        }
        getButton.setOnClickListener { requestFile() }

        binding.saveToFileButton.setOnClickListener {
            saveToFile()
        }
    }

    private fun saveToFile() {
        if (sb.toString().trim() != "") {
            try {
                val folder = File(this.filesDir, File.separator + FILE_PATH)
                if (!folder.exists()) {
                    folder.mkdir()
                }
                val filePath = File(this.filesDir, FILE_PATH)
                val newFile = File(filePath, "my_secret_data.txt")
                FileOutputStream(newFile, false).use {
                    it.write(sb.toString().toByteArray())
                }
                Toast.makeText(this, getString(R.string.create_file_success),
                    Toast.LENGTH_SHORT).show()
                fileContents.setText("")
            } catch (e: IOException) {
                Log.e(TAG, resources.getString(R.string.io_exception_msg))
            }
        } else {
            Toast.makeText(this, getString(R.string.file_no_content_error_msg),
                Toast.LENGTH_SHORT).show()
            Log.e(TAG, resources.getString(R.string.file_no_content_error))
        }
    }

    private fun requestFile() {
        startActivityForResult(requestFileIntent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data?.data?.also { returnUri ->
            inputPFD = try {
                contentResolver.openFileDescriptor(returnUri, "r")!!
            } catch (e: FileNotFoundException) {
                Log.e(MAIN_ACTIVITY, "File Not Found")
                return
            }

            val fd = inputPFD.fileDescriptor
            val input = FileInputStream(fd)
            var i = input.read()
            while (i != -1) {
                sb.append(Char(i))
                println(Char(i))
                i = input.read()
            }
            input.close()
        }
        fileContents.setText(sb.toString())
    }

    companion object {
        const val FILE_PATH = "secretdata"
        const val TAG = "Main Activity"


    }
}