package db

import scala.concurrent.Future
import java.io.FileWriter
import java.io.File
import java.io.FileReader
import java.io.BufferedReader
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

case class Doc(key: String, value: String)
trait DocDb {
  def append(collection: String, doc: Doc): Unit

  def getByKey(collection: String, key: String): Option[Doc]
  def getAll(collection: String): List[Doc]
}

object DocDb extends DocDb {

  private val baseAddress = "storage/"
  def cleanup(collection: String): Unit = {
    getCollectionFile(collection).delete()
  }

  private def getCollectionFile(name: String): File = new File(
    baseAddress + name + ".db"
  )

  override def append(
      collection: String,
      doc: Doc
  ): Unit = {
    import doc.{key, value}
    val writer = new FileWriter(getCollectionFile(collection), true)
    val keyArray = key.toCharArray()
    val valueArray = value.toCharArray()
    writer.write(keyArray.length)
    writer.write(valueArray.length)
    writer.write(key.toCharArray())
    writer.write(value.toCharArray())
    writer.close()
  }

  override def getByKey(
      collection: String,
      key: String
  ): Option[Doc] = ???

  override def getAll(collection: String): List[Doc] = {
    val reader = new FileInputStream(getCollectionFile(collection))

    def readSample(r: FileInputStream): Doc = {
      val keyLength = r.read()
      val valueLength = r.read()
      val keyArray = r.readNBytes(keyLength)
      val valueArray = r.readNBytes(valueLength)
      val key = new String(keyArray, StandardCharsets.UTF_8);
      val value = new String(valueArray, StandardCharsets.UTF_8);
      Doc(key, value)
    }
    val data = List.unfold[Doc, FileInputStream](reader) { r =>
      if (r.available() > 0)
        Some(readSample(r) -> r)
      else
        None
    }
    reader.close()

    data
  }

}