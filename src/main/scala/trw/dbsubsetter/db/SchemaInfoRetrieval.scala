package trw.dbsubsetter.db

import java.util.NoSuchElementException

import trw.dbsubsetter.config.{ConfigColumn, SchemaConfig}
import trw.dbsubsetter.db.ColumnTypes.ColumnType

object SchemaInfoRetrieval {
  def getSchemaInfo(dbMetadata: DbMetadataQueryResult, schemaConfig: SchemaConfig): SchemaInfo = {
    val includedTables =
      dbMetadata.tables
        .map { tableQueryRow =>
          val schema = Schema(tableQueryRow.schema)
          Table(schema, tableQueryRow.name)
        }
        .filterNot(schemaConfig.excludeTables)

    val tablesByName =
      includedTables
        .map(table => (table.schema.name, table.name) -> table)
        .toMap

    val tablesWithAutoincrementMetadata =
      includedTables
        .map { table =>
          val hasSqlServerAutoincrement =
            dbMetadata.columns
              .exists { columnQueryRow =>
                columnQueryRow.schema == table.schema.name &&
                  columnQueryRow.table == table.name &&
                  columnQueryRow.isSqlServerAutoincrement
              }

          TableWithAutoincrementMetadata(table, hasSqlServerAutoincrement)
        }

    val colsByTableAndName: Map[Table, Map[String, Column]] = {
      dbMetadata.columns
        .filterNot { columnQueryRow =>
          val schema = Schema(columnQueryRow.schema)
          val table = Table(schema, columnQueryRow.table)
          val configColumn = ConfigColumn(table, columnQueryRow.name)
          schemaConfig.excludeColumns.contains(configColumn)
        }
        .filter(c => tablesByName.contains((c.schema, c.table)))
        .groupBy(c => tablesByName(c.schema, c.table))
        .map { case (table, cols) =>
          table -> cols.zipWithIndex.map { case (c, i) =>
            val columnType: ColumnType = ColumnTypes.fromRawInfo(c.jdbcType, c.typeName, dbMetadata.vendor)
            val column: Column = new Column(
              table = table,
              name = c.name,
              keyOrdinalPosition = -1, // Placeholder value that will be mutated (corrected) later
              dataOrdinalPosition = i,
              dataType = columnType
            )
            c.name -> column
          }.toMap
        }
    }

    val allColumnsByTableOrdered: Map[Table, Vector[Column]] = {
      colsByTableAndName.map { case (table, map) => table -> map.values.toVector.sortBy(_.dataOrdinalPosition) }
    }

    val pksByTable: Map[Table, PrimaryKey] = {
      val autodetectedPrimaryKeys =
        dbMetadata.primaryKeyColumns
          .filter(c => tablesByName.contains((c.schema, c.table)))
          .groupBy(pk => tablesByName(pk.schema, pk.table))
          .map { case (table, singleTablePrimaryKeyMetadataRows) =>
            val columnNames = singleTablePrimaryKeyMetadataRows.map(_.column).toSet
            val orderedColumns = allColumnsByTableOrdered(table).filter(c => columnNames.contains(c.name))
            table -> new PrimaryKey(orderedColumns)
          }

      val configuredPrimaryKeys =
        schemaConfig.extraPrimaryKeys
          .map { cmdLinePrimaryKey =>
            val table = cmdLinePrimaryKey.table
            val columnNames = cmdLinePrimaryKey.columns.map(_.name).toSet
            val orderedColumns = allColumnsByTableOrdered(table).filter(c => columnNames.contains(c.name))
            table -> new PrimaryKey(orderedColumns)
          }

      autodetectedPrimaryKeys ++ configuredPrimaryKeys
    }

    val foreignKeysOrdered: Array[ForeignKey] = {
      val configuredForeignKeys =
        schemaConfig.extraForeignKeys
          .flatMap { efk =>
            efk.fromColumns
              .zip(efk.toColumns)
              .map { case (fromColumn, toColumn) =>
                ForeignKeyColumnQueryRow(
                  efk.fromTable.schema.name,
                  efk.fromTable.name,
                  fromColumn.name,
                  efk.toTable.schema.name,
                  efk.toTable.name,
                  toColumn.name
                )
              }
          }

      val combinedForeignKeys = dbMetadata.foreignKeyColumns ++ configuredForeignKeys

      val fksUnordered =
        combinedForeignKeys
          .filter(fk => tablesByName.contains((fk.fromSchema, fk.fromTable)))
          .filter(fk => tablesByName.contains((fk.toSchema, fk.toTable)))
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
            lazy val mysqlWorkaround =
              colsByTableAndName
                .mapValues { nameToColMap =>
                  nameToColMap
                    .map { case (k, v) =>
                      k.toLowerCase -> v
                    }
                }
            val toCols = partialForeignKeys.map { pfk =>
              try {
                colsByTableAndName(toTable)(pfk.toColumn)
              } catch {
                case _: NoSuchElementException if dbMetadata.vendor == DbVendor.MySQL =>
                  mysqlWorkaround(toTable)(pfk.toColumn)
                case e: Throwable =>
                  throw e
              }
            }

            val pointsToPk = pksByTable.get(toTable).fold(false)(pk => pk.columns == toCols)

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

    val keyColumnsByTableOrdered: Map[Table, Vector[Column]] = {
      allColumnsByTableOrdered.map { case (table, allColumns) =>
        val primaryKey: PrimaryKey = pksByTable(table)
        val foreignKeysFromTable: Seq[ForeignKey] = fksFromTable(table)
        val foreignKeysToTable: Seq[ForeignKey] = fksToTable(table)

        def isKeyColumn(col: Column): Boolean = {
          primaryKey.columns.contains(col) ||
            foreignKeysFromTable.exists(_.fromCols.contains(col)) ||
            foreignKeysToTable.exists(_.toCols.contains(col))
        }

        val keyColumns: Vector[Column] = allColumns.filter(isKeyColumn)

        keyColumns.zipWithIndex.foreach { case (keyColumn, i) =>
          keyColumn.keyOrdinalPosition = i
        }

        table -> keyColumns
      }
    }

    new SchemaInfo(
      tables = tablesWithAutoincrementMetadata,
      keyColumnsByTableOrdered = keyColumnsByTableOrdered,
      dataColumnsByTableOrdered = allColumnsByTableOrdered,
      pksByTable = pksByTable,
      fksOrdered = foreignKeysOrdered,
      fksFromTable = fksFromTable,
      fksToTable = fksToTable
    )
  }
}
