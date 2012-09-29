package controllers

import models.{Validator, Login, Account}
import play.api.db.DB
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Action
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 *
 * @author thackbarth
 */
object LoginController extends AbstractProjectController {

  /**
   * This form will be used during the login.
   */
  val loginForm = Form(
    mapping(
      "username" -> Validator.username,
      "password" -> Validator.password
    )(Login.apply)(Login.unapply)
      .verifying("error.login", login => checkCredentialsAreCorrect(login.username, login.password)))

  // --------- VALIDATORS --------

  def checkCredentialsAreCorrect(username: String, password: String): Boolean = {
    DB.withTransaction {
      implicit connection => {
        Account.checkCredentialsAreCorrect(username, password).isDefined
      }
    }
  }

  def login = MustBeNotLoggedInAction {
    implicit request => {
      Ok(views.html.login(loginForm))
    }
  }

  def loginAction = MustBeNotLoggedInAction {
    implicit request => {
      loginForm.bindFromRequest.fold(
        errors => {
          // remove password from data
          val cleanData = errors.data - "password"
          BadRequest(views.html.login(errors.copy(data = cleanData)))
        },
        success => {
          // open transaction
          DB.withTransaction {
            implicit connection => {
              val identifier = Account.getIdentifierByUsername(success.username)
              if (identifier.isDefined) {
                Redirect(routes.BookmarkController.listBookmarks())
                  .withSession(session +(Account.SESSION_ACCOUNT_ID, identifier.get))
              } else {
                Redirect(routes.LoginController.login())
                  .withSession(session - Account.SESSION_ACCOUNT_ID)
              }
            }
          }
        }
      )
    }
  }

  def logout = Action {
    implicit request => {
      Redirect(routes.BookmarkController.listBookmarks()).withNewSession
    }
  }

}
