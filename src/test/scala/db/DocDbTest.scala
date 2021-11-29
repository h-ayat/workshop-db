package db

import scala.concurrent.{Future, ExecutionContext, duration}
import ExecutionContext.Implicits.global
import duration._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfter
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks._
import org.scalacheck.Gen
import scala.concurrent.Await

class DocDbTest extends AnyFlatSpec with Matchers with BeforeAndAfter {
  private implicit val generatorDrivenConfig =
    PropertyCheckConfiguration(minSuccessful = 500)

  val docGen = for {
    key <- Gen.alphaStr
    value <- Gen.alphaStr
  } yield Doc(key, value)

  val docsGen = Gen.listOf(docGen)

  "db" should "append and retrieve multiple rows" in {
    forAll(docsGen) { docList =>
      DocDb.cleanup("test")
      docList.foreach(DocDb.append("test", _))
      val all = DocDb.getAll("test")

      docList shouldBe all
    }
  }

  it should "apped and get all in parallel" in {
    forAll(docsGen) { docList =>
      val collectionName = "test2"
      DocDb.cleanup(collectionName)

      val futures = docList.map { doc =>
        Future {
          DocDb.append(collectionName, doc)
        }
      }
      Await.result(Future.sequence(futures), 2.minutes)

      val all = DocDb.getAll(collectionName)

      docList.toSet shouldBe all.toSet
    }
  }

  it should "getby key" in {
    forAll(docsGen) { rawDocList =>
      val docList = rawDocList.distinctBy(_.key)

      val keys = docList.map(_.key).toSet
      val collectionName = "test2"
      DocDb.cleanup(collectionName)

      docList.foreach(DocDb.append("test2", _))
      val all = DocDb.getAll(collectionName)

      docList
        .map(_.key)
        .flatMap(key => DocDb.getByKey(collectionName, key)) shouldBe docList

      forAll("randomKey") { randomKey: String =>
        whenever(!keys.contains(randomKey)) {
          DocDb.getByKey(collectionName, randomKey) shouldBe None
        }
      }
    }
  }
}
