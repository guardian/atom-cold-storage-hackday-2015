package com.gu.contentatomcoldstorage

import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import java.util.UUID

import akka.actor.ActorRef
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.{ IRecordProcessor, IRecordProcessorFactory }
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration

import com.amazonaws.services.kinesis.clientlibrary.types.{ InitializationInput, ShutdownInput, ProcessRecordsInput }

class KinesisStreamReader {

  val appName = "content-atom-cold-storage-reader"

  def kinesisConfig = new KinesisClientLibConfiguration(
    appName,
    Config.kinesisStreamName,
    Config.credentialsProvider,
    appName + UUID.randomUUID().toString
  ).withRegionName(Config.awsRegion)

  class RecordProcessor extends IRecordProcessor {
    def initialize(input: InitializationInput) = ()
   def processRecords(input: ProcessRecordsInput) = ()
    def shutdown(input: ShutdownInput) = ()
  }

  class ProcessorFactory extends IRecordProcessorFactory {
    def createProcessor(): IRecordProcessor = new RecordProcessor()
  }

  def kinesisWorker: Worker = {
    (new Worker.Builder)
      .recordProcessorFactory(new ProcessorFactory)
      .config(kinesisConfig)
      .build
  }

  def kinesisThread = new Thread(kinesisWorker)

  //private lazy val kinesis = new AmazonKinesisClient

//   private def getRecords = {
//     val req =
// //kinesis.getRecords(x$1: GetRecordsRequest)
//   }

// lazy val worker = new Worker(

}
