/*
 * MIT License
 *
 * Copyright (C) 2020-2022 The SimpleCloud authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package eu.thesimplecloud.plugin.startup

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.client.NetworkComponentType
import eu.thesimplecloud.api.external.ICloudModule
import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.api.service.ServiceState
import eu.thesimplecloud.client.packets.PacketOutCloudClientLogin
import eu.thesimplecloud.clientserverapi.client.INettyClient
import eu.thesimplecloud.clientserverapi.client.NettyClient
import eu.thesimplecloud.clientserverapi.lib.connection.IConnection
import eu.thesimplecloud.jsonlib.JsonLib
import eu.thesimplecloud.plugin.ICloudServicePlugin
import eu.thesimplecloud.plugin.impl.CloudAPIImpl
import java.io.File
import kotlin.concurrent.thread


class CloudPlugin(val cloudServicePlugin: ICloudServicePlugin) : ICloudModule {

    companion object {
        @JvmStatic
        lateinit var instance: CloudPlugin
            private set
    }

    @Volatile
    private var thisService: ICloudService? = null

    @Volatile
    private var thisServiceGroupName: String? = null

    @Volatile
    lateinit var communicationClient: INettyClient
        private set

    @Volatile
    lateinit var connectionToManager: IConnection
        private set

    @Volatile
    lateinit var thisServiceName: String
        private set

    @Volatile
    private var nettyThread: Thread

    init {
        println("<---------- Starting SimpleCloud-Plugin ---------->")
        instance = this
        if (!loadConfig())
            cloudServicePlugin.shutdown()
        println("<---------- Service-Name: $thisServiceName ---------->")
        CloudAPIImpl(cloudServicePlugin.getCloudPlayerManagerClass().java.newInstance())

        this.communicationClient.setPacketSearchClassLoader(this::class.java.classLoader)
        this.communicationClient.addPacketsByPackage("eu.thesimplecloud.plugin.network.packets")
        this.communicationClient.addPacketsByPackage("eu.thesimplecloud.client.packets")
        this.communicationClient.addPacketsByPackage("eu.thesimplecloud.api.network.packets")

        nettyThread = thread(true, isDaemon = false, contextClassLoader = this::class.java.classLoader) {
            println("<------Starting cloud client----------->")
            this.communicationClient.start().then {
                println("<-------- Connection is now set up -------->")
                this.connectionToManager.sendUnitQuery(
                    PacketOutCloudClientLogin(
                        NetworkComponentType.SERVICE,
                        thisServiceName
                    ), 10000
                )
                    .addFailureListener { throw it }
            }.addFailureListener { println("<-------- Failed to connect to server -------->") }
                .addFailureListener { throw it }
        }

        UsedMemoryUpdater().startUpdater()

        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                this.communicationClient.shutdown()
            } catch (e: Exception) {
            }

        })
    }

    /**
     * Returns whether the config was loaded successful
     */
    private fun loadConfig(): Boolean {
        val jsonLib = JsonLib.fromJsonFile(File("SIMPLE-CLOUD.json")) ?: return false
        thisServiceName = jsonLib.getString("serviceName") ?: return false
        thisServiceGroupName = jsonLib.getString("groupName") ?: return false
        val host = jsonLib.getString("managerHost") ?: return false
        val port = jsonLib.getInt("managerPort") ?: return false
        this.communicationClient = NettyClient(host, port, ConnectionHandlerImpl())
        this.connectionToManager = this.communicationClient.getConnection()
        return true
    }

    @Synchronized
    fun thisService(): ICloudService {
        if (this.thisService == null) this.thisService =
            CloudAPI.instance.getCloudServiceManager().getCloudServiceByName(thisServiceName)
        while (this.thisService == null || !this.thisService!!.isAuthenticated()) {
            Thread.sleep(10)
            if (this.thisService == null)
                this.thisService = CloudAPI.instance.getCloudServiceManager().getCloudServiceByName(thisServiceName)
        }
        return this.thisService!!
    }

    override fun onEnable() {
        if (thisService().getServiceGroup()
                .isStateUpdatingEnabled() && thisService().getState() == ServiceState.STARTING
        ) {
            thisService().setState(ServiceState.VISIBLE)
            cloudServicePlugin.onBeforeFirstUpdate()
            updateThisService()
        }
    }

    override fun onDisable() {
    }

    @Synchronized
    fun updateThisService() {
        thisService().update()
    }

    fun getGroupName(): String = this.thisServiceGroupName!!

}