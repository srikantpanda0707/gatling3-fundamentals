package videogameDB.feeders

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random

class ComplexcustomFeeder extends Simulation{

  val httpProtocol = http.baseUrl("https://videogamedb.uk/api")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  var idNumbers = (1 to 10).iterator
  var rnd = new Random()
  val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val now = LocalDate.now()

  def randomString(length: Int) = {
     rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def getRandomDate(startDate: LocalDate,random: Random):String ={
    startDate.minusDays(random.nextInt(30)).format(pattern)
  }

  val customFeeder = Iterator.continually(Map(
    "gameId" -> idNumbers.next(),
    "name" -> ("Game-" + randomString(5)),
    "releaseDate" -> getRandomDate(now, rnd),
    "reviewScore" -> rnd.nextInt(100),
    "category" -> ("Category-" + randomString(6)),
    "rating" -> ("Rating-" + randomString(4))
  ))

  def authenticate(): ChainBuilder = {
    exec(http("Authenticate")
    .post("/authenticate")
    .body(StringBody(
      "{\n  \"password\": \"admin\",\n  \"username\": \"admin\"\n}"
    ))
    .check(jsonPath("$.token").saveAs("token")))
  }

  def createNewGame(): ChainBuilder = {
    repeat(10){
      feed(customFeeder)
        .exec(http("create new Game - #{name}")
        .post("/videogame")
        .header("authorization","Bearer #{token}")
        .body(ElFileBody("bodies/newGameTemplate.json")).asJson
        .check(bodyString.saveAs("responseBody")))
        .exec{session => println(session("responseBody").as[String]);session}
        .pause(1)
    }
  }


  val scn = scenario("Complex Custom Feeder Test")
    .exec(authenticate())
    .exec(createNewGame())

  setUp(
    scn.inject(atOnceUsers(1)
    ).protocols(httpProtocol)
  )

}
