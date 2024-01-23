package com.votingsystem.politicalgroup.configs

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

final case class DbConfig(driver: String, nThreads: Int, host: String, dbName: String, user: String, password: String)

object DbConfig {
  implicit val dbConfigReader: ConfigReader[DbConfig] = deriveReader[DbConfig]
}
