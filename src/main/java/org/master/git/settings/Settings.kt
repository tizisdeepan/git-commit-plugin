package org.master.git.settings

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
import org.master.git.ItemModel
import java.awt.event.ItemEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableModel


class Settings(private val project: Project) : Configurable, DocumentListener {

    var dtm = object : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int): Boolean = false
    }

    private var modified = true

    var column = arrayOf("NAME", "TYPE", "VALUE")
    var items: ArrayList<ItemModel> = ArrayList()

    override fun createComponent(): JComponent? {
        val config = PropertiesComponent.getInstance()
        val itemsJson = config.getValue("ITEMS_JSON", Gson().toJson(items).toString())
        items = Gson().fromJson(itemsJson, object : TypeToken<ArrayList<ItemModel>>() {}.type)

        val scroll = JBScrollPane()
        val configTable = JBTable()

        configTable.rowSelectionAllowed = true
        configTable.model = dtm
        dtm.setColumnIdentifiers(column)

        scroll.add(configTable)

        val addPanel = createAddPanel()

        if (items.isNotEmpty()) {
            for (i in 0 until items.size) {
                createValueRow(items[i])
            }
        }

        return panel {
            noteRow("Customize the list of templates to be included in the Git Master View")
            row {
                configTable(grow)
            }
            row {
                addPanel(grow)
            }
        }
    }

    private fun createValueRow(itemModel: ItemModel) {
        dtm.addRow(arrayOf(itemModel.name, itemModel.type, itemModel.label))
    }

    private fun addRow(itemModel: ItemModel) {
        items.add(itemModel)
        dtm.addRow(arrayOf(itemModel.name, itemModel.type, itemModel.label))
    }

    private fun createAddPanel(): JPanel {
        val typeField = JComboBox(arrayOf(ItemModel.TEXT, ItemModel.OPTIONS))
        val optionsLabel = JLabel("Add items comma separated (eg:Bug,Task,Issue)")
        val optionsField = JTextField("")
        optionsLabel.isVisible = false
        optionsField.isVisible = false
        val nameField = JTextField("")
        val panel = panel {
            row {
                label("Name")
                nameField(grow)
            }
            row {
                label("Type")
                typeField(grow)
            }
            row {
                optionsLabel(grow)
                optionsField(grow)
            }
            row {
                label("")
                label("")
                right {
                    button("Add") {
                        addRow(ItemModel(nameField.text, typeField.selectedItem as String, optionsField.text))
                    }
                }
            }
        }
        typeField.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                when (it.item as? String) {
                    ItemModel.OPTIONS -> {
                        optionsLabel.isVisible = true
                        optionsField.isVisible = true
                    }
                    else -> {
                        optionsLabel.isVisible = false
                        optionsField.isVisible = false
                    }
                }
            }
        }
        return panel
    }

    override fun isModified(): Boolean = modified

    override fun apply() {
        val config = PropertiesComponent.getInstance()
        config.setValue("ITEMS_JSON", Gson().toJson(items).toString())
        modified = false
    }

    override fun getDisplayName(): String = "Git Master"

    override fun insertUpdate(e: DocumentEvent?) {
        modified = true
    }

    override fun removeUpdate(e: DocumentEvent?) {
        modified = true
    }

    override fun changedUpdate(e: DocumentEvent?) {
        modified = true
    }
}
