package im.mange.sews.db

import im.mange.sews.Cmd

case class DbCmd[DB](db: Db[DB]) {
  def save(key: String, value: DB): Cmd = () => { db.save(key, value) }
}
