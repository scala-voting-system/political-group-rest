package com.votingsystem.politicalgroup.configs

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class AppConfig(emberConfig: EmberConfig, dbConfig: DbConfig)

object AppConfig {
  implicit val appConfigReader: ConfigReader[AppConfig] = deriveReader[AppConfig]
}
