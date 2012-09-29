package utils

import java.security.{MessageDigest, SecureRandom}
import org.apache.commons.codec.binary.Base64

/**
 * This Creator creates hashes from Strings.
 *
 * @author thackbarth
 */

object HashCreator {

  val ALGORITHM = "sha1"

  def createHash(salt: String, hashValue: String): String = {
    val md: MessageDigest = MessageDigest.getInstance(ALGORITHM)
    md.update(salt.getBytes)
    val digest: Array[Byte] = md.digest(hashValue.getBytes)
    new String(Base64.encodeBase64URLSafe(digest))
  }

  def createRandomSalt(): String = {
    val random: SecureRandom = new SecureRandom()
    val md: MessageDigest = MessageDigest.getInstance(ALGORITHM)
    md.update(random.generateSeed(32))
    val digest: Array[Byte] = md.digest()
    new String(Base64.encodeBase64URLSafe(digest))
  }

}
