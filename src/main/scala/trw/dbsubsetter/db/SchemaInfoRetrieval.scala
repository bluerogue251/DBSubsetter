package trw.dbsubsetter.db

import java.util.NoSuchElementException

import trw.dbsubsetter.config.Config
import trw.dbsubsetter.db.ColumnTypes.ColumnType

// scalastyle:off
object SchemaInfoRetrieval {
  def getSchemaInfo(config: Config): SchemaInfo = {
    val DbMetadataQueryResult(tables, columns, primaryKeys, foreignKeys, dbVendor) = DbMetadataQueries.queryDb(config)

    val tablesByName = tables.map { t =>
      val hasSqlServerAutoincrement = columns.exists(c => c.schema == t.schema && c.table == t.name && c.isSqlServerAutoincrement)
      (t.schema, t.name) -> new Table(t.schema, t.name, hasSqlServerAutoincrement)
    }.toMap

    val colsByTableAndName: Map[Table, Map[ColumnName, Column]] = {
      columns
        .groupBy(c => tablesByName(c.schema, c.table))
        .map { case (table, cols) =>
          table -> cols.zipWithIndex.map { case (c, i) =>
            val columnType: ColumnType = ColumnTypes.fromRawInfo(c.jdbcType, c.typeName, dbVendor)
            c.name -> new Column(table, c.name, i, columnType)
          }.toMap
        }
    }

    val colsByTableOrdered: Map[Table, Vector[Column]] = {
      colsByTableAndName.map { case (table, map) => table -> map.values.toVector.sortBy(_.ordinalPosition) }
    }

    val pksByTableOrdered: Map[Table, Vector[Column]] = {
      primaryKeys
        .groupBy(pk => tablesByName(pk.schema, pk.table))
        .map { case (table, pks) =>
          val pkColNames = pks.map(_.column).toSet
          val pkCols = colsByTableOrdered(table).filter(c => pkColNames.contains(c.name))
          table -> pkCols
        }
    }

    val foreignKeysOrdered: Array[ForeignKey] = {
      val fksUnordered = foreignKeys
        .groupBy(fkm => (fkm.fromSchema, fkm.fromTable, fkm.toSchema, fkm.toTable))
        .map { case ((fromSchemaName, fromTableName, toSchemaName, toTableName), partialForeignKeys) =>
          val fromTable = tablesByName(fromSchemaName, fromTableName)
          val fromCols = partialForeignKeys.map { pfk => colsByTableAndName(fromTable)(pfk.fromColumn) }
          val toTable = tablesByName(toSchemaName, toTableName)
          // MySQL schema introspection has a bug where they don't properly capitalize column names of
          // the `pointedTo` side of foreign keys.
          //
          // Doesn't seem to be remedied by &useInformationSchema=true in DB URL so that the `DatabaseMetaDataUsingInfoSchema` class is used
          //
          // This seems to be a bug at the MySQL layer, not at the JDBC Driver layer because the same
          // issue is present in the command line program using the `show create table my_table` command
          //
          // This is a hacky workaround and could cause problems in a schema where the same column name
          // is used twice in the same table with different capitalization. (This seems like it ought to be either impossible or rare though).
          //
          // A less hacky workaround would be to match by column ordinal rather than by name, but unfortunately that info
          // is not included in the result of the query we make to get foreign keys.
          lazy val mysqlWorkaround = colsByTableAndName.mapValues(nameToColMap => nameToColMap.map { case (k, v) => k.toLowerCase -> v })
          val toCols = partialForeignKeys.map { pfk =>
            try {
              colsByTableAndName(toTable)(pfk.toColumn)
            } catch {
              case _: NoSuchElementException if dbVendor == DbVendor.MySQL => mysqlWorkaround(toTable)(pfk.toColumn)
              case e: Throwable => throw e
            }
          }

          val pointsToPk = pksByTableOrdered.get(toTable).fold(false)(pkCols => pkCols == toCols)

        new ForeignKey(fromCols, toCols, pointsToPk, 0)
      }

      fksUnordered.toArray.zipWithIndex.map { case (fk, idx) =>
        fk.setIndex(idx.toShort)
        fk
      }
    }

    val fksFromTable: Map[Table, Vector[ForeignKey]] = {
      foreignKeysOrdered.toVector.groupBy(_.fromTable).withDefaultValue(Vector.empty)
    }

    val fksToTable: Map[Table, Vector[ForeignKey]] = {
      foreignKeysOrdered.toVector.groupBy(_.toTable).withDefaultValue(Vector.empty)
    }

    new SchemaInfo(
      tablesByName,
      colsByTableOrdered,
      pksByTableOrdered,
      foreignKeysOrdered,
      fksFromTable,
      fksToTable
    )
  }
}
