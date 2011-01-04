// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.components.IODataComponent;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTool;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.FTPConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.SAPConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SAPIDocUnit;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.builder.connection.impl.XmlFileConnectionImpl;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.utils.DesignerUtilities;
import org.talend.designer.core.utils.SAPParametersUtils;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.ui.utils.ConnectionContextHelper;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class ChangeValuesFromRepository extends ChangeMetadataCommand {

    private final Map<String, Object> oldValues;

    private final IElement elem;

    private final Connection connection;

    private final String value;

    private final String propertyName;

    private String oldMetadata;

    private boolean isGuessQuery = false;

    private final String propertyTypeName;

    private final String updataComponentParamName;

    // use for SAP
    private String sapFunctionLabel = null;

    private String sapIDocLabel = null;

    // for jobtemplate plugin(true, bug 5198)
    private boolean ignoreContextMode = false;

    private boolean dragAndDropAction = false;

    private boolean reOpenXSDBool = false;

    private int index;

    private String currentTableName;

    private IMetadataTable table;

    public ChangeValuesFromRepository(IElement elem, Connection connection, String propertyName, String value) {
        this.elem = elem;
        this.connection = connection;
        this.value = value;
        this.propertyName = propertyName;
        oldValues = new HashMap<String, Object>();
        setLabel(Messages.getString("PropertyChangeCommand.Label")); //$NON-NLS-1$
        // for job settings extra (feature 2710)
        // if (JobSettingsConstants.isExtraParameter(propertyName)) {
        // propertyTypeName = JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName());
        // repositoryPropertyTypeName =
        // JobSettingsConstants.getExtraParameterName(EParameterName.REPOSITORY_PROPERTY_TYPE
        // .getName());
        // updataComponentParamName =
        // JobSettingsConstants.getExtraParameterName(EParameterName.UPDATE_COMPONENTS.getName());
        // } else {
        propertyTypeName = EParameterName.PROPERTY_TYPE.getName();
        EParameterName.REPOSITORY_PROPERTY_TYPE.getName();
        updataComponentParamName = EParameterName.UPDATE_COMPONENTS.getName();
        // }
    }

    public ChangeValuesFromRepository(IElement elem, Connection connection, String propertyName, String value, int index,
            boolean isNotSim) {
        this.elem = elem;
        this.connection = connection;
        this.value = value;
        this.propertyName = propertyName;
        this.index = index;
        this.isNotSim = isNotSim;
        oldValues = new HashMap<String, Object>();
        setLabel(Messages.getString("PropertyChangeCommand.Label")); //$NON-NLS-1$
        propertyTypeName = EParameterName.PROPERTY_TYPE.getName();
        EParameterName.REPOSITORY_PROPERTY_TYPE.getName();
        updataComponentParamName = EParameterName.UPDATE_COMPONENTS.getName();
    }

    public ChangeValuesFromRepository(IElement elem, Connection connection, String propertyName, String value,
            boolean reOpenXSDBool) {
        this.reOpenXSDBool = reOpenXSDBool;
        this.elem = elem;
        this.connection = connection;
        this.value = value;
        this.propertyName = propertyName;
        oldValues = new HashMap<String, Object>();
        if (connection instanceof XmlFileConnectionImpl) {
            if (((XmlFileConnectionImpl) connection).getXmlFilePath().endsWith(".xsd")
                    || ((XmlFileConnectionImpl) connection).getXmlFilePath().endsWith(".xsd\""))
                dragAndDropAction = true;
        }
        setLabel(Messages.getString("PropertyChangeCommand.Label")); //$NON-NLS-1$
        propertyTypeName = EParameterName.PROPERTY_TYPE.getName();
        EParameterName.REPOSITORY_PROPERTY_TYPE.getName();
        updataComponentParamName = EParameterName.UPDATE_COMPONENTS.getName();
    }

    public ChangeValuesFromRepository(IElement elem, Connection connection, IMetadataTable table, String propertyName,
            String value, boolean reOpenXSDBool) {
        this(elem, connection, propertyName, value, reOpenXSDBool);
        this.table = table;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {

        // Force redraw of Commponents propoerties
        elem.setPropertyValue(updataComponentParamName, new Boolean(true));

        // hywang add for excel2007
        // Object obj = RepositoryToComponentProperty.getValue(connection, EParameterName.FILE_PATH.getName());
        // String s = TalendTextUtils.removeQuotes(obj.toString());
        // String versionCheckFor2007 = "false";
        // if (s.endsWith(".xlsx")) {
        // versionCheckFor2007 = "true";
        // }
        // if (elem.getElementParameter("VERSION_2007") != null) {
        // elem.getElementParameter("VERSION_2007").setValue(versionCheckFor2007);
        // }

        boolean allowAutoSwitch = true;

        IElementParameter elemParam = elem.getElementParameter(EParameterName.REPOSITORY_ALLOW_AUTO_SWITCH.getName());
        if (elemParam != null) {
            allowAutoSwitch = (Boolean) elemParam.getValue();
        }
        if (!allowAutoSwitch && (elem instanceof Node)) {
            // force the autoSwitch to true if the schema is empty and if the
            // query is not set.
            Node node = (Node) elem;
            boolean isSchemaEmpty = false;
            if (node.getMetadataList().size() > 0) {
                isSchemaEmpty = node.getMetadataList().get(0).getListColumns().size() == 0;
            } else {
                isSchemaEmpty = true;
            }

            for (IElementParameter curParam : node.getElementParameters()) {
                if (curParam.getFieldType().equals(EParameterFieldType.MEMO_SQL)) {
                    if (curParam.getDefaultValues().size() > 0) {
                    }
                }
            }
            if (isSchemaEmpty) {
                allowAutoSwitch = true;
            }
        }

        if (propertyName.split(":")[1].equals(propertyTypeName)) { //$NON-NLS-1$
            elem.setPropertyValue(propertyName, value);
            if (allowAutoSwitch) {
                setOtherProperties();
            }
        } else {
            oldMetadata = (String) elem.getPropertyValue(propertyName);
            elem.setPropertyValue(propertyName, value);
            if (allowAutoSwitch) {
                setOtherProperties();
            }
        }

        if (propertyName.split(":")[1].equals(propertyTypeName) && (EmfComponent.BUILTIN.equals(value))) { //$NON-NLS-1$
            for (IElementParameter param : elem.getElementParameters()) {

                boolean paramFlag = JobSettingsConstants.isExtraParameter(param.getName());
                boolean extraFlag = JobSettingsConstants.isExtraParameter(propertyName.split(":")[0]); //$NON-NLS-1$
                if (paramFlag == extraFlag) {
                    // for memo sql
                    if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                        IElementParameter querystoreParam = elem.getElementParameterFromField(
                                EParameterFieldType.QUERYSTORE_TYPE, param.getCategory());
                        if (querystoreParam != null) {
                            Map<String, IElementParameter> childParam = querystoreParam.getChildParameters();
                            if (childParam != null) {
                                IElementParameter queryTypeParam = childParam.get(EParameterName.QUERYSTORE_TYPE.getName());
                                if (queryTypeParam != null && EmfComponent.REPOSITORY.equals(queryTypeParam.getValue())) {// is
                                    continue;
                                }
                            }
                        }
                    }
                    param.setReadOnly(false);
                    // for job settings extra.(feature 2710)
                    param.setRepositoryValueUsed(false);
                }

            }
        } else {
            oldValues.clear();
            IElementParameter propertyParam = elem.getElementParameter(propertyName);
            EComponentCategory currentCategory = propertyParam.getCategory();
            for (IElementParameter param : elem.getElementParameters()) {
                String repositoryValue = param.getRepositoryValue();
                boolean b = elem instanceof INode
                        && (((INode) elem).getComponent().getName().equals("tHL7Input") //$NON-NLS-1$
                                || ((INode) elem).getComponent().getName().equals("tAdvancedFileOutputXML") //$NON-NLS-1$
                                || ((INode) elem).getComponent().getName().equals("tMDMOutput")
                                || ((INode) elem).getComponent().getName().equals("tWebService") || ((INode) elem).getComponent()
                                .getName().equals("tCreateTable")); //$NON-NLS-1$

                if (("TYPE".equals(repositoryValue) || (param.isShow(elem.getElementParameters())) || b) //$NON-NLS-1$
                        && (repositoryValue != null) && (!param.getName().equals(propertyTypeName))) {
                    IElementParameter relatedPropertyParam = elem.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE,
                            param.getCategory());
                    if (!relatedPropertyParam.getCategory().equals(currentCategory) && !repositoryValue.equals("ENCODING")) { //$NON-NLS-1$
                        continue;
                    }
                    Object objectValue;
                    if (connection instanceof XmlFileConnection && this.dragAndDropAction == true
                            && repositoryValue.equals("FILE_PATH") && reOpenXSDBool == true) {

                        objectValue = RepositoryToComponentProperty.getXmlAndXSDFileValue((XmlFileConnection) connection,
                                repositoryValue);
                    } else {
                        objectValue = RepositoryToComponentProperty.getValue(connection, repositoryValue, table);
                    }
                    if (objectValue != null) {
                        oldValues.put(param.getName(), param.getValue());
                        if (param.getFieldType().equals(EParameterFieldType.CLOSED_LIST)
                                && param.getRepositoryValue().equals("TYPE")) { //$NON-NLS-1$
                            String dbVersion = "";
                            if (connection instanceof DatabaseConnection) {
                                dbVersion = ((DatabaseConnection) connection).getDbVersionString();
                            }
                            boolean found = false;
                            String[] list = param.getListRepositoryItems();
                            for (int i = 0; (i < list.length) && (!found); i++) {
                                if (objectValue.equals(list[i])) {
                                    found = true;
                                    elem.setPropertyValue(param.getName(), param.getListItemsValue()[i]);
                                }
                            }

                            IElementParameter elementParameter = null;
                            IElementParameter elementParameter2 = null;
                            if (EParameterName.DB_TYPE.getName().equals(param.getName())) {
                                elementParameter = elem.getElementParameter(EParameterName.DB_VERSION.getName());
                                elementParameter2 = elem.getElementParameter(EParameterName.SCHEMA_DB.getName());
                            } else {
                                elementParameter = elem.getElementParameter(JobSettingsConstants
                                        .getExtraParameterName(EParameterName.DB_VERSION.getName()));
                                elementParameter2 = elem.getElementParameter(JobSettingsConstants
                                        .getExtraParameterName(EParameterName.SCHEMA_DB.getName()));
                            }
                            String dbType = "";
                            if (param.getValue() != null) {
                                int indexOfItemFromList = param.getIndexOfItemFromList(param.getValue().toString());
                                if (indexOfItemFromList != -1) {
                                    dbType = param.getListItemsDisplayCodeName()[indexOfItemFromList];
                                }
                            }
                            if (StatsAndLogsConstants.JDBC.equals(dbType)) {
                                IElementParameter dbNameParm = elem.getElementParameter(EParameterName.DBNAME.getName());
                                if (dbNameParm != null) {
                                    dbNameParm.setValue("");
                                }
                            } else {
                                IElementParameter rulParam = elem.getElementParameter(EParameterName.URL.getName());
                                if (rulParam != null) {
                                    rulParam.setValue("");
                                }
                                IElementParameter classParam = elem.getElementParameter(EParameterName.DRIVER_CLASS.getName());
                                if (classParam != null) {
                                    classParam.setValue("");
                                }
                                IElementParameter jarParam = elem.getElementParameter(EParameterName.DRIVER_JAR.getName());
                                if (jarParam != null) {
                                    jarParam.setValue(new ArrayList<Map<String, Object>>());
                                }
                            }

                            setDbVersion(elementParameter, dbVersion);
                            DesignerUtilities.setSchemaDB(elementParameter2, param.getValue());
                        } else if (param.getFieldType().equals(EParameterFieldType.CLOSED_LIST)
                                && param.getRepositoryValue().equals("FRAMEWORK_TYPE")) { //$NON-NLS-1$
                            String[] list = param.getListItemsDisplayName();
                            for (int i = 0; i < list.length; i++) {
                                if (objectValue.equals(list[i])) {
                                    elem.setPropertyValue(param.getName(), param.getListItemsValue()[i]);
                                }
                            }
                        } else {
                            if (repositoryValue.equals("ENCODING")) { //$NON-NLS-1$
                                IElementParameter paramEncoding = param.getChildParameters().get(
                                        EParameterName.ENCODING_TYPE.getName());
                                if (connection instanceof FTPConnection) {
                                    if (((FTPConnection) connection).getEcoding() != null) {
                                        paramEncoding.setValue(((FTPConnection) connection).getEcoding());
                                    } else {
                                        paramEncoding.setValue(EmfComponent.ENCODING_TYPE_CUSTOM);
                                    }

                                } else {
                                    paramEncoding.setValue(EmfComponent.ENCODING_TYPE_CUSTOM);
                                    // paramEncoding.setRepositoryValueUsed(true);
                                }

                            } else if (repositoryValue.equals("CSV_OPTION")) { //$NON-NLS-1$
                                setOtherProperties();
                            }

                            if (repositoryValue.equals("MODULENAME")) {//$NON-NLS-1$
                                List list = new ArrayList();
                                Object[] listItemsValue = elem.getElementParameter("MODULENAME").getListItemsValue();
                                for (int i = 0; i < listItemsValue.length; i++) {
                                    list.add(listItemsValue[i]);
                                }
                                if (list != null && !list.contains(objectValue)) {
                                    objectValue = "CustomModule";//$NON-NLS-1$
                                }
                            }

                            // hywang add for excel 2007
                            if (repositoryValue.equals(EParameterName.FILE_PATH.getName())) {
                                String filePath = "";
                                if (connection.isContextMode()) {
                                    ContextItem contextItem = ContextUtils.getContextItemById2(connection.getContextId());

                                    if (contextItem != null) {
                                        String selectedContext = contextItem.getDefaultContext();
                                        final ContextType contextTypeByName = ContextUtils.getContextTypeByName(contextItem,
                                                selectedContext, true);
                                        filePath = ConnectionContextHelper.getOriginalValue(contextTypeByName,
                                                objectValue.toString());
                                    }
                                } else {

                                    filePath = TalendTextUtils.removeQuotes(objectValue.toString());
                                }
                                boolean versionCheckFor2007 = false; //$NON-NLS-N$
                                if (filePath != null && filePath.endsWith(".xlsx")) { //$NON-NLS-N$
                                    versionCheckFor2007 = true; //$NON-NLS-N$
                                }
                                if (elem.getElementParameter("VERSION_2007") != null) { //$NON-NLS-N$
                                    elem.setPropertyValue("VERSION_2007", versionCheckFor2007); //$NON-NLS-N$
                                }
                            }
                            if (param.getFieldType().equals(EParameterFieldType.FILE)) {
                                if (objectValue != null) {
                                    objectValue = objectValue.toString().replace("\\", "/");
                                }
                            }
                            elem.setPropertyValue(param.getName(), objectValue);
                        }
                        param.setRepositoryValueUsed(true);
                    } else if (param.getFieldType().equals(EParameterFieldType.TABLE)
                            && param.getRepositoryValue().equals("XML_MAPPING")) { //$NON-NLS-1$

                        List<Map<String, Object>> table = (List<Map<String, Object>>) elem.getPropertyValue(param.getName());
                        if (((Node) elem).getMetadataList().size() > 0) {
                            IMetadataTable metaTable = ((Node) elem).getMetadataList().get(0);
                            RepositoryToComponentProperty.getTableXmlFileValue(connection, "XML_MAPPING", param, //$NON-NLS-1$
                                    table, metaTable);
                            param.setRepositoryValueUsed(true);
                        }
                    } else if (param.getFieldType().equals(EParameterFieldType.TABLE)
                            && param.getRepositoryValue().equals("WSDL_PARAMS") && connection != null) { //$NON-NLS-1$
                        List<Map<String, Object>> table = (List<Map<String, Object>>) elem.getPropertyValue(param.getName());
                        table.clear();
                        ArrayList parameters = ((WSDLSchemaConnection) connection).getParameters();
                        for (Object object : parameters) {
                            Map<String, Object> map2 = new HashMap<String, Object>();
                            map2.put("VALUE", TalendTextUtils.addQuotes(object.toString())); //$NON-NLS-1$
                            table.add(map2);
                        }
                        param.setRepositoryValueUsed(true);
                    } else {
                        // For SAP
                        String paramName = param.getName();
                        if ("MAPPING_INPUT".equals(paramName)
                                || "SAP_FUNCTION".equals(paramName) // INPUT_PARAMS should be MAPPING_INPUT,bug16426
                                || "OUTPUT_PARAMS".equals(paramName) || "SAP_ITERATE_OUT_TYPE".equals(paramName)
                                || "SAP_ITERATE_OUT_TABLENAME".equals(paramName)) {
                            SAPParametersUtils.retrieveSAPParams(elem, connection, param, getSapFunctionLabel());
                        }
                        if ("GATEWAYSERVICE".equals(paramName) || "PROGRAMID".equals(paramName) || "FORMAT_XML".equals(paramName)
                                || "FILE_IDOC_XML".equals(paramName) || "FORMAT_HTML".equals(paramName)
                                || "FILE_IDOC_HTML".equals(paramName)) {
                            SAPParametersUtils.getSAPIDocParams(elem, connection, param, getSapIDocLabel());
                        }
                    }

                    if (param.isRepositoryValueUsed()) {
                        param.setReadOnly(false);
                    }
                }
            }
            // (bug 5198)
            IElementParameter parentParameter = propertyParam.getParentParameter();
            if (parentParameter != null) {
                IElementParameter param = parentParameter.getChildParameters().get(
                        EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                if (param != null && propertyParam == param) { // avoid to process twice.
                    ConnectionItem connItem = UpdateRepositoryUtils.getConnectionItemByItemId((String) param.getValue());
                    if (connItem != null) {
                        if (elem instanceof Node) {
                            ConnectionContextHelper.addContextForNodeParameter((Node) elem, connItem, ignoreContextMode);
                        } else if (elem instanceof Process) {
                            ConnectionContextHelper.addContextForProcessParameter((Process) elem, connItem, param.getCategory(),
                                    ignoreContextMode);
                        }
                    }
                }
            }
        }
        // change AS400 value
        for (IElementParameter curParam : elem.getElementParameters()) {
            if (curParam.getFieldType().equals(EParameterFieldType.AS400_CHECK)) {
                setOtherProperties();
            }
        }

        if (elem instanceof Node) {
            ((Process) ((Node) elem).getProcess()).checkProcess();
        }
    }

    private void setDbVersion(IElementParameter elementParameter, String value) {
        if (elementParameter == null || value == null) {
            return;
        }
        if (value.indexOf("Access") != -1) {//$NON-NLS-1$
            elementParameter.setValue(value);
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.ACCESS_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.ACCESS_VERSION_DRIVER);
        } else if (value.indexOf("ORACLE") != -1) {//$NON-NLS-1$
            elementParameter.setValue(value);
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.ORACLE_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.ORACLE_VERSION_DRIVER);
        } else if (value.indexOf("AS400") != -1) {//$NON-NLS-1$
            elementParameter.setValue(value);
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.AS400_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.AS400_VERSION_DRIVER);
        } else if (value.indexOf("MYSQL") != -1) {//$NON-NLS-1$
            elementParameter.setValue(value);
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.MYSQL_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.MYSQL_VERSION_DRIVER);
        }
    }

    private String getFirstRepositoryTable(Item item) {
        if (isNotSim) {
            if (item != null) {
                // final List<MetadataTable> tables = UpdateRepositoryUtils.getMetadataTablesFromItem(item);
                // if (tables != null && !tables.isEmpty()) {
                // if (tables.size() > index) {
                // for (MetadataTable table : tables) {
                // if (table.getLabel().equals(getCurrentTableName())) {
                // return table.getLabel();
                // }
                // }
                // return tables.get(index).getLabel();
                // } else {
                // return tables.get(0).getLabel();
                // }
                //
                // }
                return getCurrentTableName();
            }
        } else {
            if (item != null) {
                final List<MetadataTable> tables = UpdateRepositoryUtils.getMetadataTablesFromItem(item);
                if (tables != null && !tables.isEmpty()) {
                    return tables.get(0).getLabel();
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * qzhang Comment method "setOtherProperties".
     */
    private void setOtherProperties() {
        boolean metadataInput = false;
        IElementParameter currentParam = elem.getElementParameter(propertyName);

        Item item = null;
        IElementParameter repositoryParam = elem.getElementParameter(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
        if (repositoryParam != null) {
            item = UpdateRepositoryUtils.getConnectionItemByItemId((String) repositoryParam.getValue());
        }

        if (elem instanceof Node) {
            Node node = (Node) elem;
            if (node.getCurrentActiveLinksNbInput(EConnectionType.FLOW_MAIN) > 0
                    || node.getCurrentActiveLinksNbInput(EConnectionType.FLOW_REF) > 0
                    || node.getCurrentActiveLinksNbInput(EConnectionType.TABLE) > 0) {
                metadataInput = true;
            }

            boolean hasSchema = elem.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE) != null;
            if (value.equals(EmfComponent.BUILTIN)) {
                if (!metadataInput && hasSchema) {
                    elem.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), value);
                }
                elem.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), value);
            } else {
                if (hasSchema) {
                    for (IElementParameter param : elem.getElementParameters()) {
                        if (param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)) {
                            if (!metadataInput) {
                                IElementParameter repositorySchemaTypeParameter = param.getChildParameters().get(
                                        EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
                                String repositoryTable = null;
                                if (propertyName.split(":")[1].equals(EParameterName.PROPERTY_TYPE.getName())) { //$NON-NLS-1$
                                    repositoryTable = (String) repositorySchemaTypeParameter.getValue();
                                } else if (item != null) {
                                    repositoryTable = item.getProperty().getId() + " - " + getFirstRepositoryTable(item); //$NON-NLS-1$
                                    repositorySchemaTypeParameter.setValue(repositoryTable);
                                }
                                if (isNotSim) {
                                    if (item != null && UpdateRepositoryUtils.getMetadataTablesFromItem(item) != null) {
                                        if (repositoryTable != null && !"".equals(repositoryTable)) { //$NON-NLS-1$
                                            param.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName())
                                                    .setValue(EmfComponent.REPOSITORY);
                                            IMetadataTable table = MetadataTool.getMetadataFromRepository(repositoryTable);
                                            // repositoryTableMap.get(repositoryTable);
                                            if (table != null) {
                                                table = table.clone();
                                                setDBTableFieldValue(node, table.getTableName(), null);
                                                setSAPFunctionName(node, table.getLabel());
                                                table.setTableName(node.getMetadataList().get(0).getTableName());
                                                if (!table.sameMetadataAs(node.getMetadataList().get(0))) {
                                                    ChangeMetadataCommand cmd = new ChangeMetadataCommand(node, param, null,
                                                            table, param);
                                                    cmd.setConnection(connection);
                                                    cmd.setRepositoryMode(true);
                                                    cmd.execute(true);
                                                }
                                            }
                                        }
                                    }
                                } else if (isMdmOutput()) {
                                    if (item != null && UpdateRepositoryUtils.getMetadataTablesFromItem(item) != null
                                            && UpdateRepositoryUtils.getMetadataTablesFromItem(item).size() > 0) {
                                        if (repositoryTable != null && !"".equals(repositoryTable)) { //$NON-NLS-1$
                                            param.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName())
                                                    .setValue(EmfComponent.REPOSITORY);
                                            IMetadataTable table = MetadataTool.getMetadataFromRepository(repositoryTable);
                                            // repositoryTableMap.get(repositoryTable);
                                            if (table != null) {
                                                table = table.clone();
                                                setDBTableFieldValue(node, table.getTableName(), null);
                                                setSAPFunctionName(node, table.getLabel());
                                                table.setTableName(node.getMetadataList().get(0).getTableName());
                                                ChangeMetadataCommand cmd = new ChangeMetadataCommand(node, param, null, table,
                                                        param);
                                                cmd.setConnection(connection);
                                                cmd.setRepositoryMode(true);
                                                cmd.execute(true);
                                            }
                                        }
                                    }
                                } else {
                                    if (item != null && UpdateRepositoryUtils.getMetadataTablesFromItem(item) != null
                                            && UpdateRepositoryUtils.getMetadataTablesFromItem(item).size() == 1) {
                                        if (repositoryTable != null && !"".equals(repositoryTable)) { //$NON-NLS-1$
                                            param.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName())
                                                    .setValue(EmfComponent.REPOSITORY);
                                            IMetadataTable table = MetadataTool.getMetadataFromRepository(repositoryTable);
                                            // repositoryTableMap.get(repositoryTable);
                                            if (table != null) {
                                                table = table.clone();
                                                setDBTableFieldValue(node, table.getTableName(), null);
                                                setSAPFunctionName(node, table.getLabel());
                                                table.setTableName(node.getMetadataList().get(0).getTableName());
                                                if (!table.sameMetadataAs(node.getMetadataList().get(0))) {
                                                    ChangeMetadataCommand cmd = new ChangeMetadataCommand(node, param, null,
                                                            table, param);
                                                    cmd.setConnection(connection);
                                                    cmd.setRepositoryMode(true);
                                                    cmd.execute(true);
                                                }
                                            }
                                        }
                                    } else if (item != null && UpdateRepositoryUtils.getMetadataTablesFromItem(item) != null
                                            && UpdateRepositoryUtils.getMetadataTablesFromItem(item).size() == 2) {
                                        final List<MetadataTable> tables = UpdateRepositoryUtils.getMetadataTablesFromItem(item);
                                        if (tables != null && !tables.isEmpty()) {
                                            if (param.getName().equals("INPUT_SCHEMA")) {
                                                repositoryTable = item.getProperty().getId() + " - " + "Input";
                                                repositorySchemaTypeParameter.setValue(repositoryTable);
                                            } else {
                                                repositoryTable = item.getProperty().getId() + " - " + "Output";
                                                repositorySchemaTypeParameter.setValue(repositoryTable);
                                            }
                                            if (repositoryTable != null && !"".equals(repositoryTable)) { //$NON-NLS-1$
                                                param.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName())
                                                        .setValue(EmfComponent.REPOSITORY);

                                                IMetadataTable table = MetadataTool.getMetadataFromRepository(repositoryTable);
                                                if (table != null) {
                                                    table = table.clone();
                                                    setDBTableFieldValue(node, table.getTableName(), null);
                                                    setSAPFunctionName(node, table.getLabel());
                                                    INodeConnector mainConnector = node
                                                            .getConnectorFromType(EConnectionType.FLOW_MAIN);
                                                    IMetadataTable stable = null;
                                                    INodeConnector outputConnector = mainConnector;
                                                    if (mainConnector.getMaxLinkOutput() == 0) {
                                                        for (INodeConnector currentConnector : node.getListConnector()) {
                                                            if (!currentConnector.getBaseSchema().equals(
                                                                    EConnectionType.FLOW_MAIN.getName())
                                                                    && currentConnector.getMaxLinkOutput() > 0) {
                                                                outputConnector = currentConnector;

                                                            }
                                                        }
                                                    }
                                                    if (param.getName().equals("INPUT_SCHEMA")) {
                                                        stable = node.getMetadataFromConnector("FLOW");

                                                    } else if (param.getName().equals("SCHEMA")) {
                                                        stable = node.getMetadataFromConnector("OUTPUT");
                                                    }
                                                    if (stable != null) {
                                                        table.setTableName(stable.getTableName());
                                                        if (!table.sameMetadataAs(stable)) {
                                                            ChangeMetadataCommand cmd = new ChangeMetadataCommand(node, param,
                                                                    null, table, param);
                                                            cmd.setConnection(connection);
                                                            cmd.setRepositoryMode(true);
                                                            cmd.execute(true);
                                                            // break;
                                                        }
                                                    }

                                                }
                                            }

                                        }

                                    }
                                }

                            } else {
                                Node sourceNode = getRealSourceNode((INode) elem);
                                if (sourceNode != null) {
                                    IMetadataTable sourceMetadataTable = sourceNode.getMetadataList().get(0);
                                    Object sourceSchema = sourceNode.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
                                    boolean isTake = !sourceNode.isExternalNode() && sourceSchema != null
                                            && elem.getPropertyValue(EParameterName.SCHEMA_TYPE.getName()) != null;
                                    if (isTake && getTake()) {
                                        ChangeMetadataCommand cmd = new ChangeMetadataCommand((Node) elem, param, null,
                                                sourceMetadataTable, param);
                                        cmd.setConnection(connection);
                                        cmd.execute(true);
                                        elem.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), sourceSchema);
                                        if (sourceSchema.equals(EmfComponent.REPOSITORY)) {
                                            elem.setPropertyValue(EParameterName.REPOSITORY_SCHEMA_TYPE.getName(),
                                                    sourceNode.getPropertyValue(EParameterName.REPOSITORY_SCHEMA_TYPE.getName()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            IElementParameter queryParam = elem.getElementParameterFromField(EParameterFieldType.QUERYSTORE_TYPE,
                    currentParam.getCategory());
            IElementParameter queryStoreType = null;
            if (queryParam != null) {
                queryStoreType = queryParam.getChildParameters().get(EParameterName.QUERYSTORE_TYPE.getName());
            }

            if (item != null) {
                final List<Query> queries = UpdateRepositoryUtils.getQueriesFromItem(item);

                if (propertyName.split(":")[1].equals(EParameterName.PROPERTY_TYPE.getName())) { //$NON-NLS-1$
                    if (queries != null && !queries.isEmpty()) {
                        if (queryParam != null) {
                            queryStoreType.setValue(value);
                            if (value.equals(EmfComponent.REPOSITORY)) {
                                setQueryToRepositoryMode(queryParam, queries, item);
                            }
                        }
                        // query change
                    }
                } else {
                    if (queryParam != null) {
                        if (this.isGuessQuery || queries == null || (queries != null && queries.isEmpty())) {
                            queryStoreType.setValue(EmfComponent.BUILTIN);
                        } else {
                            queryStoreType.setValue(EmfComponent.REPOSITORY);
                            setQueryToRepositoryMode(queryParam, queries, item);
                        }
                    }
                    List<MetadataTable> tables = UpdateRepositoryUtils.getMetadataTablesFromItem(item);
                    if (tables == null || tables.isEmpty()) {
                        elem.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                    }
                }
            }
        }
    }

    private boolean isMdmOutput() {
        if (elem instanceof Node) {
            Node node = (Node) elem;
            if ("tMDMOutput".equals(node.getComponent().getName())) {
                return true;
            }
        }
        return false;
    }

    private void setQueryToRepositoryMode(IElementParameter queryParam, List<Query> queries, Item item) {

        IElementParameter repositoryParam = queryParam.getChildParameters().get(
                EParameterName.REPOSITORY_QUERYSTORE_TYPE.getName());
        Query query = UpdateRepositoryUtils.getQueryById(item, (String) repositoryParam.getValue());
        if (query == null && queries != null && !queries.isEmpty()) {
            query = queries.get(0);
            if (query != null) {
                repositoryParam.setValue(item.getProperty().getId() + " - " + query.getLabel()); //$NON-NLS-1$
            }

        }

        if (query != null) {
            IElementParameter memoSqlParam = elem.getElementParameterFromField(EParameterFieldType.MEMO_SQL,
                    queryParam.getCategory());
            String queryStr = QueryUtil.checkAndAddQuotes(query.getValue());
            memoSqlParam.setValue(queryStr);
            memoSqlParam.setRepositoryValueUsed(true);
        }
    }

    @SuppressWarnings("unchecked")
    protected Node getRealSourceNode(INode target) {
        Node sourceNode = null;
        IODataComponent input = null;
        List<org.talend.designer.core.ui.editor.connections.Connection> incomingConnections = null;
        incomingConnections = (List<org.talend.designer.core.ui.editor.connections.Connection>) target.getIncomingConnections();
        for (org.talend.designer.core.ui.editor.connections.Connection connec : incomingConnections) {
            if (connec.isActivate() && connec.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                input = new IODataComponent(connec);
            }
        }
        if (input != null) {
            INode source = input.getSource();
            if (source instanceof Node) {
                sourceNode = (Node) source;
                // final IExternalNode externalNode =
                // sourceNode.getExternalNode();
                // if (sourceNode.getPluginFullName() != null &&
                // !"".equals(sourceNode.getPluginFullName())
                // && sourceNode.getExternalNode() != null) {
                // return getRealSourceNode(externalNode);
                // }
            }
        }
        return sourceNode;
    }

    /**
     * qzhang Comment method "getTake".
     * 
     * @return
     */
    private Boolean take = null;

    private boolean getTake() {
        if (take == null) {
            take = MessageDialog.openQuestion(new Shell(), "", Messages //$NON-NLS-1$
                    .getString("ChangeValuesFromRepository.messageDialog.takeMessage")); //$NON-NLS-1$
        }
        return take;
    }

    @Override
    public void undo() {
        // Force redraw of Commponents propoerties
        elem.setPropertyValue(updataComponentParamName, new Boolean(true));

        if (propertyName.split(":")[1].equals(propertyTypeName) && (EmfComponent.BUILTIN.equals(value))) { //$NON-NLS-1$
            for (IElementParameter param : elem.getElementParameters()) {
                String repositoryValue = param.getRepositoryValue();
                if (param.isShow(elem.getElementParameters()) && (repositoryValue != null)
                        && (!param.getName().equals(propertyTypeName))) {
                    boolean paramFlag = JobSettingsConstants.isExtraParameter(param.getName());
                    boolean extraFlag = JobSettingsConstants.isExtraParameter(propertyTypeName);
                    if (paramFlag == extraFlag) {
                        // for job settings extra.(feature 2710)
                        param.setRepositoryValueUsed(true);
                    }
                }
            }
        } else {
            for (IElementParameter param : elem.getElementParameters()) {
                String repositoryValue = param.getRepositoryValue();
                if (param.isShow(elem.getElementParameters()) && (repositoryValue != null)) {
                    Object objectValue = RepositoryToComponentProperty.getValue(connection, repositoryValue, null);
                    if (dragAndDropAction == true && connection instanceof XmlFileConnectionImpl) {
                        objectValue = RepositoryToComponentProperty.getXmlAndXSDFileValue((XmlFileConnection) connection,
                                repositoryValue);
                        dragAndDropAction = false;
                    }
                    if (objectValue != null) {
                        elem.setPropertyValue(param.getName(), oldValues.get(param.getName()));
                        param.setRepositoryValueUsed(false);
                    }
                }
            }
        }
        IElementParameter currentParam = elem.getElementParameter(propertyName);
        if (propertyName.split(":")[1].equals(propertyTypeName)) { //$NON-NLS-1$
            if (value.equals(EmfComponent.BUILTIN)) {
                currentParam.setValue(EmfComponent.REPOSITORY);
            } else {
                currentParam.setValue(EmfComponent.BUILTIN);
                IElementParameter schemaParam = elem.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE,
                        currentParam.getCategory());
                if (schemaParam != null) {
                    IElementParameter schemaType = schemaParam.getChildParameters().get(EParameterName.SCHEMA_TYPE.getName());
                    schemaType.setValue(EmfComponent.BUILTIN);
                }

                IElementParameter queryParam = elem.getElementParameterFromField(EParameterFieldType.QUERYSTORE_TYPE,
                        currentParam.getCategory());
                if (queryParam != null) {
                    IElementParameter queryStoreType = queryParam.getChildParameters().get(
                            EParameterName.QUERYSTORE_TYPE.getName());
                    queryStoreType.setValue(EmfComponent.BUILTIN);
                }
            }
        } else {
            elem.setPropertyValue(propertyName, oldMetadata);
        }

        JobSettings.switchToCurJobSettingsView();
    }

    /**
     * Sets a sets of maps.
     * 
     * @param tablesmap
     * @param queriesmap
     * @param repositoryTableMap
     */

    public void setMaps(Map<String, IMetadataTable> repositoryTableMap) {
    }

    /**
     * 
     * ggu Comment method "isGuessQuery".
     * 
     * for guess query
     * 
     * @return
     */
    public boolean isGuessQuery() {
        return this.isGuessQuery;
    }

    public void setGuessQuery(boolean isGuessQuery) {
        this.isGuessQuery = isGuessQuery;
    }

    /**
     * Getter for sapFunctionName.
     * 
     * @return the sapFunctionName
     */
    public String getSapFunctionLabel() {
        // Use the first function
        if (this.sapFunctionLabel == null) {

            if (connection == null) {
                return null;
            }

            if (!(connection instanceof SAPConnection)) {
                return null;
            }
            SAPConnection sapConn = (SAPConnection) connection;
            if (sapConn.getFuntions() != null && !sapConn.getFuntions().isEmpty()) {
                return ((SAPFunctionUnit) sapConn.getFuntions().get(0)).getName();
            }
        }
        return this.sapFunctionLabel;
    }

    /**
     * Sets the sapFunctionName.
     * 
     * @param sapFunctionName the sapFunctionName to set
     */
    public void setSapFunctionLabel(String sapFunctionName) {
        this.sapFunctionLabel = sapFunctionName;
    }

    public String getSapIDocLabel() {
        // Use the first function
        if (this.sapIDocLabel == null) {

            if (connection == null) {
                return null;
            }

            if (!(connection instanceof SAPConnection)) {
                return null;
            }
            SAPConnection sapConn = (SAPConnection) connection;
            if (sapConn.getIDocs() != null && !sapConn.getIDocs().isEmpty()) {
                return ((SAPIDocUnit) sapConn.getIDocs().get(0)).getName();
            }
        }
        return this.sapIDocLabel;
    }

    public void setSapIDocLabel(String sapIDocLabel) {
        this.sapIDocLabel = sapIDocLabel;
    }

    public boolean isIgnoreContextMode() {
        return this.ignoreContextMode;
    }

    public void ignoreContextMode(boolean ignore) {
        this.ignoreContextMode = ignore;
    }

    public String getCurrentTableName() {
        return this.currentTableName;
    }

    public void setCurrentTableName(String currentTableName) {
        this.currentTableName = currentTableName;
    }
}
