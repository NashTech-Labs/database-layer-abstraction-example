package com.knoldus.db.connectionpool

import java.sql.Connection

import scala.util.Try

trait ConnectionProvider {

  def getConnection: Try[Connection]

}
