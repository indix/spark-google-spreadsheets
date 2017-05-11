package com.github.potix2.spark.google.spreadsheets

import com.google.api.services.sheets.v4.model.{CellData, ExtendedValue, RowData}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import scala.collection.JavaConverters._

object Util {
  def convert(schema: StructType, row: Row): Map[String, Object] =
    schema.iterator.zipWithIndex.map { case (f, i) => f.name -> row(i).asInstanceOf[AnyRef]} toMap

  def toRowData(row: Row): RowData =
      new RowData().setValues(
        row.schema.fields.zipWithIndex.map { case (f, i) =>
          new CellData()
            .setUserEnteredValue(
              f.dataType match {
                case DataTypes.TimestampType => typeConverter(f.dataType, row.getTimestamp(i).toString)
                case _ => typeConverter(f.dataType, row.get(i))
              }
            )
        }.toList.asJava
      )

  def typeConverter(dataType: DataType, value: Any): ExtendedValue = (dataType,value) match {
    case (_, null) | (NullType, _) => null
    case (StringType, v: String) => new ExtendedValue().setStringValue(v)
    case (TimestampType, v: java.sql.Timestamp) =>  new ExtendedValue().setStringValue(v.toString)
    case (IntegerType, v: Int) =>  new ExtendedValue().setNumberValue(v.toDouble)
    case (ShortType, v: Short) =>  new ExtendedValue().setNumberValue(v.toDouble)
    case (FloatType, v: Float) =>  new ExtendedValue().setNumberValue(v.toDouble)
    case (DoubleType, v: Double) =>  new ExtendedValue().setNumberValue(v.toDouble)
    case (LongType, v: Long) =>  new ExtendedValue().setNumberValue(v.toDouble)
    case (DecimalType(), v: Decimal) => new ExtendedValue().setStringValue(v.toJavaBigDecimal.toPlainString)
    case (ByteType, v: Byte) => new ExtendedValue().setNumberValue(v.toDouble)
    case (BinaryType, v: Array[Byte]) => new ExtendedValue().setStringValue(v.toString)
    case (BooleanType, v: Boolean) => new ExtendedValue().setBoolValue(v)
    case (DateType, v: Int) => new ExtendedValue().setStringValue(v.toString)
    case (ArrayType(ty, _), v: Array[_]) =>
      new ExtendedValue().setStringValue(v.map(x => typeConverter(ty, x)).toList.mkString("[",",","]"))
    case (MapType(key, value, _), v: Map[_,_]) =>
      new ExtendedValue().setStringValue(v.map(s => s"{${typeConverter(key, s._1)}:${typeConverter(value, s._2)}}").toList.mkString(","))
    case (StructType(ty), v: Object) => {
      new ExtendedValue().setStringValue(v.toString)
    }
  }
}
