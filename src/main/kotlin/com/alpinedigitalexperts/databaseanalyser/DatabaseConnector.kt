package com.alpinedigitalexperts.databaseanalyser

import java.sql.Connection
import java.sql.DriverManager

object DatabaseConnector {

    fun connect(): Connection {

        return DriverManager.getConnection(
            DatabaseConfig.URL,
            DatabaseConfig.USER,
            DatabaseConfig.PASSWORD
        )
    }


    fun connect(
        runtimeConfig: RuntimeConfig
    ): Connection {

        return DriverManager.getConnection(
            runtimeConfig.url,
            runtimeConfig.user,
            runtimeConfig.password
        )
    }
}
