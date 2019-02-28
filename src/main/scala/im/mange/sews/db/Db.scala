package im.mange.sews.db

import im.mange.sews.JsonCodec

//TODO: is this Registry
//TODO: make a redis version
//TODO: make a postgres version
//TODO: make an in memory version
//TODO: this ultimately could support some of versioning/rollback by use of a atomic long ...

case class Db[DB](store: Store, codec: JsonCodec[DB, DB]) {
  def loadOrElse(key: String, default: DB): DB = if (store.exists(key)) load(key) else default
  def save(key: String, value: DB) { store.save(key, codec.encode(value, pretty = true)) }

  private def load(key: String): DB = codec.decode(store.load(key))
}