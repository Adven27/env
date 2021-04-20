package io.github.adven27.env.db.postgresql

import io.github.adven27.env.container.parseImage
import io.github.adven27.env.core.Environment.Companion.setProperties
import io.github.adven27.env.core.Environment.Prop
import io.github.adven27.env.core.Environment.Prop.Companion.set
import io.github.adven27.env.core.ExternalSystem
import io.github.adven27.env.core.PortsExposingStrategy
import io.github.adven27.env.core.PortsExposingStrategy.SystemPropertyToggle
import mu.KLogging
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@Suppress("LongParameterList")
class PostgreSqlContainerSystem @JvmOverloads constructor(
    dockerImageName: DockerImageName = DEFAULT_IMAGE,
    portsExposingStrategy: PortsExposingStrategy = SystemPropertyToggle(),
    fixedPort: Int = POSTGRESQL_PORT,
    private var config: Config = Config(),
    private val afterStart: PostgreSqlContainerSystem.() -> Unit = { }
) : PostgreSQLContainer<Nothing>(dockerImageName), ExternalSystem {

    @JvmOverloads
    constructor(imageName: DockerImageName = DEFAULT_IMAGE, afterStart: PostgreSqlContainerSystem.() -> Unit) : this(
        dockerImageName = imageName,
        afterStart = afterStart
    )

    init {
        if (portsExposingStrategy.fixedPorts()) {
            addFixedExposedPort(fixedPort, POSTGRESQL_PORT)
        }
    }

    override fun start() {
        super.start()
        config = config.refreshValues()
        apply(afterStart)
    }

    private fun Config.refreshValues() = Config(
        jdbcUrl.name set getJdbcUrl(),
        username.name set getUsername(),
        password.name set getPassword(),
        driver.name set driverClassName
    )

    override fun running() = isRunning

    fun config() = config

    override fun describe() = super.describe() + "\n\t" + config.asMap().entries.joinToString("\n\t") { it.toString() }

    data class Config @JvmOverloads constructor(
        var jdbcUrl: Prop = PROP_URL set "jdbc:postgresql://localhost:$POSTGRESQL_PORT/postgres?stringtype=unspecified",
        var username: Prop = PROP_USER set "test",
        var password: Prop = PROP_PASSWORD set "test",
        var driver: Prop = PROP_DRIVER set "org.postgresql.Driver"
    ) {
        init {
            asMap().setProperties()
        }

        fun asMap() = mapOf(jdbcUrl.pair(), username.pair(), password.pair(), driver.pair())

        constructor(url: String, username: String, password: String) : this(
            PROP_URL set url,
            PROP_USER set username,
            PROP_PASSWORD set password
        )
    }

    companion object : KLogging() {
        const val PROP_URL = "env.db.postgresql.url"
        const val PROP_USER = "env.db.postgresql.username"
        const val PROP_PASSWORD = "env.db.postgresql.password"
        const val PROP_DRIVER = "env.db.postgresql.driver"

        @JvmField
        val DEFAULT_IMAGE = "postgres:9.6.12".parseImage()
    }
}
