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

package eu.thesimplecloud.api.servicegroup.impl

import eu.thesimplecloud.api.service.version.ServiceVersion
import eu.thesimplecloud.api.servicegroup.ICloudServiceGroupUpdater
import eu.thesimplecloud.api.servicegroup.grouptype.ICloudProxyGroup
import eu.thesimplecloud.api.servicegroup.grouptype.updater.ICloudProxyGroupUpdater
import eu.thesimplecloud.api.servicegroup.impl.updater.DefaultProxyGroupUpdater
import eu.thesimplecloud.clientserverapi.lib.json.PacketExclude
import eu.thesimplecloud.jsonlib.JsonLib
import eu.thesimplecloud.jsonlib.JsonLibExclude

class DefaultProxyGroup(
    name: String,
    templateName: String,
    serviceNameSplitter: String,
    maxMemory: Int,
    maxPlayers: Int,
    minimumOnlineServiceCount: Int,
    maximumOnlineServiceCount: Int,
    maintenance: Boolean,
    static: Boolean,
    percentToStartNewService: Int,
    wrapperName: String?,
    @Volatile private var startPort: Int,
    serviceVersion: ServiceVersion,
    startPriority: Int,
    javaCommand: String,
    permission: String?
) : AbstractServiceGroup(
    name,
    templateName,
    serviceNameSplitter,
    maxMemory,
    maxPlayers,
    minimumOnlineServiceCount,
    maximumOnlineServiceCount,
    maintenance,
    static,
    percentToStartNewService,
    wrapperName,
    serviceVersion,
    startPriority,
    javaCommand,
    permission
), ICloudProxyGroup {

    @Volatile
    @JsonLibExclude
    @PacketExclude
    private var updater: DefaultProxyGroupUpdater? = DefaultProxyGroupUpdater(this)

    override fun getStartPort(): Int = this.startPort

    override fun setStartPort(port: Int) {
        getUpdater().setStartPort(port)
    }

    override fun getUpdater(): ICloudProxyGroupUpdater {
        if (this.updater == null) {
            this.updater = DefaultProxyGroupUpdater(this)
        }
        return this.updater!!
    }

    override fun applyValuesFromUpdater(updater: ICloudServiceGroupUpdater) {
        super.applyValuesFromUpdater(updater)
        updater as ICloudProxyGroupUpdater
        this.startPort = updater.getStartPort()
    }

    override fun toString(): String {
        return JsonLib.fromObject(this).getAsJsonString()
    }

}