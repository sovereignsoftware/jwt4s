package software.sovereign.jwt4s

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.util
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import jawn.{Facade, Parser, AsyncParser}
import jawn.ast._
import software.sovereign.jwt4s.exceptions.{InvalidClaims, InvalidHeader, InvalidToken}

import scala.util.{Success, Failure, Try}
import scalaz.{\/, -\/, \/-}
import scalaz.concurrent.{Future, Task}

final case class JWT(
  secret: Array[Byte],
  algorithm: Algorithm
) {

  val base64Encoder = Base64.getUrlEncoder.withoutPadding
  val base64Decoder = Base64.getUrlDecoder
  val bdot = ".".getBytes(UTF_8)
  val hs256Header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9".getBytes(UTF_8)
  val keySpec = new SecretKeySpec(secret, algorithm.toString)

  /**
    * Verify a token's integrity. This only checks if the token has a valid signature.
    *
    * @param payload
    * @return
    */
  def verify(payload: Array[Byte], signature: Array[Byte]): Task[Boolean] = Task {
    val mac = Mac.getInstance(algorithm.toString)
    mac.init(keySpec)
    val mySignature = mac.doFinal(payload)
    util.Arrays.equals(mySignature, signature)
  }

  /**
    * Encode a Json object
    *
    * Base64(header) + "." + Base64(claims) + "." + Base64(Hmacsha256(Base64(header) + "." + Base64(claims)))
    *
    * @param claims
    * @return eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiIxMjM0NTY3ODkwIiwiZXhwIjoiMTQ0NzU2NDA4OCJ9.MENDNjJFNEM5OUFEODc3NzVBM0VDRjIyODExRTFDNzQzNzY1QjE2NjYwQTIzQ0JFMzYzNDZBQzI0OUYwQjcwMg==
    */
  def encode[A](claims: String): Task[String] = Task {
    val bHeader =
      if (algorithm.name == "HS256") hs256Header
      else base64Encoder.encode(s"""{"typ":"JWT","alg":"${algorithm.name}"}""".getBytes(UTF_8))
    val bClaims = base64Encoder.encode(claims.getBytes(UTF_8))
    val payload = bHeader ++ bdot ++ bClaims

    println("payload: " + new String(payload, UTF_8))

    val mac = Mac.getInstance(algorithm.toString)
    mac.init(keySpec)
    val hmac = mac.doFinal(payload)

    new String(payload ++ bdot, UTF_8) ++ base64Encoder.encodeToString(hmac)
  }

  /**
    * Verify a token and return its decoded Json payload.
    *
    * @param token
    * @return
    */
  def decode[J](token: String)(implicit f: Facade[J]): Task[J] = {

    val parts = token.split('.')
    if (parts.length != 3) Task.fail(InvalidToken(s"Expected a 3-part token but found a ${parts.length} token."))
    else {
      val base64Header = parts(0)
      val base64Claims = parts(1)
      val base64Signature = parts(2)

      val header = base64Decoder.decode(base64Header)
      val claims = base64Decoder.decode(base64Claims)
      val signature = base64Decoder.decode(base64Signature)

      val payload = (base64Header + "." + base64Claims).getBytes(UTF_8)

      for {
        sigValid <- verify(payload, signature)
        _ <- if (!sigValid) Task.fail(InvalidToken("The signature of this token is invalid. Do not trust the integrity of this token!")) else Task.now(())

        claims <- new Task(Future {
          val timeA = System.nanoTime

          val tryClaims = for {
            header <- JParser.parseFromByteBuffer(ByteBuffer.wrap(header))
            _ <- header.get("typ").getString match {
              case Some(typ) if typ == "JWT" => Success(())
              case _ => Failure(InvalidHeader("Header must have typ: JWT"))
            }
            claims <- Parser.parseFromByteBuffer(ByteBuffer.wrap(claims))
          } yield claims

          tryClaims match {
            case Success(claims) => \/.right(claims)
            case Failure(ex) => \/.left(ex)
          }
        })
      } yield claims
    }
  }
}

object JWT {
  def apply(secret: String): JWT = {
    new JWT(secret.getBytes(UTF_8), Algorithm.HS256)
  }

  def apply(secret: Array[Byte]): JWT = {
    new JWT(secret, Algorithm.HS256)
  }

  def apply(secret: Array[Byte], algo: Option[String] = None): JWT = {
    new JWT(secret,Algorithm(algo.getOrElse("HS256")))
  }

  def apply(secret: String, algo: Option[String]): JWT = {
    new JWT(secret.getBytes(UTF_8), Algorithm(algo.getOrElse("HS256")))
  }


  def encodeBase64(bytes: Array[Byte]): String = {
    javax.xml.bind.DatatypeConverter.printBase64Binary(bytes)
  }

  def decodeBase64(base64: String): Array[Byte] = {
    javax.xml.bind.DatatypeConverter.parseBase64Binary(base64)
  }
}

