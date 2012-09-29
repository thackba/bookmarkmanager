package models

import play.api.data.Forms._
import play.api.data.validation.Constraints._
import util.matching.Regex
import play.api.i18n.Messages

/**
 * Created with IntelliJ IDEA.
 *
 * @author thackbarth
 */
object Validator {

  val forename = nonEmptyText(2, 48)
    .verifying(pattern(new Regex("[a-zA-Z äöüßÄÖÜ]{2,48}"), error = Messages("error.name")))

  val name = nonEmptyText(8, 192)

  val password = nonEmptyText(8, 48)

  val surname = text
    .verifying(pattern(new Regex("[a-zA-Z äöüßÄÖÜ]{0,48}"), error = Messages("error.name")))

  val url = nonEmptyText(8, 192)
    .verifying(Messages("error.url"), url => {
    val lowerUrl = url.toLowerCase
    ((lowerUrl.startsWith("http://")) || (lowerUrl.startsWith("https://")))
  })

  val username = nonEmptyText(6, 48)
    .verifying(pattern(new Regex("[a-zA-Z0-9_]{6,48}"), error = Messages("error.username")))

}
