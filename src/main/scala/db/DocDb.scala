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

  private val baseAddress = {
    new File("storage/").mkdirs()
    "storage/"
  }

  def cleanup(collection: String): Unit = {
    val f = getCollectionFile(collection)
    if (f.exists())
      getCollectionFile(collection).delete()
  }

  private def getCollectionFile(name: String): File = {
    val f = new File(
      baseAddress + name + ".db"
    )

    if (!f.exists()) {
      val writer = new FileWriter(f)
      writer.close()
    }

    f
  }

  override def append(
      collection: String,
      doc: Doc
  ): Unit = {
    collection.synchronized {
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
  }

  override def getByKey(
      collection: String,
      key: String
  ): Option[Doc] = getAll(collection).find(_.key == key)

  override def getAll(collection: String): List[Doc] = {
    collection.synchronized {
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
}
