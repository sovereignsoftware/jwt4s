package software.sovereign.jwt4s

sealed trait Algorithm {
  def name: String
}

object Algorithm {
  def apply(name: String) = {
    name.toUpperCase match {
      case "HS256" => HS256
      case _ => throw new Exception("Unknown algorithm")
    }
  }

  case object HS256 extends Algorithm {
    override val name = "HS256"
    override def toString = "Hmacsha256"
  }
}