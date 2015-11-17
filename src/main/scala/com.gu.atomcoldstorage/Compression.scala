package com.gu.contentatomcoldstorage

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream, IOException }
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import org.apache.commons.io.IOUtils

sealed trait CompressionType
case object NoneType extends CompressionType
case object GzipType extends CompressionType

object Compression {

  def gzip(data: Array[Byte]): Array[Byte] = {
    val bos = new ByteArrayOutputStream()
    val out = new GZIPOutputStream(bos)
    out.write(data)
    out.close()
    bos.toByteArray
  }

  def gunzip(buffer: ByteBuffer): ByteBuffer = {
    val bos = new ByteArrayOutputStream(8192)
    val in = new GZIPInputStream(new ByteBufferBackedInputStream(buffer))
    IOUtils.copy(in, bos)
    in.close()
    bos.close()
    ByteBuffer.wrap(bos.toByteArray())
  }

  def gunzip(data: Array[Byte]): Array[Byte] = {
    val buffer = ByteBuffer.wrap(data)
    val byteBuffer = gunzip(buffer)
    val result = new Array[Byte](byteBuffer.remaining)
    byteBuffer.get(result)
    result
  }

}
