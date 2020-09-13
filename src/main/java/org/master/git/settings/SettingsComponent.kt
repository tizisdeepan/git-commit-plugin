package org.master.git.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.io.Serializable

@State(
    name = "SettingsComponent", storages = [
        Storage(value = "SettingsComponent.xml")
    ]
)
class SettingsComponent : Serializable, PersistentStateComponent<SettingsComponent> {

    override fun getState(): SettingsComponent? = this

    override fun loadState(state: SettingsComponent) = XmlSerializerUtil.copyBean(state, this)
}