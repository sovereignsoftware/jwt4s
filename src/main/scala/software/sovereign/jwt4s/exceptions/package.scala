package software.sovereign.jwt4s

package object exceptions {
  case class InvalidAlgorithm(message: String) extends Exception
  case class InvalidToken(message: String) extends Exception
  case class InvalidHeader(message: String) extends Exception
  case class InvalidClaims(message: String) extends Exception
}
