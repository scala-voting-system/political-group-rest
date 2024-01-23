package com.votingsystem.politicalgroup.configs

import com.comcast.ip4s.{ Host, Port }
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.semiauto._

final case class EmberConfig(host: Host, port: Port)

object EmberConfig {

  implicit val hostReader: ConfigReader[Host] = ConfigReader[String].emap { hostString =>
    Host
      .fromString(hostString)
      .toRight(CannotConvert(hostString, Host.getClass.toString, s"Invalid host string: $hostString"))
  }

  implicit val portReader: ConfigReader[Port] = ConfigReader[Int].emap { portNumber =>
    Port
      .fromInt(portNumber)
      .toRight(CannotConvert(portNumber.toString, Port.getClass.toString, s"Invalid port number $portNumber"))
  }

  implicit val emberConfigReader: ConfigReader[EmberConfig] = deriveReader[EmberConfig]
}
