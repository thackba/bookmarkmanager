package controllers

import models.Account
import play.api.mvc._
import play.api.i18n.Lang
import play.api.cache.Cache
import play.api.db.DB
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 *
 * @author thackbarth
 */
trait AbstractProjectController extends Controller {

  val CACHE_IDENTIFIER = "ident."

  /**
   * This method returns a possible current account from the session.
   *
   * @param request the implicit request to get the session from.
   * @return the option of current account.
   */
  implicit def account(implicit request: RequestHeader): Option[Account] = {
    val identifier: Option[String] = request.session.get(Account.SESSION_ACCOUNT_ID)
    if (identifier.isDefined) {
      Cache.getOrElse(CACHE_IDENTIFIER + identifier.get)({
        // read account from db
        DB.withTransaction {
          implicit conn => {
            Account.findOneByIdentifier(identifier.get)
          }
        }
      })
    } else {
      Option.empty
    }
  }

  override implicit def lang(implicit request: RequestHeader) = {
    Lang("de")
  }

  // --------- ACTIONS --------

  def MustBeNotLoggedInAction(f: Request[AnyContent] => Result): Action[AnyContent] = {
    Action {
      implicit request => {
        if (account.isEmpty) {
          f(request)
        } else {
          Redirect(routes.BookmarkController.listBookmarks())
        }
      }
    }
  }

  def MustBeLoggedInAction(f: Request[AnyContent] => Result): Action[AnyContent] = {
    Action {
      implicit request => {
        if (account.isDefined) {
          f(request)
        } else {
          Redirect(routes.LoginController.login())
        }
      }
    }
  }
}
