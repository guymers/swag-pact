package swag_pact
package properties

import io.circe.Json
import org.scalatest.EitherValues
import org.scalatest.FunSpec

class PropertiesJsonSpec extends FunSpec with EitherValues {

  describe("Properties from Json") {

    describe("null") {
      it("should convert to an unknown property") {
        val json = Json.Null
        val property = Property.fromJson(json)

        assert(property === UnknownProperty(required = Some(false)))
      }
    }

    describe("boolean") {
      it("should convert to a boolean property") {
        val json = Json.fromBoolean(true)
        val property = Property.fromJson(json)

        assert(property === BooleanProperty(None))
      }
    }

    describe("number") {
      it("should convert an int like number to an int property") {
        val json = io.circe.parser.parse("10").right.value
        val property = Property.fromJson(json)

        assert(property === IntProperty(None))
      }

      it("should convert a long like number to a long property") {
        val json = io.circe.parser.parse("100000000000").right.value
        val property = Property.fromJson(json)

        assert(property === LongProperty(None))
      }

      it("should convert a decimal like number to a double property") {
        val json = io.circe.parser.parse("10.34").right.value
        val property = Property.fromJson(json)

        assert(property === DoubleProperty(None))
      }
    }

    describe("string") {
      it("should convert to a string property") {
        val json = Json.fromString("value")
        val property = Property.fromJson(json)

        assert(property === StringProperty(None))
      }
    }

    describe("array") {
      it("should convert an empty array property") {
        val json = Json.arr()
        val property = Property.fromJson(json)

        assert(property === ArrayProperty(UnknownProperty(None), None))
      }

      it("should convert a non-empty array property") {
        val json = Json.arr(Json.fromString(""))
        val property = Property.fromJson(json)

        assert(property === ArrayProperty(StringProperty(None), None))
      }
    }

    describe("object") {
      it("should convert to a object property") {
        val json =
          Json.obj("key_a" -> Json.fromString("value_a"), "key_b" -> Json.fromString("value_b"))
        val property = Property.fromJson(json)

        assert(
          property === ObjectProperty(Map("key_a" -> StringProperty(None), "key_b" -> StringProperty(None)), None)
        )
      }
    }
  }
}
