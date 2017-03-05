package swag_pact.properties

import java.util

import io.swagger.models.Model
import io.swagger.models.RefModel
import io.swagger.models.properties.{ArrayProperty => SwaggerArrayProperty}
import io.swagger.models.properties.{BooleanProperty => SwaggerBooleanProperty}
import io.swagger.models.properties.{DateProperty => SwaggerDateProperty}
import io.swagger.models.properties.{DateTimeProperty => SwaggerDateTimeProperty}
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
import org.scalatest.EitherValues
import org.scalatest.FunSpec

class PropertiesSwaggerSpec extends FunSpec with EitherValues {

  private val emptyDefinitions = Map.empty[String, Model]

  describe("Properties from Swagger") {

    describe("string") {
      it("should convert to a string property") {
        val swaggerProperty = new SwaggerStringProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === StringProperty(required = Some(false)))
      }
    }

    describe("long") {
      it("should convert to a long property") {
        val swaggerProperty = new SwaggerLongProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === LongProperty(required = Some(false)))
      }
    }

    describe("int") {
      it("should convert to an int property") {
        val swaggerProperty = new SwaggerIntegerProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === IntProperty(required = Some(false)))
      }
    }

    describe("double") {
      it("should convert to a double property") {
        val swaggerProperty = new SwaggerDoubleProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === DoubleProperty(required = Some(false)))
      }
    }

    describe("float") {
      it("should convert to a float property") {
        val swaggerProperty = new SwaggerFloatProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === FloatProperty(required = Some(false)))
      }
    }

    describe("boolean") {
      it("should convert to a boolean property") {
        val swaggerProperty = new SwaggerBooleanProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === BooleanProperty(required = Some(false)))
      }
    }

    describe("date") {
      it("should convert to a date property") {
        val swaggerProperty = new SwaggerDateProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === DateProperty(required = Some(false)))
      }
    }

    describe("datetime") {
      it("should convert to a datetime property") {
        val swaggerProperty = new SwaggerDateTimeProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === DateTimeProperty(required = Some(false)))
      }
    }

    describe("email") {
      it("should convert to a string property") {
        val swaggerProperty = new SwaggerEmailProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === StringProperty(required = Some(false)))
      }
    }

    describe("password") {
      it("should convert to a string property") {
        val swaggerProperty = new SwaggerPasswordProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === StringProperty(required = Some(false)))
      }
    }

    describe("uuid") {
      it("should convert to a string property") {
        val swaggerProperty = new SwaggerUUIDProperty()
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === StringProperty(required = Some(false)))
      }
    }

    describe("reference") {
      val objModel = new RefModel()
      objModel.setProperties {
        val props = new util.HashMap[String, SwaggerProperty]()
        props.put("str", new SwaggerStringProperty())
        props
      }

      val definitions = Map("ObjModel" -> objModel)

      it("should convert to an object property") {
        val swaggerProperty = new SwaggerRefProperty("ObjModel")
        val property = Property.fromSwagger(definitions, swaggerProperty)

        assert(
          property === ObjectProperty(
            properties = Map("str" -> StringProperty(required = Some(false))),
            required = Some(false)
          )
        )
      }
    }

    describe("object") {
      it("should convert to an object property") {
        val swaggerProperty = new SwaggerObjectProperty()
        swaggerProperty.property("str", new SwaggerStringProperty())
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(
          property === ObjectProperty(
            properties = Map("str" -> StringProperty(required = Some(false))),
            required = Some(false)
          )
        )
      }
    }

    describe("array") {
      it("should convert to an array property") {
        val swaggerProperty = new SwaggerArrayProperty(new SwaggerStringProperty())
        val property = Property.fromSwagger(emptyDefinitions, swaggerProperty)

        assert(property === ArrayProperty(contains = StringProperty(required = Some(false)), required = Some(false)))
      }
    }
  }
}
