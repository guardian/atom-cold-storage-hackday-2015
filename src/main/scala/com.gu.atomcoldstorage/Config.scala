package com.gu.contentatomcoldstorage
import com.amazonaws.auth._

import com.typesafe.config.ConfigFactory

object Config {
  private val conf = ConfigFactory.load()

  lazy val dynamoDBTable = "atom-browser-hackday-2015"

  lazy val awsRegion = conf.getString("aws.region")

  lazy val kinesisStreamName = conf.getString("kinesis.stream.name")

  lazy val startWebServer = conf.getBoolean("start.webserver")
  lazy val startReader = conf.getBoolean("start.reader")

  lazy val credentialsProvider = new AWSCredentialsProviderChain(
    new EnvironmentVariableCredentialsProvider(),
    new SystemPropertiesCredentialsProvider(),
    new profile.ProfileCredentialsProvider("coldstorage"),
    new InstanceProfileCredentialsProvider()
  )

  lazy val stage = conf.getString("stage")

}
