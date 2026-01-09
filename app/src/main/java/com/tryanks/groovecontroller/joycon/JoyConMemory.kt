package com.tryanks.groovecontroller.joycon

import android.content.Context
import java.io.File
import java.io.RandomAccessFile

class JoyConMemory(context: Context, type: ControllerType) {
    private val memoryFile: File = File(context.filesDir, "${type.btName}.bin")
    private val raf: RandomAccessFile

    init {
        if (!memoryFile.exists()) {
            context.resources.openRawResource(type.memoryResource).use { input ->
                memoryFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        raf = RandomAccessFile(memoryFile, "rw")
    }

    fun read(location: Int, length: Int): ByteArray {
        val result = ByteArray(length)
        raf.seek(location.toLong())
        raf.read(result)
        return result
    }

    fun write(location: Int, data: ByteArray) {
        raf.seek(location.toLong())
        raf.write(data)
    }
}
