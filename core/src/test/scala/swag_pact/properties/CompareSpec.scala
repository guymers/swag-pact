package swag_pact
package properties

import org.scalatest.compatible.Assertion
import org.scalatest.EitherValues
import org.scalatest.FunSpec
import swag_pact.properties.CompareError.TypeMismatch
import swag_pact.properties.ComparePath.ArrayPath
import swag_pact.properties.ComparePath.ObjectPath

class CompareSpec extends FunSpec with EitherValues {

  describe("Compare unknowns") {
    it("unknown to unknown") {
      noErrors(UnknownProperty(None), UnknownProperty(None))
    }
  }

  describe("Compare booleans") {
    it("boolean to boolean") {
      noErrors(BooleanProperty(None), BooleanProperty(None))
    }

    it("boolean to string") {
      typeMismatch(BooleanProperty(None), StringProperty(None))
    }
  }

  describe("Compare strings") {
    it("string to string") {
      noErrors(StringProperty(None), StringProperty(None))
    }

    it("string to date") {
      noErrors(StringProperty(None), DateProperty(None))
    }

    it("string to datetime") {
      noErrors(StringProperty(None), DateTimeProperty(None))
    }

    it("string to uuid") {
      noErrors(StringProperty(None), UUIDProperty(None))
    }
  }

  describe("Compare dates") {
    it("date to date") {
      noErrors(DateProperty(None), DateProperty(None))
    }

    it("date to string") {
      typeMismatch(DateProperty(None), StringProperty(None))
    }
  }

  describe("Compare date times") {
    it("datetime to datetime") {
      noErrors(DateTimeProperty(None), DateTimeProperty(None))
    }
  }

  describe("Compare uuids") {
    it("uuid to uuid") {
      noErrors(UUIDProperty(None), UUIDProperty(None))
    }
  }

  describe("Compare numeric properties") {
    it("double to double") {
      noErrors(DoubleProperty(None), DoubleProperty(None))
    }
    it("double to float") {
      noErrors(DoubleProperty(None), FloatProperty(None))
    }
    it("double to long") {
      noErrors(DoubleProperty(None), LongProperty(None))
    }
    it("double to int") {
      noErrors(DoubleProperty(None), IntProperty(None))
    }

    it("float to double") {
      noErrors(FloatProperty(None), DoubleProperty(None))
    }
    it("float to float") {
      noErrors(FloatProperty(None), FloatProperty(None))
    }
    it("float to long") {
      noErrors(FloatProperty(None), LongProperty(None))
    }
    it("float to int") {
      noErrors(FloatProperty(None), IntProperty(None))
    }

    it("long to double") {
      noErrors(LongProperty(None), DoubleProperty(None))
    }
    it("long to float") {
      noErrors(LongProperty(None), FloatProperty(None))
    }
    it("long to long") {
      noErrors(LongProperty(None), LongProperty(None))
    }
    it("long to int") {
      noErrors(LongProperty(None), IntProperty(None))
    }

    it("int to double") {
      noErrors(IntProperty(None), DoubleProperty(None))
    }
    it("int to float") {
      noErrors(IntProperty(None), FloatProperty(None))
    }
    it("int to long") {
      noErrors(IntProperty(None), LongProperty(None))
    }
    it("int to int") {
      noErrors(IntProperty(None), IntProperty(None))
    }
  }

  describe("Compare arrays") {
    it("Array[String] to Array[String]") {
      noErrors(ArrayProperty(StringProperty(None), None), ArrayProperty(StringProperty(None), None))
    }

    it("Array[String] to Array[Int]") {
      val expected = StringProperty(None)
      val actual = IntProperty(None)
      val errors = Compare.compare(ArrayProperty(expected, None), ArrayProperty(actual, None))
      assert(errors === List(TypeMismatch(List(ArrayPath), expected, actual)))
    }
  }

  describe("Compare objects") {
    it("Object[String] to Object[String]") {
      val props = Map("str" -> StringProperty(None))
      noErrors(ObjectProperty(props, None), ObjectProperty(props, None))
    }

    it("Object[String] to Object[Int]") {
      val expectedProps = Map("str" -> StringProperty(None))
      val actualProps = Map("str" -> IntProperty(None))
      val errors = Compare.compare(ObjectProperty(expectedProps, None), ObjectProperty(actualProps, None))
      assert(errors === List(TypeMismatch(List(ObjectPath("str")), StringProperty(None), IntProperty(None))))
    }
  }

  private def noErrors(expected: Property, actual: Property): Assertion = {
    val errors = Compare.compare(expected, actual)
    assert(errors.isEmpty)
  }

  private def typeMismatch(expected: Property, actual: Property): Assertion = {
    val errors = Compare.compare(expected, actual)
    assert(errors === List(TypeMismatch(Nil, expected, actual)))
  }
}
