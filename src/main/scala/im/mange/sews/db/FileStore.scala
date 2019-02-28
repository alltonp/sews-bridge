package im.mange.sews.db

import java.io.File
import java.nio.file._

import im.mange.little.file.Filepath

case class FileStore(root: String) extends Store {
  //TODO: use Filepath.exists()
  def exists(key: String): Boolean = file(key).toFile.exists()
  def load(key: String): String = Filepath.load(file(key))
  def save(key: String, value: String) { Filepath.save(value, file(key)) }

  def list: List[String] =
    Filepath.listDir(FileSystems.getDefault.getPath(root), "*.json").map(_.replace(".json", ""))

  //TODO: pass bits separately, to avoid separator
  private def file(name: String) = Paths.get(s"$root${File.separator}$name.json")
}
