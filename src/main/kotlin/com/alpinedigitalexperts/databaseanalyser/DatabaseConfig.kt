package com.alpinedigitalexperts.databaseanalyser

object DatabaseConfig {

    val HOST: String =
        System.getenv("DB_HOST")
            ?: "localhost"

    val PORT: String =
        System.getenv("DB_PORT")
            ?: "5432"

    val DATABASE_NAME: String =
        System.getenv("DB_NAME")
            ?: "companydb"

    val USER: String =
        System.getenv("DB_USER")
            ?: "analyst"

    val PASSWORD: String =
        System.getenv("DB_PASSWORD")
            ?: error(
                "DB_PASSWORD environment variable is required."
            )

    val URL: String =
        "jdbc:postgresql://$HOST:$PORT/$DATABASE_NAME"
}
