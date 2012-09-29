package models

import anorm._
import anorm.SqlParser._

import play.api.db._
import play.api.Play.current
import java.util

import utils.{StringHelper, HashCreator}
import anorm.~
import java.sql.Connection

/**
 * This class and the object will communicate with the DB table "account".
 *
 * @author thackbarth
 */

case class Account(id: Long,
                   username: String,
                   identifier: String,
                   email: String,
                   forename: String,
                   surname: Option[String])

object Account {

  val SESSION_ACCOUNT_ID = "ident"

  val parser = {
    get[Long]("id") ~
      get[String]("username") ~
      get[String]("identifier") ~
      get[String]("email") ~
      get[String]("forename") ~
      get[Option[String]]("surname") map {
      case id ~ username ~ ident ~ email ~ forename ~ surname =>
        Account(id, username, ident, email, forename, surname)
    }
  }

  val parserQuery = "select id, username, identifier, email, " +
    "forename, surname from account where "

  /**
   * This method activates the account with the entered id and sets the password.
   *
   * @param id the id of the account to activate
   * @param password the non hashed password. It will be hashed with a salt in this method.
   */
  def activate(id: Long, password: String)(implicit connection: Connection) {
    val salt = SQL("select salt from account where id={id}").on('id -> id)
      .as(scalar[String].singleOpt)
    if (salt.isDefined) {
      val passwordHash = HashCreator.createHash(salt.get, password)
      SQL("update account set password={password}, activation=null where id={id}")
        .on('password -> passwordHash, 'id -> id).executeUpdate()
    }
  }

  /**
   * Check that the lowercase username is not present in the database.
   *
   * @param username the entered username
   * @return true if the username is not present in the database
   */
  def checkAccountNotExists(username: String)(implicit connection: Connection): Boolean = {
    val result = SQL("select id from account where username_low={username}")
      .on('username -> username.toLowerCase)
    result.singleOpt().isEmpty
  }

  /**
   * Check that the entered credentials (username and password) are correct and belongs
   * to an account.
   *
   * @param username the entered username
   * @param password the entered password
   * @return the option of the id of the account to that the username belongs.
   */
  def checkCredentialsAreCorrect(username: String, password: String)(implicit connection: Connection): Option[Long] = {
    val salt = SQL("select salt from account where username_low={username}")
      .on('username -> username.toLowerCase).as(scalar[String].singleOpt)
    if (salt.isEmpty) {
      Option.empty
    } else {
      val passwordHash = HashCreator.createHash(salt.get, password)
      val accountId = SQL("select id from account where username_low={username} and password={password}")
        .on('username -> username.toLowerCase, 'password -> passwordHash).as(scalar[Long].singleOpt)
      // Set last login
      if (accountId.isDefined) {
        SQL("update account set lastlogin={lastlogin} where id={id}")
          .on('lastlogin -> new util.Date(), 'id -> accountId.get).executeUpdate()
      }
      // return the id of the account
      accountId
    }
  }

  /**
   * This method retrieves one account from the database by the activation token.
   *
   * @param token the activation token
   * @return the option of an account
   */
  def findOneByActivateToken(token: String)(implicit connection: Connection): Option[Account] = {
    SQL(parserQuery + "activation = {activation}")
      .on('activation -> token).as(Account.parser.singleOpt)
  }

  /**
   * This method retrieves one account from the database by the identifier.
   *
   * @param id the id (primary key) of the account
   * @return the option of an account
   */
  def findOneById(id: Long): Option[Account] = {
    DB.withTransaction {
      implicit conn => {
        SQL(parserQuery + "id = {id}")
          .on('id -> id).as(Account.parser.singleOpt)
      }
    }
  }

  /**
   * This method retrieves one account from the database by the identifier.
   *
   * @param identifier the identifier
   * @return the option of an account
   */
  def findOneByIdentifier(identifier: String)(implicit connection: Connection): Option[Account] = {
    SQL(parserQuery + "identifier = {identifier}")
      .on('identifier -> identifier).as(Account.parser.singleOpt)
  }

  /**
   * This method returns the activation code for an account.
   *
   * @param id id of the account to lock for.
   * @return the option of the activation code
   */
  def getActivationById(id: Long): Option[String] = {
    DB.withTransaction {
      implicit conn => {
        SQL("select activation from account where id={id}")
          .on('id -> id).as(scalar[String].singleOpt)
      }
    }
  }

  /**
   * This method reads the identifier for an entered username from the database.
   *
   * @param username the entered username
   * @return the option of the identifier
   */
  def getIdentifierByUsername(username: String)(implicit connection: Connection): Option[String] = {
    SQL("select identifier from account where username_low={username}")
      .on('username -> username.toLowerCase).as(scalar[String].singleOpt)
  }

  /**
   * This method creates a not activated account with the data from the RegistrationContext.
   *
   * @param registration the RegistrationContext with the account data
   * @return the id (primary key) of the new account
   */
  def insertAccount(registration: Registration)(implicit connection: Connection): Long = {
    val activateToken = HashCreator.createRandomSalt()
    val salt = HashCreator.createRandomSalt()
    val query = "insert into account (username, username_low, identifier, email, salt, " +
      "forename, surname, activation, createdate) values ({username}, {usernameLow}, " +
      "{ident}, {email}, {salt}, {forename}, {surname}, {activation}, {createdate})"
    SQL(query).on(
      'username -> registration.username,
      'usernameLow -> registration.username.toLowerCase,
      'ident -> HashCreator.createHash(salt, registration.username + registration.email),
      'email -> registration.email,
      'salt -> salt,
      'forename -> registration.forename,
      'surname -> StringHelper.clearString(registration.surname),
      'activation -> activateToken,
      'createdate -> new util.Date()
    ).executeInsert(scalar[Long].single)
  }

}
