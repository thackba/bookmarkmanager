package models

import java.sql.Connection
import anorm._
import anorm.SqlParser._

case class Bookmark(id: Long, owner: Long, name: String, url: String, description: Option[String])

object Bookmark {

  val UNSAVED = -1

  val parser = {
    get[Long]("id") ~
      get[Long]("owner") ~
      get[String]("name") ~
      get[String]("url") ~
      get[Option[String]]("description") map {
      case id ~ owner ~ name ~ url ~ description =>
        Bookmark(id, owner, name, url, description)
    }
  }

  val parserQuery = "select id, owner, name, url, description from bookmark where "

  def create(account: Account) = {
    Bookmark(Bookmark.UNSAVED, account.id, "", "http://", Option.empty)
  }

  def loadAll(account: Account)(implicit connection: Connection): List[Bookmark] = {
    SQL(parserQuery + "owner = {owner}")
      .on('owner -> account.id)
      .as(parser *)
  }

  def loadOne(account: Account, id: Long)(implicit connection: Connection): Option[Bookmark] = {
    SQL(parserQuery + "owner = {owner} and id = {id}")
      .on('owner -> account.id, 'id -> id)
      .as(parser.singleOpt)
  }

  def delete(account: Account, id: Long)(implicit connection: Connection): Option[Long] = {
    Some(SQL("delete from bookmark where owner = {owner} and id = {id}")
      .on('owner -> account.id, 'id -> id)
      .executeUpdate())
  }

  def save(account: Account, bookmark: Bookmark)(implicit connection: Connection): Option[Long] = {

    def insert: Option[Long] = {
      Some(SQL("insert into bookmark (owner, name, url, description) values ({owner}, {name}, {url}, {desc})")
        .on(
        'owner -> account.id,
        'name -> bookmark.name,
        'url -> bookmark.url,
        'desc -> bookmark.description
      ).executeInsert(scalar[Long].single))
    }

    def update: Option[Long] = {
      val updated = SQL("update bookmark set name={name}, url={url}, description={desc} where owner = {owner} and id={id}")
        .on(
        'name -> bookmark.name,
        'url -> bookmark.url,
        'desc -> bookmark.description,
        'owner -> account.id,
        'id -> bookmark.id
      ).executeUpdate()
      if (updated == 1) {
        Some(bookmark.id)
      } else {
        Option.empty
      }
    }

    if (bookmark.id == Bookmark.UNSAVED) {
      insert
    } else {
      update
    }
  }

}
