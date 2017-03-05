package swag_pact
package properties

import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

import cats.Show
import io.circe.Json
import io.swagger.models.Model
import io.swagger.models.properties.{ArrayProperty => SwaggerArrayProperty}
import io.swagger.models.properties.{BaseIntegerProperty => SwaggerBaseIntegerProperty}
import io.swagger.models.properties.{BooleanProperty => SwaggerBooleanProperty}
import io.swagger.models.properties.{DateProperty => SwaggerDateProperty}
import io.swagger.models.properties.{DateTimeProperty => SwaggerDateTimeProperty}
import io.swagger.models.properties.{DecimalProperty => SwaggerDecimalProperty}
import io.swagger.models.properties.{DoubleProperty => SwaggerDoubleProperty}
import io.swagger.models.properties.{EmailProperty => SwaggerEmailProperty}
import io.swagger.models.properties.{FloatProperty => SwaggerFloatProperty}
import io.swagger.models.properties.{IntegerProperty => SwaggerIntegerProperty}
import io.swagger.models.properties.{LongProperty => SwaggerLongProperty}
import io.swagger.models.properties.{ObjectProperty => SwaggerObjectProperty}
import io.swagger.models.properties.{PasswordProperty => SwaggerPasswordProperty}
import io.swagger.models.properties.{Property => SwaggerProperty}
import io.swagger.models.properties.{RefProperty => SwaggerRefProperty}
import io.swagger.models.properties.{StringProperty => SwaggerStringProperty}
import io.swagger.models.properties.{UUIDProperty => SwaggerUUIDProperty}

import scala.util.Try

sealed trait Property extends Product with Serializable {
  def required: Option[Boolean]
}

final case class ObjectProperty(properties: Map[String, Property], required: Option[Boolean]) extends Property
final case class ArrayProperty(contains: Property, required: Option[Boolean]) extends Property

final case class DoubleProperty(required: Option[Boolean]) extends Property
final case class FloatProperty(required: Option[Boolean]) extends Property
final case class LongProperty(required: Option[Boolean]) extends Property
final case class IntProperty(required: Option[Boolean]) extends Property

final case class BooleanProperty(required: Option[Boolean]) extends Property

final case class DateProperty(required: Option[Boolean]) extends Property
final case class DateTimeProperty(required: Option[Boolean]) extends Property

final case class UUIDProperty(required: Option[Boolean]) extends Property
final case class StringProperty(required: Option[Boolean]) extends Property

final case class UnknownProperty(required: Option[Boolean]) extends Property

object Property {

  def fromSwagger(definitions: Map[String, Model], prop: SwaggerProperty): Property = {
    prop match {
      case refProp: SwaggerRefProperty =>
        val model = definitions.get(refProp.getSimpleRef)
        val properties =
          model.map(_.getProperties.asScalaMap).getOrElse(Map.empty[String, SwaggerProperty])
        fromSwaggerObject(prop, definitions, properties)

      case objectProp: SwaggerObjectProperty =>
        val properties = objectProp.getProperties.asScalaMap
        fromSwaggerObject(prop, definitions, properties)

      case arrayProp: SwaggerArrayProperty =>
        val convertedArrayProp = fromSwagger(definitions, arrayProp.getItems)
        ArrayProperty(convertedArrayProp, Option(prop.getRequired))

      case _: SwaggerLongProperty => LongProperty(Option(prop.getRequired))
      case _: SwaggerIntegerProperty => IntProperty(Option(prop.getRequired))
      case _: SwaggerBaseIntegerProperty => IntProperty(Option(prop.getRequired))

      case _: SwaggerDoubleProperty => DoubleProperty(Option(prop.getRequired))
      case _: SwaggerFloatProperty => FloatProperty(Option(prop.getRequired))
      case _: SwaggerDecimalProperty => FloatProperty(Option(prop.getRequired))

      case _: SwaggerBooleanProperty => BooleanProperty(Option(prop.getRequired))

      case _: SwaggerDateProperty => DateProperty(Option(prop.getRequired))
      case _: SwaggerDateTimeProperty => DateTimeProperty(Option(prop.getRequired))

      case _: SwaggerEmailProperty => StringProperty(Option(prop.getRequired))
      case _: SwaggerPasswordProperty => StringProperty(Option(prop.getRequired))
      case _: SwaggerUUIDProperty => StringProperty(Option(prop.getRequired))
      case _: SwaggerStringProperty => StringProperty(Option(prop.getRequired))

      case _ => UnknownProperty(Option(prop.getRequired))
    }
  }

  private def fromSwaggerObject(
    prop: SwaggerProperty,
    definitions: Map[String, Model],
    properties: Map[String, SwaggerProperty]
  ): ObjectProperty = {
    val props = properties.foldLeft(Map.empty[String, Property]) {
      case (map, (name, property)) =>
        map + (name -> fromSwagger(definitions, property))
    }
    ObjectProperty(props, Option(prop.getRequired))
  }

  def fromJson(json: Json): Property = json.fold(
    jsonNull = UnknownProperty(Some(false)),
    jsonBoolean = _ => BooleanProperty(None),
    jsonNumber = num => {
      num.toInt.map(_ => IntProperty(None)) orElse
        num.toLong.map(_ => LongProperty(None)) getOrElse
        DoubleProperty(None)
    },
    jsonString = str => {
      if (looksLikeDate(str)) DateProperty(None)
      else if (looksLikeDateTime(str)) DateTimeProperty(None)
      else if (looksLikeUUID(str)) UUIDProperty(None)
      else StringProperty(None)
    },
    jsonArray = arr => {
      val itemProp = arr.headOption.map(fromJson).getOrElse(UnknownProperty(None))
      ArrayProperty(itemProp, None)
    },
    jsonObject = obj => {
      val props = obj.toList.foldLeft(Map.empty[String, Property]) {
        case (map, (key, value)) =>
          map + (key -> fromJson(value))
      }
      ObjectProperty(props, None)
    }
  )

  private def looksLikeDate(str: String): Boolean = {
    Try { LocalDate.parse(str) }.toOption.isDefined
  }

  private def looksLikeDateTime(str: String): Boolean = {
    Try { ZonedDateTime.parse(str) }.toOption.isDefined
  }

  private def looksLikeUUID(str: String): Boolean = {
    Try { UUID.fromString(str) }.toOption.isDefined
  }

  implicit val show: Show[Property] = Show.fromToString
}
