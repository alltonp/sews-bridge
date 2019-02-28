package im.mange.sews.db

trait Store {
  def exists(key: String): Boolean
  def load(key: String): String
  def save(key: String, value: String): Unit
  def list: List[String]
}
