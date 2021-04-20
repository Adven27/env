package io.github.adven27.env.db.db2

import io.github.adven27.env.core.Environment.Companion.setProperties
import io.github.adven27.env.core.Environment.Prop
import io.github.adven27.env.core.Environment.Prop.Companion.set
import io.github.adven27.env.core.ExternalSystem
import io.github.adven27.env.core.PortsExposingStrategy
import mu.KLogging
import org.testcontainers.containers.Db2Container
import org.testcontainers.utility.DockerImageName

@Suppress("unused", "LongParameterList")
class Db2ContainerSystem @JvmOverloads constructor(
    dockerImageName: DockerImageName,
    portsExposingStrategy: PortsExposingStrategy = PortsExposingStrategy.SystemPropertyToggle(),
    fixedPort: Int = DB2_PORT,
    private var config: Config = Config(),
    private val afterStart: Db2ContainerSystem.() -> Unit = { }
) : Db2Container(dockerImageName), ExternalSystem {

    init {
        acceptLicense()
        if (portsExposingStrategy.fixedPorts()) {
            addFixedExposedPort(fixedPort, DB2_PORT)
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
        var jdbcUrl: Prop = PROP_URL set "jdbc:db2://localhost:$DB2_PORT/test",
        var username: Prop = PROP_USER set "db2inst1",
        var password: Prop = PROP_PASSWORD set "foobar1234",
        var driver: Prop = PROP_DRIVER set "com.ibm.db2.jcc.DB2Driver"
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
        const val PROP_URL = "env.db.db2.url"
        const val PROP_USER = "env.db.db2.username"
        const val PROP_PASSWORD = "env.db.db2.password"
        const val PROP_DRIVER = "env.db.db2.driver"
    }
}
