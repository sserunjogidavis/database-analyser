package com.alpinedigitalexperts.databaseanalyser

data class CommandLineConfig(
    val host: String? = null,
    val port: String? = null,
    val databaseName: String? = null,
    val user: String? = null,
    val nullWarningThreshold: Double? = null,
    val nullCriticalThreshold: Double? = null,
    val outlierZScoreThreshold: Double? = null,
    val outlierMinimumSampleSize: Long? = null,
    val compareWith: String? = null
) {

    companion object {

        fun parse(
            args: Array<String>
        ): CommandLineConfig {

            var host: String? = null
            var port: String? = null
            var databaseName: String? = null
            var user: String? = null
            var nullWarningThreshold: Double? = null
            var nullCriticalThreshold: Double? = null
            var outlierZScoreThreshold: Double? = null
            var outlierMinimumSampleSize: Long? = null
            var compareWith: String? = null

            var index = 0

            while (index < args.size) {

                when (val argument = args[index]) {

                    "--host" -> {
                        host =
                            requireValue(
                                args = args,
                                index = index,
                                optionName = argument
                            )

                        index += 2
                    }

                    "--port" -> {
                        port =
                            requireValue(
                                args = args,
                                index = index,
                                optionName = argument
                            )

                        index += 2
                    }

                    "--database" -> {
                        databaseName =
                            requireValue(
                                args = args,
                                index = index,
                                optionName = argument
                            )

                        index += 2
                    }

                    "--user" -> {
                        user =
                            requireValue(
                                args = args,
                                index = index,
                                optionName = argument
                            )

                        index += 2
                    }

                    "--null-warning-threshold" -> {

                        val value =
                            requireValue(
                                args = args,
                                index = index,
                                optionName = argument
                            )

                        nullWarningThreshold =
                            value.toDoubleOrNull()
                                ?: error(
                                    "$argument must be a valid decimal number."
                                )

                        index += 2
                    }

                    "--null-critical-threshold" -> {

                        val value =
                            requireValue(
                                args = args,
                                index = index,
                                optionName = argument
                            )

                        nullCriticalThreshold =
                            value.toDoubleOrNull()
                                ?: error(
                                    "$argument must be a valid decimal number."
                                )

                        index += 2
                    }

                    "--outlier-zscore-threshold" -> {

                        val value =
                            requireValue(
                                args = args,
                                index = index,
                                optionName = argument
                            )

                        outlierZScoreThreshold =
                            value.toDoubleOrNull()
                                ?: error(
                                    "$argument must be a valid decimal number."
                                )

                        index += 2
                    }

                    "--outlier-min-sample-size" -> {

                        val value =
                            requireValue(
                                args = args,
                                index = index,
                                optionName = argument
                            )

                        outlierMinimumSampleSize =
                            value.toLongOrNull()
                                ?: error(
                                    "$argument must be a valid whole number."
                                )

                        index += 2
                    }

                    "--compare-with" -> {
                        compareWith =
                            requireValue(
                                args = args,
                                index = index,
                                optionName = argument
                            )

                        index += 2
                    }

                    else -> {
                        error(
                            "Unknown command-line option: $argument"
                        )
                    }
                }
            }

            return CommandLineConfig(
                host = host,
                port = port,
                databaseName = databaseName,
                user = user,
                nullWarningThreshold = nullWarningThreshold,
                nullCriticalThreshold = nullCriticalThreshold,
                outlierZScoreThreshold = outlierZScoreThreshold,
                outlierMinimumSampleSize = outlierMinimumSampleSize,
                compareWith = compareWith
            )
        }


        private fun requireValue(
            args: Array<String>,
            index: Int,
            optionName: String
        ): String {

            val valueIndex =
                index + 1

            if (valueIndex >= args.size) {
                error(
                    "Missing value for command-line option: $optionName"
                )
            }

            return args[valueIndex]
        }
    }
}
