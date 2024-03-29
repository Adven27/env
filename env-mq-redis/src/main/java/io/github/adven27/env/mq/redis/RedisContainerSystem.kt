package io.github.adven27.env.mq.redis

import io.github.adven27.env.container.parseImage
import io.github.adven27.env.core.ExternalSystem
import io.github.adven27.env.core.ExternalSystemConfig
import mu.KLogging
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds

open class RedisContainerSystem @JvmOverloads constructor(
    dockerImageName: DockerImageName = DEFAULT_IMAGE,
    private val defaultPort: Int = PORT,
    private var config: Config = Config(),
    private val afterStart: RedisContainerSystem.() -> Unit = { }
) : GenericContainer<Nothing>(dockerImageName), ExternalSystem {

    @JvmOverloads
    constructor(imageName: DockerImageName = DEFAULT_IMAGE, afterStart: RedisContainerSystem.() -> Unit) : this(
        dockerImageName = imageName,
        afterStart = afterStart
    )

    override fun start(fixedEnv: Boolean) {
        withExposedPorts(PORT)
        withStartupTimeout(ofSeconds(STARTUP_TIMEOUT))
        if (fixedEnv) {
            addFixedExposedPort(defaultPort, PORT)
        }
        start()
    }

    override fun start() {
        super.start()
        config = Config(host, firstMappedPort)
        apply(afterStart)
    }

    override fun config() = config
    override fun running() = isRunning

    data class Config @JvmOverloads constructor(val host: String = "localhost", val port: Int = PORT) :
        ExternalSystemConfig("env.mq.redis.host" to host, "env.mq.redis.port" to port.toString())

    companion object : KLogging() {
        private const val PORT = 6379
        private const val STARTUP_TIMEOUT = 30L

        @JvmField
        val DEFAULT_IMAGE = "redis:5.0.3-alpine".parseImage()
    }
}
