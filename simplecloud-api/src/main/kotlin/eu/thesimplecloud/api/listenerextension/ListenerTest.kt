package eu.thesimplecloud.api.listenerextension

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.event.player.CloudPlayerLoginEvent
import eu.thesimplecloud.api.eventapi.BasicEventManager
import eu.thesimplecloud.api.eventapi.IEventManager
import eu.thesimplecloud.api.external.ICloudModule
import eu.thesimplecloud.api.player.ICloudPlayerManager
import eu.thesimplecloud.api.screen.ICommandExecuteManager
import eu.thesimplecloud.api.service.ICloudServiceManager
import eu.thesimplecloud.api.servicegroup.ICloudServiceGroupManager
import org.jetbrains.annotations.TestOnly
import java.util.*

fun main() {
    val eventManager = BasicEventManager()
    AdvancedListener<CloudPlayerLoginEvent>(object: ICloudModule {
        override fun onEnable() {
        }

        override fun onDisable() {
        }
    }, eventManager).addAction { println(it.playerName) }
    eventManager.call(CloudPlayerLoginEvent(UUID.randomUUID(), "tessssssssst"))
    Thread.sleep(200)
}