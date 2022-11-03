package videogameDB.feeders

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

class CsvFeeder extends Simulation{

  val httpProtocol = http.baseUrl("https://videogamedb.uk/api")
    .acceptHeader("application/json")


  val csvFeeder = csv("data/gameCsvFile.csv").circular

  def getSpecificVideoGame(): ChainBuilder={
    repeat(10){
      feed(csvFeeder)
        .exec(http("Get Videogame with Name - #{gameName}")
        .get("/videogame/#{gameId}")
        .check(jsonPath("$.name").is("#{gameName}"))
        .check(status.is(200)))
        .pause(1)
    }
  }

  val scn = scenario("Csv Feeder Test")
    .exec(getSpecificVideoGame())


  setUp(
    scn.inject(atOnceUsers(1)
    ).protocols(httpProtocol)
  )
}
