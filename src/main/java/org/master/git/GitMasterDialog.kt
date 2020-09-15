package org.master.git

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import java.awt.Dimension
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class GitMasterDialog : DialogWrapper(true), ItemListener, DocumentListener {

    private var items: ArrayList<ItemModel> = ArrayList()

    private val variableMap: LinkedHashMap<String, JComponent> = LinkedHashMap()
    private val previewLabel = JLabel()
    private var template = ""

    override fun createCenterPanel(): JComponent? {
        val config = PropertiesComponent.getInstance()
        val itemsJson = config.getValue("ITEMS_JSON", Gson().toJson(items).toString())
        template = config.getValue("TEMPLATE", "")
        items = Gson().fromJson(itemsJson, object : TypeToken<ArrayList<ItemModel>>() {}.type)

        val itemPanel = createRow()
        return panel {
            row {
                itemPanel(grow)
            }
            row {
                previewLabel(grow)
                previewLabel.text = previewTemplate(template)
            }
        }
    }

    private fun previewTemplate(template: String): String {
        var modifiedTemplate = template
        variableMap.forEach {
            val replaceItem = "\$${it.key}"
            println(replaceItem + ": " + it.value::class.java.name)
            modifiedTemplate = modifiedTemplate.replace(
                replaceItem, when (it.value) {
                    is JTextField -> {
                        (it.value as? JTextField)?.text ?: replaceItem
                    }
                    is JComboBox<*> -> {
                        (it.value as? JComboBox<*>)?.selectedItem?.toString() ?: replaceItem
                    }
                    else -> replaceItem
                }, true
            )
        }
        return modifiedTemplate
    }

    private fun createRow(): JPanel {
        return panel {
            items.forEach {
                row {
                    label(it.name)
                    if (it.type == ItemModel.TEXT) {
                        val textLabel = JTextField()
                        textLabel.minimumSize = Dimension(300, textLabel.preferredSize.height)
                        variableMap[it.variableName] = textLabel
                        textLabel.document.addDocumentListener(this@GitMasterDialog)
                        textLabel(grow)
                    } else {
                        val typeField = JComboBox(it.label.split(",").toTypedArray())
                        typeField.minimumSize = Dimension(300, typeField.preferredSize.height)
                        variableMap[it.variableName] = typeField
                        typeField.addItemListener(this@GitMasterDialog)
                        typeField(grow)
                    }
                }
            }
        }
    }

    init {
        init()
        title = "Git Master"
    }

    override fun itemStateChanged(e: ItemEvent?) {
        previewLabel.text = previewTemplate(template)
    }

    override fun insertUpdate(e: DocumentEvent?) {
        previewLabel.text = previewTemplate(template)
    }

    override fun removeUpdate(e: DocumentEvent?) {
        previewLabel.text = previewTemplate(template)
    }

    override fun changedUpdate(e: DocumentEvent?) {
        previewLabel.text = previewTemplate(template)
    }
}