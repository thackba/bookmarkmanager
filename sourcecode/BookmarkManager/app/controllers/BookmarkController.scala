package controllers

import play.api.mvc.Action
import play.api.db.DB
import play.api.Play.current
import models.{Validator, Bookmark}
import play.api.data._
import play.api.data.Forms._

object BookmarkController extends AbstractProjectController {

  val bookmarkForm = Form(
    mapping(
      "id" -> longNumber,
      "owner" -> longNumber,
      "name" -> Validator.name,
      "url" -> Validator.url,
      "description" -> optional(text)
    )(Bookmark.apply)(Bookmark.unapply)
  )

  def listBookmarks = Action {
    implicit request => {
      if (account.isDefined) {
        Ok(views.html.listBookmarks())
      } else {
        Ok(views.html.index())
      }
    }
  }

  def listBookmarksAjax = MustBeLoggedInAction {
    implicit request => {
      DB.withTransaction {
        implicit connection => {
          val listOfBookMarks = Bookmark.loadAll(account.get)
          Ok(views.html.ajax.listBookmarksAjax(listOfBookMarks))
        }
      }
    }
  }

  def addBookmark = MustBeLoggedInAction {
    implicit request => {
      Ok(views.html.editBookmark(bookmarkForm.fill(Bookmark.create(account.get))))
    }
  }

  def editBookmark(id: Long) = MustBeLoggedInAction {
    implicit request => {
      DB.withTransaction {
        implicit connection => {
          val edit = Bookmark.loadOne(account.get, id)
          if (edit.isDefined) {
            Ok(views.html.editBookmark(bookmarkForm.fill(edit.get)))
          } else {
            NotFound
          }
        }
      }
    }
  }

  def deleteBookmark(id: Long) = Action {
    implicit request => {
      DB.withTransaction {
        implicit connection => {
          val delete = Bookmark.delete(account.get, id)
          if (delete.isDefined) {
            Ok("ok!")
          } else {
            NotFound
          }
        }
      }
    }
  }

  def saveBookmark = Action {
    implicit request => {
      bookmarkForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.editBookmark(errors))
        },
        success => {
          DB.withTransaction {
            implicit connection => {
              Bookmark.save(account.get, success)
              Redirect(routes.BookmarkController.listBookmarks())
            }
          }
        }
      )
    }
  }

}
