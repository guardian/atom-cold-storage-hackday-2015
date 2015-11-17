package com.gu.contentatomcoldstorage

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TIOStreamTransport
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream

import java.nio.ByteBuffer
import scala.util.Try

trait ThriftDeserializer[T <: ThriftStruct] {

  val codec: ThriftStructCodec[T]

  /**
   * Deserialize a Flexible API event from a Thrift-serialized byte buffer
   */
  def deserializeEvent(buffer: ByteBuffer): Try[T] = {
    Try {
      val settings = buffer.get()
      val compressionType = compression(settings)
      compressionType match {
        case NoneType => payload(buffer)
        case GzipType => payload(Compression.gunzip(buffer))
      }
    }
  }

  private def compression(settings: Byte): CompressionType = {
    val compressionMask = 0x07.toByte
    val compressionType = (settings & compressionMask).toByte
    compressionType match {
      case 0 => NoneType
      case 1 => GzipType
      case x => throw new RuntimeException(s"The compression type: ${x} is not supported")
    }
  }

  private def payload(buffer: ByteBuffer): T = {
    val bbis = new ByteBufferBackedInputStream(buffer)
    val transport = new TIOStreamTransport(bbis)
    val protocol = new TCompactProtocol(transport)
    codec.decode(protocol)
  }

}
