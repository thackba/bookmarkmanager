package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.{Mode, Logger}
import play.api.i18n.Messages
import models.{Activation, Registration, Validator, Account}
import play.api.db.DB
import play.api.Play.current

/**
 * This Controller is responsible for the registration process.
 *
 * @author thackbarth
 */

object RegistrationController extends AbstractProjectController {

  /**
   * This form will be used during the registration process.
   */
  val registrationForm = Form(
    mapping(
      "username" -> Validator.username
        .verifying(Messages("error.userExist"), name => checkAccountNotExists(name)),
      "email" -> email,
      "forename" -> Validator.forename,
      "surname" -> Validator.surname,
      "terms" -> boolean.verifying(Messages("error.terms"), terms => terms)
    )(Registration.apply)(Registration.unapply))

  /**
   * This form will be used during the activation of an account.
   */
  val activationForm = Form(
    mapping(
      "password" -> Validator.password,
      "passwordRepeat" -> Validator.password
    )(Activation.apply)(Activation.unapply)
      .verifying("error.passwordNotEqual", activation => {
      activation.password.equals(activation.passwordRepeat)
    }))

  // --------- VALIDATOR --------

  def checkAccountNotExists(name: String): Boolean = {
    DB.withTransaction {
      implicit connection => {
        Account.checkAccountNotExists(name)
      }
    }
  }

  // --------- ACTIONS --------

  def register = MustBeNotLoggedInAction {
    implicit request => {
      Ok(views.html.register(registrationForm))
    }
  }

  def registerAction = MustBeNotLoggedInAction {
    implicit request => {
      registrationForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.register(errors))
        },
        success => {
          Logger.info("register " + success.username)
          DB.withTransaction {
            implicit connection => {
              // store registration in database
              val newId = Account.insertAccount(success)
              // Add the auth token to the flash in developer mode
              val token = if (current.mode == Mode.Dev) {
                connection.commit()
                Account.getActivationById(newId).getOrElse("--")
              } else {
                // TODO: Send Mail via actor
                "--"
              }
              Redirect(routes.RegistrationController.registered())
                .flashing("forename" -> success.forename,
                "username" -> success.username,
                "token" -> token)
            }
          }
        }
      )
    }
  }

  def registered = MustBeNotLoggedInAction {
    implicit request => {
      // read inserted data from flash
      val data = Registration(flash.get("username").getOrElse("--"), "",
        flash.get("forename").getOrElse("--"), flash.get("token").getOrElse("--"))
      Ok(views.html.registered(data))
    }
  }

  def activate(token: String) = MustBeNotLoggedInAction {
    implicit request => {
      DB.withTransaction {
        implicit connection => {
          val tokenAcc: Option[Account] = Account.findOneByActivateToken(token)
          if (tokenAcc.isDefined) {
            Ok(views.html.activate(token, activationForm))
          } else {
            Ok(views.html.activateError())
          }
        }
      }
    }
  }

  def activateAction(token: String) = MustBeNotLoggedInAction {
    implicit request => {
      activationForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.activate(token, errors))
        },
        success => {
          DB.withTransaction {
            implicit connection => {
              val tokenAcc: Option[Account] = Account.findOneByActivateToken(token)
              if (tokenAcc.isDefined) {
                Account.activate(tokenAcc.get.id, success.password)
                Redirect(routes.RegistrationController.activated())
              } else {
                Redirect(routes.RegistrationController.activate(token))
              }
            }
          }
        }
      )
    }
  }

  def activated = MustBeNotLoggedInAction {
    implicit request => {
      Ok(views.html.activated())
    }
  }

}
