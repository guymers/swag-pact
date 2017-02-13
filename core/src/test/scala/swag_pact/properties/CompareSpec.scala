package swag_pact
package properties

import org.scalatest.compatible.Assertion
import org.scalatest.{EitherValues, FunSpec}
import swag_pact.properties.CompareError.TypeMismatch

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
      val expected = BooleanProperty(None)
      val actual = StringProperty(None)
      val errors = Compare.compare(expected, actual)
      assert(errors === List(TypeMismatch(Nil, expected, actual)))
    }
  }

  describe("Compare strings") {
    it("string to string") {
      noErrors(StringProperty(None), StringProperty(None))
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
      noErrors(
        ArrayProperty(StringProperty(None), None),
        ArrayProperty(StringProperty(None), None)
      )
    }
  }

  private def noErrors(expected: Property, actual: Property): Assertion = {
    val errors = Compare.compare(expected, actual)
    assert(errors.isEmpty)
  }
}
