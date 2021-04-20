![CI](https://github.com/Adven27/env/workflows/CI/badge.svg)
# env
Java library for a microservice environment emulation


### How to use
1. Add needed dependencies:
```groovy
testImplementation "io.github.adven27:env-db-postgresql:2.1.0"
testImplementation "io.github.adven27:env-db-mysql:2.1.0"
testImplementation "io.github.adven27:env-db-oracle:2.1.0"
testImplementation "io.github.adven27:env-db-db2:2.1.0"
testImplementation "io.github.adven27:env-mq-rabbit:2.1.0"
testImplementation "io.github.adven27:env-mq-ibmmq:2.1.0"
testImplementation "io.github.adven27:env-mq-redis:2.1.0"
testImplementation "io.github.adven27:env-grpc-mock:2.1.0"
testImplementation "io.github.adven27:env-wiremock:2.1.0"
```
2. Set up systems:
```kotlin
class SomeEnvironment : Environment(
    mapOf(
        "RABBIT" to RabbitContainerSystem(),
        "IBMMQ" to IbmMQContainerSystem(),
        "REDIS" to RedisContainerSystem(),
        "POSTGRES" to PostgreSqlContainerSystem(),
        "ORACLE" to OracleContainerSystem(),
        "MYSQL" to MySqlContainerSystem(),
        "GRPC" to GrpcMockContainerSystem(1, listOf("common.proto", "wallet.proto")).apply {
            withLogConsumer(Slf4jLogConsumer(logger).withPrefix("GRPC-$serviceId"))
        },
        "WIREMOCK" to WiremockSystem()
    )
) {
    fun rabbit() = find<RabbitContainerSystem>("RABBIT")
    fun mock() = find<WiremockSystem>("WIREMOCK").server
}
```      
3. Use in tests:
```kotlin 
class MyTestClass {
    companion object {
        private val ENV: SomeEnvironment = SomeEnvironment() 

        @BeforeClass @JvmStatic 
        fun setup() {
           ENV.up()
        }

        @AfterClass @JvmStatic 
        fun teardown() {
           ENV.down()
        }
    }

    @Test fun testSomething() {
        //some interactions with environment
        ENV.mock().resetRequests()
        //some test
        ...
    }
} 
```

### Run as standalone process

Environment class implementation could be run as standalone java application with `io.github.adven27.env.core.EnvStarter`

For example as gradle task:
```groovy
task runEnv(type: JavaExec) {
    group = "Execution"
    description = "Run some environment"
    classpath = sourceSets.test.runtimeClasspath
    main = "io.github.adven27.env.core.EnvStarter"

    args 'SomeEnvironment'
    systemProperty 'SPECS_ENV_START', true
    systemProperty 'SPECS_ENV_FIXED', true
    standardInput = System.in
}
``` 
