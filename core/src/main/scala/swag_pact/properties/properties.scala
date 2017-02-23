package swag_pact
package properties

import cats.Show
import io.circe.Json
import io.swagger.models.Model
import io.swagger.models.properties.{
  ArrayProperty => SwaggerArrayProperty,
  BaseIntegerProperty => SwaggerBaseIntegerProperty,
  BooleanProperty => SwaggerBooleanProperty,
  DateProperty => SwaggerDateProperty,
  DateTimeProperty => SwaggerDateTimeProperty,
  DecimalProperty => SwaggerDecimalProperty,
  DoubleProperty => SwaggerDoubleProperty,
  EmailProperty => SwaggerEmailProperty,
  FloatProperty => SwaggerFloatProperty,
  IntegerProperty => SwaggerIntegerProperty,
  LongProperty => SwaggerLongProperty,
  ObjectProperty => SwaggerObjectProperty,
  PasswordProperty => SwaggerPasswordProperty,
  Property => SwaggerProperty,
  RefProperty => SwaggerRefProperty,
  StringProperty => SwaggerStringProperty,
  UUIDProperty => SwaggerUUIDProperty
}

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
        val properties = model.map(_.getProperties.asScalaMap).getOrElse(Map.empty)
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
    val props = properties.foldLeft(Map.empty[String, Property]) { case (map, (name, property)) =>
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
    jsonString = _ => StringProperty(None),
    jsonArray = arr => {
      val itemProp = arr.headOption.map(fromJson).getOrElse(UnknownProperty(None))
      ArrayProperty(itemProp, None)
    },
    jsonObject = obj => {
      val props = obj.toList.foldLeft(Map.empty[String, Property]) { case (map, (key, value)) =>
        map + (key -> fromJson(value))
      }
      ObjectProperty(props, None)
    }
  )

  implicit val show: Show[Property] = Show.fromToString
}
