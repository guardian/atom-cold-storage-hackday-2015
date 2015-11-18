package com.gu.contentatomcoldstorage

import com.amazonaws.services.kinesis.metrics.interfaces.MetricsLevel
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker
import java.util.UUID

import scala.collection.JavaConverters._

import akka.actor.ActorRef
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.{ IRecordProcessor, IRecordProcessorFactory }
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration

import com.amazonaws.services.kinesis.clientlibrary.types.{ InitializationInput, ShutdownInput, ProcessRecordsInput }

class KinesisStreamReader(messageProcessor: ActorRef) extends Logging {

  val appName = "content-atom-cold-storage-reader"

  log.info(s"Kinesis stream name: ${Config.kinesisStreamName}")

  def kinesisConfig = new KinesisClientLibConfiguration(appName,
                                                        Config.kinesisStreamName,
                                                        Config.credentialsProvider,
                                                        appName + UUID.randomUUID().toString)
    .withRegionName(Config.awsRegion)
    .withMetricsLevel(MetricsLevel.NONE)

  class RecordProcessor extends IRecordProcessor {
    def initialize(input: InitializationInput) = ()

    def processRecords(input: ProcessRecordsInput) = {
      input.getRecords.asScala.foreach(rec => messageProcessor ! rec)
      input.getCheckpointer.checkpoint
    }

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

}
