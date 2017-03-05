package swag_pact
package properties

import org.scalatest.FunSpec
import swag_pact.properties.ComparePath.ArrayPath
import swag_pact.properties.ComparePath.ObjectPath

class ComparePathSpec extends FunSpec {

  describe("Compare path to string") {
    it("converts an empty path") {
      val path = Nil
      assert(ComparePath.pathToString(path) === "")
    }

    it("converts an array path") {
      val path = List(ArrayPath)
      assert(ComparePath.pathToString(path) === "[]")
    }

    it("converts an object path") {
      val path = List(ObjectPath("str"))
      assert(ComparePath.pathToString(path) === "str")
    }

    it("converts an object, array path") {
      val path = List(ObjectPath("str"), ArrayPath)
      assert(ComparePath.pathToString(path) === "str[]")
    }

    it("converts an object, object path") {
      val path = List(ObjectPath("str"), ObjectPath("int"))
      assert(ComparePath.pathToString(path) === "str.int")
    }

    it("converts an object, object, array, object path") {
      val path = List(ObjectPath("str"), ObjectPath("int"), ArrayPath, ObjectPath("bool"))
      assert(ComparePath.pathToString(path) === "str.int[].bool")
    }
  }
}
