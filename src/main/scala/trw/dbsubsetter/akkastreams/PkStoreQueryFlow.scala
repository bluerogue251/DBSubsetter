package trw.dbsubsetter.akkastreams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import trw.dbsubsetter.db.SchemaInfo
import trw.dbsubsetter.workflow._

import scala.collection.mutable

object PkStoreQueryFlow {
  def flow(schemaInfo: SchemaInfo): Flow[PkRequest, PkResult, NotUsed] = {
    Flow[PkRequest].statefulMapConcat { () =>
      val pkStore = schemaInfo.tablesByName.values.map(t => t -> mutable.HashSet.empty[Vector[Any]]).toMap

      request => {
        request match {
          case fkt@FkTask(table, _, fkValue, _) =>
            if (pkStore(table).contains(fkValue)) List.empty else List(fkt)
          case OriginDbResult(table, rows, fetchChildren) =>
            val ordinals = table.pkColumnOrdinals
            val newRows = rows.filter { row =>
              val pkValue = ordinals.map(row)
              pkStore(table).add(pkValue)
            }
            List(PksAdded(table, newRows, fetchChildren))
        }
      }
    }
  }
}
