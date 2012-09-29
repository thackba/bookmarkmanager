package utils

/**
 * This class contains useful methods for Strings.
 *
 * @author thackbarth
 */

object StringHelper {

  def clearString(data: String): String = {
    if (data.trim.length == 0) {
      null
    } else {
      data.trim
    }
  }

}
