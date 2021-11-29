package db

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks._
import org.scalacheck.Gen

class DocDbTest extends AnyFlatSpec with Matchers with BeforeAndAfter {

  val docGen = for {
    key <- Gen.asciiStr
    value <- Gen.asciiStr
  } yield Doc(key, value)

  val docsGen = Gen.listOf(docGen)

  "db" should "Work" in {
    forAll(docsGen) { docList =>
      println(docList.length)
      DocDb.cleanup("test")
      docList.foreach(DocDb.append("test", _))
      val all = DocDb.getAll("test")

      docList shouldBe all
    }
  }
}
