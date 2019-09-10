package com.github.potix2.spark.google.spreadsheets

import com.google.api.services.sheets.v4.model.{CellData, ExtendedValue, RowData}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import scala.collection.JavaConverters._
import scala.collection.TraversableOnce

object Util {
  def convert(schema: StructType, row: Row): Map[String, Object] =
    schema.iterator.zipWithIndex.map { case (f, i) => f.name -> row(i).asInstanceOf[AnyRef]} toMap

  def toRowData(row: Row): RowData =
      new RowData().setValues(
        row.schema.fields.zipWithIndex.map { case (f, i) =>
          new CellData()
            .setUserEnteredValue(
              f.dataType match {
                case DataTypes.StringType => new ExtendedValue().setStringValue(row.getString(i))
                case DataTypes.LongType => new ExtendedValue().setNumberValue(row.getLong(i).toDouble)
                case DataTypes.IntegerType => new ExtendedValue().setNumberValue(row.getInt(i).toDouble)
                case DataTypes.FloatType => new ExtendedValue().setNumberValue(row.getFloat(i).toDouble)
                case DataTypes.BooleanType => new ExtendedValue().setBoolValue(row.getBoolean(i))
                case DataTypes.DateType => new ExtendedValue().setStringValue(row.getDate(i).toString)
                case DataTypes.ShortType => new ExtendedValue().setNumberValue(row.getShort(i).toDouble)
                case DataTypes.TimestampType => new ExtendedValue().setStringValue(row.getTimestamp(i).toString)
                case DataTypes.DoubleType => new ExtendedValue().setNumberValue(row.getDouble(i))
              }
            )
        }.toList.asJava
      )

  def typeConverter(dataType: DataType, value: Any): ExtendedValue =  {
    def getValue(dataType: DataType, value: Any): Any = (dataType,value) match {
      case (_, null) | (NullType, _) => null
      case (StringType, v: String) => v
      case (TimestampType, v: java.sql.Timestamp) =>  v.toString
      case (IntegerType, v: Int) =>  v.toDouble
      case (ShortType, v: Short) =>  v.toDouble
      case (FloatType, v: Float) =>  v.toDouble
      case (DoubleType, v: Double) =>  v.toDouble
      case (LongType, v: Long) =>  v.toDouble
      case (DecimalType(), v: Decimal) => v.toJavaBigDecimal.toPlainString
      case (ByteType, v: Byte) => v.toDouble
      case (BinaryType, v: Array[Byte]) => v.mkString("")
      case (BooleanType, v: Boolean) => v
      case (DateType, v: Int) => v.toString
      case (ArrayType(ty, _), v: TraversableOnce[_]) =>
        v.map(x => getValue(ty, x)).toList.mkString("[",",","]")
      case (MapType(key, value, _), v: Map[_,_]) =>
        v.map(s => s"{${getValue(key, s._1)}:${getValue(value, s._2)}}").toList.mkString(",")
      case (StructType(ty), v: Object) => {
        v.toString
      }
    }

    getValue(dataType, value) match{
      case null => new ExtendedValue().setStringValue(null)
      case x: String => new ExtendedValue().setStringValue(x)
      case x: Double => new ExtendedValue().setNumberValue(x.toDouble)
      case x: Boolean => new ExtendedValue().setBoolValue(x)
    }
  }
}
