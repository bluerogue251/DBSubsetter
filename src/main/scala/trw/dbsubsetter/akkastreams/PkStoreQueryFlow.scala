package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

import scala.collection.mutable

object PkStoreQueryFlow {
  def flow(schemaInfo: SchemaInfo): Flow[PkRequest, PkResult, NotUsed] = {
    Flow[PkRequest].statefulMapConcat { () =>
      val pkStore = schemaInfo.pksByTable.keys.map(t => t -> mutable.HashSet.empty[Vector[Any]]).toMap
      request => {
        request match {
          case fkt@FkTask(table, _, fkValue, _) =>
            if (pkStore(table).contains(fkValue)) List.empty else List(fkt)
          case OriginDbResult(table, rows, fetchChildren) =>
            val newRows = rows.filter { row =>
              val pkValues = schemaInfo.pksByTable(table).columns.map(c => row(c.name))
              pkStore(table).add(pkValues)
            }
            List(PksAdded(table, newRows, fetchChildren))
        }
      }
    }
  }
}
