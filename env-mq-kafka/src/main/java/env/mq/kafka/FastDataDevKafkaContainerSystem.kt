package env.mq.kafka

import env.core.Environment.Companion.setProperties
import env.core.Environment.Prop
import env.core.Environment.Prop.Companion.set
import env.core.ExternalSystem
import env.core.PortsExposingStrategy
import env.core.PortsExposingStrategy.SystemPropertyToggle
import mu.KLogging
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds

open class FastDataDevKafkaContainerSystem @JvmOverloads constructor(
    dockerImageName: DockerImageName = DockerImageName.parse(IMAGE),
    portsExposingStrategy: PortsExposingStrategy = SystemPropertyToggle(),
    fixedPort: Int = PORT,
    fixedPortAdm: Int = PORT_ADM,
    private var config: Config = Config(),
    private val afterStart: FastDataDevKafkaContainerSystem.() -> Unit = { }
) : GenericContainer<Nothing>(dockerImageName), ExternalSystem {
    private val fixedPort: Int

    init {
        withEnv("ADV_HOST", "127.0.0.1")
        withExposedPorts(PORT, PORT_ADM)
        withStartupTimeout(ofSeconds(STARTUP_TIMEOUT))
        this.fixedPort = fixedPort
        if (portsExposingStrategy.fixedPorts()) {
            addFixedExposedPort(fixedPort, PORT)
            addFixedExposedPort(fixedPortAdm, PORT_ADM)
        }
    }

    override fun start() {
        super.start()
        config = Config(config.host.name set host, config.port.name set getMappedPort(PORT).toString())
        apply(afterStart)
    }

    fun config() = config

    data class Config @JvmOverloads constructor(
        val host: Prop = "env.mq.kafka.host" set "localhost",
        val port: Prop = "env.mq.kafka.port" set PORT.toString()
    ) {
        init {
            mapOf(host.pair(), port.pair()).setProperties()
        }
    }

    companion object : KLogging() {
        private const val PORT = 9092
        private const val PORT_ADM = 3030
        private const val IMAGE = "lensesio/fast-data-dev"
        private const val STARTUP_TIMEOUT = 30L
    }

    override fun running() = isRunning
}