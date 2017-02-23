package swag_pact
package properties

import cats.Show
import cats.instances.string._
import cats.syntax.semigroup._
import cats.syntax.show._

object Compare {
  import CompareError._
  import ComparePath._

  def compare(expected: Property, actual: Property): List[CompareError] = {

    //    @tailrec
    def go(expected: Property, actual: Property, errs: List[CompareError], path: List[ComparePath]): List[CompareError] = {
      (expected, actual) match {
        case (ObjectProperty(expectedProps, _), ObjectProperty(actualProps, _)) =>
          val eee = expectedProps.toList.foldLeft(errs) { case (e, (key, prop)) =>
            val actualProp = actualProps.get(key)
            actualProp match {
              case None => MissingObjectKey(path, key) :: e
              case Some(ap) => go(prop, ap, e, ObjectPath(key) :: path)
            }
          }

          actualProps.keySet.diff(expectedProps.keySet).foldLeft(eee) { case (e, key) =>
            ExtraObjectKey(path, key) :: e
          }

        case (ArrayProperty(expectedArrayProp, _), ArrayProperty(actualArrayProp, _)) =>
          go(expectedArrayProp, actualArrayProp, errs, ArrayPath :: path)

        case (DoubleProperty(_), DoubleProperty(_)) => errs
        case (DoubleProperty(_), FloatProperty(_)) => errs
        case (DoubleProperty(_), LongProperty(_)) => errs
        case (DoubleProperty(_), IntProperty(_)) => errs
        case (FloatProperty(_), DoubleProperty(_)) => errs
        case (FloatProperty(_), FloatProperty(_)) => errs
        case (FloatProperty(_), LongProperty(_)) => errs
        case (FloatProperty(_), IntProperty(_)) => errs
        case (LongProperty(_), DoubleProperty(_)) => errs
        case (LongProperty(_), FloatProperty(_)) => errs
        case (LongProperty(_), LongProperty(_)) => errs
        case (LongProperty(_), IntProperty(_)) => errs
        case (IntProperty(_), DoubleProperty(_)) => errs
        case (IntProperty(_), FloatProperty(_)) => errs
        case (IntProperty(_), LongProperty(_)) => errs
        case (IntProperty(_), IntProperty(_)) => errs

        case (BooleanProperty(_), BooleanProperty(_)) => errs

        case (StringProperty(_), StringProperty(_)) => errs

        case (UnknownProperty(_), UnknownProperty(_)) => errs

        case (e, a) => TypeMismatch(path, e, a) :: errs
      }
    }
    go(expected, actual, Nil, Nil).reverse
  }
}

sealed trait ComparePath extends Product with Serializable
object ComparePath {
  case object ArrayPath extends ComparePath
  final case class ObjectPath(key: String) extends ComparePath

  def pathToString(path: List[ComparePath]): String = path match {
    case Nil => ""
    case ArrayPath :: Nil => "[]"
    case ObjectPath(key) :: Nil => key
    case ArrayPath :: ps => "[]" |+| pathToString(ps)
    case ObjectPath(key) :: ps => "." |+| key |+| pathToString(ps)
  }
}

sealed trait CompareError extends Product with Serializable {
  def path: List[ComparePath]
}
object CompareError {
  final case class TypeMismatch(path: List[ComparePath], expected: Property, actual: Property) extends CompareError
  final case class MissingObjectKey(path: List[ComparePath], key: String) extends CompareError
  final case class ExtraObjectKey(path: List[ComparePath], key: String) extends CompareError

  implicit val show: Show[CompareError] = Show.show {
    case TypeMismatch(path, expected, actual) =>
      ComparePath.pathToString(path) |+| ": expected " |+| expected.show |+| ": actual " |+| actual.show
    case MissingObjectKey(path, key) =>
      ComparePath.pathToString(path) |+| ": missing key " |+| key
    case ExtraObjectKey(path, key) =>
      ComparePath.pathToString(path) |+| ": extra key " |+| key
  }

}
