package videogameDB.finalSimulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random

class VideoGameFullTest extends Simulation{

  val httpProtocol = http.baseUrl("https://videogamedb.uk/api")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  //Variables for feeders//
  //runtime variables
  def USERCOUNT =System.getProperty("USERS","5").toInt
  def RAMPDURATION =System.getProperty("RAMP_DURATION","10").toInt
  def TESTDURATION =System.getProperty("TEST_DURATION","30").toInt

  val csvFeeder = csv("data/gameCsvFile.csv").random

  before{
    println(s"Running test with ${USERCOUNT} users")
    println(s"Ramping users over ${RAMPDURATION} seconds")
    println(s"Total test duration ${TESTDURATION} seconds")
  }

  /**http calls**/

  def authenticate()={
    exec(http("Authentication")
    .post("/authenticate")
    .body(StringBody(
      "{\n  \"password\": \"admin\",\n  \"username\": \"admin\"\n}"
    ))
    .check(jsonPath("$.token").saveAs("token")))
  }

  def createNewGame()={
    feed(csvFeeder)
      .exec(http("Create new Game - #{name}")
      .post("/videogame")
      .header("Authorization","Bearer #{token}")
      .body(ElFileBody("bodies/newGameTemplate.json")).asJson)
  }

  def getAllVideoGames()={
    exec(http("Get all video games")
    .get("/videogame")
    .check(status.is(200)))
  }

  def getSpecificGame()={
    exec(http("Get Specific Game - #{name}")
    .get("/videogame/#{gameId}")
    .check(jsonPath("$.name").is("#{name}")))
  }

  def deleteGame()={
    exec(http("Delete a game - #{name}")
    .delete("/videogame/#{gameId}")
      .header("Authorization","Bearer #{token}")
      .check(bodyString.is("Video game deleted")))

  }


  /**Scenario design*/
  val scn = scenario("Video game DB Final Script")
    .forever{
      exec(getAllVideoGames())
        .pause(2)
        .exec(authenticate())
        .pause(2)
        .exec(createNewGame())
        .pause(2)
        .exec(getSpecificGame())
        .pause(2)
        .exec(deleteGame())
    }

  setUp(
    scn.inject(
      nothingFor(5),
      rampUsers(USERCOUNT).during(RAMPDURATION)
    ).protocols(httpProtocol)
  ).maxDuration(TESTDURATION)

after{
  println("Stress test Completed")
}

}
