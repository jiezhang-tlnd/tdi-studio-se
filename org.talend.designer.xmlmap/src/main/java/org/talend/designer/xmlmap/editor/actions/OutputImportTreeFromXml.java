// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.xmlmap.editor.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlmapFactory;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.Attribute;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.Element;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.FOXTreeNode;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.NameSpaceNode;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.util.TreeUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class OutputImportTreeFromXml extends SelectionAction {

    private OutputTreeNode parentNode;

    private MapperManager mapperManager;

    private Shell shell;

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.OutputImportTreeFromXml";

    public OutputImportTreeFromXml(IWorkbenchPart part, Shell shell) {
        super(part);
        this.shell = shell;
        setId(ID);
        setText("Import From File");
    }

    @Override
    public void run() {
        List<FOXTreeNode> list = new ArrayList<FOXTreeNode>();
        FileDialog f = new FileDialog(shell);
        String file = f.open();
        if (file != null) {
            list = TreeUtil.getFoxTreeNodes(file);
            TreeNode treeNodeRoot = XmlMapUtil.getOutputTreeNodeRoot(parentNode);

            XmlMapUtil.detachNodeConnections(treeNodeRoot, mapperManager.getCopyOfMapData(), true);
            parentNode.getChildren().clear();
            prepareEmfTreeNode(list, parentNode);
            if (mapperManager != null && parentNode.eContainer() instanceof OutputXmlTree) {
                mapperManager.refreshOutputTreeSchemaEditor((OutputXmlTree) parentNode.eContainer());
            }
        }

    }

    private void prepareEmfTreeNode(List<FOXTreeNode> list, OutputTreeNode parent) {
        if (list == null || list.isEmpty()) {
            return;
        }
        String xPath = parent.getXpath();
        for (FOXTreeNode foxNode : list) {
            OutputTreeNode createTreeNode = XmlmapFactory.eINSTANCE.createOutputTreeNode();
            createTreeNode.setName(foxNode.getLabel());
            if (foxNode instanceof Element) {
                createTreeNode.setNodeType(NodeType.ELEMENT);
            } else if (foxNode instanceof Attribute) {
                createTreeNode.setNodeType(NodeType.ATTRIBUT);
            } else if (foxNode instanceof NameSpaceNode) {
                createTreeNode.setNodeType(NodeType.NAME_SPACE);
            }
            createTreeNode.setXpath(XmlMapUtil.getXPath(xPath, createTreeNode.getName(), createTreeNode.getNodeType()));
            if (foxNode.getDataType() != null && "".equals(foxNode.getDataType())) {
                createTreeNode.setType(foxNode.getDataType());
            } else {
                createTreeNode.setType(XmlMapUtil.DEFAULT_DATA_TYPE);
            }
            parent.getChildren().add(createTreeNode);
            if (foxNode.getChildren() != null && !foxNode.getChildren().isEmpty()) {
                prepareEmfTreeNode(foxNode.getChildren(), createTreeNode);
            }
        }

    }

    public void setMapperManager(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        } else {
            Object object = getSelectedObjects().get(0);
            if (object instanceof TreeNodeEditPart) {
                TreeNodeEditPart parentPart = (TreeNodeEditPart) object;
                parentNode = (OutputTreeNode) parentPart.getModel();
                if (parentNode.eContainer() instanceof OutputXmlTree && XmlMapUtil.DOCUMENT.equals(parentNode.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void update(Object selection) {
        setSelection(new StructuredSelection(selection));
    }
}
