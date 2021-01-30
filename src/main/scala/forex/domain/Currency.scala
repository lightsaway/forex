package forex.domain

import cats.Show
import enumeratum.EnumEntry
import enumeratum._

import scala.collection.immutable

sealed abstract class Currency(val value: String) extends EnumEntry

object Currency extends Enum[Currency] {
  val values: immutable.IndexedSeq[Currency] = findValues

  case object AUD extends Currency("AUD")
  case object CAD extends Currency("CAD")
  case object CHF extends Currency("CHF")
  case object EUR extends Currency("EUR")
  case object GBP extends Currency("GBP")
  case object NZD extends Currency("NZD")
  case object JPY extends Currency("JPY")
  case object SGD extends Currency("SGD")
  case object USD extends Currency("USD")

  implicit val show: Show[Currency] = Show.show {_.value}

  def fromString(s: String): Option[Currency] = values.find(_.value.toUpperCase == s.toUpperCase)

  lazy val combinations: Set[Rate.Pair] = values.toList.combinations(2)
    .map{ case f ::t :: Nil => List(Rate.Pair(f,t), Rate.Pair(t, f))
    case _ => List.empty}.toList.flatten.toSet
}

