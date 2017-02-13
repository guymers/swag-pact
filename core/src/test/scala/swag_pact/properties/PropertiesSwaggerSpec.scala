package swag_pact.properties

import io.swagger.models.Model
import io.swagger.models.properties.{StringProperty => SwaggerStringProperty}
import org.scalatest.{EitherValues, FunSpec}

class PropertiesSwaggerSpec extends FunSpec with EitherValues {

  private val definitions = Map.empty[String, Model]

  describe("Properties from Swagger") {

    describe("string") {
      it("should convert to a string property") {
        val swaggerProperty = new SwaggerStringProperty()
        val property = Property.fromSwagger(definitions, swaggerProperty)

        assert(property === StringProperty(required = Some(false)))
      }
    }
  }
}
