package io.github.adven27.env.container

import io.github.adven27.env.core.GenericExternalSystem
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

/**
 * System implementation based on docker container
 */
@Suppress("unused")
open class ContainerExternalSystem<T : GenericContainer<*>> @JvmOverloads constructor(
    system: T,
    afterStart: T.() -> Unit = { }
) : GenericExternalSystem<T>(
    system,
    start = { it.start() },
    stop = { it.stop() },
    running = { it.isRunning },
    afterStart = afterStart,
)

fun String.parseImage(): DockerImageName = DockerImageName.parse(this)

infix fun String.asCompatibleSubstituteFor(other: String): DockerImageName =
    parseImage().asCompatibleSubstituteFor(other)

infix fun String.asCompatibleSubstituteFor(other: DockerImageName): DockerImageName =
    parseImage().asCompatibleSubstituteFor(other)
