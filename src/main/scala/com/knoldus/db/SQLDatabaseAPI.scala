package com.knoldus.db

import com.knoldus.contracts.{ TableRow, TableRowValue }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

trait SQLDatabaseAPI[RS] {

  def execute[T](
    query: String,
    binds: Seq[String] = Seq.empty[String]
  )(rs: RS => Try[T])(implicit ec: ExecutionContext): Future[Seq[T]]

  def parseTableRows(results: RS): Try[TableRow]

  def parseSingleResult: RS => Try[TableRowValue] =
    parseTableRows _ andThen (_.map(_.values.headOption.get))

  def parseTableRowCount(results: RS): Try[Long]

}
