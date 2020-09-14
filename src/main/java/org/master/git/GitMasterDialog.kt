package org.master.git

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import java.awt.Component
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class GitMasterDialog : DialogWrapper(true) {

    var items: ArrayList<ItemModel> = ArrayList()

    override fun createCenterPanel(): JComponent? {
        val config = PropertiesComponent.getInstance()
        val itemsJson = config.getValue("ITEMS_JSON", Gson().toJson(items).toString())
        items = Gson().fromJson(itemsJson, object : TypeToken<ArrayList<ItemModel>>() {}.type)

        val view = JPanel()
        view.layout = BoxLayout(view, BoxLayout.Y_AXIS)
        val spn = JScrollPane(view)

        val ctrl: JComponent = createRow()
        ctrl.alignmentX = Component.LEFT_ALIGNMENT
        val pnl = spn.viewport.view as JPanel
        pnl.add(ctrl)

        return spn
    }

    private fun createRow(): JPanel {
        return panel {
            items.forEach {
                row {
                    label(it.name)
                    if (it.type == ItemModel.TEXT) label(ItemModel.TEXT)
                    else label(ItemModel.OPTIONS)
                }
            }
        }
    }

    init {
        init()
        title = "Git Master"
    }
}