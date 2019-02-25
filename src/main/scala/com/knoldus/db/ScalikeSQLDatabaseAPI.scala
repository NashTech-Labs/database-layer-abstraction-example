package com.knoldus.db

import java.sql.ResultSet

import com.knoldus.contracts.TableRowValue._
import com.knoldus.contracts.{ TableRow, TableRowValue }
import com.knoldus.db.connectionpool.ConnectionProvider
import scalikejdbc.{ DB, SQL }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try
import com.knoldus.utils.TryExtensions._

class ScalikeSQLDatabaseAPI(
  connectionProvider: ConnectionProvider
) extends SQLDatabaseAPI[ResultSet] {

  override def execute[T](
    query: String,
    binds: Seq[String] = Seq.empty[String]
  )(resultParser: ResultSet => Try[T])(implicit ec: ExecutionContext): Future[Seq[T]] =
    for {
      connection <- connectionProvider.getConnection.toFuture
      tryResults <- Future {
        // TODO get rid of scalikeJDBC. We're only using it in this block, the rest is done with pure JDBC.
        DB(connection) readOnly { implicit session =>
          val sql = if (binds.nonEmpty) SQL(query).bind(binds: _*) else SQL(query)
          sql.map(wrappedResultSet => resultParser(wrappedResultSet.underlying)).list.apply()
        }
      }
      results <- Try.sequence(tryResults).toFuture
    } yield results


  override def parseTableRows(results: ResultSet): Try[TableRow] = Try {
    val metadata = results.getMetaData
    val columnCount = metadata.getColumnCount

    val row = for {
      c <- 1 to columnCount
    } yield {
      val columnData = metadata.getColumnTypeName(c)
      toTableValue(columnData, Option(results.getString(c)))
    }
    TableRow(row)
  }

  override def parseTableRowCount(results: ResultSet): Try[Long] = Try {
    results.getInt(1)
  }

  private def toTableValue(dataType: String, data: Option[String]): TableRowValue = {
    data match {
      case Some(value) =>
        dataType match {
          case "VARCHAR" => StringValue(value)
          case "TINYINT" | "SMALLINT" | "INTEGER" => IntegerValue(value.toInt)
          case "BOOLEAN" => BooleanValue {
            value match {
              case "0" => false
              case "1" => true
              case unknown => throw new IllegalArgumentException(unknown)
            }
          }
          case "DECIMAL" => DoubleValue(value.toDouble)
        }
      case None => NullValue
    }
  }

}
