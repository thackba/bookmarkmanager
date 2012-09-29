package models


/**
 * This case class will be used in the activation Form.
 *
 * @author thackbarth
 */
case class Activation(password: String, passwordRepeat: String)

/**
 * This case class will be used in the login Form.
 *
 * @author thackbarth
 */
case class Login(username: String, password: String)

/**
 * This case class will be used in the registration Form.
 *
 * @author thackbarth
 */
case class Registration(username: String, email: String,
                        forename: String, surname: String,
                        terms: Boolean = false)

/**
 * This case class will be used in the main template to transfer page values.
 *
 * @author thackbarth
 */
case class TemplateInfo(id: String, title: String)