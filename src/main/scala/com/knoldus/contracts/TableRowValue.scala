package com.knoldus.contracts

sealed trait TableRowValue

object TableRowValue {

  case class StringValue(value: String) extends TableRowValue

  case class BooleanValue(value: Boolean) extends TableRowValue

  case class IntegerValue(value: Int) extends TableRowValue

  case class DoubleValue(value: Double) extends TableRowValue

  case object NullValue extends TableRowValue

}
