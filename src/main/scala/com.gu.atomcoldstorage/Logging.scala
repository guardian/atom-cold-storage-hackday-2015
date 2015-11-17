package com.gu.contentatomcoldstorage

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

trait Logging {
  val log: Logger = LoggerFactory.getLogger(this.getClass)
}
