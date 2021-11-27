package db

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks._

class DocDbTest extends AnyFlatSpec with Matchers with BeforeAndAfter {

  "getAll" should "Work" in {

    forAll { (a: String, b: String) =>
      val doc = Doc(a, b)
    }
    val a = Doc("key1", "aosnteuhasoethusntao")
    val b = Doc("key2", "aosnteuhasoethusntao")
    val c = Doc("key3", "aosnteuhasoethusntao")

    val allToWrite = a :: b :: c :: Nil
    allToWrite.foreach(DocDb.append("test", _))

    val all = DocDb.getAll("test")

    allToWrite shouldBe all
  }

  "aoeu" should "aoeu" in {

    val a = ???
    // val result = mySin(a)

  }
}
