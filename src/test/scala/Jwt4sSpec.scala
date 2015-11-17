package jwt4s

import java.nio.charset.StandardCharsets
import java.util.Date

import _root_.io.jsonwebtoken.impl.DefaultClaims
import _root_.io.jsonwebtoken.{SignatureAlgorithm, Jwts}
import argonaut.CodecJson
import org.specs2._
import software.sovereign.jwt4s.JWT
import jawn.ast.JawnFacade

class Jwt4sSpec extends mutable.Specification {

  val payload = """{"exp":1448038742,"iss":"1234567890"}"""
  val secret = "this_is_a_super_secure_shared_secret"
  val secretB64 = "dGhpc19pc19hX3N1cGVyX3NlY3VyZV9zaGFyZWRfc2VjcmV0"
  val expectedToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0NDgwMzg3NDIsImlzcyI6IjEyMzQ1Njc4OTAifQ.8UqWzwlNPCI-wf0rKVkILs0Nq_tMLpYpwXFF58mZgGg"


  val payload2 = new DefaultClaims()
  payload2.setIssuer("1234567890")
  payload2.setExpiration(new Date(1448038742000L))

  val jjwtToken = Jwts.builder.setHeaderParam("typ", "JWT").setClaims(payload2).signWith(SignatureAlgorithm.HS256, secretB64).compact


  "The Jwt4s library should" >> {
    "encode claims with a secret" >> {
      val jwt4s = JWT(secret)
      val token = jwt4s.encode(payload).run
      token === expectedToken
    }

    "decode claims with a secret" >> {
      val jwt4s = JWT(secret)

      val timeA = System.nanoTime

      val claims = jwt4s.decode(expectedToken)(JawnFacade).map { result =>
        val timeB = System.nanoTime
        println(s"#1 took ${(timeB-timeA)/1000000L} ms")
        result.render
      }.run

      val decoder = Jwts.parser.setSigningKey("dGhpc19pc19hX3N1cGVyX3NlY3VyZV9zaGFyZWRfc2VjcmV0")

      val timeC = System.nanoTime
      val claims2 = decoder.parse(expectedToken)
      val timeD = System.nanoTime
      println(s"#2 took ${(timeD-timeC)/1000000L} ms")

      claims === payload
    }
  }

}