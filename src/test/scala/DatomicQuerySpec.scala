import org.specs2.mutable._

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import datomic.Entity
import datomic.Connection
import datomic.Database
import datomic.Peer
import datomic.Util

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

import java.io.Reader
import java.io.FileReader

import scala.concurrent._
import scala.concurrent.util._
import java.util.concurrent.TimeUnit._

import reactivedatomic._
import scala.concurrent.ExecutionContext.Implicits.global



@RunWith(classOf[JUnitRunner])
class DatomicQuerySpec extends Specification {
  "Datomic" should {
    "query simple" in {
      import Datomic._
      import DatomicData._

      implicit val uri = "datomic:mem://datomicqueryspec"
      DatomicBootstrap(uri)

      pureQuery("""
        [ :find ?e ?n 
          :where  [ ?e :person/name ?n ] 
                  [ ?e :person/character :person.character/violent ]
        ]
      """).prepare().execute().collect {
        case List(e: DLong, n: DString) => 
          val entity = database.entity(e)
          println("Q1 entity: "+ e + " name:"+n+ " - e:" + entity.get(":person/character"))
      }
      
      success
    }
  }
}