package org.master.git.settings

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.Configurable
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
import org.master.git.AddItemDialog
import org.master.git.ItemModel
import java.awt.BorderLayout
import java.util.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableModel
import javax.swing.table.JTableHeader
import kotlin.collections.ArrayList


class Settings : Configurable, DocumentListener {

    private val configTable = JBTable()
    private val templateArea = JTextField()

    var dtm = object : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int): Boolean = false
    }

    private var modified = true

    var column = arrayOf("NAME", "TYPE", "VARIABLE", "VALUE")

    private var cachedItems: ArrayList<ItemModel> = ArrayList()

    override fun createComponent(): JComponent? {
        val config = PropertiesComponent.getInstance()
        val itemsJson = config.getValue("ITEMS_JSON", Gson().toJson(ArrayList<ItemModel>()).toString())
        val template = config.getValue("TEMPLATE", "")
        cachedItems = try {
            Gson().fromJson(itemsJson, object : TypeToken<ArrayList<ItemModel>>() {}.type)
        } catch (e: Exception) {
            ArrayList()
        }

        val scroll = JBScrollPane()

        configTable.rowSelectionAllowed = true
        configTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        val tableHeader = JTableHeader()
        tableHeader.table = configTable
        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.add(tableHeader, BorderLayout.NORTH)
        panel.add(configTable, BorderLayout.CENTER)
        dtm.setColumnIdentifiers(column)
        configTable.model = dtm

        configTable.selectionModel.addListSelectionListener(ListSelectionListener { event ->
            if (event.valueIsAdjusting) return@ListSelectionListener
        })

        val refreshAction = object : AnActionButton("Refresh", AllIcons.Actions.Refresh) {
            override fun actionPerformed(anActionEvent: AnActionEvent) {
                populateCachedValues()
            }
        }

        val tableToolbarDecorator: ToolbarDecorator = ToolbarDecorator.createDecorator(configTable).setAddAction {
            AddItemDialog(getCurrentItems(), this::addRow).show()
        }.setRemoveAction {
            dtm.removeRow(configTable.selectedRow)
        }.setRemoveActionUpdater { configTable.selectedRow != -1 }
            .setMoveUpAction {
                val row = configTable.selectedRow
                dtm.moveRow(row, row, row - 1)
                configTable.setRowSelectionInterval(row - 1, row - 1)
            }.setMoveDownAction {
                val row = configTable.selectedRow
                dtm.moveRow(row, row, row + 1)
                configTable.setRowSelectionInterval(row + 1, row + 1)
            }.addExtraAction(refreshAction)

        val panelTable = tableToolbarDecorator.createPanel()

        scroll.add(panelTable)

        populateCachedValues()

        templateArea.text = template
        return panel {
            noteRow("Customize the list of templates to be included in the Git Master View")
            row {
                panelTable(grow)
            }
            row {
                noteRow("Write your own template by using the variable names created above enclosed within \$variableName\nEg: [\$type] - [\$issueName] [\$environment]: \$commitMessage")
            }
            row {
                templateArea(grow)
            }
        }
    }

    private fun populateCachedValues() {
        while (dtm.rowCount > 0) {
            dtm.removeRow(0)
        }
        if (cachedItems.isNotEmpty()) {
            for (i in 0 until cachedItems.size) {
                createValueRow(cachedItems[i])
            }
        }
    }

    private fun createValueRow(itemModel: ItemModel) {
        dtm.addRow(arrayOf(itemModel.name, itemModel.type, itemModel.variableName, itemModel.label))
    }

    private fun addRow(itemModel: ItemModel) {
        dtm.addRow(arrayOf(itemModel.name, itemModel.type, itemModel.variableName, itemModel.label))
    }

    private fun getCurrentItems(): List<ItemModel> {
        val currentItems: ArrayList<ItemModel> = ArrayList()
        val vector = dtm.dataVector
        for (i in 0 until dtm.rowCount) {
            val name = ((vector.elementAt(i) as? Vector<*>)?.elementAt(0) as? String) ?: ""
            val type = ((vector.elementAt(i) as? Vector<*>)?.elementAt(1) as? String) ?: ""
            val variable = ((vector.elementAt(i) as? Vector<*>)?.elementAt(2) as? String) ?: ""
            val value = ((vector.elementAt(i) as? Vector<*>)?.elementAt(3) as? String) ?: ""
            currentItems.add(ItemModel(name, type, variable, value))
        }
        println(currentItems.toString())
        return currentItems
    }

    override fun isModified(): Boolean = modified

    override fun apply() {
        val config = PropertiesComponent.getInstance()
        config.setValue("ITEMS_JSON", Gson().toJson(getCurrentItems()).toString())
        config.setValue("TEMPLATE", templateArea.text)
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
