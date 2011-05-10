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

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.parts.OutputTreeNodeEditPart;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.util.StringUtil;

/**
 * DOC talend class global comment. Detailled comment
 */
public class FixValueAction extends SelectionAction {

    private OutputTreeNode selectedNode;

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.FixValueAction";

    public FixValueAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setText("Set A Fixed Prefix");
    }

    @Override
    public void run() {
        if (selectedNode != null) {
            IInputValidator validator = new IInputValidator() {

                public String isValid(String newText) {
                    if (StringUtil.validateLabelForFixedValue(newText)) {
                        return null;
                    } else {
                        return "Prefix is invalid";
                    }

                }

            };

            InputDialog dialog = new InputDialog(null, "Input a fix prefix", "Input the default value' valid label",
                    selectedNode.getDefaultValue(), validator);
            if (dialog.open() == Window.OK) {
                selectedNode.setDefaultValue(dialog.getValue());
            }
        }
    }

    @Override
    protected boolean calculateEnabled() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        } else {
            Object object = getSelectedObjects().get(0);
            if (object instanceof OutputTreeNodeEditPart) {
                OutputTreeNodeEditPart nodePart = (OutputTreeNodeEditPart) object;
                this.selectedNode = (OutputTreeNode) nodePart.getModel();
                boolean isNameSpace = NodeType.NAME_SPACE.equals(selectedNode.getNodeType());
                if (isNameSpace && (selectedNode.getExpression() == null || "".equals(selectedNode.getExpression()))) {
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
