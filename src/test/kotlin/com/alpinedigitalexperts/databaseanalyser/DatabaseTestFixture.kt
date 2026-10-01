package com.alpinedigitalexperts.databaseanalyser

import java.sql.Connection

object DatabaseTestFixture {

    fun loadQualityFixture(
        connection: Connection
    ) {

        val resource =
            DatabaseTestFixture::class.java
                .classLoader
                .getResource(
                    "sql/database-quality-fixture.sql"
                )
                ?: error(
                    "Missing test resource: " +
                        "sql/database-quality-fixture.sql"
                )

        val sql =
            resource
                .readText()

        val statements =
            sql
                .split(";")
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }

        for (statementSql in statements) {

            connection
                .prepareStatement(
                    statementSql
                )
                .use { statement ->

                    statement.execute()
                }
        }
    }
}
