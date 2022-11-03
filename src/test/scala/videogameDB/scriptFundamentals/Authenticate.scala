package videogameDB.scriptFundamentals

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

class Authenticate extends Simulation{

  val httpProtocol = http.baseUrl("https://videogamedb.uk/api")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  def authenticate(): ChainBuilder ={
    exec(http("Authenticate")
    .post("/authenticate").body(StringBody(
      "{\n  \"password\": \"admin\",\n  \"username\": \"admin\"\n}"))
    .check(jsonPath("$.token").saveAs("token")))

  }

  def createNewGame(): ChainBuilder ={
    exec(http("Create New Game")
    .post("/videogame")
      .header("Authorization", "Bearer #{token}")
    .body(StringBody(
      "{\n  \"category\": \"Platform\",\n  \"name\": \"Mario\",\n  \"rating\": \"Mature\",\n  \"releaseDate\": \"2012-05-04\",\n  \"reviewScore\": 85\n}"
    )))
  }

  val scn = scenario("Authenticate")
    .exec(authenticate())
    .pause(1)
    .exec(createNewGame())


  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpProtocol)

}
